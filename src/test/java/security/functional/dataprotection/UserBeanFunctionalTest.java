package security.functional.dataprotection;

import Model.UserBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali di sicurezza per UserBean.
 * Verifica hashing password, getter/setter, parsing date, valori di default.
 */
@DisplayName("UserBean - Test funzionale")
class UserBeanFunctionalTest {

    private UserBean user;

    @BeforeEach
    void setUp() {
        user = new UserBean();
    }

    // ==========================================================
    // 1. HASHING PASSWORD (A02)
    // ==========================================================

    @Test
    @DisplayName("BCrypt: hash di 60 caratteri con prefisso $2a$")
    void testPasswordHash() {
        user.setPassword("password123");
        assertThat(user.getPassword())
                .hasSize(60)
                .startsWith("$2a$");
    }

    @Test
    @DisplayName("SECURITY: password non in chiaro")
    void testPasswordNotPlaintext() {
        user.setPassword("secret");
        assertThat(user.getPassword()).isNotEqualTo("secret");
    }

    @Test
    @DisplayName("BCrypt: checkPassword verifica correttamente")
    void testCheckPassword() {
        user.setPassword("password123");
        assertThat(user.checkPassword("password123")).isTrue();
        assertThat(user.checkPassword("wrongpassword")).isFalse();
    }

    @Test
    @DisplayName("BCrypt: stessa password produce hash diversi (salt casuale)")
    void testDeterministicHash() {
        UserBean user1 = new UserBean();
        UserBean user2 = new UserBean();

        user1.setPassword("SamePassword");
        user2.setPassword("SamePassword");

        // Con BCrypt, i due hash saranno DIVERSI
        assertThat(user1.getPassword())
                .as("BCrypt usa un salt casuale, quindi gli hash sono diversi")
                .isNotEqualTo(user2.getPassword()); // era isEqualTo

        // Ma entrambi validano correttamente la password
        assertThat(user1.checkPassword("SamePassword")).isTrue();
        assertThat(user2.checkPassword("SamePassword")).isTrue();
    }

    @Test
    @DisplayName("Password diverse producono hash diversi")
    void testDifferentPasswordsProduceDifferentHashes() {
        UserBean u1 = new UserBean();
        UserBean u2 = new UserBean();
        u1.setPassword("passwordOne");
        u2.setPassword("passwordTwo");
        assertThat(u1.getPassword()).isNotEqualTo(u2.getPassword());
    }

    // ==========================================================
    // 2. GETTER/SETTER
    // ==========================================================

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

    // ==========================================================
    // 3. PARSING DATE
    // ==========================================================

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

    // ==========================================================
    // 4. VALORI DI DEFAULT
    // ==========================================================

    @Test
    @DisplayName("Default: admin null, id 0, name null")
    void testDefaults() {
        UserBean u = new UserBean();
        assertThat(u.getId()).isZero();
        assertThat(u.isAdmin()).isNull();
        assertThat(u.getName()).isNull();
    }

    @Test
    @DisplayName("Un utente appena creato ha admin=null")
    void testNewUserHasNullAdmin() {
        UserBean u = new UserBean();
        assertThat(u.isAdmin()).isNull();
    }

    // ==========================================================
    // 5. FINDING
    // ==========================================================

    @Test
    @DisplayName("BCrypt: hash di 60 caratteri (CWE-916 risolto)")
    void testDocumentSha1Usage() {
        user.setPassword("password123");
        assertThat(user.getPassword())
                .as("BCrypt genera un hash di 60 caratteri, non più 40")
                .hasSize(60) // era 40
                .startsWith("$2a$");
    }
}