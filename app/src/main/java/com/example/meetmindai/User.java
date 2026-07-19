package com.example.meetmindai;

public class User {
    public String name;
    public String email;
    public String birthDate;
    public String address;
    public String phone;

    public User() {}

    public User(String name, String email, String birthDate, String address, String phone) {
        this.name = name;
        this.email = email;
        this.birthDate = birthDate;
        this.address = address;
        this.phone = phone;
    }
}
