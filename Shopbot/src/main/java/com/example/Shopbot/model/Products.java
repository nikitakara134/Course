package com.example.Shopbot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "product")
public class Products {

    @Id
    private int id;

    private String category;

    private String name;

    private String description;

    private String additional;

    private float weight;

    private float price;

    private String image;


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Products dish = (Products) o;

        return id == dish.getId() && name.equals(dish.getName());
    }
}
