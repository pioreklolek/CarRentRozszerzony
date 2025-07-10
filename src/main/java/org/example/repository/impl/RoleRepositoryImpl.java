package org.example.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;
import org.example.model.Role;
import org.example.repository.RoleRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Lazy;
import java.util.List;

@Repository
@Transactional
public class RoleRepositoryImpl implements RoleRepository {
    @PersistenceContext
    private  EntityManager entityManager;


    @Override
    public Role save(Role role) {
        if (role.getId() == null) {
            entityManager.persist(role);
            return role;
        } else {
            return entityManager.merge(role);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Role findById(Long id) {
        return entityManager.find(Role.class, id);    }

    @Override
    @Transactional(readOnly = true)
    public Role findByName(String name) {
        TypedQuery<Role> query = entityManager.createQuery(
                        "SELECT r FROM Role r WHERE r.name = :name", Role.class)
                .setParameter("name", name);

        List<Role> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return entityManager.createQuery("SELECT r FROM Role r ORDER BY r.id", Role.class)
                .getResultList();    }

    @Override
    public void delete(Role role) {
        Role managedRole = entityManager.contains(role) ? role : entityManager.merge(role);
        entityManager.remove(managedRole);    }

    @Override
    public void deleteById(Long id) {
        Role role = entityManager.find(Role.class, id);
        if (role != null) {
            entityManager.remove(role);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(String name) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(r) FROM Role r WHERE r.name = :name", Long.class)
                .setParameter("name", name)
                .getSingleResult();
        return count != null && count > 0;
    }
}

