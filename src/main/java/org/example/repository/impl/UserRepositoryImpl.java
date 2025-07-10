package org.example.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class UserRepositoryImpl implements UserRepository {
    @PersistenceContext
    private  EntityManager entityManager;


    @Override
    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
        } else {
            user = entityManager.merge(user);
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByLogin(String login) {
        TypedQuery<User> query = entityManager.createQuery(
                        "SELECT DISTINCT u FROM User u JOIN FETCH u.roles WHERE u.login = :login AND u.deleted = false", User.class)
                .setParameter("login", login);

        List<User> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return entityManager.createQuery("SELECT u FROM User u", User.class)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        TypedQuery<User> query = entityManager.createQuery(
                        "SELECT DISTINCT u FROM User u JOIN FETCH u.roles WHERE u.id = :id AND u.deleted = false", User.class)
                .setParameter("id", id);

        List<User> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public void delete(User user) {
        User managedUser = entityManager.contains(user) ? user : entityManager.merge(user);
        managedUser.setDeleted(true);
        entityManager.merge(managedUser);
    }

    @Override
    public void deleteById(Long userId) {
        User user = findById(userId);
        if (user != null) {
            user.setDeleted(true);
            entityManager.merge(user);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllActiveUsers() {
        return entityManager.createQuery("SELECT u FROM User u WHERE u.deleted = false", User.class)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllActiveUsersByRolename(String rolename) {
        return entityManager.createQuery(
                        "SELECT DISTINCT u FROM User u " +
                                "JOIN u.roles r " +
                                "WHERE r.name = :roleName AND u.deleted = false", User.class)
                .setParameter("roleName", rolename)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findDeletedUsers() {
        return entityManager.createQuery("SELECT u FROM User u WHERE u.deleted = true", User.class)
                .getResultList();
    }

}