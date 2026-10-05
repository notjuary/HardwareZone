package security.functional.businesslogic;

import Model.ProductCartBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per ProductCartBean.
 */
@DisplayName("ProductCartBean - Test funzionale")
class ProductCartBeanFunctionalTest {

    @Test
    @DisplayName("Getter/setter id e quantity")
    void testGetSet() {
        ProductCartBean pcb = new ProductCartBean();
        pcb.setId(7);
        pcb.setQuantity(99);

        assertThat(pcb.getId()).isEqualTo(7);
        assertThat(pcb.getQuantity()).isEqualTo(99);
    }

    @Test
    @DisplayName("Default: id 0, quantity 0")
    void testDefaults() {
        ProductCartBean pcb = new ProductCartBean();
        assertThat(pcb.getId()).isEqualTo(0);
        assertThat(pcb.getQuantity()).isEqualTo(0);
    }
}