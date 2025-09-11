package com.reliablecarriers.Reliable.Carriers.config;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if it doesn't exist
        if (!userRepository.findByEmail("admin@reliablecarriers.com").isPresent()) {
            User adminUser = new User();
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setEmail("admin@reliablecarriers.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setPhone("+1234567890");
            adminUser.setAddress("123 Admin Street");
            adminUser.setCity("Admin City");
            adminUser.setState("AS");
            adminUser.setZipCode("12345");
            adminUser.setCountry("USA");
            adminUser.setRole(UserRole.ADMIN);
            userRepository.save(adminUser);
            System.out.println("Admin user created successfully!");
        }

        // Create a test customer
        if (!userRepository.findByEmail("customer@test.com").isPresent()) {
            User customerUser = new User();
            customerUser.setFirstName("John");
            customerUser.setLastName("Doe");
            customerUser.setEmail("customer@test.com");
            customerUser.setPassword(passwordEncoder.encode("customer123"));
            customerUser.setPhone("+1987654321");
            customerUser.setAddress("456 Customer Ave");
            customerUser.setCity("Customer City");
            customerUser.setState("CS");
            customerUser.setZipCode("54321");
            customerUser.setCountry("USA");
            customerUser.setRole(UserRole.CUSTOMER);
            userRepository.save(customerUser);
            System.out.println("Test customer created successfully!");
        }

        // Create a test driver
        if (!userRepository.findByEmail("driver@test.com").isPresent()) {
            User driverUser = new User();
            driverUser.setFirstName("Mike");
            driverUser.setLastName("Driver");
            driverUser.setEmail("driver@test.com");
            driverUser.setPassword(passwordEncoder.encode("driver123"));
            driverUser.setPhone("+1555123456");
            driverUser.setAddress("789 Driver Road");
            driverUser.setCity("Driver City");
            driverUser.setState("DR");
            driverUser.setZipCode("67890");
            driverUser.setCountry("USA");
            driverUser.setRole(UserRole.DRIVER);
            userRepository.save(driverUser);
            System.out.println("Test driver created successfully!");
        }
    }
}
