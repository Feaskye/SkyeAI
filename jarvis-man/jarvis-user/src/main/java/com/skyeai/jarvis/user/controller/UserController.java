package com.skyeai.jarvis.user.controller;

import com.skyeai.jarvis.user.model.User;
import com.skyeai.jarvis.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建新用户
     */
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    /**
     * 根据用户名获取用户
     */
    @GetMapping("/username/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    /**
     * 根据邮箱获取用户
     */
    @GetMapping("/email/{email}")
    public User getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * 获取所有用户
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * 获取所有启用的用户
     */
    @GetMapping("/enabled")
    public List<User> getAllEnabledUsers() {
        return userService.getAllEnabledUsers();
    }

    /**
     * 获取所有已验证的用户
     */
    @GetMapping("/verified")
    public List<User> getAllVerifiedUsers() {
        return userService.getAllVerifiedUsers();
    }

    /**
     * 根据角色获取用户
     */
    @GetMapping("/role/{role}")
    public List<User> getUsersByRole(@PathVariable String role) {
        return userService.getUsersByRole(role);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Map<String, String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Map.of("message", "User deleted successfully");
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/enable")
    public User enableUser(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        boolean enabled = request.get("enabled");
        return userService.enableUser(id, enabled);
    }

    /**
     * 验证用户
     */
    @PutMapping("/{id}/verify")
    public User verifyUser(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        boolean verified = request.get("verified");
        return userService.verifyUser(id, verified);
    }

    /**
     * 更新用户密码
     */
    @PutMapping("/{id}/password")
    public User updatePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        return userService.updatePassword(id, newPassword);
    }

    /**
     * 更新用户最后登录时间
     */
    @PutMapping("/{id}/last-login")
    public User updateLastLoginTime(@PathVariable Long id) {
        return userService.updateLastLoginTime(id);
    }

    /**
     * 检查用户名是否已存在
     */
    @GetMapping("/check-username/{username}")
    public Map<String, Boolean> checkUsernameExists(@PathVariable String username) {
        boolean exists = userService.isUsernameExists(username);
        return Map.of("exists", exists);
    }

    /**
     * 检查邮箱是否已存在
     */
    @GetMapping("/check-email/{email}")
    public Map<String, Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.isEmailExists(email);
        return Map.of("exists", exists);
    }
}
