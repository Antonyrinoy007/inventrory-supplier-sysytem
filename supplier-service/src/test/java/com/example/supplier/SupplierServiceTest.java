package com.example.supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createSupplier_shouldReturnSavedSupplier() {
        Supplier supplier = new Supplier("Test Supplier", "John Doe", "123-456-7890", "john.doe@test.com");
        when(supplierRepository.save(supplier)).thenReturn(supplier);

        Supplier createdSupplier = supplierService.createSupplier(supplier);

        assertNotNull(createdSupplier);
        assertEquals("Test Supplier", createdSupplier.getName());
        verify(supplierRepository, times(1)).save(supplier);
    }

    @Test
    void getSupplierById_shouldReturnSupplier_whenFound() {
        Supplier supplier = new Supplier("Test Supplier", "John Doe", "123-456-7890", "john.doe@test.com");
        supplier.setId(1L);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        Optional<Supplier> foundSupplier = supplierService.getSupplierById(1L);

        assertTrue(foundSupplier.isPresent());
        assertEquals(1L, foundSupplier.get().getId());
        verify(supplierRepository, times(1)).findById(1L);
    }

    @Test
    void getSupplierById_shouldReturnEmpty_whenNotFound() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Supplier> foundSupplier = supplierService.getSupplierById(1L);

        assertFalse(foundSupplier.isPresent());
        verify(supplierRepository, times(1)).findById(1L);
    }

    @Test
    void updateSupplier_shouldReturnUpdatedSupplier_whenFound() {
        Supplier existingSupplier = new Supplier("Old Name", "Old Contact", "111", "old@test.com");
        existingSupplier.setId(1L);
        Supplier updatedDetails = new Supplier("New Name", "New Contact", "222", "new@test.com");

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(updatedDetails);

        Supplier result = supplierService.updateSupplier(1L, updatedDetails);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("New Contact", result.getContactPerson());
        verify(supplierRepository, times(1)).findById(1L);
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void updateSupplier_shouldThrowException_whenNotFound() {
        Supplier updatedDetails = new Supplier("New Name", "New Contact", "222", "new@test.com");
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            supplierService.updateSupplier(1L, updatedDetails);
        });

        assertEquals("Supplier not found with id 1", exception.getMessage());
        verify(supplierRepository, times(1)).findById(1L);
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void deleteSupplier_shouldCallRepositoryDelete() {
        doNothing().when(supplierRepository).deleteById(1L);

        supplierService.deleteSupplier(1L);

        verify(supplierRepository, times(1)).deleteById(1L);
    }
}
