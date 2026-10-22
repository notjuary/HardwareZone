package security.audit.dataprotection;

import Model.PaymentBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Payment Security - Dati carte di credito e PCI-DSS")
class PaymentSecurityTest {

    private PaymentBean payment;

    // Regex replicate dal Payment.java doPost
    private static final Pattern NUMBER_STRING = Pattern.compile("^\\d+$");

    @BeforeEach
    void setUp() {
        payment = new PaymentBean();
    }

    // ==========================================================
    // 1. VALIDAZIONE NUMERO CARTA (Parametrizzato)
    // ==========================================================

    @ParameterizedTest(name = "Numero carta {0} -> atteso valido: {1}")
    @CsvSource({
            "1234567890123456, true",   // 16 cifre valide
            "123456789012345,  false",  // Troppo corto (15)
            "12345678901234567, false", // Troppo lungo (17)
            "1234abcd56789012, false"   // Contiene lettere
    })
    @DisplayName("Validazione numero carta (casi multipli)")
    void testCardNumberValidation(String cardNumber, boolean expected) {
        boolean valid = cardNumber.length() == 16 && NUMBER_STRING.matcher(cardNumber).find();
        assertThat(valid).isEqualTo(expected);
    }

    // ==========================================================
    // 2. VALIDAZIONE CVV (Parametrizzato)
    // ==========================================================

    @ParameterizedTest(name = "CVV {0} -> atteso valido: {1}")
    @CsvSource({
            "123, true",   // 3 cifre valide
            "12,  false",  // Troppo corto
            "1234, false"  // Troppo lungo
    })
    @DisplayName("Validazione CVV (casi multipli)")
    void testCvvValidation(String cvv, boolean expected) {
        boolean valid = cvv.length() == 3 && NUMBER_STRING.matcher(cvv).find();
        assertThat(valid).isEqualTo(expected);
    }

    // ==========================================================
    // 3. VALIDAZIONE SCADENZA
    // ==========================================================

    @Test
    @DisplayName("Scadenza futura e valida")
    void testFutureDeadlineIsValid() {
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        String deadline = (currentYear + 5) + "-12-31";
        String[] parts = deadline.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;
        int day = Integer.parseInt(parts[2]);

        java.util.GregorianCalendar deadlineCal = new java.util.GregorianCalendar(year, month, day);
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();

        assertThat(deadlineCal.after(today)).isTrue();
    }

    @Test
    @DisplayName("Scadenza passata NON e valida")
    void testPastDeadlineIsInvalid() {
        String deadline = "2020-01-01";
        String[] parts = deadline.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;
        int day = Integer.parseInt(parts[2]);

        java.util.GregorianCalendar deadlineCal = new java.util.GregorianCalendar(year, month, day);
        java.util.GregorianCalendar today = new java.util.GregorianCalendar();

        assertThat(deadlineCal.after(today)).isFalse();
    }

