package ua.edu.ukma.ivanov.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DiscountMutationFixedTest {

    @Mock private WarehouseService warehouseService;
    @Mock private PaymentGateway paymentGateway;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void discount_exactlyAt5000_returns15Percent() {
        assertThat(orderService.calculateDiscount(5000)).isEqualTo(15);
    }

    @Test
    void discount_above5000_returns15Percent() {
        assertThat(orderService.calculateDiscount(10000)).isEqualTo(15);
    }

    @Test
    void discount_justBelow5000_returns10PercentNotFifteen() {
        assertThat(orderService.calculateDiscount(4999)).isEqualTo(10);
    }

    @Test
    void discount_exactlyAt1000_returns10Percent() {
        assertThat(orderService.calculateDiscount(1000)).isEqualTo(10);
    }

    @Test
    void discount_midRange1000to5000_returns10Percent() {
        assertThat(orderService.calculateDiscount(2500)).isEqualTo(10);
    }

    @Test
    void discount_justBelow1000_returns5PercentNotTen() {
        assertThat(orderService.calculateDiscount(999)).isEqualTo(5);
    }

    @Test
    void discount_exactlyAt500_returns5Percent() {
        assertThat(orderService.calculateDiscount(500)).isEqualTo(5);
    }

    @Test
    void discount_midRange500to1000_returns5Percent() {
        assertThat(orderService.calculateDiscount(750)).isEqualTo(5);
    }

    @Test
    void discount_justBelow500_returnsZeroNotFive() {
        assertThat(orderService.calculateDiscount(499)).isEqualTo(0);
    }

    @Test
    void discount_zero_returnsZeroPercent() {
        assertThat(orderService.calculateDiscount(0)).isEqualTo(0);
    }

    @Test
    void discount_smallAmount_returnsZeroPercent() {
        assertThat(orderService.calculateDiscount(100)).isEqualTo(0);
    }

    @Test
    void discount_negative_returnsZeroPercent() {
        assertThat(orderService.calculateDiscount(-50)).isEqualTo(0);
    }
}
