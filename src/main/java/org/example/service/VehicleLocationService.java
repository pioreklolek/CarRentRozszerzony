package org.example.service;

import org.example.model.AllowedLocation;
import org.example.model.Vehicle;
import org.example.repository.AllowedLocationRepository;
import org.example.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class VehicleLocationService {
    private final VehicleRepository vehicleRepository;
    private final AllowedLocationRepository allowedLocationRepository;
    private final Random random = new Random();

    public VehicleLocationService(VehicleRepository vehicleRepository, AllowedLocationRepository allowedLocationRepository) {
        this.vehicleRepository = vehicleRepository;
        this.allowedLocationRepository = allowedLocationRepository;
    }

    public boolean isVehicleAtAllowedLocation(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle == null || vehicle.getLatitude() == null || vehicle.getLongitude() == null) {
            return  false;
        }
        List<AllowedLocation> allowedLocations = allowedLocationRepository.findAllActive();

        for (AllowedLocation location : allowedLocations) {
            double distance = calculateDistance(
                    vehicle.getLatitude(), vehicle.getLongitude(),location.getLatitude(), location.getLongitude()
            );
            if (distance <= location.getRadiusMeters()) {
                return true;
            }
        }
        return false;
    }
    public Vehicle setVehicleLocation(Long vehicleId, BigDecimal latitude, BigDecimal longitude, String locationName) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if(vehicle == null) {
            throw new IllegalArgumentException("Pojazd nie został znaleziony!");
        }
        boolean isAtAllowedLocation = isLocationAllowed(latitude,longitude);
        vehicle.updateLocation(latitude,longitude,locationName,isAtAllowedLocation);

        return vehicleRepository.save(vehicle);
    }

    public Vehicle setRandomVehicleLocation(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle == null) {
            return  null;
        }
        BigDecimal latitude = generateRandomCoordinate(new BigDecimal("52.2292"), new BigDecimal("0.1"));
        BigDecimal longitude = generateRandomCoordinate(new BigDecimal("21.0122"), new BigDecimal("0.1"));

        String locationName = "Losowa lokalizacja " + random.nextInt(1000);
        boolean isAtAllowedLocation = isLocationAllowed(latitude,longitude);

        vehicle.updateLocation(latitude,longitude,locationName,isAtAllowedLocation);

        return vehicleRepository.save(vehicle);
    }
    private boolean isLocationAllowed(BigDecimal latitude, BigDecimal longitude) {
        List<AllowedLocation> allowedLocations = allowedLocationRepository.findAllActive();

        for(AllowedLocation location : allowedLocations) {
            double distance = calculateDistance(latitude,longitude, location.getLatitude(), location.getLongitude());
            if(distance <= location.getRadiusMeters()) {
                return true;
            }
        }
        return false;
    }

    private double calculateDistance(BigDecimal lat1, BigDecimal long1, BigDecimal lat2, BigDecimal long2) {
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double long1Rad = Math.toRadians(long1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double long2Rad = Math.toRadians(long2.doubleValue());

        double earthRadius = 6371000;

        double dLat = lat2Rad - lat1Rad;
        double dLong = long2Rad - long1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLong / 2) * Math.sin(dLong / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return  earthRadius * c;
    }
    private BigDecimal generateRandomCoordinate(BigDecimal base, BigDecimal range) {
        double randomOffset = (random.nextDouble() - 0.5) * 2 * range.doubleValue();
        return base.add(BigDecimal.valueOf(randomOffset)).setScale(8, RoundingMode.HALF_UP);
    }
    public List<Vehicle> getVehiclesNotAtAllowedLocation() {
        return vehicleRepository.findByIsAtAllowedLocationFalse();
    }
    public void updateAllVehicleLocationStatus() {
        List<Vehicle> allVehicles = vehicleRepository.findAll();

        for(Vehicle vehicle : allVehicles) {
            if (vehicle.getLatitude() != null && vehicle.getLongitude() != null) {
                boolean isAtAllowedLocation = isLocationAllowed(vehicle.getLatitude(),vehicle.getLongitude());
                vehicle.setAtAllowedLocation(isAtAllowedLocation);
                vehicleRepository.save(vehicle);
            }
        }
    }
    public boolean isVehicleAtMainOffice(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId);
            if (vehicle == null || vehicle.getLatitude() == null ||  vehicle.getLongitude() == null) {
                return false;
            }
            List<AllowedLocation> mainOffices = allowedLocationRepository.findByIsMainOffice(true);

            for(AllowedLocation office : mainOffices) {
                double distance = calculateDistance(
                        vehicle.getLatitude(), vehicle.getLongitude(),
                        office.getLatitude(), office.getLongitude()
                );
                if (distance <= office.getRadiusMeters()) {
                    return true;
                }
            }
            return false;
    }
    public Vehicle setVehicleLocationByName(Long vehicleId, String locationName) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Pojazd nie został znaleziony!");
        }

        AllowedLocation location = allowedLocationRepository.findByNameAndIsActiveTrue(locationName);
        if (location == null) {
            throw new IllegalArgumentException("Lokalizacja nie została znaleziona lub jest nieaktywna!");
        }

        vehicle.updateLocation(
                location.getLatitude(),
                location.getLongitude(),
                location.getName(),
                true
        );

        return vehicleRepository.save(vehicle);
    }
}
