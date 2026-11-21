
package com.financesuite.zsurapp.account.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

import com.financesuite.zsurapp.account.entity.AccountType;

public record CreateAccountRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120)
    String name,

    @NotNull(message = "El tipo es obligatorio")
    AccountType type,

    @NotBlank(message = "La moneda es obligatoria")
    @Pattern(
        regexp = "[A-Z]{3}",
        message = "La moneda debe tener tres letras mayusculas"
    )
    String currency,

    @NotNull(message = "El saldo inicial es obligatorio")
    @Digits(integer = 15, fraction = 4)
    BigDecimal initialBalance

) {}
