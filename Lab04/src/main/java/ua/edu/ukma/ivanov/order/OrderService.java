package ua.edu.ukma.ivanov.order;

import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final WarehouseService warehouse;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;

    private final List<Order> processedOrders = new ArrayList<>();

    public OrderService(WarehouseService warehouse,
                        PaymentGateway paymentGateway,
                        NotificationService notificationService) {
        this.warehouse = warehouse;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
    }

    public OrderResult processOrder(Order order) {
        if (order == null) {
            return OrderResult.failure("Order must not be null");
        }

        if (order.getItems().isEmpty()) {
            return OrderResult.failure("Order has no items");
        }

        boolean inStock = warehouse.checkStock(order);
        if (!inStock) {
            notificationService.sendOutOfStockNotification(order);
            order.setStatus(Order.Status.CANCELLED);
            return OrderResult.failure("Items out of stock");
        }

        boolean charged = paymentGateway.charge(order.getCustomerId(), order.getTotalAmount());
        if (!charged) {
            notificationService.sendPaymentFailureNotification(order);
            order.setStatus(Order.Status.CANCELLED);
            return OrderResult.failure("Payment failed");
        }

        warehouse.reserveStock(order);
        order.setStatus(Order.Status.CONFIRMED);
        processedOrders.add(order);

        notificationService.sendOrderConfirmation(order);

        return OrderResult.success(order.getId());
    }

    public boolean cancelOrder(Order order) {
        if (order == null) {
            return false;
        }

        if (order.getStatus() != Order.Status.CONFIRMED) {
            return false;
        }

        paymentGateway.refund(order.getCustomerId(), order.getTotalAmount());
        warehouse.releaseStock(order);
        order.setStatus(Order.Status.CANCELLED);
        notificationService.sendCancellationNotification(order);
        processedOrders.remove(order);

        return true;
    }

    public int calculateDiscount(double amount) {
        if (amount >= 5000) {
            return 15;
        } else if (amount >= 1000) {
            return 10;
        } else if (amount >= 500) {
            return 5;
        } else {
            return 0;
        }
    }

    public List<Order> getProcessedOrders() {
        return new ArrayList<>(processedOrders);
    }
}
