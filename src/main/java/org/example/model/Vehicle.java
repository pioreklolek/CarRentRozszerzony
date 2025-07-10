    package org.example.model;

    import jakarta.persistence.*;
    import lombok.*;
    import org.hibernate.annotations.JdbcTypeCode;
    import org.hibernate.type.SqlTypes;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.Objects;

    @Entity
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
    @DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
    @Table(name = "vehicle")
    public class Vehicle {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(insertable = false, updatable = false)
        private String type;

        private String brand;
        private String model;
        private int year;
        private int price;

        private boolean rented;

        @Column(name = "deleted")
        private boolean deleted = false;


        @Column(nullable = false, unique = true)
        private String plate;

        @JdbcTypeCode(SqlTypes.JSON)
        @Column(columnDefinition = "jsonb")
        private Map<String, String> attributes;

        @Column(name = "latitude", precision = 11, scale = 8)
        private BigDecimal latitude;

        @Column(name = "longitude", precision = 11, scale = 8)
        private BigDecimal longitude;

        @Column(name = "location_name")
        private String locationName;

        @Column(name = "last_location_update")
        private LocalDateTime lastLocationUpdate;

        @Column(name = "isAtAllowedLocation")
        private boolean isAtAllowedLocation;


        public Vehicle(String type, String brand, String model, int year, int price, String plate, Map<String, String> attributes) {
            this.type = type;
            this.brand = brand;
            this.model = model;
            this.year = year;
            this.price = price;
            this.plate = plate;
            this.attributes = attributes != null ? attributes : new HashMap<>();
            this.rented = false;
            this.isAtAllowedLocation = true;
        }
        public void updateLocation(BigDecimal latitude, BigDecimal longitude, String locationName, boolean isAtAllowedLocation) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.locationName = locationName;
            this.isAtAllowedLocation = isAtAllowedLocation;
            this.lastLocationUpdate = LocalDateTime.now();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Vehicle)) return false;
            Vehicle vehicle = (Vehicle) o;
            return year == vehicle.year &&
                    price == vehicle.price &&
                    rented == vehicle.rented &&
                    Objects.equals(id, vehicle.id) &&
                    Objects.equals(brand, vehicle.brand) &&
                    Objects.equals(model, vehicle.model) &&
                    Objects.equals(type, vehicle.type) &&
                    Objects.equals(plate, vehicle.plate);
        }


        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(boolean deleted) {
            this.deleted = deleted;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, type, brand, model, year, price, rented, plate);
        }
    }