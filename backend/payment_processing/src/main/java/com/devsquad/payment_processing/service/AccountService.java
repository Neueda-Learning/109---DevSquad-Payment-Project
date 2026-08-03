package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.Account;
import com.devsquad.payment_processing.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public ArrayList<Account> getAllAccountsS() {
        return accountRepository.getAllAccountsR();
    }
}
