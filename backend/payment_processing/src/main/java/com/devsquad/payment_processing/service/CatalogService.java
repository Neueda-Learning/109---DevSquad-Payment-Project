package com.devsquad.payment_processing.service;

import com.devsquad.payment_processing.model.Currency;
import com.devsquad.payment_processing.model.PaymentMode;
import com.devsquad.payment_processing.model.Tag;
import com.devsquad.payment_processing.repository.CatalogRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CatalogService {

    @Autowired
    private CatalogRepository catalogRepo;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "https://api.frankfurter.app/latest";

    private List<Map<String, String>> currencies = new ArrayList<>();

    @PostConstruct
    public void loadCurrencies() {
        try (InputStream is = new ClassPathResource("currencies.json").getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            currencies = mapper.readValue(is, new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception ignored) {
            // ignore - currencies will be empty
        }
    }

    /**
     * Get currency code by ID from loaded currencies.json (1-based index)
     */
    private String getCurrencyCodeById(Integer currencyId) {
        System.out.println("getCurrencyCodeById: currencyId = " + currencyId);
        if (currencyId == null || currencyId < 0) return null;
        int idx = currencyId;
        System.out.println("getCurrencyCodeById: idx = " + idx);
        if (idx < 0 || idx >= currencies.size()) return null;

        Map<String, String> entry = currencies.get(idx);
        for(Map<String, String> map : currencies) {
            System.out.println("Currency: " + map.get("currency") );
        }
        if (entry == null) return null;
        String code = entry.get("currency");
        return code;
    }
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
        try {
            return catalogRepo.createTag(tag);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "TAG_CREATE_FAILED: " + e.getMessage());
        }
    }

    // Delete tag
    public void deleteTag(Integer tagId) {
        if (tagId == null || tagId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: tagId must be positive");
        }
        int deletedRows = catalogRepo.deleteTag(tagId);
        if (deletedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "TAG_NOT_FOUND: " + tagId);
        }
    }

    public BigDecimal convertCurrency(Integer fromCurrency, Integer toCurrency, BigDecimal amount) {
        String fromCurrencyName = getCurrencyCodeById(fromCurrency);
        String toCurrencyName = getCurrencyCodeById(toCurrency);
        System.out.println(fromCurrency + "---------------------- " + toCurrency);
        System.out.println("fromcurrency: " + fromCurrencyName + ", toCurrency: " + toCurrencyName + ", amount: " + amount);
        if (fromCurrencyName == null || fromCurrencyName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: 'from' currency is required");
        }

        if (toCurrencyName == null || toCurrencyName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: 'to' currency is required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR: amount must be positive");
        }

        if (fromCurrencyName.equalsIgnoreCase(toCurrencyName)) {
            return amount;
        }

        String url = API_URL + "?from=" + fromCurrencyName.toUpperCase() + "&to=" + toCurrencyName.toUpperCase();

        try {
            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("rates")) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "EXTERNAL_API_ERROR: No rates returned");
            }

            Map<String, Double> rates = (Map<String, Double>) response.get("rates");
            Double rateValue = rates.get(toCurrencyName.toUpperCase());

            if (rateValue == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "UNSUPPORTED_CURRENCY: No rate found for " + toCurrencyName.toUpperCase());
            }

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