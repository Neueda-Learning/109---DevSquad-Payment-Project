package com.devsquad.payment_processing.api;

import com.devsquad.payment_processing.model.User;
import com.devsquad.payment_processing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/1.0/users")
public class UserRestController {
    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ArrayList<User> getAllUsers() {
        return userService.getAllUsersS();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userService.getUserByIdS(userId);
    }
}
