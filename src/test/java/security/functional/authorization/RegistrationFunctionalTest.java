package security.functional.authorization;

import Controller.Registration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.mockito.Mockito.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
/**
 * Test funzionali di sicurezza per Registration.
 */
@DisplayName("Registration - Test funzionale di sicurezza")
class RegistrationFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
    }

    private void setupValidRegistration() {
        when(support.request.getParameter("name")).thenReturn("Mario");
        when(support.request.getParameter("surname")).thenReturn("Rossi");
        when(support.request.getParameter("email")).thenReturn("mario.rossi@test.com");
        when(support.request.getParameter("password")).thenReturn("password123");
        when(support.request.getParameter("phone")).thenReturn("3331234567");
        when(support.request.getParameter("city")).thenReturn("Salerno");
        when(support.request.getParameter("province")).thenReturn("SA");
        when(support.request.getParameter("postalCode")).thenReturn("84100");
        when(support.request.getParameter("address")).thenReturn("Via Roma 1");
        when(support.request.getParameter("birthday")).thenReturn("1990-01-01");
    }

    @Test
    @DisplayName("SECURITY: registrazione con dati validi crea l'utente")
    void testRegistrazioneValida() throws Exception {
        setupValidRegistration();

        Registration servlet = new Registration();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.session).setAttribute(eq("user"), any());
    }

    @Test
    @DisplayName("SECURITY: registrazione con email duplicata fallisce")
    void testEmailDuplicata() throws Exception {
        executeSql("INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) " +
                "VALUES ('Esistente','Test','1990-01-01','mario.rossi@test.com','hash','3331234567','SA','SA','84100','Via 1','2024-01-01','true','false')");

        setupValidRegistration();

        Registration servlet = new Registration();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.session, never()).setAttribute(eq("user"), any());
    }

    @ParameterizedTest(name = "SECURITY: registrazione con {0}=\"{1}\" fallisce ({2})")
    @CsvSource({
            "email, invalid-email,              email malformata",
            "name,  '',                         nome vuoto",
            "name,  '; DROP TABLE Utente; --',  SQL injection nel nome"
    })
    @DisplayName("SECURITY: registrazione rifiutata con input invalido")
    void testRegistrazioneInputInvalido(String fieldName, String invalidValue, String description) throws Exception {
        setupValidRegistration();
        when(support.request.getParameter(fieldName)).thenReturn(invalidValue);

        Registration servlet = new Registration();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.session, never()).setAttribute(eq("user"), any());
    }
}