package org.example.service;

import org.example.model.AllowedLocation;
import org.example.repository.AllowedLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class AllowedLocationService {
    private final AllowedLocationRepository allowedLocationRepository;

    public AllowedLocationService(AllowedLocationRepository allowedLocationRepository) {
        this.allowedLocationRepository = allowedLocationRepository;
    }
    public AllowedLocation save(AllowedLocation allowedLocation) {
        return allowedLocationRepository.save(allowedLocation);
    }
    public AllowedLocation findById(Long id) {
        return allowedLocationRepository.findById(id);
    }
    public List<AllowedLocation> findAll() {
        return allowedLocationRepository.findAll();
    }
    public List<AllowedLocation> findAllActive() {
        return allowedLocationRepository.findAllActive();
    }
    public List<AllowedLocation> findMainOffices() {
        return allowedLocationRepository.findByIsMainOffice(true);
    }
    public void deleteById(Long id) {
        allowedLocationRepository.deleteById(id);
    }
    public AllowedLocation createMainOffice(String name, BigDecimal latitude, BigDecimal longitude, Integer radiusMeters) {
        AllowedLocation mainOffice = AllowedLocation.builder()
                .name(name)
                .latitude(latitude)
                .longitude(longitude)
                .radiusMeters(radiusMeters)
                .isMainOffice(true)
                .isActive(true)
                .build();
        return save(mainOffice);
    }
}
