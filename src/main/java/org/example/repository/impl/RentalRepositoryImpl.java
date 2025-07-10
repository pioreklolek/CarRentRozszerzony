package org.example.repository.impl;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;
import org.example.model.Rental;
import org.example.repository.RentalRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Lazy;
import java.util.List;
import java.util.Optional;
@Repository
@Transactional
public class RentalRepositoryImpl implements RentalRepository {
    @PersistenceContext
    private  EntityManager entityManager;


    @Override
    public void save(Rental rental) {
        if (rental.getId() == null) {
            entityManager.persist(rental);
        } else {
            entityManager.merge(rental);
        }    }

    @Override
    public void delete(Rental rental) {
        Rental managedRental = entityManager.contains(rental) ? rental : entityManager.merge(rental);
        entityManager.remove(managedRental);    }

    public void deleteById(Long id) {
        Rental rental = findById(id);
        if (rental != null) {
            delete(rental);
        }
    }
    @Override
    @Transactional(readOnly = true)
    public Rental findById(Long id) {
        return entityManager.find(Rental.class, id);    }

    @Override
    @Transactional(readOnly = true)
    public List<Rental> findAll() {
        return entityManager.createQuery("SELECT r FROM Rental r", Rental.class)
                .getResultList();    }

    @Override
    @Transactional(readOnly = true)
    public List<Rental> findByUserId(Long userId) {
        return entityManager.createQuery("SELECT r FROM Rental r WHERE r.userId = :userId", Rental.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rental> findByVehicleId(Long vehicleId) {
        return entityManager.createQuery("SELECT r FROM Rental r WHERE r.vehicleId = :vehicleId", Rental.class)
                .setParameter("vehicleId", vehicleId)
                .getResultList();
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<Rental> findActiveRentalByVehicleId(Long vehicleId) {
        TypedQuery<Rental> query = entityManager.createQuery(
                        "SELECT r FROM Rental r WHERE r.vehicleId = :vehicleId AND r.returned = false",
                        Rental.class)
                .setParameter("vehicleId", vehicleId);

        List<Rental> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rental> findActiveRentalByUserId(Long userId) {
        return entityManager.createQuery(
                        "SELECT r FROM Rental r WHERE r.userId = :userId AND r.returned = false",
                        Rental.class)
                .setParameter("userId", userId)
                .getResultList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<Rental> findAllActiveRentals() {
        return entityManager.createQuery("SELECT r FROM Rental r WHERE r.returned = false", Rental.class)
                .getResultList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<Rental> findAllRentalsHistory() {
        return entityManager.createQuery("SELECT r FROM Rental r WHERE r.returned = true", Rental.class)
                .getResultList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<Rental> historyByUserId(Long userId){
        return entityManager.createQuery(
                        "SELECT r FROM Rental r WHERE r.userId = :userId AND r.returned = true",
                        Rental.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Rental findByStripeSessionId(String sessionId) {
        TypedQuery<Rental> query = entityManager.createQuery(
                        "SELECT r FROM Rental r WHERE r.stripeSessionId = :sessionId",
                        Rental.class)
                .setParameter("sessionId", sessionId);

        List<Rental> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}