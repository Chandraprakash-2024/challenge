package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.MoneyTransfer;
import com.dws.challenge.exception.AccountNotExistsException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    @Autowired
    private final NotificationService notificationService;

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public AccountsRepositoryInMemory(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }


    @Override
    @Transactional
    public void transferMoney(MoneyTransfer moneyTransfer) {

        Account frmAccount = accounts.get(moneyTransfer.getAccountFromId());
       if(frmAccount==null)
           throw new AccountNotExistsException("AccountId "+moneyTransfer.getAccountFromId()+" does not exists");
        Account toAccount = accounts.get(moneyTransfer.getAccountToId());
        if(toAccount==null)
            throw new AccountNotExistsException("AccountId "+moneyTransfer.getAccountToId()+" does not exists");

        BigDecimal fromBalance =frmAccount.getBalance().subtract(moneyTransfer.getAmount());
        if(fromBalance.compareTo(BigDecimal.ZERO)>=0)
            frmAccount.setBalance(fromBalance);
        else
            throw new InsufficientBalanceException("AccountId "+moneyTransfer.getAccountFromId()+" doesnt have sufficient balance.");

        toAccount.setBalance(toAccount.getBalance().add(moneyTransfer.getAmount()));
        accounts.put(frmAccount.getAccountId(),frmAccount);
        accounts.put(toAccount.getAccountId(),toAccount);
        this.notificationService.notifyAboutTransfer(frmAccount,"Amount "+moneyTransfer.getAmount()+ " transferred to Account "+toAccount.getAccountId());
        this.notificationService.notifyAboutTransfer(toAccount,"Amount "+moneyTransfer.getAmount()+ " credited from Account "+frmAccount.getAccountId());
    }
}
