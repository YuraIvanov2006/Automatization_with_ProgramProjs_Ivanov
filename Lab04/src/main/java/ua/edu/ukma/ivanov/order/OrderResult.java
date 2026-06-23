package ua.edu.ukma.ivanov.order;

public class OrderResult {

    private final boolean success;
    private final String message;

    private OrderResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static OrderResult success(String orderId) {
        return new OrderResult(true, "Order " + orderId + " successfully processed");
    }

    public static OrderResult failure(String reason) {
        return new OrderResult(false, reason);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return "OrderResult{success=" + success + ", message='" + message + "'}";
    }
}
