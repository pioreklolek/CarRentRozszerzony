package org.example.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Entity
@DiscriminatorValue("Motorcycle")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Motorcycle extends Vehicle {

    @Column(name = "licence_category")
    private String licenceCategory;

    public Motorcycle(String brand, String model, int year, int price, String licenceCategory, String plate, Map<String, String> attributes) {
        super("Motorcycle", brand, model, year, price, plate, attributes);
        this.licenceCategory = licenceCategory;
    }



    @Override
    public String toString() {
        return getId() + " " + getType() + " " + getBrand() + " " + getModel() + " " +
                getYear() + " "  + getPlate() + " " +
                (licenceCategory != null ? licenceCategory + " " : "") +
                getPrice() + " " +
                (isRented() ? "Wypożyczony" : "Dostępny");
    }

    public void setLicenceCategory(String licenceCategory) {
        this.licenceCategory = licenceCategory;
    }

    public String getLicenceCategory() {
        return licenceCategory;
    }
}