package org.example.api.controller;

import org.example.dto.PaymentRequest;
import org.example.dto.PaymentResponse;
import org.example.model.PaymentStatus;
import org.example.service.PaymentService;
import org.springframework.security.core.Authentication;import org.example.model.Rental;
import org.example.model.User;
import org.example.service.RentalService;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {
    private final RentalService rentalService;
    private final UserService userService;
    private final PaymentService paymentService;

    public RentalController(RentalService rentalService, UserService userService,PaymentService paymentService) {
        this.rentalService = rentalService;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<Rental>> getAllRentals() {
        return ResponseEntity.ok(rentalService.allActiveRentlas());
    }

    @GetMapping("/{vehicleId}/status")
    @PreAuthorize("hasAuthority('admin')")

    public ResponseEntity<Boolean> isVehicleRented(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(rentalService.isVehicleRented(vehicleId));
    }

    @GetMapping("/history/user/{userId}")
    @PreAuthorize("hasAuthority('admin') or @userServiceImpl.findById(#userId).orElse(new org.example.model.User()).login == authentication.name")

    public ResponseEntity<List<Rental>> getRentalHistoryByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(rentalService.historyByUserId(userId));
    }

    @GetMapping("/history/vehicle/{vehicleId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<Rental>> getRentalHistoryByVehicleId(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(rentalService.historyByVehicleId(vehicleId));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<Rental>> getAllRentalHistory() {
        return ResponseEntity.ok(rentalService.allRentalHistory());
    }
    @GetMapping("/history/my")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<Rental>> getMyRentalHistory(Authentication authentication) {
        User user = userService.findByLogin(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(rentalService.historyByUserId(user.getId()));
    }
    @GetMapping("active/my")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<?> getMyActiveRentals(Authentication authentication) {
        User user = userService.findByLogin(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().body("Nieprawidłowy użytkownik");
        }

        List<Rental> activeRentals = rentalService.findActiveRentalByUserId(user.getId());

        if (activeRentals.isEmpty()) {
            return ResponseEntity.status(204).body("Brak aktywnych wypożyczeń.");
        }
        return ResponseEntity.ok(activeRentals);
    }

    @PostMapping("/rent/{vehicleId}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Rental> rentVehicle(@PathVariable Long vehicleId,Authentication authentication) {
        User user = userService.findByLogin(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return Optional.ofNullable(rentalService.rent(vehicleId, user.getId())).map(ResponseEntity::ok).orElse(ResponseEntity.badRequest().build());
    }
    @PostMapping("/return/{vehicleId}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<?> returnVehicle(@PathVariable Long vehicleId, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName());
        if(user == null) {
            return ResponseEntity.badRequest().build();
        }

        Rental returnedRental = rentalService.returnRental(vehicleId,user.getId());
        if(returnedRental == null) {
            return ResponseEntity.badRequest().build();
        }
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setRentalId(returnedRental.getId());
        PaymentResponse paymentResponse = paymentService.createCheckoutSession(paymentRequest,returnedRental);

        Map<String, Object> response = new HashMap<>();
        response.put("rental", returnedRental);
        response.put("message", "Pojazd został zwrócony. Aby dokończyc proces, utwórz płatność używając /api/payments/create-checkout-session");
        response.put("totalCost", returnedRental.getTotalCost());
        response.put("rentalDays", returnedRental.getRentalDays());
        response.put("rentalId", returnedRental.getId());
        response.put("paymentStatus", returnedRental.getPaymentStatus());

        if("success".equals(paymentResponse.getStatus())) {
            response.put("message", "Pojazd został zwrócony. Przejdź do płatności klikając link poniżej:");
            response.put("paymentUrl", paymentResponse.getUrl());
            response.put("paymentStatus", PaymentStatus.PENDING);
            response.put("sessionId", paymentResponse.getPaymentIntentId());

            response.put("instructions", Map.of(
                    "step1", "Kliknij w paymentUrl aby przejść do płatności",
                    "step2", "Po dokonaniu płatności wróć do aplikacji",
                    "step3", "Użyj endpoint /api/payments/check-payment-status?sessionId=" + paymentResponse.getPaymentIntentId() + " aby sprawdzić status"
            ));
        } else {
            response.put("message","Pojazd został zwrócony ale wystąpił problem podczas tworzenia płatności. Spróboj ponownie.");
            response.put("paymentError",paymentResponse.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rent/{vehicleId}/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Rental> rentVehicleForUser(
            @PathVariable Long vehicleId,
            @PathVariable Long userId) {
        return Optional.ofNullable(rentalService.rent(vehicleId, userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
    @PostMapping("/return/{vehicleId}/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Rental> returnVehicleForUser(
            @PathVariable Long vehicleId,
            @PathVariable Long userId) {
        return Optional.ofNullable(rentalService.returnRental(vehicleId, userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}
