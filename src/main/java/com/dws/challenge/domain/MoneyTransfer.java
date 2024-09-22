package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MoneyTransfer {

    @NotNull
    @NotEmpty
    private final String accountFromId;

    @NotNull
    @NotEmpty
    private final String accountToId;

    @NotNull
    @Min(value = 1, message = "Amount must be positive.")
    private final BigDecimal amount;

    public MoneyTransfer(String accountFromId, String accountToId) {
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.amount = BigDecimal.ZERO;
    }

    @JsonCreator
    public MoneyTransfer(@JsonProperty("accountFromId") String accountFromId,
                         @JsonProperty("accountToId") String accountToId,
                   @JsonProperty("amount") BigDecimal amount) {
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.amount = amount;
    }
}
