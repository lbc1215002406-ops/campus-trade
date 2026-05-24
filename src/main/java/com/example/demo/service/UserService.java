package com.example.demo.service;

import com.example.demo.entity.User;

import java.util.List;

public interface UserService {
    User register(User user);
    User login(String username, String password);
    User getById(Long id);
    void updateById(User user);
    List<User> searchByKeyword(String keyword);
    List<User> listAll();
    void removeById(Long id);
}
