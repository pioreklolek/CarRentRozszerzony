package org.example.repository;

import org.example.model.Rental;

import java.util.List;
import java.util.Optional;

public interface RentalRepository{
    void save(Rental rental);
    void delete(Rental rental);
    List<Rental> findByUserId(Long userId);
    Rental findById(Long id);
    List<Rental> findByVehicleId(Long vehicleId);
    List<Rental> findAll();
    void deleteById(Long id);
    Optional<Rental> findActiveRentalByVehicleId(Long vehicleId);
    List<Rental> findAllActiveRentals();
    List<Rental> findAllRentalsHistory();
    List<Rental> historyByUserId(Long userId);
    List<Rental> findActiveRentalByUserId(Long userId);
    Rental findByStripeSessionId(String sessionId);
}