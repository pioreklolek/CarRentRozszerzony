package org.example.dto;
import lombok.Data;

@Data
public class PaymentResponse {
    private String status;
    private String message;
    private String clientSecret;
    private String paymentIntentId;
    private String checkoutUrl;
    private String url;
    private Long amount;

    public PaymentResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
