package security.functional.dataprotection;

import Model.PaymentBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test funzionali per PaymentBean.
 * Documenta il FINDING: getter CVV esposto.
 */
@DisplayName("PaymentBean - Test funzionale")
class PaymentBeanFunctionalTest {

    private PaymentBean payment;

    @BeforeEach
    void setUp() {
        payment = new PaymentBean();
    }

    @Test
    @DisplayName("Getter/setter di tutti i campi")
    void testGetSetAll() {
        payment.setOrder(1);
        payment.setCardNumber("1234567890123456");
        payment.setCVV("123");
        payment.setHolder("Mario Rossi");
        payment.setDeadline("2030-12-31");
        payment.setDatePayment("2024-10-05");

        assertThat(payment.getOrder()).isEqualTo(1);
        assertThat(payment.getCardNumber()).isEqualTo("1234567890123456");
        assertThat(payment.getCVV()).isEqualTo("123");
        assertThat(payment.getHolder()).isEqualTo("Mario Rossi");
        assertThat(payment.getDeadline()).isEqualTo("2030-12-31");
        assertThat(payment.getDatePayment()).isEqualTo("2024-10-5");
    }

    @Test
    @DisplayName("setDatePayment senza argomenti imposta oggi")
    void testDatePaymentNow() {
        payment.setDatePayment();
        assertThat(payment.getDatePayment()).isNotNull();
    }

    @Test
    @DisplayName("FINDING: getter CVV è pubblico (rischio log)")
    void testCvvGetterExposed() {
        payment.setCVV("123");
        assertThat(payment.getCVV())
                .as("FINDING: getCVV() pubblico può far trapelare il CVV nei log")
                .isEqualTo("123");
    }
}