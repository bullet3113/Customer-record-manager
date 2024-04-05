package com.sunbase.CRMApplication.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Customer {

    private String first_name;
    private String last_name;
    private String street;
    private String address;
    private String city;
    private String state;
    @Id
    private String email;
    private String phone;
}
