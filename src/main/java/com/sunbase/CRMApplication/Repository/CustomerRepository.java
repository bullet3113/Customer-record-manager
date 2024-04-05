package com.sunbase.CRMApplication.Repository;

import com.sunbase.CRMApplication.Entity.Customer;
import jakarta.persistence.OrderBy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    
    public Page<Customer> findAll(Pageable pageable);
    @Query(value = "select * from Customer where first_name LIKE :fName", nativeQuery = true)
    public Page<Customer> findCustomerByFirstName(String fName, Pageable pageable);
    public Page<Customer> findByPhone(String phone, Pageable pageable);
    public Page<Customer> findByEmail(String email, Pageable pageable);
    public Page<Customer> findByCity(String city, Pageable pageable);
}
