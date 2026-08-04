package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.Currency;
import com.devsquad.payment_processing.model.PaymentMode;
import com.devsquad.payment_processing.model.Tag;
import com.devsquad.payment_processing.repository.CatalogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CatalogService {

    @Autowired
    private CatalogRepository catalogRepo;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "https://api.frankfurter.app/latest";
    // Get all supported currencies
    public List<Currency> getAllCurrencies() {
        return catalogRepo.getAllCurrencies();
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

    public BigDecimal convertCurrency(String fromCurrency, String toCurrency, BigDecimal amount) {

        String url = API_URL + "?from=" + fromCurrency + "&to=" + toCurrency;

        Map response = restTemplate.getForObject(url, Map.class);
        Map<String, Double> rates = (Map<String, Double>) response.get("rates");

        BigDecimal rate = BigDecimal.valueOf(rates.get(toCurrency));

        return amount.multiply(rate);
    }

}