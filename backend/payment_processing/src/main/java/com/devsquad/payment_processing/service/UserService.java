package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.User;
import com.devsquad.payment_processing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public ArrayList<User> getAllUsersS() {
        return userRepository.getAllUsers();
    }

    public User getUserByIdS(Long userId) {
        return userRepository.getUserById(userId);
    }
}
