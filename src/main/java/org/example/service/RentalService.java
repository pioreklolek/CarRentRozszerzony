package org.example.service;

import org.example.model.Rental;
import org.example.model.User;
import org.example.model.Vehicle;
import org.example.repository.RentalRepository;
import org.example.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RentalService {
    boolean isVehicleRented(Long vehicleId);
    Optional<Rental> findActiveRentalByVehicleId(Long vehicleId);
    Rental rent(Long vehicleId, Long userId);
    Rental returnRental(Long vehicleId, Long userId);
    List<Rental> findAll();
    List<Rental> allActiveRentlas();
    List<Rental> allRentalHistory();
    List<Rental> historyByUserId(Long userId);
    List<Rental> historyByVehicleId(Long vehicleId);
    List<Rental> findActiveRentalByUserId(Long userId);
    Rental findById(Long rentalId);
    Rental updatePaymentStatus(Long rentalId, String stripePaymentId, boolean isCheckoutSession);
    Rental findByStripeSessionId(String sessionId);
    void save(Rental rental);
}