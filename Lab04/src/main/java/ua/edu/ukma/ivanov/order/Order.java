package ua.edu.ukma.ivanov.order;

import java.util.ArrayList;
import java.util.List;

public class Order {

    public enum Status {
        PENDING, CONFIRMED, PAID, SHIPPED, CANCELLED
    }

    private final String id;
    private final String customerId;
    private final List<OrderItem> items;
    private Status status;
    private double totalAmount;

    public Order(String id, String customerId) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.status = Status.PENDING;
        this.totalAmount = 0.0;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        totalAmount += item.getPrice() * item.getQuantity();
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return items; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public double getTotalAmount() { return totalAmount; }
}
