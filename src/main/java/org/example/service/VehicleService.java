package org.example.service;

import org.example.model.Vehicle;
import org.example.repository.VehicleRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleService {
    public List<Vehicle> getAll();
    List<Vehicle> getAllActive();
    Optional<Vehicle> findById(Long vehicleId);
    Vehicle save(Vehicle vehicle);
    public List<Vehicle> getAvailable();
    public List<Vehicle> getRented();
    boolean isAvailable(Long vehicleId);
    void deleteById(Long vehicleId);
    public List<Vehicle> getDeleted();
}