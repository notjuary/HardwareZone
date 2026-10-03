package security.dataprotection;

import Model.PaymentBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di sicurezza per l'area "Payment" (dati sensibili carte di credito).
 * Verifica:
 *  - validazione numero carta, CVV, scadenza, titolare
 *  - salvataggio sicuro dei dati (o mancanza di)
 *  - conformità PCI-DSS
 *
 * Riferimento: OWASP Testing Guide - OTG-CRYPST, PCI-DSS v4.0
 * Riferimento CWE: CWE-312 (Cleartext Storage), CWE-319, CWE-311
 */
@DisplayName("Payment Security - Dati carte di credito e PCI-DSS")
class PaymentSecurityTest {

    private PaymentBean payment;

    // Regex replicate dal Payment.java doPost
    private static final Pattern NUMBER_STRING = Pattern.compile("^\\d+$");
    private static final Pattern HOLDER_STRING = Pattern.compile("^([a-zA-Z\\\\xE0\\\\xE8\\\\xE9\\\\xF9\\\\xF2\\\\xEC\\\\x27]\\s?){2,255}$");

    @BeforeEach
    void setUp() {
        payment = new PaymentBean();
    }

    // ==========================================================
    // 1. VALIDAZIONE NUMERO CARTA (formato)
    // ==========================================================

    @Test
    @DisplayName("Numero carta valido: 16 cifre numeriche")
    void testValidCardNumber() {
        String cardNumber = "1234567890123456";
        boolean valid = cardNumber.length() == 16 && NUMBER_STRING.matcher(cardNumber).find();
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("Numero carta invalido: 15 cifre (troppo corto)")
    void testCardNumberTooShort() {
        String cardNumber = "123456789012345";
        boolean valid = cardNumber.length() == 16 && NUMBER_STRING.matcher(cardNumber).find();
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Numero carta invalido: 17 cifre (troppo lungo)")
    void testCardNumberTooLong() {
        String cardNumber = "12345678901234567";
        boolean valid = cardNumber.length() == 16 && NUMBER_STRING.matcher(cardNumber).find();
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Numero carta invalido: contiene lettere")
    void testCardNumberWithLetters() {
        String cardNumber = "1234abcd56789012";
        boolean valid = cardNumber.length() == 16 && NUMBER_STRING.matcher(cardNumber).find();
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("FINDING: il numero carta non supera il controllo di Luhn (nessuna verifica)")
    void testDocumentNoLuhnCheck() throws Exception {
        String source = readSource("src/main/java/Controller/Payment.java");

        boolean noLuhnCheck =
                !source.contains("luhn") &&
                        !source.contains("Luhn") &&
                        !source.contains("isValidCard");

        assertThat(noLuhnCheck)
                .as("FINDING: Payment.java non esegue il controllo di Luhn sul numero carta. "
                        + "Un utente puo inserire un numero che non corrisponde a nessuna "
                        + "carta reale, ma il sistema lo accetta comunque. "
                        + "Fix: implementare algoritmo di Luhn per validare il checksum. "
                        + "(Riferimento: ISO/IEC 7812)")
                .isTrue();
    }

    // ==========================================================
    // 2. VALIDAZIONE CVV
    // ==========================================================

    @Test
    @DisplayName("CVV valido: 3 cifre")
    void testValidCvv() {
        String cvv = "123";
        boolean valid = cvv.length() == 3 && NUMBER_STRING.matcher(cvv).find();
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("CVV invalido: 2 cifre")
    void testCvvTooShort() {
        String cvv = "12";
        boolean valid = cvv.length() == 3 && NUMBER_STRING.matcher(cvv).find();
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("CVV invalido: 4 cifre")
    void testCvvTooLong() {
        String cvv = "1234";
        boolean valid = cvv.length() == 3 && NUMBER_STRING.matcher(cvv).find();
        assertThat(valid).isFalse();
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

        // Cerchiamo il pattern nel doPost
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

        // doGet verifica la quantita, ma doPost no
        boolean checksStockInGet =
                source.contains("productCartBean.getQuantity() > catalogProduct.getQuantity()");

        // doPost sottrae senza ricontrollare atomicamente
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