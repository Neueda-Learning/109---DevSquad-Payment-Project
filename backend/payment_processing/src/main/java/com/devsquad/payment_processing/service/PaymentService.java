
package  com.devsquad.payment_processing.service;
import com.devsquad.payment_processing.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.devsquad.payment_processing.model.Payment;
import java.util.List;
@Service
public class PaymentService {
    @Autowired
    PaymentRepository paymentRepo;
    public Payment createPayment(Payment request){
        return paymentRepo.createPayment(request);
    };
    public Payment getPaymentById(Integer paymentId){
        return   paymentRepo.getPaymentById(paymentId);
    };

    public List<Payment> getAllPayments(){
        return paymentRepo.getAllPayments();
    };
    public void deletePayment(Integer paymentId){paymentRepo.deletePayment(paymentId);
    };

}