package org.example.api.controller;

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
public class VehicleLocationContoller {
    private final VehicleLocationService vehicleLocationService;
    private final AllowedLocationService allowedLocationService;
    private final RentalService rentalService;
    private final UserService userService;

    public VehicleLocationContoller(VehicleLocationService vehicleLocationService, AllowedLocationService allowedLocationService, RentalService rentalService, UserService userService) {
        this.vehicleLocationService = vehicleLocationService;
        this.allowedLocationService = allowedLocationService;
        this.rentalService = rentalService;
        this.userService = userService;
    }

    @GetMapping("/allowed")
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    public ResponseEntity<List<AllowedLocation>> getAllowedLocations() {
        return ResponseEntity.ok(allowedLocationService.findAllActive());
    }

    @PostMapping("/allowed/create")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<AllowedLocation> createAllowedLocation(@RequestBody AllowedLocation allowedLocation) {
        AllowedLocation savedLocation = allowedLocationService.save(allowedLocation);
        return ResponseEntity.ok(savedLocation);
    }

    @DeleteMapping("/allowed/delete/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> deleteAllowedLocation(@PathVariable Long id) {
        allowedLocationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/set-location-by-coords/{vehicleId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> setVehicleLocation(
            @PathVariable Long vehicleId,
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
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
    public ResponseEntity<?> setRandomVehicleLocation(
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
    public ResponseEntity<?> checkVehicleLocationStatus(
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
    public ResponseEntity<List<Vehicle>> getVehiclesAtNotAllowedLocation() {
        List<Vehicle> vehicles = vehicleLocationService.getVehiclesNotAtAllowedLocation();
        return  ResponseEntity.ok(vehicles);
    }
    @PostMapping("/update-all-status")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> updateAllVehicleLocationStatus() {
        vehicleLocationService.updateAllVehicleLocationStatus();
        return ResponseEntity.ok(Map.of("message", "Status lokalizacji zostal zaktualizowany"));
    }


    @PostMapping("/set-location/{vehicleId}")
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    public ResponseEntity<?> setVehicleLocationByName(
            @PathVariable Long vehicleId,
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
