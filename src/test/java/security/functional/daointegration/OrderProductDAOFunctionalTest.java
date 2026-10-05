package security.functional.daointegration;

import Model.OrderProductBean;
import Model.OrderProductDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per OrderProductDAO con H2.
 */
@DisplayName("OrderProductDAO - Test funzionale DAO")
class OrderProductDAOFunctionalTest extends BaseFunctionalTest {

    private OrderProductDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        dao = new OrderProductDAO();

        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Mario','Rossi','1990-01-01','mario@test.com','hash','3331234567','SA','SA','84100','Via 1','2024-01-01','true','false')");
        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Ryzen', 'CPU', 299.99, 10, 0, '/img.png', 'CPU')");
        executeSql("INSERT INTO Ordine (Utente, Totale) VALUES (1, 599.98)");
    }

    @Test
    @DisplayName("doSave inserisce un order-product")
    void testDoSave() {
        OrderProductBean op = new OrderProductBean();
        op.setProduct(1);
        op.setQuantity(2);
        op.setPrice(299.99);
        op.setOrder(1);

        dao.doSave(op);

        ArrayList<OrderProductBean> retrieved = dao.doRetrieveById(1);
        assertThat(retrieved).hasSize(1);
    }

    @Test
    @DisplayName("doRetrieveById restituisce i prodotti di un ordine")
    void testDoRetrieveById() {
        OrderProductBean op1 = new OrderProductBean();
        op1.setProduct(1);
        op1.setQuantity(1);
        op1.setPrice(299.99);
        op1.setOrder(1);
        dao.doSave(op1);

        OrderProductBean op2 = new OrderProductBean();
        op2.setProduct(1);
        op2.setQuantity(1);
        op2.setPrice(299.99);
        op2.setOrder(1);
        dao.doSave(op2);

        ArrayList<OrderProductBean> retrieved = dao.doRetrieveById(1);
        assertThat(retrieved).hasSize(2);
    }
}