    @Test
    @DisplayName("FINDING: Payment.java non gestisce deadline null (NPE)")
    void testDocumentNullDeadline() throws Exception {
        String source = readSource("src/main/java/Controller/Payment.java");

        boolean hasNullRisk =
                source.contains("String deadline = request.getParameter(\"scadenza\")") &&
                        source.contains("deadline.split(\"-\")") &&
                        !source.contains("deadline != null") &&
                        !source.contains("deadline == null");

        assertThat(hasNullRisk)
                .as("FINDING: Payment.java chiama deadline.split(\"-\") senza verificare "
                        + "che deadline != null. Se il parametro manca, si verifica NPE "
                        + "(CWE-476). Fix: validare tutti i parametri prima dell'uso.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: Payment.java non gestisce NumberFormatException su date")
    void testDocumentNumberFormatExceptionOnDate() throws Exception {
        String source = readSource("src/main/java/Controller/Payment.java");

        boolean noTryCatch =
                source.contains("Integer.parseInt(dateSplit[0])") &&
                        !source.contains("try {");

        assertThat(noTryCatch)
                .as("FINDING: Payment.java chiama Integer.parseInt senza try/catch. "
                        + "Se la data non e nel formato YYYY-MM-DD, lancia NumberFormatException "
                        + "(CWE-20). Fix: try/catch + validazione regex.")
                .isTrue();
    }

    // ==========================================================
    // 4. FINDING CRITICI - Salvataggio dati carta
    // ==========================================================

    @Test
    @DisplayName("FINDING CRITICO PCI-DSS: il numero carta viene salvato in chiaro nel DB")
    void testDocumentPlaintextCardNumberStorage() throws Exception {
        String source = readSource("src/main/java/Model/PaymentDAO.java");

        boolean storesPlaintext =
                source.contains("ps.setString(3, payment.getCardNumber())") &&
                        !source.contains("encrypt") &&
                        !source.contains("cipher");

        assertThat(storesPlaintext)
                .as("FINDING CRITICO (PCI-DSS 3.4): PaymentDAO salva il numero carta "
                        + "in chiaro nel database (colonna Numero_Carta). "
                        + "Questo viola PCI-DSS Requirement 3.4 che richiede la cifratura "
                        + "dei dati sensibili a riposo. CWE-312: Cleartext Storage of "
                        + "Sensitive Information. OWASP A02:2021. "
                        + "Fix: cifrare con AES-256 + gestione chiavi sicura, "
                        + "oppure delegare a un payment gateway (Stripe, PayPal).")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING CRITICO PCI-DSS: il CVV viene salvato nel DB (vietato)")
    void testDocumentCvvStorage() throws Exception {
        String source = readSource("src/main/java/Model/PaymentDAO.java");

        boolean storesCvv = source.contains("ps.setString(4, payment.getCVV())");

        assertThat(storesCvv)
                .as("FINDING CRITICO (PCI-DSS 3.2): il CVV viene salvato nel database. "
                        + "PCI-DSS VIETA esplicitamente di memorizzare il CVV dopo "
                        + "l'autorizzazione della transazione, in qualsiasi forma. "
                        + "CWE-312: Cleartext Storage of Sensitive Information. "
                        + "Fix: NON salvare mai il CVV. Usarlo solo per la transazione "
                        + "e scartarlo immediatamente.")
                .isTrue();
    }

    @Test
    @DisplayName("FINDING: la tabella Pagamento non cifra i dati")
    void testDocumentNoEncryptionColumn() throws Exception {
        String schema = readSource("database/createDB.sql");

        boolean noEncryption =
                schema.contains("Numero_Carta CHAR(16)") &&
                        !schema.contains("Numero_Carta_Encrypted") &&
                        !schema.contains("Numero_Carta_Hash");

        assertThat(noEncryption)
                .as("FINDING: la colonna Numero_Carta e CHAR(16) senza cifratura. "
                        + "Anche se il DB fosse compromesso, i numeri di carta "
                        + "sarebbero leggibili in chiaro (CWE-311).")
                .isTrue();
    }

    // ==========================================================
    // 5. AUTENTICAZIONE - Payment richiede login
    // ==========================================================

    @Test
    @DisplayName("Payment: doGet verifica che l'utente sia loggato")
    void testPaymentRequiresLogin() throws Exception {
        String source = readSource("src/main/java/Controller/Payment.java");

        assertThat(source)
                .as("Il payment deve verificare che l'utente sia loggato")
                .contains("session.getAttribute(\"user\") == null");
    }

    @Test
    @DisplayName("FINDING: Payment.doPost NON verifica login (NPE su user)")
    void testDocumentMissingLoginCheckInPost() throws Exception {
        String source = readSource("src/main/java/Controller/Payment.java");

        int postIndex = source.indexOf("doPost");
        String postSource = postIndex > 0 ? source.substring(postIndex) : "";

        boolean noLoginCheckInPost =
                postSource.contains("session.getAttribute(\"user\")") &&
                        !postSource.contains("if (session.getAttribute(\"user\") == null)") &&
                        !postSource.contains("user == null");

        assertThat(noLoginCheckInPost)
                .as("FINDING: Payment.doPost non verifica che l'utente sia loggato "
                        + "prima di chiamare user.getId(). Se un utente non autenticato "
                        + "invia una POST a /payment-servlet, si verifica NPE (CWE-476). "
                        + "Fix: stessa verifica di doGet all'inizio di doPost.")
                .isTrue();
    }

    // ==========================================================
    // 6. RACE CONDITION - Stock disponibile
    // ==========================================================

    @Test
    @DisplayName("FINDING: Payment verifica lo stock in doGet ma non in doPost (race condition)")
    void testDocumentRaceConditionOnStock() throws Exception {
        String source = readSource("src/main/java/Controller/Payment.java");

        boolean checksStockInGet =
                source.contains("productCartBean.getQuantity() > catalogProduct.getQuantity()");

        boolean decrementsStockInPost =
                source.contains("productBean.setQuantity(productBean.getQuantity() - product.getQuantity())");

        assertThat(checksStockInGet && decrementsStockInPost)
                .as("FINDING: Payment.doGet verifica la disponibilita in magazzino, "
                        + "ma Payment.doPost decrementa senza ricontrollare in modo atomico. "
                        + "Due utenti che acquistano lo stesso ultimo pezzo "
                        + "contemporaneamente possono causare overselling "
                        + "(race condition, CWE-362). Fix: usare transazione DB con "
                        + "SELECT ... FOR UPDATE o un decremento atomico con WHERE quantity >= ?")
                .isTrue();
    }

    // ==========================================================
    // 7. PaymentBean - Validazione
    // ==========================================================

    @Test
    @DisplayName("PaymentBean: setCardNumber memorizza il valore")
    void testPaymentBeanSetCardNumber() {
        payment.setCardNumber("1234567890123456");
        assertThat(payment.getCardNumber()).isEqualTo("1234567890123456");
    }

    @Test
    @DisplayName("PaymentBean: setCVV memorizza il valore")
    void testPaymentBeanSetCvv() {
        payment.setCVV("123");
        assertThat(payment.getCVV()).isEqualTo("123");
    }

    @Test
    @DisplayName("FINDING: PaymentBean espone CVV tramite getter pubblico (rischio log)")
    void testDocumentCvvGetterExposure() throws Exception {
        String source = readSource("src/main/java/Model/PaymentBean.java");

        boolean exposesCvv =
                source.contains("public String getCVV()") &&
                        source.contains("return CVV");

        assertThat(exposesCvv)
                .as("FINDING: PaymentBean espone getCVV() pubblico. Se il bean viene "
                        + "loggato, serializzato in JSON, o incluso in una JSP, il CVV "
                        + "puo trapelare nei log o nelle risposte HTTP "
                        + "(CWE-200: Exposure of Sensitive Information). "
                        + "Fix: rimuovere il getter CVV o marcare il bean con @JsonIgnore.")
                .isTrue();
    }

    @Test
    @DisplayName("PaymentBean: setHolder memorizza il titolare")
    void testPaymentBeanSetHolder() {
        payment.setHolder("Mario Rossi");
        assertThat(payment.getHolder()).isEqualTo("Mario Rossi");
    }

    @Test
    @DisplayName("PaymentDAO: usa PreparedStatement (no SQL injection)")
    void testPaymentDAO_UsesPreparedStatement() throws Exception {
        String source = readSource("src/main/java/Model/PaymentDAO.java");

        assertThat(source).contains("PreparedStatement");
        assertThat(source).doesNotContain("con.createStatement()");
    }

    // ==========================================================
    // Utility
    // ==========================================================

    private String readSource(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}