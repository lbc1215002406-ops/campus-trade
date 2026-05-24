package com.example.demo.ai.controller;

import com.example.demo.ai.dto.*;
import com.example.demo.ai.prompt.PromptTemplates;
import com.example.demo.ai.service.AiGatewayService;
import com.example.demo.common.Result;
import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiGatewayService aiGateway;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private com.example.demo.mapper.ProductMapper productMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpRequest;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 自然语言 → 自动生成表单 */
    @PostMapping("/generate-form")
    public Result<FormGenerateResponse> generateForm(@RequestBody FormGenerateRequest req) {
        try {
            String categories = categoryService.listAll().stream()
                    .map(c -> c.getId() + "-" + c.getName())
                    .collect(Collectors.joining(","));
            String prompt = PromptTemplates.FORM_GENERATE
                    .replace("{categories}", categories)
                    .replace("{userInput}", req.getUserInput());
            String json = aiGateway.chatJson("你是校园二手交易平台的智能发布助手。", prompt);
            FormGenerateResponse resp;
            try {
                resp = objectMapper.readValue(extractJson(json), FormGenerateResponse.class);
            } catch (Exception ex) {
                // json_object 模式可能返回不完整 JSON，回退到普通 chat
                json = aiGateway.chat(prompt, "");
                resp = objectMapper.readValue(extractJson(json), FormGenerateResponse.class);
            }
            // 分类兜底：AI 未正确分类时，用关键词匹配
            if (resp.getCategoryName() == null || resp.getCategoryName().isBlank()
                    || resp.getCategoryName().equals("其他")) {
                String matched = matchCategory(req.getUserInput());
                if (matched != null) resp.setCategoryName(matched);
            }
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("AI 表单生成失败: " + e.getMessage());
        }
    }

    /** 智能定价建议 */
    @PostMapping("/suggest-price")
    public Result<PriceSuggestResponse> suggestPrice(@RequestBody PriceSuggestRequest req) {
        try {
            // 查询同类商品真实均价
            String avgPrice = getAvgPriceByCategory(req.getCategoryName());

            // 成色映射
            String conditionText = mapCondition(req.getItemCondition());

            String prompt = PromptTemplates.PRICE_SUGGEST
                    .replace("{title}", req.getTitle() != null ? req.getTitle() : "")
                    .replace("{description}", req.getDescription() != null ? req.getDescription() : "")
                    .replace("{userPrice}", req.getPrice() != null ? req.getPrice().toString() : "未填写")
                    .replace("{originalPrice}", req.getOriginalPrice() != null ? req.getOriginalPrice().toString() : "未填写")
                    .replace("{condition}", conditionText)
                    .replace("{category}", req.getCategoryName() != null ? req.getCategoryName() : "未填写")
                    .replace("{avgPrice}", avgPrice);

            String json = aiGateway.chatJson("你是专业的二手商品估价助手。", prompt);
            PriceSuggestResponse resp;
            try {
                resp = objectMapper.readValue(extractJson(json), PriceSuggestResponse.class);
            } catch (Exception ex) {
                json = aiGateway.chat(prompt, "");
                resp = objectMapper.readValue(extractJson(json), PriceSuggestResponse.class);
            }
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("定价建议失败: " + e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        Object attr = httpRequest.getAttribute("userId");
        if (attr instanceof Long) return (Long) attr;
        return null;
    }

    private boolean isAdmin(Long userId) {
        User user = userService.getById(userId);
        return user != null && user.getRole() != null && user.getRole() == 1;
    }

    /** 智能搜索解析 */
    @PostMapping("/search")
    public Result<SearchQueryResponse> search(@RequestBody SearchQueryRequest req) {
        try {
            // 太短的输入直接走普通搜索，不调 AI
            if (req.getQuery() == null || req.getQuery().trim().length() <= 2) {
                SearchQueryResponse resp = new SearchQueryResponse();
                resp.setKeywords(Collections.singletonList(req.getQuery().trim()));
                resp.setSortBy("time_desc");
                resp.setParsedQuery("关键词搜索");
                return Result.success(resp);
            }
            String categories = categoryService.listAll().stream()
                    .map(c -> c.getId() + "-" + c.getName())
                    .collect(Collectors.joining(","));
            String prompt = PromptTemplates.SEARCH_PARSE
                    .replace("{categories}", categories)
                    .replace("{userQuery}", req.getQuery());
            String json = aiGateway.chatJson("你是搜索解析器。", prompt);
            SearchQueryResponse resp;
            try {
                resp = objectMapper.readValue(extractJson(json), SearchQueryResponse.class);
            } catch (Exception ex) {
                json = aiGateway.chat(prompt, "");
                resp = objectMapper.readValue(extractJson(json), SearchQueryResponse.class);
            }
            return Result.success(resp);
        } catch (Exception e) {
            return Result.error("智能搜索失败: " + e.getMessage());
        }
    }

    /** AI 商品推荐 */
    @PostMapping("/recommend")
    public Result<Map<String, Object>> recommend(@RequestBody RecommendRequest req) {
        try {
            // 取在售商品（最多50条）
            List<Product> onSale = productService.listAll().stream()
                    .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                    .limit(50)
                    .collect(Collectors.toList());

            String candidates = onSale.stream()
                    .map(p -> String.format("{\"id\":%d,\"title\":\"%s\",\"price\":%s,\"categoryId\":%d}",
                            p.getId(), p.getTitle(), p.getPrice(), p.getCategoryId()))
                    .collect(Collectors.joining(","));

            String prompt = PromptTemplates.RECOMMEND
                    .replace("{browsedProducts}", "暂无（新用户）")
                    .replace("{favoritedProducts}", "暂无")
                    .replace("{candidateProducts}", candidates);

            String json = aiGateway.chatJson("你是推荐系统。", prompt);
            if (json == null || json.isBlank()) {
                json = aiGateway.chat(prompt, "");
            }
            List<RecommendResponse.RecommendItem> items = parseRecommendItems(json);
            if (items == null) items = new ArrayList<>();

            // 补充完整的 Product 信息
            List<Map<String, Object>> enrichedList = new ArrayList<>();
            for (RecommendResponse.RecommendItem item : items) {
                Product p = productService.getById(item.getProductId());
                if (p != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", p.getId());
                    map.put("title", p.getTitle());
                    map.put("price", p.getPrice());
                    map.put("images", p.getImages());
                    map.put("reason", item.getReason());
                    enrichedList.add(map);
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("recommendations", enrichedList);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("推荐失败: " + e.getMessage());
        }
    }

    /** AI 润色描述 */
    @PostMapping("/polish")
    public Result<Map<String, String>> polish(@RequestBody PolishRequest req) {
        try {
            String prompt = PromptTemplates.DESCRIPTION_POLISH
                    .replace("{title}", req.getTitle())
                    .replace("{description}", req.getDescription());
            String polished = aiGateway.chat(prompt, "");
            Map<String, String> result = new HashMap<>();
            result.put("polished", polished.trim());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("润色失败: " + e.getMessage());
        }
    }

    /** 从 AI 返回中提取 JSON（处理可能的 markdown 包裹） */
    private String extractJson(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1);
        }
        // 尝试数组格式
        start = s.indexOf('[');
        end = s.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1);
        }
        return s;
    }

    /** 解析推荐结果，兼容 AI 可能返回的多种 JSON 格式 */
    private List<RecommendResponse.RecommendItem> parseRecommendItems(String raw) {
        String s = raw.trim();
        List<RecommendResponse.RecommendItem> items = new ArrayList<>();

        // 去掉可能的 markdown 包裹
        if (s.startsWith("```")) {
            int end = s.indexOf("\n", 3);
            s = s.substring(end + 1);
            if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
            s = s.trim();
        }

        // 尝试1：标准格式 {"recommendations": [...]}
        try {
            RecommendResponse resp = objectMapper.readValue(extractJson(s), RecommendResponse.class);
            if (resp.getRecommendations() != null && !resp.getRecommendations().isEmpty()) {
                return resp.getRecommendations();
            }
        } catch (Exception ignored) {}

        // 尝试2：纯数组格式 [{"productId":..., "reason":...}]
        try {
            int arrStart = s.indexOf('[');
            int arrEnd = s.lastIndexOf(']');
            if (arrStart >= 0 && arrEnd > arrStart) {
                String arrJson = s.substring(arrStart, arrEnd + 1);
                items = objectMapper.readValue(arrJson,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, RecommendResponse.RecommendItem.class));
                if (!items.isEmpty()) return items;
            }
        } catch (Exception ignored) {}

        // 尝试3：逐个解析 {...} 对象
        try {
            int idx = 0;
            while (idx < s.length()) {
                int objStart = s.indexOf('{', idx);
                if (objStart < 0) break;
                int objEnd = findMatchingBrace(s, objStart);
                if (objEnd < 0) break;
                String objJson = s.substring(objStart, objEnd + 1);
                try {
                    RecommendResponse.RecommendItem item = objectMapper.readValue(objJson, RecommendResponse.RecommendItem.class);
                    if (item.getProductId() != null) items.add(item);
                } catch (Exception ignored) {}
                idx = objEnd + 1;
            }
        } catch (Exception ignored) {}

        return items;
    }

    /** 查询同类商品真实均价 */
    private String getAvgPriceByCategory(String categoryName) {
        try {
            if (categoryName == null || categoryName.isBlank()) return "暂无";
            Category cat = categoryService.listAll().stream()
                    .filter(c -> c.getName().equals(categoryName))
                    .findFirst().orElse(null);
            if (cat == null) return "暂无";
            BigDecimal avg = productMapper.selectAvgPriceByCategory(cat.getId());
            if (avg == null) return "暂无";
            return "￥" + avg.setScale(0, java.math.RoundingMode.DOWN).toString();
        } catch (Exception e) {
            return "暂无";
        }
    }

    /** 成色数字转文字 */
    private String mapCondition(Integer condition) {
        if (condition == null) return "未填写";
        switch (condition) {
            case 1: return "全新";
            case 2: return "九成新";
            case 3: return "八成新";
            case 4: return "五成新";
            default: return "未填写";
        }
    }

    /** 关键词匹配商品分类，AI 分类不准时兜底 */
    private String matchCategory(String userInput) {
        if (userInput == null) return null;
        String s = userInput.toLowerCase();

        if (containsAny(s, "手机", "iphone", "华为", "小米", "oppo", "vivo", "三星", "平板", "ipad",
                "耳机", "airpods", "充电宝", "数据线", "充电器", "智能手表", "手环"))
            return "手机数码";
        if (containsAny(s, "电脑", "笔记本", "macbook", "显示器", "键盘", "鼠标", "u盘", "硬盘",
                "内存", "cpu", "主板", "显卡", "打印机"))
            return "电脑办公";
        if (containsAny(s, "教材", "课本", "书", "考试", "四六级", "考研", "英语", "数学", "笔记",
                "习题", "试卷", "文具", "笔", "词典", "字帖"))
            return "图书教材";
        if (containsAny(s, "衣服", "鞋", "包", "帽子", "裙子", "裤子", "外套", "t恤", "衬衫",
                "手表", "项链", "耳环", "戒指", "墨镜", "皮带", "围巾"))
            return "服饰鞋包";
        if (containsAny(s, "篮球", "足球", "排球", "球拍", "羽毛球", "乒乓球", "网球", "健身",
                "跑步", "哑铃", "瑜伽", "跳绳", "滑板", "泳镜", "帐篷", "登山"))
            return "运动户外";
        if (containsAny(s, "台灯", "收纳", "水杯", "杯子", "化妆品", "护肤品", "洗面奶", "香水",
                "镜子", "被褥", "枕头", "风扇", "电热毯", "日用品", "洗衣液"))
            return "生活用品";
        if (containsAny(s, "玩具", "模型", "手办", "卡牌", "积木", "娃娃", "盲盒", "奥特曼",
                "变身器", "游戏机", "switch", "ps5", "游戏卡"))
            return "其他";

        return null;
    }

    private boolean containsAny(String s, String... keywords) {
        for (String kw : keywords) {
            if (s.contains(kw)) return true;
        }
        return false;
    }

    private int findMatchingBrace(String s, int start) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '{') depth++;
            else if (s.charAt(i) == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }
}
