package com.devsquad.payment_processing.Service;

//import com.devsquad.payment_processing.Model.Currency;
//import com.devsquad.payment_processing.Model.PaymentMode;
//import com.devsquad.payment_processing.Model.Tag;
import com.devsquad.payment_processing.Repository.CatalogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CatalogService {

    @Autowired
    private CatalogRepository catalogRepo;

    // Get all supported currencies
    public List<Currency> getAllCurrencies() {
        return catalogRepo.getAllCurrencies();
    }

    // Convert currency
    public BigDecimal convertCurrency(String fromCurrency,
                                      String toCurrency,
                                      BigDecimal amount) {

        return catalogRepo.convertCurrency(fromCurrency, toCurrency, amount);
    }

    // Get all payment modes
    public List<PaymentMode> getPaymentModes() {
        return catalogRepo.getPaymentModes();
    }

    // Get all tags
    public List<Tag> getAllTags() {
        return catalogRepo.getAllTags();
    }

    // Create a new tag
    public Tag createTag(Tag tag) {
        return catalogRepo.createTag(tag);
    }

    // Delete tag
    public void deleteTag(Integer tagId) {
        catalogRepo.deleteTag(tagId);
    }

}