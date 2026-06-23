package ua.edu.ukma.ivanov.order;

public interface PaymentGateway {

    boolean charge(String customerId, double amount);

    void refund(String customerId, double amount);
}
