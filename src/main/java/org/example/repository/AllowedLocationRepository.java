package org.example.repository;

import org.example.model.AllowedLocation;

import java.util.List;

public interface AllowedLocationRepository {
    AllowedLocation save(AllowedLocation allowedLocation);
    AllowedLocation findById(Long Id);
    List<AllowedLocation> findAll();
    List<AllowedLocation> findAllActive();
    List<AllowedLocation> findByIsMainOffice(boolean isMainOffice);
    void delete(AllowedLocation allowedLocation);
    void deleteById(long Id);
    AllowedLocation findByNameAndIsActiveTrue(String name);
}
