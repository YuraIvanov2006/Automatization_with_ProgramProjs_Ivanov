package ua.edu.ukma.ivanov.order;

public interface WarehouseService {

    boolean checkStock(Order order);

    void reserveStock(Order order);

    void releaseStock(Order order);
}
