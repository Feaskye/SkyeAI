package com.skyeai.jarvis.skills.registry;

import com.skyeai.jarvis.skills.model.Skill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Skill注册中心
 * 管理Skills的注册、发现和版本管理
 */
@Slf4j
@Component
public class SkillRegistry {

    @Value("${skills.registry.refresh-interval:30}")
    private int refreshInterval;

    // 内存缓存，用于快速查询
    private final Map<String, Skill> skillCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Skill>> skillVersionsCache = new ConcurrentHashMap<>();

    /**
     * 注册Skill
     * @param skill Skill对象
     * @return 注册后的Skill
     */
    public Skill registerSkill(Skill skill) {
        log.info("Registering skill: {} version: {}", skill.getName(), skill.getVersion());

        // 生成ID
        if (skill.getId() == null || skill.getId().isEmpty()) {
            skill.setId(UUID.randomUUID().toString());
        }

        // 保存到内存
        updateCache(skill);

        log.info("Skill registered successfully: {} version: {}", skill.getName(), skill.getVersion());
        return skill;
    }

    /**
     * 注销Skill
     * @param skillId Skill ID
     */
    public void unregisterSkill(String skillId) {
        log.info("Unregistering skill: {}", skillId);

        // 从内存删除
        removeFromCache(skillId);

        log.info("Skill unregistered successfully: {}", skillId);
    }

    /**
     * 根据ID获取Skill
     * @param skillId Skill ID
     * @return Skill对象
     */
    public Skill getSkillById(String skillId) {
        return skillCache.get(skillId);
    }

    /**
     * 根据名称获取Skill
     * @param name Skill名称
     * @return Skill对象
     */
    public Skill getSkillByName(String name) {
        return skillVersionsCache.getOrDefault(name, new ConcurrentHashMap<>())
                .values().stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据名称和版本获取Skill
     * @param name Skill名称
     * @param version Skill版本
     * @return Skill对象
     */
    public Skill getSkillByNameAndVersion(String name, String version) {
        return skillVersionsCache.getOrDefault(name, new ConcurrentHashMap<>())
                .get(version);
    }

    /**
     * 获取所有Skill
     * @return Skill列表
     */
    public List<Skill> getAllSkills() {
        return skillCache.values().stream().collect(Collectors.toList());
    }

    /**
     * 获取所有活跃的Skill
     * @return Skill列表
     */
    public List<Skill> getAllActiveSkills() {
        return skillCache.values().stream()
                .filter(skill -> "ACTIVE".equals(skill.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 搜索Skill
     * @param keyword 关键词
     * @return Skill列表
     */
    public List<Skill> searchSkills(String keyword) {
        return skillCache.values().stream()
                .filter(skill -> skill.getName().contains(keyword) || 
                        (skill.getDescription() != null && skill.getDescription().contains(keyword)))
                .collect(Collectors.toList());
    }

    /**
     * 更新缓存
     * @param skill Skill对象
     */
    private void updateCache(Skill skill) {
        // 更新ID缓存
        skillCache.put(skill.getId(), skill);

        // 更新名称-版本缓存
        skillVersionsCache.computeIfAbsent(skill.getName(), k -> new ConcurrentHashMap<>())
                .put(skill.getVersion(), skill);
    }

    /**
     * 从缓存移除
     * @param skillId Skill ID
     */
    private void removeFromCache(String skillId) {
        Skill skill = skillCache.get(skillId);
        if (skill != null) {
            // 从ID缓存移除
            skillCache.remove(skillId);

            // 从名称-版本缓存移除
            Map<String, Skill> versions = skillVersionsCache.get(skill.getName());
            if (versions != null) {
                versions.remove(skill.getVersion());
                if (versions.isEmpty()) {
                    skillVersionsCache.remove(skill.getName());
                }
            }
        }
    }

    /**
     * 初始化缓存
     */
    public void initializeCache() {
        log.info("Initializing skill cache");
        log.info("Skill cache initialized");
    }

}
