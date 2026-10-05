package security.functional.daointegration;

import Model.ProductBean;
import Model.ProductDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per ProductDAO con H2.
 */
@DisplayName("ProductDAO - Test funzionale DAO")
class ProductDAOFunctionalTest extends BaseFunctionalTest {

    private ProductDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        dao = new ProductDAO();
    }

    private ProductBean createProduct(String name, double price, int sales) {
        ProductBean p = new ProductBean();
        p.setName(name);
        p.setDescription("Desc " + name);
        p.setPrice(price);
        p.setQuantity(10);
        p.setSales(sales);
        p.setImage("/img/" + name + ".png");
        p.setCategory("CPU");
        return p;
    }

    @Test
    @DisplayName("doSave inserisce prodotto correttamente")
    void testDoSave() {
        ProductBean p = createProduct("Ryzen", 299.99, 0);
        dao.doSave(p);
        assertThat(p.getId()).isGreaterThan(0);
    }

    @Test
    @DisplayName("doRetrieveById restituisce prodotto corretto")
    void testDoRetrieveById() {
        ProductBean saved = createProduct("Ryzen", 299.99, 0);
        dao.doSave(saved);

        ProductBean retrieved = dao.doRetrieveById(saved.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getName()).isEqualTo("Ryzen");
        assertThat(retrieved.getPrice()).isEqualTo(299.99);
    }

    @Test
    @DisplayName("doRetrieveById inesistente restituisce null")
    void testDoRetrieveByIdInesistente() {
        assertThat(dao.doRetrieveById(99999)).isNull();
    }

    @Test
    @DisplayName("doRetrieveAll restituisce tutti i prodotti ordinati per ID")
    void testDoRetrieveAll() {
        dao.doSave(createProduct("Ryzen", 299.99, 0));
        dao.doSave(createProduct("Corsair", 99.99, 0));

        List<ProductBean> products = dao.doRetrieveAll();
        assertThat(products).hasSize(2);
        assertThat(products.get(0).getName()).isEqualTo("Ryzen");
    }

    @Test
    @DisplayName("doRetrieveSales restituisce solo prodotti con sconto > 0")
    void testDoRetrieveSales() {
        dao.doSave(createProduct("Ryzen", 299.99, 30));
        dao.doSave(createProduct("Corsair", 99.99, 0));

        List<ProductBean> sales = dao.doRetrieveSales();
        assertThat(sales).hasSize(1);
        assertThat(sales.get(0).getName()).isEqualTo("Ryzen");
    }

    @Test
    @DisplayName("doRetrieveByFilter filtra per prezzo")
    void testDoRetrieveByFilter() {
        dao.doSave(createProduct("Ryzen", 299.99, 0));
        dao.doSave(createProduct("Corsair", 99.99, 0));

        List<ProductBean> filtered = dao.doRetrieveByFilter(100, 500, "all");
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getName()).isEqualTo("Ryzen");
    }

    @Test
    @DisplayName("isAlreadyRegistered restituisce true per nome+descrizione esistente")
    void testIsAlreadyRegistered() {
        ProductBean p = createProduct("Ryzen", 299.99, 0);
        dao.doSave(p);

        assertThat(dao.isAlreadyRegistered("Ryzen", "Desc Ryzen")).isTrue();
        assertThat(dao.isAlreadyRegistered("Altro", "Altra desc")).isFalse();
    }
}