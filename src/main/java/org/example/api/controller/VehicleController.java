package org.example.api.controller;
import jakarta.validation.Valid;
import org.example.model.Car;
import org.example.model.Motorcycle;
import org.example.model.Vehicle;
import org.example.service.VehicleService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;
    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }
    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAll());
    }
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<Vehicle>> getAllActiveVehicles() {
        return ResponseEntity.ok(vehicleService.getAllActive());
    }
    @GetMapping("/available")
    @PreAuthorize("hasAuthority('user') or hasAuthority('admin')")
    public ResponseEntity<List<Vehicle>> getAvailableVehicles() {
        return ResponseEntity.ok(vehicleService.getAvailable());
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        return vehicleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/rented")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<Vehicle>> getRentedVehicles() {
        return ResponseEntity.ok(vehicleService.getRented());
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('admin')")

    public ResponseEntity<List<Vehicle>> getDeletedVehicles() {
        return ResponseEntity.ok(vehicleService.getDeleted());
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> createVehicleDetailed(@Valid @RequestBody Map<String, Object> vehicleData) {
        try {
            String type = (String) vehicleData.getOrDefault("type", "Vehicle");
            Vehicle vehicle;

            switch (type) {
                case "Car":
                    vehicle = new Car();
                    break;
                case "Motorcycle":
                    vehicle = new Motorcycle();
                    break;
                default:
                    vehicle = new Vehicle();
                    break;
            }

            vehicle.setBrand((String) vehicleData.get("brand"));
            vehicle.setModel((String) vehicleData.get("model"));
            vehicle.setYear((Integer) vehicleData.get("year"));
            vehicle.setPrice((Integer) vehicleData.get("price"));
            vehicle.setPlate((String) vehicleData.get("plate"));
            vehicle.setRented(false);

            if (vehicleData.containsKey("attributes")) {
                vehicle.setAttributes((Map<String, String>) vehicleData.get("attributes"));
            }

            if (vehicle instanceof Motorcycle && vehicleData.containsKey("licenceCategory")) {
                ((Motorcycle) vehicle).setLicenceCategory((String) vehicleData.get("licenceCategory"));
            }

            Vehicle savedVehicle = vehicleService.save(vehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicle);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Nieprawidłowe dane pojazdu: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd podczas tworzenia pojazdu: " + e.getMessage());
        }
    }
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
