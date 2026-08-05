package com.devsquad.payment_processing.api;

import com.devsquad.payment_processing.model.Currency;
import com.devsquad.payment_processing.model.PaymentMode;
import com.devsquad.payment_processing.model.Tag;
import com.devsquad.payment_processing.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CatalogController {

    @Autowired
    private CatalogService catalogService;

    @GetMapping("/currencies")
    public List<Currency> getAllCurrencies() {
        return catalogService.getAllCurrencies();
    }

    @GetMapping("/convert")
    public BigDecimal convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {
        return catalogService.convertCurrency(from, to, amount);
    }

    @GetMapping("/payment-modes")
    public List<PaymentMode> getPaymentModes() {
        return catalogService.getPaymentModes();
    }

    @GetMapping("/tags/all")
    public List<Tag> getAllTags() {
        return catalogService.getAllTags();
    }

    @PostMapping("/tags/add")
    public Tag createTag(@Valid @RequestBody Tag tag) {
        return catalogService.createTag(tag);
    }

    @DeleteMapping("/tags/{tagId}")
    public void deleteTag(@PathVariable Integer tagId) {
        catalogService.deleteTag(tagId);
    }
}