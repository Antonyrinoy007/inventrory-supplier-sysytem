package com.example.supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Autowired
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Supplier createSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        return supplierRepository.findById(id).map(supplier -> {
            supplier.setName(supplierDetails.getName());
            supplier.setContactPerson(supplierDetails.getContactPerson());
            supplier.setPhone(supplierDetails.getPhone());
            supplier.setEmail(supplierDetails.getEmail());
            return supplierRepository.save(supplier);
        }).orElseThrow(() -> new RuntimeException("Supplier not found with id " + id));
    }

    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }
}
