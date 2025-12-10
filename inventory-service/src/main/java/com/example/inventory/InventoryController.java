package com.example.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return inventoryService.createProduct(product);
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return inventoryService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return inventoryService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        try {
            Product updatedProduct = inventoryService.updateProduct(id, productDetails);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        inventoryService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/decreaseStock")
    public ResponseEntity<Product> decreaseStock(@PathVariable Long productId, @RequestBody Map<String, Integer> payload) {
        Integer amount = payload.get("amount");
        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Product updatedProduct = inventoryService.decreaseStock(productId, amount);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // Or more specific error response
        }
    }

    @PostMapping("/{productId}/increaseStock")
    public ResponseEntity<Product> increaseStock(@PathVariable Long productId, @RequestBody Map<String, Integer> payload) {
        Integer amount = payload.get("amount");
        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Product updatedProduct = inventoryService.increaseStock(productId, amount);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // Or more specific error response
        }
    }

    @GetMapping("/{productId}/supplier")
    public ResponseEntity<SupplierDTO> getProductSupplierDetails(@PathVariable Long productId) {
        return inventoryService.getProductById(productId)
                .flatMap(product -> inventoryService.getSupplierDetails(product.getSupplierId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
