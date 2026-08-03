
package  com.devsquad.payment_processing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class PaymentService {
    @Autowired
    PaymentRepository  paymentRepo;
    Payment createPayment(Payment request){
        return paymentRepo.createPayment(request);
    };
    Payment getPaymentById(Integer paymentId){
        return   paymentRepo.getPaymentById(paymentId);
    };

    List<Payment> getAllPayments(){
        return paymentRepo.getAllPayments();
    };
    void deletePayment(Integer paymentId){
        return paymentRepo.deletePayment(paymentId);
    };

}