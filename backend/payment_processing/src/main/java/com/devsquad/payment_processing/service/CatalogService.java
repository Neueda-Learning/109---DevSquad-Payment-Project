package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.Currency;
import com.devsquad.payment_processing.model.PaymentMode;
import com.devsquad.payment_processing.model.Tag;
import com.devsquad.payment_processing.repository.CatalogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

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
        int deleted = catalogRepo.deleteTag(tagId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "TAG_NOT_FOUND: " + tagId);
        }
    }

    public BigDecimal convertCurrency(String fromCurrency, String toCurrency, BigDecimal amount) {

        if (fromCurrency == null || fromCurrency.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR: 'from' currency is required");
        if (toCurrency == null || toCurrency.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR: 'to' currency is required");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR: amount must be positive");
        if (fromCurrency.equalsIgnoreCase(toCurrency))
            return amount;  // same currency, no conversion needed

        String url = API_URL + "?from=" + fromCurrency.toUpperCase() + "&to=" + toCurrency.toUpperCase();

        try {
            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("rates"))
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "EXTERNAL_API_ERROR: No rates returned");

            Map<String, Double> rates = (Map<String, Double>) response.get("rates");
            Double rateValue = rates.get(toCurrency.toUpperCase());

            if (rateValue == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "UNSUPPORTED_CURRENCY: No rate found for " + toCurrency.toUpperCase());

            BigDecimal rate = BigDecimal.valueOf(rateValue);
            return amount.multiply(rate).setScale(2, java.math.RoundingMode.HALF_UP);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "EXTERNAL_API_ERROR: Currency conversion failed - " + e.getMessage());
        }
    }

}