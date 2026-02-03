package com.skyeai.jarvis.knowledge.service.impl;

import com.skyeai.jarvis.knowledge.model.UserPreference;
import com.skyeai.jarvis.knowledge.service.UserPreferenceService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class UserPreferenceServiceImpl implements UserPreferenceService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserPreference saveUserPreference(String userId, String preferenceKey, String value, String preferenceType, int priority) {
        List<UserPreference> existingPreferences = getUserPreferences(userId, preferenceKey);
        UserPreference preference;
        if (!existingPreferences.isEmpty()) {
            preference = existingPreferences.get(0);
            preference.setValue(value);
            preference.setPreferenceType(preferenceType);
            preference.setPriority(priority);
            preference.setUpdatedAt(new Date());
            return entityManager.merge(preference);
        } else {
            preference = new UserPreference();
            preference.setUserId(userId);
            preference.setPreferenceKey(preferenceKey);
            preference.setValue(value);
            preference.setPreferenceType(preferenceType);
            preference.setPriority(priority);
            preference.setCreatedAt(new Date());
            preference.setUpdatedAt(new Date());
            entityManager.persist(preference);
            return preference;
        }
    }

    @Override
    public List<UserPreference> getUserPreferences(String userId) {
        return entityManager.createQuery(
                        "SELECT p FROM UserPreference p WHERE p.userId = :userId ORDER BY p.priority DESC",
                        UserPreference.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<UserPreference> getUserPreferences(String userId, String preferenceKey) {
        return entityManager.createQuery(
                        "SELECT p FROM UserPreference p WHERE p.userId = :userId AND p.preferenceKey = :preferenceKey",
                        UserPreference.class)
                .setParameter("userId", userId)
                .setParameter("preferenceKey", preferenceKey)
                .getResultList();
    }

    @Override
    public boolean deleteUserPreference(String userId, String preferenceKey) {
        List<UserPreference> preferences = getUserPreferences(userId, preferenceKey);
        if (!preferences.isEmpty()) {
            for (UserPreference preference : preferences) {
                entityManager.remove(preference);
            }
            return true;
        }
        return false;
    }
}
