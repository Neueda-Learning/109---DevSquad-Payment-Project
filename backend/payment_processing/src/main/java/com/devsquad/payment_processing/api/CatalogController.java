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
public class CatalogController {

    @Autowired
    private CatalogService catalogService;

    // Get all supported currencies
    @GetMapping("/currencies")
    public List<Currency> getAllCurrencies() {
        return catalogService.getAllCurrencies();
    }

    // Convert currency
//    @PostMapping("/currency/convert")
//    public BigDecimal convertCurrency(
//            @RequestParam String fromCurrency,
//            @RequestParam String toCurrency,
//            @RequestParam BigDecimal amount) {
//
//        return catalogService.convertCurrency(
//                fromCurrency,
//                toCurrency,
//                amount
//        );
//    }

    // Get all payment modes
    @GetMapping("/payment-modes")
    public List<PaymentMode> getPaymentModes() {
        return catalogService.getPaymentModes();
    }

    // Get all tags
    @GetMapping("/tags")
    public List<Tag> getAllTags() {
        return catalogService.getAllTags();
    }

    // Create a new tag
    @PostMapping("/tags")
    public Tag createTag(@Valid @RequestBody Tag tag) {
        return catalogService.createTag(tag);
    }

    // Delete a tag
    @DeleteMapping("/tags/{tagId}")
    public void deleteTag(@PathVariable Integer tagId) {

        catalogService.deleteTag(tagId);

    }

}