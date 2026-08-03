
package  com.devsquad.payment_processing.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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