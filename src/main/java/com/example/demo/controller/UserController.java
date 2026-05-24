package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.config.JwtUtil;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody User user) {
        try {
            User saved = userService.register(user);
            Map<String, Object> map = new HashMap<>();
            map.put("userId", saved.getId());
            map.put("nickname", saved.getNickname());
            return Result.success(map);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        User loginUser = userService.login(user.getUsername(), user.getPassword());
        if (loginUser == null) {
            return Result.error("用户名或密码错误");
        }
        String token = JwtUtil.generate(loginUser.getId(), loginUser.getRole());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("userId", loginUser.getId());
        map.put("nickname", loginUser.getNickname());
        map.put("username", loginUser.getUsername());
        map.put("phone", loginUser.getPhone());
        map.put("avatar", loginUser.getAvatar());
        map.put("role", loginUser.getRole());
        return Result.success(map);
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser() {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody User user) {
        Long userId = (Long) request.getAttribute("userId");
        User existing = userService.getById(userId);
        if (existing == null) {
            return Result.error("用户不存在");
        }
        existing.setNickname(user.getNickname());
        existing.setPhone(user.getPhone());
        existing.setAvatar(user.getAvatar());
        userService.updateById(existing);
        return Result.success("修改成功");
    }

    @GetMapping("/admin/list")
    public Result<List<User>> adminList(@RequestParam(required = false) String keyword) {
        List<User> users = userService.searchByKeyword(keyword);
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @DeleteMapping("/admin/{id}")
    public Result<String> adminDelete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功");
    }
}
