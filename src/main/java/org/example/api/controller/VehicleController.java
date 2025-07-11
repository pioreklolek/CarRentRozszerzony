package org.example.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Vehicle Management", description = "Zarządzanie pojazdami w wypożyczalni")
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Pobierz wszystkie pojazdy",
            description = "Zwraca listę wszystkich pojazdów w systemie (tylko dla adminów)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista pojazdów pobrana pomyślnie",
                    content = @Content(schema = @Schema(implementation = Vehicle.class))),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAll());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Pobierz aktywne pojazdy",
            description = "Zwraca listę wszystkich aktywnych pojazdów")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista aktywnych pojazdów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<List<Vehicle>> getAllActiveVehicles() {
        return ResponseEntity.ok(vehicleService.getAllActive());
    }

    @GetMapping("/available")
    @PreAuthorize("hasAuthority('user') or hasAuthority('admin')")
    @Operation(summary = "Pobierz dostępne pojazdy",
            description = "Zwraca listę pojazdów dostępnych do wypożyczenia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista dostępnych pojazdów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<List<Vehicle>> getAvailableVehicles() {
        return ResponseEntity.ok(vehicleService.getAvailable());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Pobierz pojazd po ID",
            description = "Zwraca szczegóły pojazdu o podanym ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pojazd znaleziony"),
            @ApiResponse(responseCode = "404", description = "Pojazd nie został znaleziony"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<Vehicle> getVehicleById(
            @Parameter(description = "ID pojazdu", required = true)
            @PathVariable Long id) {
        return vehicleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rented")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Pobierz wypożyczone pojazdy",
            description = "Zwraca listę wszystkich obecnie wypożyczonych pojazdów")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista wypożyczonych pojazdów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<List<Vehicle>> getRentedVehicles() {
        return ResponseEntity.ok(vehicleService.getRented());
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Pobierz usunięte pojazdy",
            description = "Zwraca listę wszystkich usuniętych pojazdów")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista usuniętych pojazdów pobrana pomyślnie"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<List<Vehicle>> getDeletedVehicles() {
        return ResponseEntity.ok(vehicleService.getDeleted());
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('admin')")
    @Operation(summary = "Utwórz nowy pojazd",
            description = "Tworzy nowy pojazd w systemie. Obsługuje różne typy pojazdów (Car, Motorcycle, Vehicle)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pojazd utworzony pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane pojazdu"),
            @ApiResponse(responseCode = "500", description = "Błąd serwera podczas tworzenia pojazdu"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<?> createVehicleDetailed(
            @Parameter(description = "Dane pojazdu do utworzenia", required = true,
                    schema = @Schema(example = "{\n" +
                            "  \"type\": \"Car\",\n" +
                            "  \"brand\": \"Toyota\",\n" +
                            "  \"model\": \"Camry\",\n" +
                            "  \"year\": 2023,\n" +
                            "  \"price\": 150,\n" +
                            "  \"plate\": \"ABC123\",\n" +
                            "  \"attributes\": {\n" +
                            "    \"color\": \"red\",\n" +
                            "    \"fuel\": \"petrol\"\n" +
                            "  },\n" +
                            "  \"licenceCategory\": \"A\" \n" +
                            "}"))
            @Valid @RequestBody Map<String, Object> vehicleData) {
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
    @Operation(summary = "Usuń pojazd",
            description = "Usuwa pojazd o podanym ID z systemu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pojazd usunięty pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Pojazd nie został znaleziony"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień dostępu"),
            @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
    })
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "ID pojazdu do usunięcia", required = true)
            @PathVariable Long id) {
        vehicleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}