package com.devsquad.payment_processing.api;


import com.devsquad.payment_processing.model.Payment;
import com.devsquad.payment_processing.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Create Payment
    @PostMapping("/create")
    public Payment createPayment(@Valid @RequestBody Payment request) {
        return paymentService.createPayment( request);
    }
    // Get Payment By Id
    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Integer id) {
        return paymentService.getPaymentById(id);
    }

    // Get All Payments
    @GetMapping("/all")
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }
    // Delete Payment
    @DeleteMapping("/{id}")
    public String deletePayment(@PathVariable Integer id) {

        paymentService.deletePayment(id);

        return "Payment deleted successfully";
    }




}