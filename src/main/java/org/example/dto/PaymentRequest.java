package org.example.dto;
import lombok.Data;

@Data
public class PaymentRequest {
    private Long rentalId;
    private String email;
    private String currency = "PLN";
    private String description;
}
