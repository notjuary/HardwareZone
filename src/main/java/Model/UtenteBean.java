package Model;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class UtenteBean {

    private int id;
    private String nome, cognome, email, password, telefono, citta, provincia, codice_postale, indirizzo;
    private GregorianCalendar data_nascita, data_registrazione;
    private boolean stato;

    public UtenteBean() {}

    public UtenteBean(String nome, String cognome, String email, String password, String telefono, String citta,
                      String provincia, String codice_postale, String indirizzo, GregorianCalendar data_nascita) {

        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.citta = citta;
        this.provincia = provincia;
        this.codice_postale = codice_postale;
        this.indirizzo = indirizzo;
        this.stato = true;
        this.data_registrazione = new GregorianCalendar();
        this.data_nascita = data_nascita;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCodice_postale() {
        return codice_postale;
    }

    public void setCodice_postale(String codice_postale) {
        this.codice_postale = codice_postale;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getDataNascita() {

        return data_nascita.get(Calendar.YEAR) + "-"
                + ((data_nascita.get(Calendar.MONTH) + 1)) + "-"
                + (data_nascita.get(Calendar.DAY_OF_MONTH));
    }

    public void setDataNascita(String data) {

        String[] data_splitted = data.split("-");

        int anno = Integer.parseInt(data_splitted[0]);
        int mese = Integer.parseInt(data_splitted[1]);
        int giorno = Integer.parseInt(data_splitted[2]);

        mese--;

        this.data_nascita = new GregorianCalendar(anno, mese, giorno);
    }

    public String getDataRegistrazione() {

        return data_registrazione.get(Calendar.YEAR) + "-"
                + ((data_registrazione.get(Calendar.MONTH) + 1)) + "-"
                + (data_registrazione.get(Calendar.DAY_OF_MONTH));
    }

    public void setDataRegistrazione(String data) {

        String[] data_splitted = data.split("-");

        int anno = Integer.parseInt(data_splitted[0]);
        int mese = Integer.parseInt(data_splitted[1]);
        int giorno = Integer.parseInt(data_splitted[2]);

        mese--;

        this.data_registrazione = new GregorianCalendar(anno, mese, giorno);
    }

    public boolean isActive() {
        return stato;
    }

    public void setStato(boolean stato) {
        this.stato = stato;
    }
}