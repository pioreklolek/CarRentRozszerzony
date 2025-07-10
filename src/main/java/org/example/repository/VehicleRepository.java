package org.example.repository;

import org.example.model.Vehicle;

import java.util.List;

public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    void delete(Vehicle vehicle);
    Vehicle findById(Long id);
    List<Vehicle> findAll();
    List<Vehicle> findByRentedFalse();
    List<Vehicle> findAllActive();
    void deleteById(Long id);
    List<Vehicle> findByRentedTrue();
    List<Vehicle> getAvailabeVehicles();
    List<Vehicle> findByDeletedTrue();


    //nowe metody
    List<Vehicle> findByIsAtAllowedLocationFalse();
    List<Vehicle> findByLatitudeIsNotNullAndLongitudeIsNotNull();
    }