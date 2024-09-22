package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-223\",\"balance\":2000}")).andExpect(status().isCreated());

    Account accountOne = accountsService.getAccount("Id-123");
    assertThat(accountOne.getAccountId()).isEqualTo("Id-123");
    assertThat(accountOne.getBalance()).isEqualByComparingTo("1000");

    Account accountTwo = accountsService.getAccount("Id-223");
    assertThat(accountTwo.getAccountId()).isEqualTo("Id-223");
    assertThat(accountTwo.getBalance()).isEqualByComparingTo("2000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  @Test
  void moneyTransferWithIncorrectFromAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/moneytransfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFromId\":\"Id-023\",\"accountToId\":\"Id-223\",\"amount\":101}")).andExpect(status().isBadRequest());
  }

  @Test
  void moneyTransferWithIncorrectToAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/moneytransfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-023\",\"amount\":101}")).andExpect(status().isBadRequest());
  }

  @Test
  void moneyTransferSameAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/moneytransfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-123\",\"amount\":101}")).andExpect(status().isBadRequest());
  }

  @Test
  void moneyTransferNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/moneytransfer").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  @Test
  void moneyTransferEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/moneytransfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFromId\":\"\",\"accountToId\":\"Id-023\",\"amount\":101}")).andExpect(status().isBadRequest());
    this.mockMvc.perform(post("/v1/accounts/moneytransfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFromId\":\"\",\"accountToId\":\"\",\"amount\":101}")).andExpect(status().isBadRequest());
    this.mockMvc.perform(post("/v1/accounts/moneytransfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFromId\":\"Id-023\",\"accountToId\":\"\",\"amount\":101}")).andExpect(status().isBadRequest());

  }

  @Test
  void moneyTransfer() throws Exception {
    String uniqueAccountOne="Id-123";
    Account accountOne = new Account(uniqueAccountOne, new BigDecimal("1000.45"));
    this.accountsService.createAccount(accountOne);
    String uniqueAccountTwo="Id-223";
    Account accountTwo = new Account(uniqueAccountTwo, new BigDecimal("2000.45"));
    this.accountsService.createAccount(accountTwo);
    this.mockMvc.perform(post("/v1/accounts/moneytransfer").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountFromId\":\""+accountOne.getAccountId()+"\",\"accountToId\":\""+accountTwo.getAccountId()+"\",\"amount\":100}"))
            .andExpect(status().isOk())
            .andExpect(
                    content().string("Money transferred successfully"));

    accountOne = accountsService.getAccount(uniqueAccountOne);
    assertThat(accountOne.getAccountId()).isEqualTo(accountOne.getAccountId());
    assertThat(accountOne.getBalance()).isEqualByComparingTo("900.45");

    accountTwo = accountsService.getAccount(uniqueAccountTwo);
    assertThat(accountTwo.getAccountId()).isEqualTo(accountTwo.getAccountId());
    assertThat(accountTwo.getBalance()).isEqualByComparingTo("2100.45");
  }

  @Test
  void moneyTransferInsufficientBalance() throws Exception {
    String uniqueAccountOne="Id-123";
    Account accountOne = new Account(uniqueAccountOne, new BigDecimal("100"));
    this.accountsService.createAccount(accountOne);
    String uniqueAccountTwo="Id-223";
    Account accountTwo = new Account(uniqueAccountTwo, new BigDecimal("200"));
    this.accountsService.createAccount(accountTwo);
    this.mockMvc.perform(post("/v1/accounts/moneytransfer").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountFromId\":\""+accountOne.getAccountId()+"\",\"accountToId\":\""+accountTwo.getAccountId()+"\",\"amount\":101}"))
            .andExpect(status().isBadRequest())
            .andExpect(
                    content().string("AccountId "+uniqueAccountOne+" doesnt have sufficient balance."));

  }
}
