package org.example.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.example.dto.PaymentRequest;
import org.example.dto.PaymentResponse;
import org.example.model.PaymentStatus;
import org.example.service.PaymentService;
import org.springframework.security.core.Authentication;
import org.example.model.Rental;
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
@Tag(name = "Wypożyczenia", description = "Zarządzanie wypożyczeniami pojazdów")
@SecurityRequirement(name = "bearerAuth")
public class RentalController {
    private final RentalService rentalService;
    private final UserService userService;
    private final PaymentService paymentService;

    public RentalController(RentalService rentalService, UserService userService, PaymentService paymentService) {
        this.rentalService = rentalService;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    @Operation(
            summary = "Pobierz wszystkie aktywne wypożyczenia",
            description = "Zwraca listę wszystkich aktywnych wypożyczeń (tylko dla administratorów)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista aktywnych wypożyczeń"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień administratora")
    })
    public ResponseEntity<List<Rental>> getAllRentals() {
        return ResponseEntity.ok(rentalService.allActiveRentlas());
    }

    @GetMapping("/{vehicleId}/status")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(
            summary = "Sprawdź czy pojazd jest wypożyczony",
            description = "Sprawdza status wypożyczenia konkretnego pojazdu"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status pojazdu"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień administratora")
    })
    public ResponseEntity<Boolean> isVehicleRented(
            @Parameter(description = "ID pojazdu", required = true)
            @PathVariable Long vehicleId) {
        return ResponseEntity.ok(rentalService.isVehicleRented(vehicleId));
    }

    @GetMapping("/history/user/{userId}")
    @PreAuthorize("hasAuthority('admin') or @userServiceImpl.findById(#userId).orElse(new org.example.model.User()).login == authentication.name")
    @Operation(
            summary = "Historia wypożyczeń użytkownika",
            description = "Zwraca historię wypożyczeń dla konkretnego użytkownika"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historia wypożyczeń użytkownika"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień")
    })
    public ResponseEntity<List<Rental>> getRentalHistoryByUserId(
            @Parameter(description = "ID użytkownika", required = true)
            @PathVariable Long userId) {
        return ResponseEntity.ok(rentalService.historyByUserId(userId));
    }

    @GetMapping("/history/vehicle/{vehicleId}")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(
            summary = "Historia wypożyczeń pojazdu",
            description = "Zwraca historię wypożyczeń dla konkretnego pojazdu"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historia wypożyczeń pojazdu"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień administratora")
    })
    public ResponseEntity<List<Rental>> getRentalHistoryByVehicleId(
            @Parameter(description = "ID pojazdu", required = true)
            @PathVariable Long vehicleId) {
        return ResponseEntity.ok(rentalService.historyByVehicleId(vehicleId));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(
            summary = "Pełna historia wypożyczeń",
            description = "Zwraca pełną historię wszystkich wypożyczeń"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pełna historia wypożyczeń"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień administratora")
    })
    public ResponseEntity<List<Rental>> getAllRentalHistory() {
        return ResponseEntity.ok(rentalService.allRentalHistory());
    }

    @GetMapping("/history/my")
    @PreAuthorize("hasAuthority('user')")
    @Operation(
            summary = "Moja historia wypożyczeń",
            description = "Zwraca historię wypożyczeń dla zalogowanego użytkownika"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historia wypożyczeń użytkownika"),
            @ApiResponse(responseCode = "400", description = "Błąd autoryzacji użytkownika")
    })
    public ResponseEntity<List<Rental>> getMyRentalHistory(Authentication authentication) {
        User user = userService.findByLogin(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(rentalService.historyByUserId(user.getId()));
    }

    @GetMapping("active/my")
    @PreAuthorize("hasAuthority('user')")
    @Operation(
            summary = "Moje aktywne wypożyczenia",
            description = "Zwraca aktywne wypożyczenia dla zalogowanego użytkownika"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aktywne wypożyczenia użytkownika"),
            @ApiResponse(responseCode = "204", description = "Brak aktywnych wypożyczeń"),
            @ApiResponse(responseCode = "400", description = "Błąd autoryzacji użytkownika")
    })
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
    @Operation(
            summary = "Wypożycz pojazd",
            description = "Wypożycza pojazd dla zalogowanego użytkownika"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pojazd został wypożyczony"),
            @ApiResponse(responseCode = "400", description = "Błąd podczas wypożyczania pojazdu")
    })
    public ResponseEntity<Rental> rentVehicle(
            @Parameter(description = "ID pojazdu do wypożyczenia", required = true)
            @PathVariable Long vehicleId,
            Authentication authentication) {
        User user = userService.findByLogin(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return Optional.ofNullable(rentalService.rent(vehicleId, user.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/return/{vehicleId}")
    @PreAuthorize("hasAuthority('user')")
    @Operation(
            summary = "Zwróć pojazd",
            description = "Zwraca pojazd i tworzy sesję płatności"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pojazd został zwrócony i utworzono sesję płatności"),
            @ApiResponse(responseCode = "400", description = "Błąd podczas zwrotu pojazdu lub pojazd nie w dozwolonym miejscu")
    })
    public ResponseEntity<?> returnVehicle(
            @Parameter(description = "ID pojazdu do zwrotu", required = true)
            @PathVariable Long vehicleId,
            Authentication authentication) {
        User user = userService.findByLogin(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        Rental returnedRental = rentalService.returnRental(vehicleId, user.getId());
        if (returnedRental == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Nie możesz zwrócić pojazdu, ponieważ nie znajduje się w dozwolonym miejscu."));
        }
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setRentalId(returnedRental.getId());
        PaymentResponse paymentResponse = paymentService.createCheckoutSession(paymentRequest, returnedRental);

        Map<String, Object> response = new HashMap<>();
        response.put("rental", returnedRental);
        response.put("message", "Pojazd został zwrócony. Aby dokończyc proces, utwórz płatność używając /api/payments/create-checkout-session");
        response.put("totalCost", returnedRental.getTotalCost());
        response.put("rentalDays", returnedRental.getRentalDays());
        response.put("rentalId", returnedRental.getId());
        response.put("paymentStatus", returnedRental.getPaymentStatus());

        if ("success".equals(paymentResponse.getStatus())) {
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
            response.put("message", "Pojazd został zwrócony ale wystąpił problem podczas tworzenia płatności. Spróboj ponownie.");
            response.put("paymentError", paymentResponse.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rent/{vehicleId}/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(
            summary = "Wypożycz pojazd dla użytkownika (admin)",
            description = "Wypożycza pojazd dla konkretnego użytkownika (tylko admin)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pojazd został wypożyczony"),
            @ApiResponse(responseCode = "400", description = "Błąd podczas wypożyczania pojazdu"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień administratora")
    })
    public ResponseEntity<Rental> rentVehicleForUser(
            @Parameter(description = "ID pojazdu", required = true)
            @PathVariable Long vehicleId,
            @Parameter(description = "ID użytkownika", required = true)
            @PathVariable Long userId) {
        return Optional.ofNullable(rentalService.rent(vehicleId, userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/return/{vehicleId}/{userId}")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(
            summary = "Zwróć pojazd dla użytkownika (admin)",
            description = "Zwraca pojazd dla konkretnego użytkownika (tylko admin)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pojazd został zwrócony"),
            @ApiResponse(responseCode = "400", description = "Błąd podczas zwrotu pojazdu"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień administratora")
    })
    public ResponseEntity<Rental> returnVehicleForUser(
            @Parameter(description = "ID pojazdu", required = true)
            @PathVariable Long vehicleId,
            @Parameter(description = "ID użytkownika", required = true)
            @PathVariable Long userId) {
        return Optional.ofNullable(rentalService.returnRental(vehicleId, userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}