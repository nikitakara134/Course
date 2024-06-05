package com.example.Shopbot.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
public class Cart {

    @Autowired
    private ProductsRepository productsRepository;

    private long chatId;
    private List<Products> dishes;

    public Cart(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
        this.dishes = new ArrayList<>();
    }

    public void addDishToCart(String dishName) {
        if (findDishInMenu(dishName) != null)
            dishes.add(findDishInMenu(dishName));
    }

    public Products findDishInMenu(String dishName) {
        var dishes = productsRepository.findAll();

        for (Products dish : dishes) {
            if (dishName.equals(dish.getName()))
                return dish;
        }
        return null;
    }

    public float getTotalPrice() {
        float totalPrice = 0;

        if (!dishes.isEmpty()) {
            for (Products dish : dishes) {
                totalPrice += dish.getPrice();
            }
        }

        return totalPrice;
    }

    public String getCartInfo() {
        StringBuilder message = new StringBuilder("Кошик порожній");

        if (!dishes.isEmpty()) {
            message = new StringBuilder("<b>Кошик:</b>\n\n" + getOrderList() + "\n<b> До сплати: " + getTotalPrice() + " $</b>");
        }

        return message.toString();
    }

    public int getDishQuantity(Products dish) {
        int dishQuantity = 0;

        for (Products value : dishes) {
            if (value.getId() == dish.getId())
                dishQuantity++;
        }

        return dishQuantity;
    }

    public void removeAllDishWithName(String dishName) {
        if (findDishInMenu(dishName) != null && !dishes.isEmpty()) {
            dishes.removeIf(dish -> dish.getName().equals(dishName));
        }
    }

    public List<String> getDishNamesToChange(String change) {
        List<String> dishNames = new ArrayList<>();

        switch (change) {
            case "Видалення з кошику" -> change = "Видалити ";
            case "Змінити кількість продуктів" -> change = "Змінити ";
            case "Decrease" -> change = "Decrease ";
            case "Increase" -> change = "Increase ";
        }

        for (Products dish : getDishesWithoutRepeat()) {
            dishNames.add(change + dish.getName());
        }
        return dishNames;
    }

    public List<Products> getDishesWithoutRepeat() {
        List<Products> uniqueDishes = new ArrayList<>();
        for (Products dish : dishes) {
            if (!uniqueDishes.contains(dish)) {
                uniqueDishes.add(dish);
            }
        }
        return uniqueDishes;
    }

    public Products getDishFromCart(String dishName) {
        List<Products> dishes = getDishesWithoutRepeat();

        for (Products dish : dishes) {
            if (dish.getName().equals(dishName))
                return dish;
        }

        return null;
    }

    public String getDishInfo(String dishName) {
        Products dish = getDishFromCart(dishName);
        String message = "Помилка";

        if (dish == null) {
            return message;
        }

        if (dish.getCategory().equals("Напої")) {
            message = "<b>" + dish.getName() + " - " + dish.getPrice() + " $ - " + getDishQuantity(dish) + " шт.</b>\n\n<i>" +
                    dish.getWeight() + " л, " + dish.getAdditional() + "\n" + dish.getDescription() + "</i>";
        } else {
            message = "<b>" + dish.getName() + " - " + dish.getPrice() + " $ - " + getDishQuantity(dish) + " шт.</b>\n\n<i>" +
                    dish.getWeight() + " грам, " + dish.getAdditional() + "\n" + dish.getDescription() + "</i>";
        }

        return message;
    }

    public String getDishPhoto(String dishName) {
        Products dish = getDishFromCart(dishName);

        if (dish == null)
            return null;

        return dish.getImage();
    }

    public void removeDish(String dishName) {
        if (findDishInMenu(dishName) != null && !dishes.isEmpty()) {
            for (int i = dishes.size() - 1; i >= 0; i--) {
                if (dishes.get(i).getName().equals(dishName)) {
                    dishes.remove(i);
                    return;
                }
            }
        }
    }

    public void removeAllFromCart() {
        dishes.clear();
    }

    public boolean isEmpty() {
        return dishes.isEmpty();
    }

    public String getOrderList() {
        StringBuilder message = new StringBuilder();

        if (!dishes.isEmpty()) {

            List<Products> dishesWithoutRepeat = new ArrayList<>(getDishesWithoutRepeat());

            for (Products dish : dishesWithoutRepeat) {
                message.append(dishesWithoutRepeat.indexOf(dish) + 1).append(". ").append(dish.getName()).append(" - ").append(dish.getPrice()).append(" $ - ").append(getDishQuantity(dish)).append(" шт.\n");
            }
        }

        return message.toString();
    }
}
