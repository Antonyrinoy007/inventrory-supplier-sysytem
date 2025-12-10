package com.example.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(inventoryService, "supplierServiceUrl", "http://localhost:8082/api/suppliers");
    }

    @Test
    void createProduct_shouldReturnSavedProduct() {
        Product product = new Product("Test Product", "Description", 10.0, 100, 1L);
        when(productRepository.save(product)).thenReturn(product);

        Product createdProduct = inventoryService.createProduct(product);

        assertNotNull(createdProduct);
        assertEquals("Test Product", createdProduct.getName());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void getProductById_shouldReturnProduct_whenFound() {
        Product product = new Product("Test Product", "Description", 10.0, 100, 1L);
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> foundProduct = inventoryService.getProductById(1L);

        assertTrue(foundProduct.isPresent());
        assertEquals(1L, foundProduct.get().getId());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_shouldReturnEmpty_whenNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Product> foundProduct = inventoryService.getProductById(1L);

        assertFalse(foundProduct.isPresent());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct_whenFound() {
        Product existingProduct = new Product("Old Name", "Old Desc", 5.0, 50, 1L);
        existingProduct.setId(1L);
        Product updatedDetails = new Product("New Name", "New Desc", 15.0, 150, 2L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedDetails);

        Product result = inventoryService.updateProduct(1L, updatedDetails);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals(150, result.getQuantityInStock());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldThrowException_whenNotFound() {
        Product updatedDetails = new Product("New Name", "New Desc", 15.0, 150, 2L);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.updateProduct(1L, updatedDetails);
        });

        assertEquals("Product not found with id 1", exception.getMessage());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void decreaseStock_shouldReduceQuantity() {
        Product product = new Product("Test Product", "Description", 10.0, 100, 1L);
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product updatedProduct = inventoryService.decreaseStock(1L, 10);

        assertEquals(90, updatedProduct.getQuantityInStock());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void decreaseStock_shouldThrowException_whenInsufficientStock() {
        Product product = new Product("Test Product", "Description", 10.0, 5, 1L);
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.decreaseStock(1L, 10);
        });
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void increaseStock_shouldIncreaseQuantity() {
        Product product = new Product("Test Product", "Description", 10.0, 100, 1L);
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product updatedProduct = inventoryService.increaseStock(1L, 10);

        assertEquals(110, updatedProduct.getQuantityInStock());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void deleteProduct_shouldCallRepositoryDelete() {
        doNothing().when(productRepository).deleteById(1L);

        inventoryService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void getSupplierDetails_shouldReturnSupplierDTO_whenFound() {
        Long supplierId = 1L;
        SupplierDTO supplierDTO = new SupplierDTO();
        supplierDTO.setId(supplierId);
        supplierDTO.setName("Mock Supplier");

        when(restTemplate.getForObject(anyString(), eq(SupplierDTO.class))).thenReturn(supplierDTO);

        Optional<SupplierDTO> result = inventoryService.getSupplierDetails(supplierId);

        assertTrue(result.isPresent());
        assertEquals(supplierId, result.get().getId());
        assertEquals("Mock Supplier", result.get().getName());
        verify(restTemplate, times(1)).getForObject("http://localhost:8082/api/suppliers/1", SupplierDTO.class);
    }

    @Test
    void getSupplierDetails_shouldReturnEmpty_whenNotFound() {
        Long supplierId = 1L;
        when(restTemplate.getForObject(anyString(), eq(SupplierDTO.class))).thenReturn(null);

        Optional<SupplierDTO> result = inventoryService.getSupplierDetails(supplierId);

        assertFalse(result.isPresent());
        verify(restTemplate, times(1)).getForObject("http://localhost:8082/api/suppliers/1", SupplierDTO.class);
    }
}
