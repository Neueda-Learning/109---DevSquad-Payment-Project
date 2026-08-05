package com.devsquad.payment_processing.api;

import com.devsquad.payment_processing.model.User;
import com.devsquad.payment_processing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/1.0/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
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
