package security.functional.businesslogic;

import Model.OrderBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per OrderBean.
 */
@DisplayName("OrderBean - Test funzionale")
class OrderBeanFunctionalTest {

    @Test
    @DisplayName("Getter/setter di tutti i campi")
    void testGetSetAll() {
        OrderBean order = new OrderBean();
        order.setId(1);
        order.setUser(42);
        order.setTotal(299.99);

        assertThat(order.getId()).isEqualTo(1);
        assertThat(order.getUser()).isEqualTo(42);
        assertThat(order.getTotal()).isEqualTo(299.99);
    }

    @Test
    @DisplayName("Default: id 0, user 0, total 0.0")
    void testDefaults() {
        OrderBean o = new OrderBean();
        assertThat(o.getId()).isZero();
        assertThat(o.getUser()).isZero();
        assertThat(o.getTotal()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Accetta total negativo (validazione a livello business)")
    void testNegativeTotal() {
        OrderBean o = new OrderBean();
        o.setTotal(-50.0);
        assertThat(o.getTotal()).isEqualTo(-50.0);
    }
}