package org.example.api.controller;

import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;
import com.stripe.net.Webhook;
import org.example.dto.PaymentRequest;
import org.example.dto.PaymentResponse;
import org.example.model.PaymentStatus;
import org.example.model.Rental;
import org.example.model.User;
import org.example.service.PaymentService;
import org.example.service.impl.RentalServiceImpl;
import org.example.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final RentalServiceImpl rentalService;
    private final UserServiceImpl userService;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    public PaymentController(PaymentService paymentService, RentalServiceImpl rentalService, UserServiceImpl userService) {
        this.paymentService = paymentService;
        this.rentalService = rentalService;
        this.userService = userService;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody PaymentRequest paymentRequest, Authentication authentication) {
        Rental rental = rentalService.findById(paymentRequest.getRentalId());
        if (rental == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono wypożyczenia!");
        }
        if (!isUserAuthorizedForRental(rental, authentication)) {
            return ResponseEntity.status(403).body("Brak uprawnień do tej płatności!");
        }
        if (rental.getPaymentStatus() == PaymentStatus.PAID) {
            return ResponseEntity.badRequest().body("Płatność została już wykonana!");
        }
        PaymentResponse response = paymentService.createPaymentIntent(paymentRequest, rental);

        if ("success".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody PaymentRequest paymentRequest, Authentication authentication) {
        Rental rental = rentalService.findById(paymentRequest.getRentalId());
        if (rental == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono wypożyczenia!");
        }
        if (!isUserAuthorizedForRental(rental, authentication)) {
            return ResponseEntity.status(403).body("Brak uprawnień do tej płatności");
        }
        if (rental.getPaymentStatus() == PaymentStatus.PAID) {
            return ResponseEntity.badRequest().body("Płatność została już wykonana!");
        }

        PaymentResponse response = paymentService.createCheckoutSession(paymentRequest, rental);

        if ("success".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/success")
    public ResponseEntity<?> paymentSuccess(@RequestParam("session_id") String sessionId) {
        try {
            PaymentStatus status = paymentService.checkCheckouSessionStatus(sessionId);

            if (status == PaymentStatus.PAID) {
                Rental rental = rentalService.findByStripeSessionId(sessionId);
                if (rental != null) {
                    rentalService.updatePaymentStatus(rental.getId(), sessionId, true);

                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Płatność została pomyślnie przetworzona!");
                    response.put("rental", rental);
                    response.put("sessionId", sessionId);

                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.badRequest().body("Nie znaleziono wypożyczenia dla tej sesji");
                }
            } else {
                return ResponseEntity.badRequest().body("Płatność nie została potwierdzona!");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd podczas przetwarzania płatności: " + e.getMessage());
        }
    }
    @GetMapping("/cancel")
    public ResponseEntity<?> paymentCancel(@RequestParam("rental_id") Long rentalId) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "cancelled");
        response.put("message", "Płatność została anulowana!");
        response.put("rentalId",rentalId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/update-status/{rentalId}")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long rentalId, @RequestParam String stripePaymentId,@RequestParam(defaultValue = "false") boolean isCheckoutSession, Authentication authentication) {
        Rental rental = rentalService.findById(rentalId);
        if(rental == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono wypożyczenia");
        }
        if(!isUserAuthorizedForRental(rental,authentication)) {
            return  ResponseEntity.status(403).body("Brak uprawnień");
        }
        Rental updateRental = rentalService.updatePaymentStatus(rentalId,stripePaymentId,isCheckoutSession);

        Map<String, Object> response = new HashMap<>();
        response.put("rental", updateRental);
        response.put("paymentStatus",updateRental.getPaymentStatus());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            HttpServletRequest request, @RequestHeader("Stripe-Signature") String sigHeader) {

        String payload;
        try {
            payload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            System.err.println("Błąd odczytu payload: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error reading payload");
        }

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            switch (event.getType()) {
                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "charge.succeeded":
                    handleChargeSucceeded(event);
                    break;
                default:
                    System.out.println("Nieobsługiwany typ eventu: " + event.getType());
            }
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            System.err.println("Błąd podczas przetwarzania webhook: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400).body("Webhook error: " + e.getMessage());
        }
    }
    @PostMapping("/check-payment-status")
    public ResponseEntity<?> checkPaymentStatus(@RequestParam String sessionId, Authentication authentication) {
        try {
            Rental rental = rentalService.findByStripeSessionId(sessionId);
            if (rental == null) {
                return ResponseEntity.badRequest().body("Nie znaleziono wypożyczenia");
            }

            if (!isUserAuthorizedForRental(rental, authentication)) {
                return ResponseEntity.status(403).body("Brak uprawnień");
            }

            PaymentStatus currentStatus = paymentService.checkCheckouSessionStatus(sessionId);

            if (currentStatus == PaymentStatus.PAID && rental.getPaymentStatus() != PaymentStatus.PAID) {
                rental = rentalService.updatePaymentStatus(rental.getId(), sessionId, true);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("rental", rental);
            response.put("paymentStatus", rental.getPaymentStatus());
            response.put("sessionId", sessionId);

            if (rental.getPaymentStatus() == PaymentStatus.PAID) {
                response.put("message", "Płatność została pomyślnie zrealizowana!");
            } else {
                response.put("message", "Płatność jeszcze nie została zrealizowana");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd podczas sprawdzania statusu: " + e.getMessage());
        }
    }


    private void handleChargeSucceeded(Event event) {
        System.out.println("Charge succeeded: " + event.getId());
    }
    private void handleCheckoutSessionCompleted(Event event) {
        try {
            StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);

            if (stripeObject instanceof Session) {
                Session session = (Session) stripeObject;

                Rental rental = rentalService.findByStripeSessionId(session.getId());
                if (rental != null && rental.getPaymentStatus() != PaymentStatus.PAID) {
                    rentalService.updatePaymentStatus(rental.getId(),session.getId(), true);
                    System.out.printf("Płatność automatycznie zaktualizowana dla rental ID: " + rental.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas obsługi checkout.session.completed:" + e.getMessage());
        }
    }
    private void handlePaymentIntentSucceeded(Event event) {
        System.out.printf("PaymentIntent succeeded:" + event.getId());
    }



    private boolean isUserAuthorizedForRental(Rental rental, Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("admin"))) {
            return true;
        }
        User user = userService.findByLogin(authentication.getName());
        if (user == null) {
            return false;
        }
        return rental.getUserId().equals(user.getId());
    }
}
