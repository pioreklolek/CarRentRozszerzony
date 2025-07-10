package org.example.api.controller;

import org.example.model.AllowedLocation;
import org.example.model.Vehicle;
import org.example.repository.VehicleRepository;
import org.example.service.AllowedLocationService;
import org.example.service.VehicleLocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/locations")
public class VehicleLocationContoller {
    private final VehicleLocationService vehicleLocationService;
    private final AllowedLocationService allowedLocationService;

    public VehicleLocationContoller(VehicleLocationService vehicleLocationService, AllowedLocationService allowedLocationService) {
        this.vehicleLocationService = vehicleLocationService;
        this.allowedLocationService = allowedLocationService;
    }

    @GetMapping("/allowed")
    @PreAuthorize("hasAuthority('admin')")
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

    @PostMapping("/vehicles/{vehicleId}/location")
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

    @PostMapping("/vehicles/{vehicleId}/random-location")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> setRandomVehicleLocation(@PathVariable Long vehicleId) {
        Vehicle vehicle = vehicleLocationService.setRandomVehicleLocation(vehicleId);

        if (vehicle == null) {
            return ResponseEntity.badRequest().body("Pojazd nie został znaleziony!");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("vehicle", vehicle);
        response.put("message", "Losowa lokalizacja pojazdu została ustawiona");
        response.put("isAtAllowedLocation", vehicle.isAtAllowedLocation());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicles/{vehicleId}/status")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> checkVehicleLocationStatus(@PathVariable Long vehicleId) {
        boolean isAtAllowedLocation = vehicleLocationService.isVehicleAtAllowedLocation(vehicleId);
        boolean isAtMainOffice = vehicleLocationService.isVehicleAtMainOffice(vehicleId);

        Map<String, Object> response = new HashMap<>();
        response.put("vehicleId", vehicleId);
        response.put("isAtAllowedLocation", isAtAllowedLocation);
        response.put("isAtMainOffice", isAtMainOffice);
        response.put("message", isAtAllowedLocation ? "Pojazd jest w dozwolonej lokacji!" : "Pojazd jest w niedozwolonej lokacji!");

        return ResponseEntity.ok(response);
    }
    @GetMapping("/vehicles/not-allowed")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<Vehicle>> getVehiclesAtNotAllowedLocation() {
        List<Vehicle> vehicles = vehicleLocationService.getVehiclesNotAtAllowedLocation();
        return  ResponseEntity.ok(vehicles);
    }
    @PostMapping("/vehicles/update-all-status")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> updateAllVehicleLocationStatus() {
        vehicleLocationService.updateAllVehicleLocationStatus();
        return ResponseEntity.ok(Map.of("message", "Status lokalizacji zostal zaktualizowany"));
    }
}
