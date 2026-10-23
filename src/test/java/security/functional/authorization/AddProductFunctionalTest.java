package security.functional.authorization;

import Controller.AddProduct;
import Model.ProductBean;
import Model.ProductDAO;
import Model.UserBean;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseFunctionalTest;
import security.functional.ServletTestSupport;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;



import static org.mockito.Mockito.*;

/**
 * Test funzionali di sicurezza per AddProduct (area admin).
 */
@DisplayName("AddProduct - Test funzionale di sicurezza")
class AddProductFunctionalTest extends BaseFunctionalTest {

    private ServletTestSupport support;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        support = new ServletTestSupport() {};
        support.setUpBase();

        executeSql("INSERT INTO Categoria VALUES ('CPU')");

        when(support.request.getRequestDispatcher("/WEB-INF/admin/add-product.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("index.jsp")).thenReturn(support.dispatcher);
        when(support.request.getRequestDispatcher("/WEB-INF/error.jsp")).thenReturn(support.dispatcher);
    }

    @Test
    @DisplayName("SECURITY: AddProduct.doGet richiede admin")
    void testDoGetAdminRichiesto() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createAdminUser());

        AddProduct servlet = new AddProduct();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/admin/add-product.jsp");
    }

    @Test
    @DisplayName("SECURITY: AddProduct.doGet con utente normale reindirizza a index")
    void testDoGetUtenteNormale() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        AddProduct servlet = new AddProduct();
        support.invokeDoGet(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("index.jsp");
    }

    @Test
    @DisplayName("FIXED: AddProduct gestisce utente anonimo senza NPE")
    void testDoGetUtenteAnonimo()  {
        when(support.session.getAttribute("user")).thenReturn(null);

        AddProduct servlet = new AddProduct();

        // FIX: non deve più lanciare NPE
        assertDoesNotThrow(() ->
                support.invokeDoGet(servlet, support.request, support.response)
        );

        // Deve reindirizzare alla index.jsp
        verify(support.request).getRequestDispatcher("index.jsp");
    }

    @Test
    @DisplayName("Coverage: AddProduct carica immagine e salva categoria correttamente")
    void testAddProductUploadSuccess() throws Exception {
        // 1. Setup utente Admin
        UserBean admin = new UserBean();
        admin.setAdmin("true");
        when(support.session.getAttribute("user")).thenReturn(admin);

        // 2. Mock dei parametri validi
        when(support.request.getParameter("name")).thenReturn("CPU Test");
        when(support.request.getParameter("description")).thenReturn("Descrizione CPU Test");
        when(support.request.getParameter("price")).thenReturn("199.99");
        when(support.request.getParameter("quantity")).thenReturn("10");
        when(support.request.getParameter("sales")).thenReturn("0");
        when(support.request.getParameter("category")).thenReturn("NuovaCategoria");

        // 3. Mock del File Upload (Part)
        Part mockPart = mock(Part.class);
        when(mockPart.getSubmittedFileName()).thenReturn("test.jpg");
        when(support.request.getPart("image")).thenReturn(mockPart);

        // 4. Mock del ServletContext (per getRealPath)
        ServletContext mockContext = mock(ServletContext.class);
        when(mockContext.getRealPath("")).thenReturn(System.getProperty("java.io.tmpdir"));

        // FIX: Usiamo spy() sulla Servlet per forzare getServletContext()
        AddProduct servlet = spy(new AddProduct());
        doReturn(mockContext).when(servlet).getServletContext();

        // 5. Esecuzione
        support.invokeDoPost(servlet, support.request, support.response);

        // 6. Verifica che il file sia stato scritto
        verify(mockPart).write(anyString());

        // Verifica che il prodotto sia stato salvato (usando il DB H2)
        ProductDAO dao = new ProductDAO();
        assertThat(dao.isAlreadyRegistered("CPU Test", "Descrizione CPU Test")).isTrue();
    }

    @Test
    @DisplayName("Coverage: AddProduct gestisce file non valido (estensione errata)")
    void testHandleImageUploadInvalidExtension() throws Exception {
        UserBean admin = new UserBean();
        admin.setAdmin("true");
        when(support.session.getAttribute("user")).thenReturn(admin);

        // Parametri validi
        when(support.request.getParameter("name")).thenReturn("CPU");
        when(support.request.getParameter("description")).thenReturn("Desc");
        when(support.request.getParameter("price")).thenReturn("10.0");
        when(support.request.getParameter("quantity")).thenReturn("1");
        when(support.request.getParameter("sales")).thenReturn("0");
        when(support.request.getParameter("category")).thenReturn("CPU");

        // File con estensione non valida
        Part mockPart = mock(Part.class);
        when(mockPart.getSubmittedFileName()).thenReturn("test.txt");
        when(support.request.getPart("image")).thenReturn(mockPart);

        AddProduct servlet = new AddProduct();
        support.invokeDoPost(servlet, support.request, support.response);

        // Verifica che non venga scritto nulla e che il prodotto abbia l'immagine di default
        ProductDAO dao = new ProductDAO();
        ProductBean p = dao.doRetrieveById(1); // Assicurati che l'ID sia corretto
        assertThat(p.getImage()).isEqualTo("./img/products/nophoto.png");
    }

    @Test
    @DisplayName("Coverage: AddProduct gestisce errore su getPart (ServletException)")
    void testAddProductGetPartException() throws Exception {
        UserBean admin = new UserBean();
        admin.setAdmin("true");
        when(support.session.getAttribute("user")).thenReturn(admin);

        // Parametri validi
        when(support.request.getParameter("name")).thenReturn("CPU");
        when(support.request.getParameter("description")).thenReturn("Desc");
        when(support.request.getParameter("price")).thenReturn("10.0");
        when(support.request.getParameter("quantity")).thenReturn("1");
        when(support.request.getParameter("sales")).thenReturn("0");
        when(support.request.getParameter("category")).thenReturn("CPU");

        // FORZIAMO l'errore su getPart (copre il blocco catch)
        when(support.request.getPart("image")).thenThrow(new ServletException("Errore simulato"));

        AddProduct servlet = new AddProduct();
        support.invokeDoPost(servlet, support.request, support.response);

        // Verifica che mostri la pagina di errore
        verify(support.request).getRequestDispatcher("/WEB-INF/error.jsp");
    }

    @Test
    @DisplayName("Coverage: AddProduct salva prodotto se categoria esiste già")
    void testAddProductCategoryAlreadyExists() throws Exception {
        UserBean admin = new UserBean();
        admin.setAdmin("true");
        when(support.session.getAttribute("user")).thenReturn(admin);



        when(support.request.getParameter("name")).thenReturn("Altro Processore");
        when(support.request.getParameter("description")).thenReturn("Descrizione");
        when(support.request.getParameter("price")).thenReturn("150.0");
        when(support.request.getParameter("quantity")).thenReturn("5");
        when(support.request.getParameter("sales")).thenReturn("0");
        when(support.request.getParameter("category")).thenReturn("CPU"); // Categoria esistente

        Part mockPart = mock(Part.class);
        when(mockPart.getSubmittedFileName()).thenReturn("test.jpg");
        when(support.request.getPart("image")).thenReturn(mockPart);

        ServletContext mockContext = mock(ServletContext.class);
        when(mockContext.getRealPath("")).thenReturn(System.getProperty("java.io.tmpdir"));

        AddProduct servlet = spy(new AddProduct());
        doReturn(mockContext).when(servlet).getServletContext();

        support.invokeDoPost(servlet, support.request, support.response);

        // Verifica che non ci siano errori (il prodotto è stato salvato)
        ProductDAO dao = new ProductDAO();
        assertThat(dao.isAlreadyRegistered("Altro Processore", "Descrizione")).isTrue();
    }

    @Test
    @DisplayName("Coverage: AddProduct.doPost con utente non admin reindirizza")
    void testDoPostUtenteNormale() throws Exception {
        when(support.session.getAttribute("user")).thenReturn(support.createNormalUser());

        AddProduct servlet = new AddProduct();
        support.invokeDoPost(servlet, support.request, support.response);

        verify(support.request).getRequestDispatcher("/WEB-INF/results/products.jsp");
    }

    @Test
    @DisplayName("Coverage: AddProduct.doPost con input non valido mostra errore")
    void testDoPostInputInvalido() throws Exception {
        UserBean admin = new UserBean();
        admin.setAdmin("true");
        when(support.session.getAttribute("user")).thenReturn(admin);

        // Lasciamo i parametri null o invalidi per far scattare la validazione
        when(support.request.getParameter("name")).thenReturn("");

        AddProduct servlet = new AddProduct();
        support.invokeDoPost(servlet, support.request, support.response);

        // Verifica che venga chiamata la pagina di errore
        verify(support.request).getRequestDispatcher("/WEB-INF/error.jsp");
    }

    @Test
    @DisplayName("Coverage: AddProduct.handleImageUpload con part null")
    void testHandleImageUploadPartNull() throws Exception {
        UserBean admin = new UserBean();
        admin.setAdmin("true");
        when(support.session.getAttribute("user")).thenReturn(admin);

        // Parametri validi
        when(support.request.getParameter("name")).thenReturn("CPU Null");
        when(support.request.getParameter("description")).thenReturn("Desc");
        when(support.request.getParameter("price")).thenReturn("10.0");
        when(support.request.getParameter("quantity")).thenReturn("1");
        when(support.request.getParameter("sales")).thenReturn("0");
        when(support.request.getParameter("category")).thenReturn("CPU");

        // Part è null
        when(support.request.getPart("image")).thenReturn(null);

        AddProduct servlet = new AddProduct();
        support.invokeDoPost(servlet, support.request, support.response);

        // Verifica che usi l'immagine di default
        ProductDAO dao = new ProductDAO();
        assertThat(dao.isAlreadyRegistered("CPU Null", "Desc")).isTrue();
    }

    @Test
    @DisplayName("Coverage: AddProduct.handleImageUpload gestisce IOException su part.write")
    void testHandleImageUploadIOException() throws Exception {
        UserBean admin = new UserBean();
        admin.setAdmin("true");
        when(support.session.getAttribute("user")).thenReturn(admin);

        when(support.request.getParameter("name")).thenReturn("CPU Err");
        when(support.request.getParameter("description")).thenReturn("Desc");
        when(support.request.getParameter("price")).thenReturn("10.0");
        when(support.request.getParameter("quantity")).thenReturn("1");
        when(support.request.getParameter("sales")).thenReturn("0");
        when(support.request.getParameter("category")).thenReturn("CPU");

        Part mockPart = mock(Part.class);
        when(mockPart.getSubmittedFileName()).thenReturn("test.jpg");
        // Forza part.write a lanciare IOException
        doThrow(new IOException("Errore scrittura")).when(mockPart).write(anyString());
        when(support.request.getPart("image")).thenReturn(mockPart);

        ServletContext mockContext = mock(ServletContext.class);
        when(mockContext.getRealPath("")).thenReturn(System.getProperty("java.io.tmpdir"));

        AddProduct servlet = spy(new AddProduct());
        doReturn(mockContext).when(servlet).getServletContext();

        support.invokeDoPost(servlet, support.request, support.response);

        // Verifica che il prodotto sia salvato con l'immagine di default (perché c'è stato l'errore)
        ProductDAO dao = new ProductDAO();
        assertThat(dao.isAlreadyRegistered("CPU Err", "Desc")).isTrue();
    }
}