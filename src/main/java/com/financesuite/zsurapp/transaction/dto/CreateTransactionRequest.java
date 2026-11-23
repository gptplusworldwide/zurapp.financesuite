
package com.financesuite.zsurapp.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.financesuite.zsurapp.transaction.entity.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTransactionRequest(

    @NotNull(message = "La cuenta es obligatoria")
    UUID accountId,

    @NotNull(message = "La categoria es obligatoria")
    UUID categoryId,

    @NotNull(message = "El tipo es obligatorio")
    TransactionType type,

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(value = "0.0001",
        message = "El importe debe ser positivo")
    @Digits(integer = 15, fraction = 4)
    BigDecimal amount,

    @Size(max = 500)
    String description,

    @NotNull(message = "La fecha es obligatoria")
    Instant occurredAt

) {}
