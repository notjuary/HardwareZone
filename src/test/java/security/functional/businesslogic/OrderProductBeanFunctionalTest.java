package security.functional.businesslogic;

import Model.OrderProductBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per OrderProductBean.
 */
@DisplayName("OrderProductBean - Test funzionale")
class OrderProductBeanFunctionalTest {

    @Test
    @DisplayName("Getter/setter di tutti i campi")
    void testGetSetAll() {
        OrderProductBean op = new OrderProductBean();
        op.setProduct(1);
        op.setQuantity(3);
        op.setPrice(299.99);
        op.setOrder(5);

        assertThat(op.getProduct()).isEqualTo(1);
        assertThat(op.getQuantity()).isEqualTo(3);
        assertThat(op.getPrice()).isEqualTo(299.99);
        assertThat(op.getOrder()).isEqualTo(5);
    }

    @Test
    @DisplayName("Default: tutti i campi a 0")
    void testDefaults() {
        OrderProductBean op = new OrderProductBean();
        assertThat(op.getProduct()).isZero();
        assertThat(op.getQuantity()).isZero();
        assertThat(op.getPrice()).isEqualTo(0.0);
        assertThat(op.getOrder()).isZero();
    }
}