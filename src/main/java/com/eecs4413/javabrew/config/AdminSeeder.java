package com.eecs4413.javabrew.config;

import com.eecs4413.javabrew.iam.model.Address;
import com.eecs4413.javabrew.iam.model.Role;
import com.eecs4413.javabrew.iam.model.User;
import com.eecs4413.javabrew.iam.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository users) {
        return args -> {
            if (users.findByUsername("admin").isEmpty()) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(encoder.encode("admin123"));
                admin.setFirstName("System");
                admin.setLastName("Admin");
                admin.setRole(Role.ADMIN);

                Address a = new Address();
                a.streetName = "Admin";
                a.streetNumber = "1";
                a.city = "Toronto";
                a.country = "Canada";
                a.postalCode = "A1A1A1";
                admin.setShippingAddress(a);

                users.save(admin);
            }
        };
    }
}