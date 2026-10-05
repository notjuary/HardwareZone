package security.functional.daointegration;

import Model.OrderBean;
import Model.OrderDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per OrderDAO con H2.
 */
@DisplayName("OrderDAO - Test funzionale DAO")
class OrderDAOFunctionalTest extends BaseFunctionalTest {

    private OrderDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        dao = new OrderDAO();

        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Mario','Rossi','1990-01-01','mario@test.com','hash','3331234567','SA','SA','84100','Via 1','2024-01-01','true','false')");
    }

    private OrderBean createOrder(int userId, double total) {
        OrderBean order = new OrderBean();
        order.setUser(userId);
        order.setTotal(total);
        return order;
    }

    @Test
    @DisplayName("doSave inserisce ordine e restituisce l'id")
    void testDoSave() {
        OrderBean order = createOrder(1, 299.99);
        int id = dao.doSave(order);

        assertThat(id).isGreaterThan(0);
        assertThat(order.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("doRetrieveById restituisce ordini di un utente")
    void testDoRetrieveById() {
        dao.doSave(createOrder(1, 100.00));
        dao.doSave(createOrder(1, 200.00));

        ArrayList<OrderBean> orders = dao.doRetrieveById(1);

        assertThat(orders).hasSize(2);
    }

    @Test
    @DisplayName("doRetrieveByIdOrder restituisce l'ordine per ID")
    void testDoRetrieveByIdOrder() {
        int id = dao.doSave(createOrder(1, 299.99));

        OrderBean order = dao.doRetrieveByIdOrder(id);

        assertThat(order).isNotNull();
        assertThat(order.getTotal()).isEqualTo(299.99);
    }

    @Test
    @DisplayName("doRetrieveAll restituisce tutti gli ordini")
    void testDoRetrieveAll() {
        dao.doSave(createOrder(1, 100.00));
        dao.doSave(createOrder(1, 200.00));

        ArrayList<OrderBean> orders = dao.doRetrieveAll();

        assertThat(orders).hasSize(2);
    }
}