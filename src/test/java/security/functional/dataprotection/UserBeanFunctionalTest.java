package security.functional.dataprotection;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per UserBean.
 * Verifica: hashing password, getter/setter, calendari.
 */
@DisplayName("UserBean - Test funzionale")
class UserBeanFunctionalTest {

    private UserBean user;

    @BeforeEach
    void setUp() {
        user = new UserBean();
    }

    @Test
    @DisplayName("Password hashata con SHA-1 (40 caratteri hex)")
    void testPasswordHash() {
        user.setPassword("password123");
        assertThat(user.getPassword()).hasSize(40);
        assertThat(user.getPassword()).matches("[0-9a-f]{40}");
    }

    @Test
    @DisplayName("Password non è memorizzata in chiaro")
    void testPasswordNotPlaintext() {
        user.setPassword("secret");
        assertThat(user.getPassword()).isNotEqualTo("secret");
    }

    @Test
    @DisplayName("Stessa password produce stesso hash (SHA-1 senza salt)")
    void testDeterministicHash() {
        UserBean u1 = new UserBean();
        UserBean u2 = new UserBean();
        u1.setPassword("samepass");
        u2.setPassword("samepass");
        assertThat(u1.getPassword()).isEqualTo(u2.getPassword());
    }

    @Test
    @DisplayName("Getter/setter di tutti i campi")
    void testGetSetAll() {
        user.setId(1);
        user.setName("Mario");
        user.setSurname("Rossi");
        user.setEmail("mario@test.com");
        user.setPhone("3331234567");
        user.setCity("Salerno");
        user.setProvince("SA");
        user.setPostalCode("84100");
        user.setAddress("Via Roma 1");
        user.setState("true");
        user.setAdmin("false");

        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getName()).isEqualTo("Mario");
        assertThat(user.getSurname()).isEqualTo("Rossi");
        assertThat(user.getEmail()).isEqualTo("mario@test.com");
        assertThat(user.getPhone()).isEqualTo("3331234567");
        assertThat(user.getCity()).isEqualTo("Salerno");
        assertThat(user.getProvince()).isEqualTo("SA");
        assertThat(user.getPostalCode()).isEqualTo("84100");
        assertThat(user.getAddress()).isEqualTo("Via Roma 1");
        assertThat(user.isActive()).isEqualTo("true");
        assertThat(user.isAdmin()).isEqualTo("false");
    }

    @Test
    @DisplayName("setBirthday e getBirthday funzionano")
    void testBirthday() {
        user.setBirthday("1990-05-15");
        assertThat(user.getBirthday()).isEqualTo("1990-5-15");
    }

    @Test
    @DisplayName("setRegister e getRegister funzionano")
    void testRegister() {
        user.setRegister("2024-01-01");
        assertThat(user.getRegister()).isEqualTo("2024-1-1");
    }

    @Test
    @DisplayName("setRegister senza argomenti imposta la data di oggi")
    void testRegisterNow() {
        user.setRegister();
        assertThat(user.getRegister()).isNotNull();
    }

    @Test
    @DisplayName("Default: admin null, id 0")
    void testDefaults() {
        UserBean u = new UserBean();
        assertThat(u.getId()).isEqualTo(0);
        assertThat(u.isAdmin()).isNull();
    }
}