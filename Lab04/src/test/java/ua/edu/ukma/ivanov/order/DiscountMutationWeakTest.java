package ua.edu.ukma.ivanov.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DiscountMutationWeakTest {

    @Mock private WarehouseService warehouseService;
    @Mock private PaymentGateway paymentGateway;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void weakTest_discount_missesBoundaryMutations() {
        assertThat(orderService.calculateDiscount(5000)).isEqualTo(15);
        assertThat(orderService.calculateDiscount(1000)).isEqualTo(10);
        assertThat(orderService.calculateDiscount(500)).isEqualTo(5);
    }
}
