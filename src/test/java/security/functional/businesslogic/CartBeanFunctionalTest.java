package security.functional.businesslogic;

import Model.CartBean;
import Model.ProductCartBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per CartBean.
 */
@DisplayName("CartBean - Test funzionale")
class CartBeanFunctionalTest {

    private CartBean cart;

    @BeforeEach
    void setUp() {
        cart = new CartBean();
    }

    @Test
    @DisplayName("addProduct aggiunge nuovo prodotto")
    void testAddNewProduct() {
        cart.addProduct(1, 3);
        assertThat(cart.getCartList()).hasSize(1);
        assertThat(cart.getNumberObject()).isEqualTo(3);
    }

    @Test
    @DisplayName("addProduct somma quantità per prodotto esistente")
    void testAddExistingProduct() {
        cart.addProduct(1, 3);
        cart.addProduct(1, 2);
        assertThat(cart.getCartList()).hasSize(1);
        assertThat(cart.getCartList().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("addProduct con quantità 0 non incrementa")
    void testAddZeroQuantity() {
        cart.addProduct(1, 0);
        assertThat(cart.getNumberObject()).isZero();
    }

    @Test
    @DisplayName("removeProduct rimuove prodotto esistente")
    void testRemoveExisting() {
        cart.addProduct(1, 3);
        cart.addProduct(2, 2);
        cart.removeProduct(1);
        assertThat(cart.getCartList()).hasSize(1);
        assertThat(cart.getCartList().get(0).getId()).isEqualTo(2);
    }

    @Test
    @DisplayName("FINDING: removeProduct con id inesistente rimuove l'ultimo elemento")
    void testRemoveNonexistentBug() {
        cart.addProduct(1, 3);
        cart.addProduct(2, 5);
        cart.removeProduct(999);
        assertThat(cart.getCartList())
                .as("BUG: rimuove l'ultimo elemento se id non esiste")
                .hasSize(1);
    }

    @Test
    @DisplayName("setCartList calcola numberObject")
    void testSetCartList() {
        ProductCartBean p1 = new ProductCartBean();
        p1.setId(1);
        p1.setQuantity(3);
        ProductCartBean p2 = new ProductCartBean();
        p2.setId(2);
        p2.setQuantity(7);

        ArrayList<ProductCartBean> list = new ArrayList<>();
        list.add(p1);
        list.add(p2);

        cart.setCartList(list);

        assertThat(cart.getNumberObject()).isEqualTo(10);
        assertThat(cart.getCartList()).hasSize(2);
    }

    @Test
    @DisplayName("CartBean vuoto: cartList vuota, numberObject 0")
    void testEmptyCart() {
        assertThat(cart.getCartList()).isEmpty();
        assertThat(cart.getNumberObject()).isZero();
    }
}
