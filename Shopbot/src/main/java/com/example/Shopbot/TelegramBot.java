package com.example.Shopbot;

import com.example.Shopbot.config.BotConfig;
import com.example.Shopbot.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Component
public class TelegramBot extends TelegramLongPollingBot {


    public static final String START = "Старт";
    public static final String MAIN_MENU = "Головне меню";
    public static final String CATEGORY_OF_DISHES = "Категорії продуктів";
    public static final String CART = "Кошик";
    public static final String ALL_DISHES = "Все меню";
    public static final String MAKE_ORDER = "Оформити замовлення";
    public static final String CART_CHANGES = "Зміни у кошику";
    public static final String REMOVE_FROM_CART = "Видалення з кошику";
    public static final String CHANGE_NUMBER_OF_DISHES = "Змінити кількість продуктів";
    public static final String ADDED_TO_CART = "Додано в кошик";
    public static final String CART_REVIEW = "Перегляд кошику";
    public static final String ADD_TO_CART = "Додавання до кошику";
    public static final String DELIVERY_ADDRESS = "Вказати адресу доставки";
    public static final String PHONE_NUMBER = "Вказати номер телефону";
    public static final String PAYMENT_METHOD = "Вказати спосіб оплати";
    public static final String ADDITIONAL_INFO = "Вказати додаткову інформацію";
    public static final String CONFIRM_ORDER = "Підтвердити замовлення";
    public static final String PREVIOUS_ORDERS_REVIEW = "Перегляд минулих замовлень";


    @Autowired
    private final ProductsRepository productsRepository;
    @Autowired
    private final OrdersRepository ordersRepository;
    Cart cart;
    Order order;
    private final BotConfig botConfig;
    private String state;


