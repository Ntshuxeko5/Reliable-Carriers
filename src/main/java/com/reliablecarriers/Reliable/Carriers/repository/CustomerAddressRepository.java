package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.CustomerAddress;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    
    List<CustomerAddress> findByCustomerAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(User customer);
    
    Optional<CustomerAddress> findByCustomerAndIdAndIsActiveTrue(User customer, Long id);
    
    Optional<CustomerAddress> findByCustomerAndIsDefaultTrueAndIsActiveTrue(User customer);
    
    List<CustomerAddress> findByCustomer(User customer);
}

