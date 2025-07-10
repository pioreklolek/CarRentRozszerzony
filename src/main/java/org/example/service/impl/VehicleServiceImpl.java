package org.example.service.impl;

import org.example.model.Vehicle;
import org.example.repository.VehicleRepository;
import org.example.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleServiceImpl implements  VehicleService {
    private final VehicleRepository repository;

    public VehicleServiceImpl(VehicleRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Vehicle> getAll() {
        return repository.findAll();
    }

    @Override
    public List<Vehicle> getAllActive() {
        return repository.findAllActive();
    }

    @Override
    public Optional<Vehicle> findById(Long vehicleId) {
        return Optional.ofNullable(repository.findById(vehicleId));
    }
    @Override
    public Vehicle save(Vehicle vehicle) {
        return repository.save(vehicle);
    }
    @Override
    public List<Vehicle> getAvailable() {
        return repository.getAvailabeVehicles();
    }
    @Override
    public List<Vehicle> getRented() {
        return repository.findByRentedTrue();
    }
    @Override
    public boolean isAvailable(Long vehicleId) {
        return repository.findById(vehicleId).isRented();
    }
    @Override
    public void deleteById(Long vehicleId) {
        repository.deleteById(vehicleId);
    }

    @Override
    public List <Vehicle> getDeleted() {
        return repository.findByDeletedTrue();
    }
}