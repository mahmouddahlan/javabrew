package com.eecs4413.javabrew.iam.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    // Store hashed password (never store raw passwords)
    @Column(nullable = false)
    private String passwordHash;

    private String firstName;
    private String lastName;

    @Embedded
    private Address shippingAddress;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Address getShippingAddress() { return shippingAddress; }

    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setShippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; }
}