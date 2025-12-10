package com.example.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    @Value("${supplier.service.url}")
    private String supplierServiceUrl;

    @Autowired
    public InventoryService(ProductRepository productRepository, RestTemplate restTemplate) {
        this.productRepository = productRepository;
        this.restTemplate = restTemplate;
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id).map(product -> {
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setQuantityInStock(productDetails.getQuantityInStock());
            product.setSupplierId(productDetails.getSupplierId());
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public Product decreaseStock(Long productId, Integer amount) {
        return productRepository.findById(productId).map(product -> {
            product.decreaseStock(amount);
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found with id " + productId));
    }

    public Product increaseStock(Long productId, Integer amount) {
        return productRepository.findById(productId).map(product -> {
            product.increaseStock(amount);
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found with id " + productId));
    }

    public Optional<SupplierDTO> getSupplierDetails(Long supplierId) {
        try {
            String url = supplierServiceUrl + "/" + supplierId;
            SupplierDTO supplier = restTemplate.getForObject(url, SupplierDTO.class);
            return Optional.ofNullable(supplier);
        } catch (Exception e) {
            // Log the exception, return empty optional if supplier service is unavailable or supplier not found
            System.err.println("Error fetching supplier details: " + e.getMessage());
            return Optional.empty();
        }
    }
}
