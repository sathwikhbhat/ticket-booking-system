package com.sathwikhbhat.bookingservice.repository;

import com.sathwikhbhat.bookingservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}