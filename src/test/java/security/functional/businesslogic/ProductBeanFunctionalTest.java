package security.functional.businesslogic;

import Model.ProductBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per ProductBean.
 */
@DisplayName("ProductBean - Test funzionale")
class ProductBeanFunctionalTest {

    private ProductBean product;

    @BeforeEach
    void setUp() {
        product = new ProductBean();
    }

    @Test
    @DisplayName("Getter/setter di tutti i campi")
    void testGetSetAll() {
        product.setId(1);
        product.setName("Ryzen");
        product.setDescription("CPU AMD");
        product.setPrice(299.99);
        product.setQuantity(10);
        product.setSales(30);
        product.setImage("/img.png");
        product.setCategory("CPU");

        assertThat(product.getId()).isEqualTo(1);
        assertThat(product.getName()).isEqualTo("Ryzen");
        assertThat(product.getDescription()).isEqualTo("CPU AMD");
        assertThat(product.getPrice()).isEqualTo(299.99);
        assertThat(product.getQuantity()).isEqualTo(10);
        assertThat(product.getSales()).isEqualTo(30);
        assertThat(product.getImage()).isEqualTo("/img.png");
        assertThat(product.getCategory()).isEqualTo("CPU");
    }

    @Test
    @DisplayName("Default: id 0, price 0.0, quantity 0")
    void testDefaults() {
        ProductBean p = new ProductBean();
        assertThat(p.getId()).isEqualTo(0);
        assertThat(p.getPrice()).isEqualTo(0.0);
        assertThat(p.getQuantity()).isEqualTo(0);
        assertThat(p.getSales()).isEqualTo(0);
    }

    @Test
    @DisplayName("Accetta prezzo negativo (validazione a livello DAO)")
    void testNegativePrice() {
        product.setPrice(-10.0);
        assertThat(product.getPrice()).isEqualTo(-10.0);
    }

    @Test
    @DisplayName("Accetta quantità negativa (validazione a livello DAO)")
    void testNegativeQuantity() {
        product.setQuantity(-5);
        assertThat(product.getQuantity()).isEqualTo(-5);
    }
}