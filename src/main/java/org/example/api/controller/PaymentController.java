package org.example.api.controller;

import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Płatności", description = "Zarządzanie płatnościami za wypożyczenia pojazdów")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "Tworzenie Payment Intent",
            description = "Tworzy Payment Intent w Stripe dla konkretnego wypożyczenia"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Intent został utworzony pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane lub płatność już wykonana"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień do tej płatności")
    })
    public ResponseEntity<?> createPaymentIntent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dane żądania płatności",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PaymentRequest.class))
            )
            @RequestBody PaymentRequest paymentRequest,
            Authentication authentication) {

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
    @Operation(
            summary = "Tworzenie sesji Checkout",
            description = "Tworzy sesję Stripe Checkout dla konkretnego wypożyczenia"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesja checkout została utworzona pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane lub płatność już wykonana"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień do tej płatności")
    })
    public ResponseEntity<?> createCheckoutSession(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dane żądania płatności",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PaymentRequest.class))
            )
            @RequestBody PaymentRequest paymentRequest,
            Authentication authentication) {

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
    @Operation(
            summary = "Potwierdzenie płatności",
            description = "Endpoint do potwierdzenia pomyślnej płatności po powrocie z Stripe"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Płatność została pomyślnie przetworzona"),
            @ApiResponse(responseCode = "400", description = "Błąd podczas przetwarzania płatności")
    })
    public ResponseEntity<?> paymentSuccess(
            @Parameter(description = "ID sesji Stripe", required = true)
            @RequestParam("session_id") String sessionId) {
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
    @Operation(
            summary = "Anulowanie płatności",
            description = "Endpoint do obsługi anulowania płatności"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Płatność została anulowana")
    })
    public ResponseEntity<?> paymentCancel(
            @Parameter(description = "ID wypożyczenia", required = true)
            @RequestParam("rental_id") Long rentalId) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "cancelled");
        response.put("message", "Płatność została anulowana!");
        response.put("rentalId", rentalId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-status/{rentalId}")
    @Operation(
            summary = "Aktualizacja statusu płatności",
            description = "Aktualizuje status płatności dla konkretnego wypożyczenia"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status płatności został zaktualizowany"),
            @ApiResponse(responseCode = "400", description = "Nie znaleziono wypożyczenia"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień")
    })
    public ResponseEntity<?> updatePaymentStatus(
            @Parameter(description = "ID wypożyczenia", required = true)
            @PathVariable Long rentalId,
            @Parameter(description = "ID płatności Stripe", required = true)
            @RequestParam String stripePaymentId,
            @Parameter(description = "Czy to sesja checkout", required = false)
            @RequestParam(defaultValue = "false") boolean isCheckoutSession,
            Authentication authentication) {

        Rental rental = rentalService.findById(rentalId);
        if (rental == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono wypożyczenia");
        }
        if (!isUserAuthorizedForRental(rental, authentication)) {
            return ResponseEntity.status(403).body("Brak uprawnień");
        }
        Rental updateRental = rentalService.updatePaymentStatus(rentalId, stripePaymentId, isCheckoutSession);

        Map<String, Object> response = new HashMap<>();
        response.put("rental", updateRental);
        response.put("paymentStatus", updateRental.getPaymentStatus());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    @Operation(
            summary = "Webhook Stripe",
            description = "Endpoint do obsługi webhooków od Stripe"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook przetworzony pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Błąd podczas przetwarzania webhook")
    })
    public ResponseEntity<String> handleStripeWebhook(
            HttpServletRequest request,
            @Parameter(description = "Sygnatura Stripe", required = true)
            @RequestHeader("Stripe-Signature") String sigHeader) {

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
    @Operation(
            summary = "Sprawdzenie statusu płatności",
            description = "Sprawdza aktualny status płatności na podstawie ID sesji"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status płatności sprawdzony pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nie znaleziono wypożyczenia lub błąd"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień")
    })
    public ResponseEntity<?> checkPaymentStatus(
            @Parameter(description = "ID sesji Stripe", required = true)
            @RequestParam String sessionId,
            Authentication authentication) {
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
                    rentalService.updatePaymentStatus(rental.getId(), session.getId(), true);
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