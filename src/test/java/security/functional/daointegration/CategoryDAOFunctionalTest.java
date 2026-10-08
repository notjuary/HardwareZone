package security.functional.daointegration;

import Model.CategoryBean;
import Model.CategoryDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per CategoryDAO con H2.
 */
@DisplayName("CategoryDAO - Test funzionale DAO")
class CategoryDAOFunctionalTest extends BaseFunctionalTest {

    private CategoryDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        dao = new CategoryDAO();
    }

    @Test
    @DisplayName("doSave inserisce una categoria")
    void testDoSave() {
        dao.doSave("CPU");

        List<CategoryBean> categories = dao.doRetrieveAll();
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getNome()).isEqualTo("CPU");
    }

    @Test
    @DisplayName("doRetrieveAll restituisce categorie ordinate alfabeticamente")
    void testDoRetrieveAllOrdinato() {
        dao.doSave("SSD");
        dao.doSave("CPU");
        dao.doSave("RAM");

        List<CategoryBean> categories = dao.doRetrieveAll();

        assertThat(categories).hasSize(3);
        assertThat(categories.get(0).getNome()).isEqualTo("CPU");
        assertThat(categories.get(1).getNome()).isEqualTo("RAM");
        assertThat(categories.get(2).getNome()).isEqualTo("SSD");
    }

    @Test
    @DisplayName("doRetrieveAll con DB vuoto restituisce lista vuota")
    void testDoRetrieveAllVuoto() {
        assertThat(dao.doRetrieveAll()).isEmpty();
    }
}