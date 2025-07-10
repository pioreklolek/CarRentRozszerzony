package org.example.repository.impl;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.model.AllowedLocation;
import org.example.repository.AllowedLocationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class AllowedLocationRepositoryImpl implements AllowedLocationRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public AllowedLocation save(AllowedLocation allowedLocation) {
        if (allowedLocation.getId() == null) {
            entityManager.persist(allowedLocation);
            return allowedLocation;
        } else {
            return entityManager.merge(allowedLocation);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AllowedLocation findById(Long Id) {
        return entityManager.find(AllowedLocation.class, Id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllowedLocation> findAll() {
        return entityManager.createQuery("SELECT a FROM AllowedLocation a", AllowedLocation.class).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllowedLocation> findAllActive() {
        return entityManager.createQuery("SELECT a FROM AllowedLocation a WHERE a.isActive = true", AllowedLocation.class).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllowedLocation> findByIsMainOffice(boolean isMainOffice) {
        return entityManager.createQuery("SELECT a FROM AllowedLocation a WHERE a.isMainOffice = :isMainOffice AND a.isActive = true", AllowedLocation.class).setParameter("isMainOffice", isMainOffice).getResultList();
    }

    @Override
    public void delete(AllowedLocation allowedLocation) {
    AllowedLocation managed = entityManager.contains(allowedLocation) ? allowedLocation : entityManager.merge(allowedLocation);
    managed.setActive(false);
    entityManager.merge(managed);
    }

    @Override
    public void deleteById(long Id) {
    AllowedLocation allowedLocation = findById(Id);
    if(allowedLocation != null) {
        delete(allowedLocation);
    }
    }
}
