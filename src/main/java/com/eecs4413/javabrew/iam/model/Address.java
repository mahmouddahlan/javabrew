package com.eecs4413.javabrew.iam.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {
    public String streetName;
    public String streetNumber;
    public String city;
    public String country;
    public String postalCode;
}