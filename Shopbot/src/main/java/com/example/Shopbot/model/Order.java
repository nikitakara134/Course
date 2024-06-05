package com.example.Shopbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private long chatId;

    private String userName;

    private String status;

    private String orderList;

    private float orderPrice;

    private long phoneNumber;

    private String deliveryAddress;

    private String paymentMethod;

    private String additionalInfo;

}
