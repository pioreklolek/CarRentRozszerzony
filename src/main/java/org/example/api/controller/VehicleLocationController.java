package org.example.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.AllowedLocation;
import org.example.model.Rental;
import org.example.model.User;
import org.example.model.Vehicle;
import org.example.service.AllowedLocationService;
import org.example.service.RentalService;
import org.example.service.UserService;
import org.example.service.VehicleLocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/locations")
@Tag(name = "Vehicle Location Management", description = "Zarządzanie lokalizacją pojazdów")
@SecurityRequirement(name = "bearerAuth")
public class VehicleLocationController {
    private final VehicleLocationService vehicleLocationService;
    private final AllowedLocationService allowedLocationService;
    private final RentalService rentalService;
    private final UserService userService;

    public VehicleLocationController(VehicleLocationService vehicleLocationService,
                                     AllowedLocationService allowedLocationService,
                                     RentalService rentalService,
                                     UserService userService) {
        this.vehicleLocationService = vehicleLocationService;
        this.allowedLocationService = allowedLocationService;
        this.rentalService = rentalService;
        this.userService = userService;
    }

    @GetMapping("/allowed")
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    @Operation(summary = "Pobierz dozwolone lokalizacje",
            description = "Zwraca listę wszystkich aktywnych dozwolonych lokalizacji")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista dozwolonych lokalizacji pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = AllowedLocation.class))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<List<AllowedLocation>> getAllowedLocations() {
        return ResponseEntity.ok(allowedLocationService.findAllActive());
    }

    @PostMapping("/allowed/create")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Utwórz dozwoloną lokalizację",
            description = "Tworzy nową dozwoloną lokalizację dla pojazdów")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lokalizacja utworzona pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane lokalizacji"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<AllowedLocation> createAllowedLocation(
            @Parameter(description = "Dane nowej dozwolonej lokalizacji", required = true)
            @RequestBody AllowedLocation allowedLocation) {
        AllowedLocation savedLocation = allowedLocationService.save(allowedLocation);
        return ResponseEntity.ok(savedLocation);
    }

    @DeleteMapping("/allowed/delete/{id}")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Usuń dozwoloną lokalizację",
            description = "Usuwa dozwoloną lokalizację o podanym ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Lokalizacja usunięta pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Lokalizacja nie została znaleziona"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<Void> deleteAllowedLocation(
            @Parameter(description = "ID lokalizacji do usunięcia", required = true)
            @PathVariable Long id) {
        allowedLocationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/set-location-by-coords/{vehicleId}")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Ustaw lokalizację pojazdu przez współrzędne",
            description = "Ustawia lokalizację pojazdu na podstawie współrzędnych GPS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lokalizacja pojazdu ustawiona pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane lokalizacji"),
            @ApiResponse(responseCode = "404", description = "Pojazd nie został znaleziony"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<?> setVehicleLocation(
            @Parameter(description = "ID pojazdu", required = true)
            @PathVariable Long vehicleId,
            @Parameter(description = "Szerokość geograficzna", required = true)
            @RequestParam BigDecimal latitude,
            @Parameter(description = "Długość geograficzna", required = true)
            @RequestParam BigDecimal longitude,
            @Parameter(description = "Nazwa lokalizacji (opcjonalna)")
            @RequestParam(required = false) String locationName) {
        try {
            Vehicle vehicle = vehicleLocationService.setVehicleLocation(vehicleId, latitude, longitude, locationName);

            Map<String, Object> response = new HashMap<>();
            response.put("vehicle", vehicle);
            response.put("message", "lokalizacja pojazdu zostałą zaktualizowana!");
            response.put("isAtAllowedLocation", vehicle.isAtAllowedLocation());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Błąd: " + e.getMessage());
        }
    }

    @PostMapping("/set-random-location/{vehicleId}")
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    @Operation(summary = "Ustaw losową lokalizację pojazdu",
            description = "Ustawia losową lokalizację dla pojazdu (tylko dla aktywnych wypożyczeń)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Losowa lokalizacja ustawiona pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Pojazd nie został znaleziony"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień lub aktywnego wypożyczenia"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<?> setRandomVehicleLocation(
            @Parameter(description = "ID pojazdu", required = true)
            @PathVariable Long vehicleId,
            Principal principal) {
        try {
            User user = userService.findByLogin(principal.getName());

            Optional<Rental> optionalRental = rentalService.findActiveRentalByVehicleId(vehicleId);

            if (optionalRental.isEmpty()) {
                return ResponseEntity.status(403).body("Brak aktywnego wypożyczenia dla tego pojazdu!");
            }

            Rental rental = optionalRental.get();

            if (!user.getId().equals(rental.getUserId()) && !principal.getName().equals("admin")) {
                return ResponseEntity.status(403).body("Nie masz uprawnień do zmiany lokalizacji tego pojazdu!");
            }

            Vehicle vehicle = vehicleLocationService.setRandomVehicleLocation(vehicleId);

            if (vehicle == null) {
                return ResponseEntity.badRequest().body("Pojazd nie został znaleziony!");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("vehicle", vehicle);
            response.put("message", "Losowa lokalizacja pojazdu została ustawiona");
            response.put("isAtAllowedLocation", vehicle.isAtAllowedLocation());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Błąd: " + e.getMessage());
        }
    }

    @GetMapping("/status/{vehicleId}")
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    @Operation(summary = "Sprawdź status lokalizacji pojazdu",
            description = "Sprawdza czy pojazd znajduje się w dozwolonej lokalizacji")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status lokalizacji pobrany pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień lub aktywnego wypożyczenia"),
            @ApiResponse(responseCode = "500", description = "Błąd serwera podczas sprawdzania statusu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<?> checkVehicleLocationStatus(
            @Parameter(description = "ID pojazdu", required = true)
            @PathVariable Long vehicleId,
            Principal principal) {

        try {
            User user = userService.findByLogin(principal.getName());

            Optional<Rental> optionalRental = rentalService.findActiveRentalByVehicleId(vehicleId);

            if (optionalRental.isEmpty()) {
                return ResponseEntity.status(403).body("Brak aktywnego wypożyczenia dla tego pojazdu!");
            }

            Rental rental = optionalRental.get();

            if (!user.getId().equals(rental.getUserId()) && !principal.getName().equals("admin")) {
                return ResponseEntity.status(403).body("Nie masz uprawnień do sprawdzenia statusu lokalizacji tego pojazdu!");
            }

            boolean isAtAllowedLocation = vehicleLocationService.isVehicleAtAllowedLocation(vehicleId);
            boolean isAtMainOffice = vehicleLocationService.isVehicleAtMainOffice(vehicleId);

            Map<String, Object> response = new HashMap<>();
            response.put("vehicleId", vehicleId);
            response.put("isAtAllowedLocation", isAtAllowedLocation);
            response.put("isAtMainOffice", isAtMainOffice);
            response.put("message", isAtAllowedLocation
                    ? "Pojazd jest w dozwolonej lokacji!"
                    : "Pojazd jest w niedozwolonej lokacji!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Wystąpił błąd podczas sprawdzania statusu: " + e.getMessage());
        }
    }

    @GetMapping("/not-allowed")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Pobierz pojazdy w niedozwolonych lokalizacjach",
            description = "Zwraca listę pojazdów znajdujących się w niedozwolonych lokalizacjach")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista pojazdów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<List<Vehicle>> getVehiclesAtNotAllowedLocation() {
        List<Vehicle> vehicles = vehicleLocationService.getVehiclesNotAtAllowedLocation();
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/update-all-status")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Zaktualizuj status lokalizacji wszystkich pojazdów",
            description = "Aktualizuje status lokalizacji dla wszystkich pojazdów w systemie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status lokalizacji zaktualizowany pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<?> updateAllVehicleLocationStatus() {
        vehicleLocationService.updateAllVehicleLocationStatus();
        return ResponseEntity.ok(Map.of("message", "Status lokalizacji zostal zaktualizowany"));
    }

    @PostMapping("/set-location/{vehicleId}")
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    @Operation(summary = "Ustaw lokalizację pojazdu przez nazwę",
            description = "Ustawia lokalizację pojazdu na podstawie nazwy lokalizacji")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lokalizacja pojazdu ustawiona pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowa nazwa lokalizacji"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień lub aktywnego wypożyczenia"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<?> setVehicleLocationByName(
            @Parameter(description = "ID pojazdu", required = true)
            @PathVariable Long vehicleId,
            @Parameter(description = "Nazwa lokalizacji", required = true)
            @RequestParam String locationName,
            Principal principal) {
        try {
            User user = userService.findByLogin(principal.getName());

            Optional<Rental> optionalRental = rentalService.findActiveRentalByVehicleId(vehicleId);

            if (optionalRental.isEmpty()) {
                return ResponseEntity.status(403).body("Brak aktywnego wypożyczenia dla tego pojazdu!");
            }

            Rental rental = optionalRental.get();

            if (!user.getId().equals(rental.getUserId()) && !principal.getName().equals("admin")) {
                return ResponseEntity.status(403).body("Nie masz uprawnień do zmiany lokalizacji tego pojazdu!");
            }

            Vehicle vehicle = vehicleLocationService.setVehicleLocationByName(vehicleId, locationName);

            Map<String, Object> response = new HashMap<>();
            response.put("vehicle", vehicle);
            response.put("message", "Lokalizacja pojazdu została ustawiona na podstawie nazwy!");
            response.put("isAtAllowedLocation", vehicle.isAtAllowedLocation());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Błąd: " + e.getMessage());
        }
    }
}