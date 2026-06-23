package ua.edu.ukma.ivanov.order;

public interface NotificationService {

    void sendOrderConfirmation(Order order);

    void sendPaymentFailureNotification(Order order);

    void sendOutOfStockNotification(Order order);

    void sendCancellationNotification(Order order);
}
