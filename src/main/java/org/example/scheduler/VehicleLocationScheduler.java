package org.example.scheduler;

import org.example.model.Vehicle;
import org.example.repository.VehicleRepository;
import org.example.service.VehicleLocationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VehicleLocationScheduler {
    private final VehicleLocationService vehicleLocationService;
    private final VehicleRepository vehicleRepository;

    public VehicleLocationScheduler(VehicleLocationService vehicleLocationService, VehicleRepository vehicleRepository) {
        this.vehicleLocationService = vehicleLocationService;
        this.vehicleRepository = vehicleRepository;
    }
    @Scheduled(fixedRate = 60000)
    public void updateRandomVehicleLocations() {
        List<Vehicle> rentedVehicles = vehicleRepository.findByRentedTrue();

        for(Vehicle vehicle : rentedVehicles) {
            vehicleLocationService.setRandomVehicleLocation(vehicle.getId());
        }
        System.out.printf("Zaktualizowano lokacjie " + rentedVehicles.size() + " pojazdow");
    }
    @Scheduled(fixedRate = 360000)
    public void updateAllVehicleLocationStatus() {
        vehicleLocationService.updateAllVehicleLocationStatus();
        System.out.printf("Zaktualizowno lokacje wszystkich pojazdow!");
    }
}
