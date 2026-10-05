package security.functional.daointegration;

import Model.UserBean;
import Model.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per UserDAO con H2.
 * Verifica: CRUD, PreparedStatement, SQL Injection, fail-fast.
 */
@DisplayName("UserDAO - Test funzionale DAO")
class UserDAOFunctionalTest extends BaseFunctionalTest {

    private UserDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        dao = new UserDAO();
    }

    private UserBean createTestUser(String email) {
        UserBean user = new UserBean();
        user.setName("Mario");
        user.setSurname("Rossi");
        user.setEmail(email);
        user.setPassword("password123");
        user.setPhone("3331234567");
        user.setCity("Salerno");
        user.setProvince("SA");
        user.setPostalCode("84100");
        user.setAddress("Via Roma 1");
        user.setBirthday("1990-01-01");
        user.setRegister("2024-01-01");
        user.setState("true");
        user.setAdmin("false");
        return user;
    }

    @Test
    @DisplayName("doSave inserisce un utente correttamente")
    void testDoSave() {
        UserBean user = createTestUser("mario@test.com");
        dao.doSave(user);
        assertThat(user.getId()).isGreaterThan(0);
    }

    @Test
    @DisplayName("doRetrieveById restituisce l'utente corretto")
    void testDoRetrieveById() {
        UserBean saved = createTestUser("mario@test.com");
        dao.doSave(saved);

        UserBean retrieved = dao.doRetrieveById(saved.getId());

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getEmail()).isEqualTo("mario@test.com");
        assertThat(retrieved.getName()).isEqualTo("Mario");
    }

    @Test
    @DisplayName("doRetrieveById con id inesistente restituisce null")
    void testDoRetrieveByIdInesistente() {
        UserBean retrieved = dao.doRetrieveById(99999);
        assertThat(retrieved).isNull();
    }

    @Test
    @DisplayName("doRetrieveByEmailAndPassword con credenziali corrette")
    void testDoRetrieveByEmailAndPassword() {
        UserBean saved = createTestUser("mario@test.com");
        dao.doSave(saved);

        UserBean retrieved = dao.doRetrieveByEmailAndPassword("mario@test.com", "password123");

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getEmail()).isEqualTo("mario@test.com");
    }

    @Test
    @DisplayName("doRetrieveByEmailAndPassword con password errata restituisce null")
    void testPasswordErrata() {
        UserBean saved = createTestUser("mario@test.com");
        dao.doSave(saved);

        UserBean retrieved = dao.doRetrieveByEmailAndPassword("mario@test.com", "wrongpassword");

        assertThat(retrieved).isNull();
    }

    @Test
    @DisplayName("SECURITY: SQL Injection nel login non funziona (PreparedStatement)")
    void testSqlInjectionLogin() {
        UserBean saved = createTestUser("mario@test.com");
        dao.doSave(saved);

        UserBean retrieved = dao.doRetrieveByEmailAndPassword("' OR '1'='1", "anything");

        assertThat(retrieved).as("SQL Injection deve fallire").isNull();
    }

    @Test
    @DisplayName("isAlreadyRegistered restituisce true per email esistente")
    void testIsAlreadyRegistered() {
        UserBean saved = createTestUser("mario@test.com");
        dao.doSave(saved);

        assertThat(dao.isAlreadyRegistered("mario@test.com")).isTrue();
        assertThat(dao.isAlreadyRegistered("nonexistent@test.com")).isFalse();
    }

    @Test
    @DisplayName("doRetrieveAll restituisce tutti gli utenti")
    void testDoRetrieveAll() {
        dao.doSave(createTestUser("mario@test.com"));
        dao.doSave(createTestUser("luigi@test.com"));

        List<UserBean> users = dao.doRetrieveAll();

        assertThat(users).hasSize(2);
    }
}