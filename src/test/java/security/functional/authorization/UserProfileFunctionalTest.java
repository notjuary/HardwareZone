package security.functional.authorization;

import Controller.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per UserProfile.
 */
@DisplayName("UserProfile - Test funzionale di sicurezza")
class UserProfileFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        when(support.request.getRequestDispatcher("WEB-INF/admin/profile-admin.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("WEB-INF/user/profile-user.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: UserProfile con admin reindirizza a profile-admin")
    void testAdmin() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());

        UserProfile servlet = new UserProfile();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("WEB-INF/admin/profile-admin.jsp");
    }

    @Test
    @DisplayName("SECURITY: UserProfile con utente normale reindirizza a profile-user")
    void testUtenteNormale() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        UserProfile servlet = new UserProfile();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("WEB-INF/user/profile-user.jsp");
    }

    @Test
    @DisplayName("SECURITY: UserProfile con utente null causa NPE (finding)")
    void testUtenteNull() {
        when(support.session.getAttribute("user")).thenReturn(null);

        UserProfile servlet = new UserProfile();
        assertThrows(Exception.class, () ->
                        support.invokeDoGet(servlet, support.request, support.response),
                "NPE atteso");
    }
}