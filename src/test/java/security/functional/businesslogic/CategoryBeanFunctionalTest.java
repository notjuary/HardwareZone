package security.functional.businesslogic;

import Model.CategoryBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per CategoryBean.
 */
@DisplayName("CategoryBean - Test funzionale")
class CategoryBeanFunctionalTest {

    @Test
    @DisplayName("Getter/setter nome")
    void testGetSetNome() {
        CategoryBean c = new CategoryBean();
        c.setNome("CPU");
        assertThat(c.getNome()).isEqualTo("CPU");
    }

    @Test
    @DisplayName("Default: nome null")
    void testDefault() {
        assertThat(new CategoryBean().getNome()).isNull();
    }
}