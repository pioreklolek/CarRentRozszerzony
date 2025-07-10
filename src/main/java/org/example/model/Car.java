package org.example.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Entity
@DiscriminatorValue("Car")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Car extends Vehicle {

    public Car(String brand, String model, int year, int price, String plate, Map<String, String> attributes) {
        super("Car", brand, model, year, price, plate, attributes);
    }

    @Override
    public String toString() {
        return getId() + " " + getType() + " " + getBrand() + " " + getModel() + " " +
                getYear() + " " + getPlate() + " " + getPrice() + " " +
                (isRented() ? "Wypożyczony" : "Dostępny");
    }
}