package org.example.service.impl;

import org.example.model.PaymentStatus;
import org.example.model.Rental;
import org.example.model.Vehicle;
import org.example.repository.RentalRepository;
import org.example.repository.VehicleRepository;
import org.example.service.PaymentService;
import org.example.service.RentalService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Component
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepo;
    private final VehicleRepository vehicleRepo;
    private final PaymentService paymentService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public RentalServiceImpl(RentalRepository rentalRepo,VehicleRepository vehicleRepo, PaymentService paymentService) {
        this.rentalRepo = rentalRepo;
        this.vehicleRepo = vehicleRepo;
        this.paymentService = paymentService;
    }

    @Override
    public boolean isVehicleRented(Long vehicleId) {
        Optional<Rental> rental =  rentalRepo.findActiveRentalByVehicleId(vehicleId);
        return rental.isPresent();
    }
    @Override
    public Optional<Rental> findActiveRentalByVehicleId(Long vehicleId){
        return rentalRepo.findActiveRentalByVehicleId(vehicleId);
    }
    @Override
    public List<Rental> findActiveRentalByUserId(Long userId){
        return rentalRepo.findActiveRentalByUserId(userId);
    }
    @Override
    public Rental rent(Long vehicleId, Long userId) {
        Vehicle vehicle = vehicleRepo.findById(vehicleId);
        if (vehicle == null || vehicle.isRented() || vehicle.isDeleted()){
            return null;
        }
        Rental rental = new Rental();
        rental.setVehicleId(vehicleId);
        rental.setUserId(userId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withSecond(0).withNano(0);
        rental.setStartDate(start.format(DATE_TIME_FORMATTER));

        rental.setEndDate(null);
        rental.setPaymentStatus(PaymentStatus.PENDING);
        rentalRepo.save(rental);
        vehicle.setRented(true);
        vehicleRepo.save(vehicle);
        return rental;
    }
    @Override
    public Rental returnRental(Long vehicleId, Long userId) {
        Optional<Rental> rental = rentalRepo.findActiveRentalByVehicleId(vehicleId);
        if (rental.isPresent()) {
            Rental activeRental = rental.get();

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime end = now.withSecond(0).withNano(0);
            activeRental.setEndDate(end.format(DATE_TIME_FORMATTER));

            int rentalDays = activeRental.calculateRentalDays();
            rentalDays = Math.max(1, rentalDays);
            activeRental.setRentalDays(rentalDays);

            BigDecimal totalCost = paymentService.calculateTotalCost(vehicleId, rentalDays);
            activeRental.setTotalCost(totalCost);
            activeRental.setPaymentStatus(PaymentStatus.PENDING);

            activeRental.setReturned(true);
            rentalRepo.save(activeRental);

            Vehicle vehicle = vehicleRepo.findById(vehicleId);
            if (vehicle != null) {
                vehicle.setRented(false);
                vehicleRepo.save(vehicle);
            }
            return activeRental;
        }
        return null;
    }
    @Override
    public List<Rental> findAll() {
        return rentalRepo.findAll();

    }
    @Override
    public List<Rental> allActiveRentlas() {
        return rentalRepo.findAllActiveRentals();
    }
    @Override
    public List<Rental> allRentalHistory() {
        return rentalRepo.findAllRentalsHistory();
    }
    @Override
    public List<Rental> historyByUserId(Long userId) {
        return rentalRepo.historyByUserId(userId);
    }
    @Override
    public List<Rental> historyByVehicleId(Long vehicleId) {
    return rentalRepo.findByVehicleId(vehicleId);
    }

    @Override
    public Rental updatePaymentStatus(Long rentalId, String stripePaymentId, boolean isCheckoutSession) {
        Rental rental = rentalRepo.findById(rentalId);
        if (rental == null) {
            return  null;
        }
        PaymentStatus newStatus;
        if (isCheckoutSession) {
            newStatus = paymentService.checkCheckouSessionStatus(stripePaymentId);
        } else {
            newStatus = paymentService.checkPaymentStatus(stripePaymentId);
        }
        rental.setPaymentStatus(newStatus);
        if(stripePaymentId != null) {
            rental.setPaymentUrl(stripePaymentId);
        }
        rentalRepo.save(rental);
        return rental;
    }
    @Override
    public  Rental findById(Long rentalId) {
        return  rentalRepo.findById(rentalId);
    }

    @Override
    public Rental findByStripeSessionId(String sessionId) {
        return rentalRepo.findByStripeSessionId(sessionId);
    }

    @Override
    public void save(Rental rental) {
         rentalRepo.save(rental);
    }
}
