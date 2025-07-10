package org.example.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.model.Car;
import org.example.model.Motorcycle;
import org.example.model.Vehicle;
import org.example.repository.VehicleRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
@Repository
@Transactional
public class VehicleRepositoryImpl implements VehicleRepository {
    @PersistenceContext
    private  EntityManager entityManager;


    @Override
    public Vehicle save(Vehicle vehicle) {
        if (vehicle.getId() == null) {
            entityManager.persist(vehicle);
            return vehicle;
        } else {
            return entityManager.merge(vehicle);
        }
    }


    @Override
    public void delete(Vehicle vehicle) {
        Vehicle managedVehicle = entityManager.contains(vehicle) ? vehicle : entityManager.merge(vehicle);
        managedVehicle.setDeleted(true);
        entityManager.merge(managedVehicle);
    }
    @Override
    public void deleteById(Long vehicleId) {
        Vehicle vehicle = findById(vehicleId);
        if (vehicle != null) {
            vehicle.setDeleted(true);
            entityManager.merge(vehicle);
        }
    }

    @Override
    public Vehicle findById(Long id) {
        return entityManager.find(Vehicle.class, id);    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findAll() {
        return entityManager.createQuery("SELECT v FROM Vehicle v", Vehicle.class)
                .getResultList();

    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findByRentedFalse() {
        return entityManager.createQuery("SELECT v FROM Vehicle v WHERE v.rented = false", Vehicle.class)
                .getResultList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getAvailabeVehicles() {
        return entityManager.createQuery(
                        "SELECT v FROM Vehicle v WHERE v.rented = false AND v.deleted = false", Vehicle.class)
                .getResultList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findByRentedTrue() {
        return entityManager.createQuery("SELECT v FROM Vehicle v WHERE v.rented = true", Vehicle.class)
                .getResultList();
    }
    public List<Car> findAllCars() {
        return entityManager.createQuery("SELECT c FROM Car c", Car.class)
                .getResultList();
    }
    public List<Motorcycle> findAllMotorcycles() {
        return entityManager.createQuery("SELECT m FROM Motorcycle m", Motorcycle.class)
                .getResultList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findAllActive() {
        return entityManager.createQuery("SELECT v FROM Vehicle v WHERE v.deleted = false", Vehicle.class)
                .getResultList();    }
    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findByDeletedTrue() {
        return entityManager.createQuery("SELECT v FROM Vehicle v WHERE v.deleted = true", Vehicle.class)
                .getResultList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findByIsAtAllowedLocationFalse() {
        return entityManager.createQuery("SELECT v FROM Vehicle v WHERE v.isAtAllowedLocation = false AND v.deleted = false", Vehicle.class).getResultList();
    }

    @Override
    public List<Vehicle> findByLatitudeIsNotNullAndLongitudeIsNotNull() {
        return entityManager.createQuery("SELECT v FROM Vehicle v WHERE v.latitude IS NOT NULL AND v.longitude IS NOT NULL AND v.deleted = false").getResultList();
    }
}