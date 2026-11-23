
package com.financesuite.zsurapp.transfer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTransferRequest(

    @NotNull(message = "La cuenta de origen es obligatoria")
    UUID fromAccountId,

    @NotNull(message = "La cuenta de destino es obligatoria")
    UUID toAccountId,

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(
        value = "0.0001",
        message = "El importe debe ser positivo"
    )
    @Digits(
        integer = 15,
        fraction = 4,
        message = "El importe debe tener como maximo 15 digitos enteros y 4 decimales"
    )
    BigDecimal amount,

    @Size(
        max = 500,
        message = "La descripcion no puede superar los 500 caracteres"
    )
    String description,

    @NotNull(message = "La fecha es obligatoria")
    Instant occurredAt

) {}
