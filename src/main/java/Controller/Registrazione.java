package Controller;

import java.io.*;
import java.util.GregorianCalendar;

import Model.UtenteBean;
import Model.UtenteDAO;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "registrazioneServlet", value = "/registrazione-servlet")
public class Registrazione extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        String nome = request.getParameter("nome");
        String cognome = request.getParameter("cognome");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String telefono = request.getParameter("telefono");
        String citta = request.getParameter("citta");
        String provincia = request.getParameter("provincia");
        String codice_postale = request.getParameter("codice-postale");
        String indirizzo = request.getParameter("indirizzo");
        String data = request.getParameter("data-di-nascita");

        String[] data_splitter = data.split("-");
        int anno = Integer.parseInt(data_splitter[0]);
        int mese = Integer.parseInt(data_splitter[1]);
        int giorno = Integer.parseInt(data_splitter[2]);
        mese--;

        GregorianCalendar data_nascita = new GregorianCalendar(anno, mese, giorno);

        UtenteDAO service = new UtenteDAO();
        UtenteBean utente = new UtenteBean(nome,cognome, email, password, telefono, citta, provincia, codice_postale, indirizzo, data_nascita);

        service.doSave(utente);
    }

    public void destroy() {
    }
}