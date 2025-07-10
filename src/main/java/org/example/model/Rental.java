    package org.example.model;
    import jakarta.persistence.*;
    import lombok.*;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.time.temporal.ChronoUnit;

    @Entity
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "rental")

    public class Rental {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "vehicle_id", nullable = false)
        private Long vehicleId;

        @Column(name = "user_id", nullable = false)
        private Long userId;

        @Column(name = "rent_date", nullable = false)
        private String rentDate;

        @Column(name = "return_date", nullable = false)
        private String returnDate;

        @Column(name = "returned", nullable = false)
        private boolean returned = false;

        @Column(name = "total_cost" ,precision = 10, scale = 2)
        private BigDecimal totalCost;

        @Enumerated(EnumType.STRING)
        @Column(name = "payment_status")
        private PaymentStatus paymentStatus = PaymentStatus.PENDING;

        @Column(name = "payment_url")
        private String paymentUrl;

        @Column(name = "rental_days")
        private Integer rentalDays;

        @Column(name = "stripe_session_id")
        private String stripeSessionId;

        @Column(name = "stripe_payment_intent_id")
        private String stripePaymentIntentId;


        public Rental(Long vehicleId, Long userId, String rentDate, String returnDate) {
            this.vehicleId = vehicleId;
            this.userId = userId;
            this.rentDate = rentDate;
            this.returnDate = returnDate;
            this.paymentStatus = PaymentStatus.PENDING;
        }

        public int calculateRentalDays(){
            if (rentDate == null || returnDate == null){
                return 1;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime startDate = LocalDateTime.parse(rentDate, formatter);
            LocalDateTime endDate = LocalDateTime.parse(returnDate, formatter);

            long days = ChronoUnit.DAYS.between(startDate, endDate);
            return Math.max(1, (int)days);
        }
        public Long getId() {
            return id;
        }

        public Long getVehicleId() {
            return vehicleId;
        }

        public Long getUserId() {
            return userId;
        }

        public String getRentDate() {
            return rentDate;
        }

        public String getReturnDate() {
            return returnDate;
        }

        public void setUser(User currentUser) {
            this.userId = currentUser.getId();
        }

        public void setVehicle(Vehicle vehicle) {
            this.vehicleId = vehicle.getId();
        }

        public void setStartDate(String l) {
            this.rentDate = String.valueOf(l);
        }

        public void setEndDate(String l) {
            this.returnDate = String.valueOf(l);
        }

        public boolean isReturned() {
            return returned;
        }
        public void setReturned(boolean returned) {
            this.returned = returned;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public BigDecimal getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(BigDecimal totalCost) {
            this.totalCost = totalCost;
        }

        public PaymentStatus getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public String getPaymentUrl() {
            return paymentUrl;
        }

        public void setPaymentUrl(String paymentUrl) {
            this.paymentUrl = paymentUrl;
        }

        public Integer getRentalDays() {
            return rentalDays;
        }

        public void setRentalDays(Integer rentalDays) {
            this.rentalDays = rentalDays;
        }

        public void setVehicleId(Long vehicleId) {
            this.vehicleId = vehicleId;
        }

        public String getStripeSessionId() {
            return stripeSessionId;
        }

        public void setStripeSessionId(String stripeSessionId) {
            this.stripeSessionId = stripeSessionId;
        }

        public String getStripePaymentIntentId() {
            return stripePaymentIntentId;
        }

        public void setStripePaymentIntentId(String stripePaymentIntentId) {
            this.stripePaymentIntentId = stripePaymentIntentId;
        }
    }