package com.skyeai.jarvis.user.service;

import com.skyeai.jarvis.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * 创建新用户
     */
    User createUser(User user);

    /**
     * 根据ID获取用户
     */
    Optional<User> getUserById(Long id);

    /**
     * 根据用户名获取用户
     */
    Optional<User> getUserByUsername(String username);

    /**
     * 根据邮箱获取用户
     */
    Optional<User> getUserByEmail(String email);

    /**
     * 获取所有用户
     */
    List<User> getAllUsers();

    /**
     * 获取所有启用的用户
     */
    List<User> getAllEnabledUsers();

    /**
     * 获取所有已验证的用户
     */
    List<User> getAllVerifiedUsers();

    /**
     * 根据角色获取用户
     */
    List<User> getUsersByRole(String role);

    /**
     * 更新用户信息
     */
    User updateUser(Long id, User user);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 启用/禁用用户
     */
    User enableUser(Long id, boolean enabled);

    /**
     * 验证用户
     */
    User verifyUser(Long id, boolean verified);

    /**
     * 更新用户密码
     */
    User updatePassword(Long id, String newPassword);

    /**
     * 更新用户最后登录时间
     */
    User updateLastLoginTime(Long id);

    /**
     * 检查用户名是否已存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查邮箱是否已存在
     */
    boolean isEmailExists(String email);
}
