package security.functional.daointegration;

import Model.CartDAO;
import Model.ProductCartBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per CartDAO con H2.
 */
@DisplayName("CartDAO - Test funzionale DAO")
class CartDAOFunctionalTest extends BaseFunctionalTest {

    private CartDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        dao = new CartDAO();

        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Mario','Rossi','1990-01-01','mario@test.com','hash','3331234567','SA','SA','84100','Via 1','2024-01-01','true','false')");
        executeSql("INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) " +
                "VALUES ('Ryzen', 'CPU', 299.99, 10, 0, '/img.png', 'CPU')");
    }

    @Test
    @DisplayName("doSave inserisce elemento nel carrello")
    void testDoSave() {
        dao.doSave(1, 1, 3);

        ArrayList<ProductCartBean> cart = dao.getCart(1);
        assertThat(cart).hasSize(1);
        assertThat(cart.get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("getCart restituisce il carrello dell'utente")
    void testGetCart() {
        dao.doSave(1, 1, 5);

        ArrayList<ProductCartBean> cart = dao.getCart(1);
        assertThat(cart).hasSize(1);
    }

    @Test
    @DisplayName("doDelete svuota il carrello dell'utente")
    void testDoDelete() {
        dao.doSave(1, 1, 3);
        dao.doSave(1, 1, 2);

        dao.doDelete(1);

        ArrayList<ProductCartBean> cart = dao.getCart(1);
        assertThat(cart).isEmpty();
    }

    @Test
    @DisplayName("getCart per utente senza carrello restituisce lista vuota")
    void testGetCartVuoto() {
        ArrayList<ProductCartBean> cart = dao.getCart(999);
        assertThat(cart).isEmpty();
    }
}