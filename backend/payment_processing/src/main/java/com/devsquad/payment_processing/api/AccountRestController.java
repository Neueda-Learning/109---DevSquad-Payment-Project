package com.devsquad.payment_processing.api;

import com.devsquad.payment_processing.model.Account;
import com.devsquad.payment_processing.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/1.0/accounts")
public class AccountRestController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/all")
    public ArrayList<Account> getAllAccounts() {
        return accountService.getAllAccountsS();
    }

//    create api to get account by id
    @GetMapping("/{accountNumber}")
    public Account getAccountById(@PathVariable Long accountNumber) {
        return accountService.getAccountByIdS(accountNumber);
    }
}
