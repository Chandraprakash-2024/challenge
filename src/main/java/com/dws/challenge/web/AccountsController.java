package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransfer;
import com.dws.challenge.exception.AccountNotExistsException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.Objects;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

  @PostMapping(path = "/moneytransfer", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> moneyTransfer(@RequestBody @Valid MoneyTransfer moneyTransfer) {
    log.info("transfer money {}", moneyTransfer);

    try {
      if(Objects.equals(moneyTransfer.getAccountFromId(), moneyTransfer.getAccountToId()))
        throw new DuplicateAccountIdException(" From Account and To account can not be same.");
      this.accountsService.transferMoney(moneyTransfer);
    } catch (AccountNotExistsException |DuplicateAccountIdException| InsufficientBalanceException die) {
      return new ResponseEntity<>(die.getMessage(), HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>("Money transferred successfully",HttpStatus.OK);
  }
}
