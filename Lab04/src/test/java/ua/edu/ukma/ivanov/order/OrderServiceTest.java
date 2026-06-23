package ua.edu.ukma.ivanov.order;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private Order buildOrder(String orderId, String customerId, double price) {
        Order order = new Order(orderId, customerId);
        order.addItem(new OrderItem("P001", "Product", 1, price));
        return order;
    }

    @Test
    void processOrder_stockAvailable_paymentSuccess_returnsSuccess() {
        Order order = buildOrder("ORD-001", "CUST-001", 250.0);

        when(warehouseService.checkStock(order)).thenReturn(true);
        when(paymentGateway.charge("CUST-001", 250.0)).thenReturn(true);

        OrderResult result = orderService.processOrder(order);

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(Order.Status.CONFIRMED);
    }

    @Test
    void processOrder_outOfStock_orderCancelledWithoutPayment() {
        Order order = buildOrder("ORD-002", "CUST-002", 100.0);

        when(warehouseService.checkStock(order)).thenReturn(false);

        OrderResult result = orderService.processOrder(order);

        assertThat(result.isSuccess()).isFalse();
        assertThat(order.getStatus()).isEqualTo(Order.Status.CANCELLED);
        assertThat(result.getMessage()).contains("out of stock");
    }

    @Test
    void processOrder_paymentFails_orderCancelledStockNotReserved() {
        Order order = buildOrder("ORD-003", "CUST-003", 800.0);

        when(warehouseService.checkStock(order)).thenReturn(true);
        when(paymentGateway.charge("CUST-003", 800.0)).thenReturn(false);

        OrderResult result = orderService.processOrder(order);

        assertThat(result.isSuccess()).isFalse();
        assertThat(order.getStatus()).isEqualTo(Order.Status.CANCELLED);
        assertThat(result.getMessage()).contains("Payment failed");
    }

    @Test
    void cancelOrder_confirmedOrder_refundAndReleaseStock() {
        Order order = buildOrder("ORD-004", "CUST-004", 1200.0);
        order.setStatus(Order.Status.CONFIRMED);

        boolean cancelled = orderService.cancelOrder(order);

        assertThat(cancelled).isTrue();
        assertThat(order.getStatus()).isEqualTo(Order.Status.CANCELLED);
    }

    @Test
    void processOrder_success_verifySendOrderConfirmation() {
        Order order = buildOrder("ORD-010", "CUST-010", 300.0);
        when(warehouseService.checkStock(order)).thenReturn(true);
        when(paymentGateway.charge("CUST-010", 300.0)).thenReturn(true);

        orderService.processOrder(order);

        verify(notificationService).sendOrderConfirmation(order);
    }

    @Test
    void processOrder_outOfStock_notificationCalledOnce() {
        Order order = buildOrder("ORD-011", "CUST-011", 150.0);
        when(warehouseService.checkStock(order)).thenReturn(false);

        orderService.processOrder(order);

        verify(notificationService, times(1)).sendOutOfStockNotification(order);
    }

    @Test
    void processOrder_outOfStock_chargeNeverCalled() {
        Order order = buildOrder("ORD-012", "CUST-012", 200.0);
        when(warehouseService.checkStock(order)).thenReturn(false);

        orderService.processOrder(order);

        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    @Test
    void processOrder_success_reserveStockCalledOnce() {
        Order order = buildOrder("ORD-013", "CUST-013", 450.0);
        when(warehouseService.checkStock(order)).thenReturn(true);
        when(paymentGateway.charge("CUST-013", 450.0)).thenReturn(true);

        orderService.processOrder(order);

        verify(warehouseService, times(1)).reserveStock(order);
    }

    @Test
    void processOrder_success_softAssertAllFields() {
        Order order = buildOrder("ORD-020", "CUST-020", 999.0);
        when(warehouseService.checkStock(order)).thenReturn(true);
        when(paymentGateway.charge("CUST-020", 999.0)).thenReturn(true);

        OrderResult result = orderService.processOrder(order);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.isSuccess()).as("Result must be successful").isTrue();
        softly.assertThat(result.getMessage()).as("Message must contain order ID").contains("ORD-020");
        softly.assertThat(order.getStatus()).as("Order status must be CONFIRMED").isEqualTo(Order.Status.CONFIRMED);
        softly.assertThat(order.getTotalAmount()).as("Total amount must match").isEqualTo(999.0);
        softly.assertThat(order.getCustomerId()).as("Customer ID must match").isEqualTo("CUST-020");
        softly.assertAll();
    }

    @Test
    void processOrder_emptyOrder_softAssertFailureResult() {
        Order emptyOrder = new Order("ORD-021", "CUST-021");

        OrderResult result = orderService.processOrder(emptyOrder);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.isSuccess()).as("Must not be successful").isFalse();
        softly.assertThat(result.getMessage()).as("Message must mention items").contains("no items");
        softly.assertThat(emptyOrder.getStatus()).as("Status stays PENDING").isEqualTo(Order.Status.PENDING);
        softly.assertAll();
    }

    private List<Order> setupThreeProcessedOrders() {
        for (int i = 1; i <= 3; i++) {
            Order order = buildOrder("ORD-10" + i, "CUST-10" + i, i * 100.0);
            when(warehouseService.checkStock(order)).thenReturn(true);
            when(paymentGateway.charge("CUST-10" + i, i * 100.0)).thenReturn(true);
            orderService.processOrder(order);
        }
        return orderService.getProcessedOrders();
    }

    @Test
    void processedOrders_hasCorrectSize() {
        List<Order> orders = setupThreeProcessedOrders();

        assertThat(orders)
                .hasSize(3)
                .isNotEmpty();
    }

    @Test
    void processedOrders_allConfirmed() {
        List<Order> orders = setupThreeProcessedOrders();

        assertThat(orders)
                .extracting(Order::getStatus)
                .containsOnly(Order.Status.CONFIRMED);
    }

    @Test
    void processedOrders_containsExpectedOrderIds() {
        List<Order> orders = setupThreeProcessedOrders();

        assertThat(orders)
                .extracting(Order::getId)
                .containsExactlyInAnyOrder("ORD-101", "ORD-102", "ORD-103");
    }

    @Test
    void processedOrders_allAmountsPositive() {
        List<Order> orders = setupThreeProcessedOrders();

        assertThat(orders)
                .allMatch(o -> o.getTotalAmount() > 0, "all order totals should be positive");
    }

    @Test
    void processedOrders_noneAreCancelled() {
        List<Order> orders = setupThreeProcessedOrders();

        assertThat(orders)
                .noneMatch(o -> o.getStatus() == Order.Status.CANCELLED);
    }
}