    public TelegramBot(ProductsRepository productsRepository, OrdersRepository ordersRepository, Cart cart, BotConfig botConfig) {
        this.productsRepository = productsRepository;
        this.ordersRepository = ordersRepository;
        this.cart = cart;
        this.order = new Order();
        this.botConfig = botConfig;
        this.state = START;
    }


    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }


    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            messageHandler(update);
        } else if (update.hasCallbackQuery()) {
            callbackQueryHandler(update);
        }
    }


    private void messageHandler(Update update) {
        String textFromUser = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        String userFirstName = update.getMessage().getFrom().getFirstName();


        if (textFromUser.equals("/start") || textFromUser.equals("Старт")) {
            cart.setChatId(chatId);
            state = MAIN_MENU;
            String message = "Вітаю, " + userFirstName + "!\uD83D\uDC4B" + "\n" +
                    "Тут Ви можете замовити доставку продуктів здорового харчування!" + "\n" +
                    "Обирайте дію з меню нижче⬇";
            sendMessage(chatId, message);
        } else if (textFromUser.equals("Меню") || textFromUser.equals("Повернутися до вибору категорій продуктів")) {
            state = CATEGORY_OF_DISHES;
            sendMessage(chatId, "Оберіть категорію продуктів для перегляду: ");
        } else if (textFromUser.equals("Кошик") || textFromUser.equals("Переглянути кошик")) {
            state = CART;
            sendMessage(chatId, cart.getCartInfo());
        } else if (getDishCategories().contains(textFromUser)) {
            state = textFromUser;
            sendMessage(chatId, "Для отримання інформації про продукт оберіть його з меню");
        } else if (getDishNames(getCategoryOfDish(textFromUser)).contains(textFromUser)) {
            sendDishInfo(chatId, textFromUser);
        } else if (textFromUser.equals("Повернутися в головне меню")) {
            state = MAIN_MENU;
            sendMessage(chatId, "Повернулися в головне меню. \nЩо бажаєте зробити?");
        } else if (textFromUser.equals("Все меню")) {
            state = ALL_DISHES;
            sendMessage(chatId, "Для отримання інформації про продукт оберіть його з меню");
        } else if (textFromUser.equals("Оформити замовлення")) {
            if (cart.isEmpty()) {
                sendMessage(chatId, "Кошик порожній, тому оформити замовлення неможливо!");
            } else {
                state = MAKE_ORDER;
                sendMessage(chatId, "Введіть ім'я одержувача замовлення або виберіть серед запропонованих варіантів:");
            }
        } else if (state.equals(MAKE_ORDER)) {
            order.setChatId(chatId);
            order.setUserName(textFromUser);
            order.setOrderList(cart.getOrderList());
            order.setOrderPrice(cart.getTotalPrice());
            state = DELIVERY_ADDRESS;
            sendMessage(chatId, "Вкажіть адресу доставки або виберіть серед запропонованих варіантів:");
        } else if (state.equals(DELIVERY_ADDRESS)) {
            order.setDeliveryAddress(textFromUser);
            state = PHONE_NUMBER;
            sendMessage(chatId, "Вкажіть номер телефону для зв'язку або виберіть серед запропонованих варіантів:");
        } else if (state.equals(PHONE_NUMBER)) {
            order.setPhoneNumber(Long.parseLong(textFromUser));
            state = PAYMENT_METHOD;
            sendMessage(chatId, "Оберіть спосіб оплати замовлення:");
        } else if (state.equals(ADDITIONAL_INFO)) {
            order.setAdditionalInfo(textFromUser);
            state = CONFIRM_ORDER;
            sendMessage(chatId, getOrderInfo(order));
        } else if (textFromUser.equals("Внести зміни до кошику")) {
            if (cart.isEmpty()) {
                sendMessage(chatId, "Кошик порожній, тому внесення змін неможливе!");
            } else {
                state = CART_CHANGES;
                sendMessage(chatId, "Що бажаєте зробити?");
            }
        } else if (textFromUser.equals("Видалити продукт з кошику")) {
            state = REMOVE_FROM_CART;
            sendMessage(chatId, "Оберіть продукт, який бажаєте видалити:");
        } else if (cart.getDishNamesToChange("Видалення з кошику").contains(textFromUser)) {
            state = CART;
            textFromUser = textFromUser.replaceFirst("Видалити ", "");
            cart.removeAllDishWithName(textFromUser);
            sendMessage(chatId, "Продукт " + textFromUser + " видалено з кошику.");
        } else if (textFromUser.equals("Змінити кількість продуктів")) {
            state = CHANGE_NUMBER_OF_DISHES;
            sendMessage(chatId, "Оберіть продукт, кількість якого бажаєте змінити:");
        } else if (cart.getDishNamesToChange("Змінити кількість продукта").contains(textFromUser)) {
            state = CART_CHANGES;
            textFromUser = textFromUser.replaceFirst("Змінити ", "");
            sendDishPhoto(chatId, cart.getDishPhoto(textFromUser), cart.getDishInfo(textFromUser), textFromUser);
        } else if (textFromUser.equals("Очистити кошик")) {
            state = MAIN_MENU;
            cart.removeAllFromCart();
            sendMessage(chatId, "Всі продукти з кошику було видалено.");
        } else if (textFromUser.equals("Повернутися до перегляду кошика")) {
            state = CART;
            sendMessage(chatId, "Повернулися до перегляду кошика.");
        } else if (textFromUser.equals("Перегляд минулих замовлень")) {
            state = PREVIOUS_ORDERS_REVIEW;
            sendMessage(chatId, "Оберіть замовлення, яке бажаєте переглянути:");
        } else if (state.equals(PREVIOUS_ORDERS_REVIEW) && getElementFromOrdersByChatId(chatId).contains(textFromUser)) {
            sendMessage(chatId, getOrderInfo(Objects.requireNonNull(getOrderById(Long.parseLong(textFromUser)))));
        } else {
            state = MAIN_MENU;
            sendMessage(chatId, "Введена команда не підтримується.");
        }
    }


    private void callbackQueryHandler(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();


        if (getDishNames(getCategoryOfDish(callbackData)).contains(callbackData) && state.equals(ADD_TO_CART)) {
            cart.addDishToCart(callbackData);
            state = ADDED_TO_CART;
            sendMessage(chatId, "Продукт " + callbackData + " був доданий до кошику!");
        } else if (callbackData.equals("Кошик") && (state.equals(ADDED_TO_CART) || state.equals(CART_REVIEW))) {
            state = CART;
            sendMessage(chatId, cart.getCartInfo());
        } else if (cart.getDishNamesToChange("Decrease").contains(callbackData) && state.equals(CART_CHANGES)) {
            callbackData = callbackData.replaceFirst("Decrease ", "");
            cart.removeDish(callbackData);
            state = CART_REVIEW;
            sendMessage(chatId, "Кількість була зменшена!");
        } else if (cart.getDishNamesToChange("Increase").contains(callbackData) && state.equals(CART_CHANGES)) {
            callbackData = callbackData.replaceFirst("Increase ", "");
            cart.addDishToCart(callbackData);
            state = ADDED_TO_CART;
            sendMessage(chatId, "Кількість була збільшена!");
        } else if ((callbackData.equals("Карта") || callbackData.equals("Готівка")) && state.equals(PAYMENT_METHOD)) {
            order.setPaymentMethod(callbackData);
            state = ADDITIONAL_INFO;
            sendMessage(chatId, "Вкажіть додаткову інформацію до замовлення:");
        } else if (callbackData.equals("Пропустити") && state.equals(ADDITIONAL_INFO)) {
            state = CONFIRM_ORDER;
            sendMessage(chatId, getOrderInfo(order));
        } else if (callbackData.equals("Оформити замовлення") && state.equals(CONFIRM_ORDER)) {
            order.setStatus("В обробці");
            ordersRepository.save(order);
            String message = "Замовлення №" + order.getId() + " оформлено успішно!";
            cart.removeAllFromCart();
            order = new Order();
            state = MAIN_MENU;
            sendMessage(chatId, message);
        }
    }


    private void sendMessage(Long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setParseMode(ParseMode.HTML);


        keyboardBuilder(message);


        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println("Error while sending message!");
        }
    }


    private void sendDishPhoto(Long chatId,String imageToSend, String textToSend, String dishName) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(String.valueOf(chatId));
        photo.setPhoto(new InputFile(imageToSend));
        photo.setParseMode(ParseMode.HTML);
        photo.setCaption(textToSend);


        if (state.equals(ADD_TO_CART)) {
            photo.setReplyMarkup(inlineKeyboardBuilder("Додати до кошику", dishName));
        } else if (state.equals(CART_CHANGES)) {
            photo.setReplyMarkup(inlineKeyboardBuilder("-", "Decrease " + dishName, "+", "Increase " + dishName));
        }


        try {
            execute(photo);
        } catch (TelegramApiException e) {
            System.out.println("Error while sending photo!");
        }
    }


    private InlineKeyboardMarkup inlineKeyboardBuilder(String ButtonText, String callbackData) {
        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();
        List<InlineKeyboardButton> inlineRow = new ArrayList<>();


        InlineKeyboardButton inlineButton = new InlineKeyboardButton();


        inlineButton.setText(ButtonText);
        inlineButton.setCallbackData(callbackData);
        inlineRow.add(inlineButton);


        inlineRows.add(inlineRow);
        inlineMarkup.setKeyboard(inlineRows);


        return inlineMarkup;
    }


    private InlineKeyboardMarkup inlineKeyboardBuilder(String firstButtonText, String firstButtonCallbackData, String secondButtonText, String secondButtonCallbackData) {
        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();
        List<InlineKeyboardButton> inlineRow = new ArrayList<>();


        InlineKeyboardButton firstButton = new InlineKeyboardButton();
        firstButton.setText(firstButtonText);
        firstButton.setCallbackData(firstButtonCallbackData);
        inlineRow.add(firstButton);


        InlineKeyboardButton secondButton = new InlineKeyboardButton();
        secondButton.setText(secondButtonText);
        secondButton.setCallbackData(secondButtonCallbackData);
        inlineRow.add(secondButton);


        inlineRows.add(inlineRow);
        inlineMarkup.setKeyboard(inlineRows);


        return inlineMarkup;
    }


    private void keyboardBuilder(SendMessage message) {
        List<String> buttons = new ArrayList<>();


        if (state.equals(START)) {
            buttons.add("Старт");
            message.setReplyMarkup(ReplyKeyboardBuilder(buttons));
        } else if (state.equals(MAIN_MENU)) {
            buttons.add("Меню");
            buttons.add("Кошик");
            buttons.add("Перегляд минулих замовлень");
            message.setReplyMarkup(ReplyKeyboardBuilder(buttons));
        } else if (state.equals(CATEGORY_OF_DISHES)) {
            buttons.add("Фрукти");
            buttons.add("Овочі");
            buttons.add("Крупи");
            buttons.add("Випічка");
            buttons.add("Батончики");
            buttons.add("Салати");
            buttons.add("Напої");
            buttons.add("Десерти без цукру");
            buttons.add("Все меню");
            buttons.add("Повернутися в головне меню");
            message.setReplyMarkup(ReplyKeyboardBuilder(buttons));
        } else if (getDishCategories().contains(state) || state.equals(ALL_DISHES)) {
            buttons = getDishNames(state);
            buttons.add("Повернутися до вибору категорій продуктів");
            message.setReplyMarkup(ReplyKeyboardBuilder(buttons));
        } else if (state.equals(CART)) {
            buttons.add("Оформити замовлення");
            buttons.add("Внести зміни до кошику");
            buttons.add("Переглянути кошик");
            buttons.add("Повернутися в головне меню");
            message.setReplyMarkup(ReplyKeyboardBuilder(buttons));
        } else if (state.equals(CART_CHANGES)) {
            buttons.add("Змінити кількість продуктів");
            buttons.add("Видалити продукт з кошику");
            buttons.add("Очистити кошик");
            buttons.add("Повернутися до перегляду кошика");
            message.setReplyMarkup(ReplyKeyboardBuilder(buttons));
        } else if (state.equals(REMOVE_FROM_CART) || state.equals(CHANGE_NUMBER_OF_DISHES)) {
            buttons = cart.getDishNamesToChange(state);
            buttons.add("Повернутися до перегляду кошика");
            message.setReplyMarkup(ReplyKeyboardBuilder(buttons));
        } else if (state.equals(PREVIOUS_ORDERS_REVIEW) || state.equals(MAKE_ORDER) || state.equals(PHONE_NUMBER) || state.equals(DELIVERY_ADDRESS)) {
            buttons = getElementFromOrdersByChatId(Long.parseLong(message.getChatId()));
            buttons.add("Повернутися в головне меню");
            message.setReplyMarkup(ReplyKeyboardBuilder(buttons));
        } else if (state.equals(ADDED_TO_CART) || state.equals(CART_REVIEW)) {
            message.setReplyMarkup(inlineKeyboardBuilder("Переглянути кошик", "Кошик"));
        } else if (state.equals(PAYMENT_METHOD)) {
            message.setReplyMarkup(inlineKeyboardBuilder("Карта", "Карта", "Готівка", "Готівка"));
        } else if (state.equals(ADDITIONAL_INFO)) {
            message.setReplyMarkup(inlineKeyboardBuilder("Пропустити", "Пропустити"));
        } else if (state.equals(CONFIRM_ORDER)) {
            message.setReplyMarkup(inlineKeyboardBuilder("Оформити замовлення", "Оформити замовлення"));
        }
    }


    private List<String> getDishNames(String dishCategory) {
        var dishes = productsRepository.findAll();


        List<String> dishNames = new ArrayList<>();


        for (Products dish : dishes) {
            if (dishCategory.equals(dish.getCategory()) || dishCategory.equals(ALL_DISHES))
                dishNames.add((dish.getName()).trim());
        }
        return dishNames;
    }


    private List<String> getDishCategories() {
        var dishes = productsRepository.findAll();


        List<String> dishCategories = new ArrayList<>();


        for (Products dish : dishes) {
            if (!dishCategories.contains(dish.getCategory()))
                dishCategories.add(dish.getCategory());
        }
        return dishCategories;
    }


    private String getCategoryOfDish(String dishName) {
        Products dish = getDishByName(dishName);


        if (dish != null)
            return dish.getCategory();


        return "none";
    }


    private void sendDishInfo(Long chatId, String dishName) {
        Products dish = getDishByName(dishName);
        String message = "Помилка";


        if (dish == null) {
            sendMessage(chatId, message);
            return;
        }


        if (dish.getCategory().equals("Напої")) {
            message = "<b>" + dish.getName() + " - " + dish.getPrice() + " $</b>\n\n<i>" + dish.getWeight() + " л, " +
                    dish.getAdditional() + "\n" + dish.getDescription() + "</i>";
        } else {
            message = "<b>" + dish.getName() + " - " + dish.getPrice() + " $</b>\n\n<i>" + dish.getWeight() + " грам, " +
                    dish.getAdditional() + "\n" + dish.getDescription() + "</i>";
        }


        if (!state.equals(CART_CHANGES))
            state = ADD_TO_CART;


        sendDishPhoto(chatId, dish.getImage(), message, dishName);
    }


    private Products getDishByName(String dishName) {
        var dishes = productsRepository.findAll();


        for (Products dish : dishes) {
            if (dishName.equals((dish.getName().trim())))
                return dish;
        }


        return null;
    }


    private List<Order> getOrdersByChatId(long chatId) {
        var orders = ordersRepository.findAll();


        List<Order> userOrders = new ArrayList<>();


        for (Order order : orders) {
            if (order.getChatId() == chatId)
                userOrders.add(order);
        }


        return userOrders;
    }


    private Order getOrderById(long orderId) {
        var orders = ordersRepository.findAll();


        for (Order order : orders) {
            if (order.getId() == orderId)
                return order;
        }


        return null;
    }


    private List<String> getElementFromOrdersByChatId(long chatId) {
        List<Order> orders = getOrdersByChatId(chatId);


        List<String> result = new ArrayList<>();


        switch (state) {
            case PREVIOUS_ORDERS_REVIEW:
                for (Order order : orders) {
                    result.add("" + order.getId());
                }
                break;
            case MAKE_ORDER:
                for (Order order : orders) {
                    if (!result.contains(order.getUserName()))
                        result.add(order.getUserName());
                }
                break;
            case PHONE_NUMBER:
                for (Order order : orders) {
                    if (!result.contains("" + order.getPhoneNumber()))
                        result.add("" + order.getPhoneNumber());
                }
                break;
            case DELIVERY_ADDRESS:
                for (Order order : orders) {
                    if (!result.contains(order.getDeliveryAddress()))
                        result.add(order.getDeliveryAddress());
                }
                break;
        }


        return result;
    }


    private String getOrderInfo(Order order) {
        String orderInfo;


        if (order.getId() != 0) {
            orderInfo = "<b>Замовлення №" + order.getId() + "</b>\n\nСтатус: " + order.getStatus();
        } else {
            orderInfo = "<b>Замовлення:</b>\n";
        }


        orderInfo = orderInfo + "\nОтримувач: " + order.getUserName() +
                "\nНомер телефону: " + order.getPhoneNumber() + "\nАдреса доставки: " + order.getDeliveryAddress() +
                "\nСпосіб оплати: " + order.getPaymentMethod();


        if (order.getAdditionalInfo() != null) {
            orderInfo = orderInfo + "\nДодаткова інформація: " + order.getAdditionalInfo();
        }
        orderInfo = orderInfo + "\n\n Продукти: \n" + order.getOrderList() + "\n<b>До сплати: " + order.getOrderPrice() + " $ </b>";


        return orderInfo;
    }


    private ReplyKeyboardMarkup ReplyKeyboardBuilder(List<String> list) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();


        for (int i = 0; i < list.size(); i++) {
            row.add(list.get(i));
            if ((1 + i) % 3 == 0 || i == list.size() - 1 || (i == list.size() - 2 && !state.equals(MAIN_MENU))) {
                keyboardRows.add(row);
                row = new KeyboardRow();
            }
        }


        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }


}
