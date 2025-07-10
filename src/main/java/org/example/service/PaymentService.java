package org.example.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.example.dto.PaymentRequest;
import org.example.dto.PaymentResponse;
import org.example.model.PaymentStatus;
import org.example.model.Rental;
import org.example.model.Vehicle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.stripe.model.checkout.Session;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PaymentService {
    private final VehicleService vehicleService;
    private final RentalService rentalService;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    public PaymentService(VehicleService vehicleService, @Lazy RentalService rentalService) {
        this.vehicleService = vehicleService;
        this.rentalService = rentalService;
    }

    public BigDecimal calculateTotalCost(Long vehicleId, int rentalDays) {
        Vehicle vehicle = vehicleService.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Pojazd nie został znaleziony!"));

        BigDecimal dailyRate = BigDecimal.valueOf(vehicle.getPrice());
        return dailyRate.multiply(BigDecimal.valueOf(rentalDays));
    }


    public PaymentResponse createPaymentIntent(PaymentRequest paymentRequest, Rental rental) {
        try {
            long amountInCents = rental.getTotalCost().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents).setCurrency(paymentRequest.getCurrency().toLowerCase())
                    .setDescription(String.format("Wypożyczenie pojazdu ID: %d na %d dni", rental.getVehicleId(), rental.getRentalDays()))
                    .putMetadata("rental_id", rental.getId().toString())
                    .putMetadata("user_email", paymentRequest.getEmail()).build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            PaymentResponse response = new PaymentResponse("success", "PaymentIntent utworzony pomyślnie!");
            response.setClientSecret(paymentIntent.getClientSecret());
            response.setPaymentIntentId(paymentIntent.getId());
            response.setAmount(amountInCents);
            return response;
        } catch (StripeException e) {
            return new PaymentResponse("error", "Błąd podczas tworzenia płatności: " + e.getMessage());
        }
    }

    public PaymentResponse createCheckoutSession(PaymentRequest paymentRequest, Rental rental) {
        try {
            long amountInCents = rental.getTotalCost()
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(baseUrl + "/api/payments/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(baseUrl + "/api/payments/cancel?rental_id=" + rental.getId())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(paymentRequest.getCurrency().toLowerCase())
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(String.format("Wypożyczenie pojazdu ID: %d", rental.getVehicleId()))
                                                                    .setDescription(String.format("Wypożyczenie na %d dni", rental.getRentalDays()))
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    );

            paramsBuilder.putMetadata("rental_id", rental.getId().toString());
            paramsBuilder.putMetadata("user_email", paymentRequest.getEmail());

            if (paymentRequest.getEmail() != null && !paymentRequest.getEmail().isEmpty()) {
                paramsBuilder.setCustomerEmail(paymentRequest.getEmail());
            }

            SessionCreateParams params = paramsBuilder.build();
            Session session = Session.create(params);

            rental.setStripeSessionId(session.getId());
            rentalService.save(rental);

            PaymentResponse response = new PaymentResponse("success", "Checkout session utworzony pomyślnie!");
            response.setCheckoutUrl(session.getUrl());
            response.setUrl(session.getUrl());
            response.setPaymentIntentId(session.getId());
            response.setAmount(amountInCents);

            return response;
        } catch (StripeException e) {
            return new PaymentResponse("error", "Błąd podczas tworzenia sesji płatności: " + e.getMessage());
        }
    }

    public PaymentStatus checkPaymentStatus(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            switch (paymentIntent.getStatus()) {
                case "succeeded":
                    return PaymentStatus.PAID;
                case "processing":
                case "requires_payment_method":
                case "requires_confirmation":
                case "requires_action":
                    return PaymentStatus.PENDING;
                case "canceled":
                case "requires_capture":
                    return PaymentStatus.FAILED;
                default:
                    return PaymentStatus.PENDING;
            }
        } catch (StripeException e) {
            return PaymentStatus.FAILED;
        }
    }

    public PaymentStatus checkCheckouSessionStatus(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);

            if ("complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus())) {
                return PaymentStatus.PAID;
            } else if ("open".equals(session.getStatus())) {
                return PaymentStatus.PENDING;
            } else {
                return PaymentStatus.FAILED;
            }
        } catch (StripeException e) {
            return PaymentStatus.FAILED;
        }
    }


    public void updatePaymentStatus(Rental rental, PaymentStatus status) {
        rental.setPaymentStatus(status);
    }

    public boolean isPaymentCompleted(Rental rental) {
        return rental.getPaymentStatus() == PaymentStatus.PAID;
    }

    public  boolean isPaymentPending(Rental rental) {
        return  rental.getPaymentStatus() == PaymentStatus.PENDING;
    }
}