package Model;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class UtenteBean {

    private int id;
    private String nome, cognome, email, password, telefono, citta, provincia, codice_postale, indirizzo, stato, admin;
    private GregorianCalendar data_nascita, data_registrazione;

    public UtenteBean() {}

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

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(password.getBytes(StandardCharsets.UTF_8));
            this.password = String.format("%040x", new BigInteger(1, digest.digest()));

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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

    public String getCodicePostale() {
        return codice_postale;
    }

    public void setCodicePostale(String codice_postale) {
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

        String[] data_split = data.split("-");

        int anno = Integer.parseInt(data_split[0]);
        int mese = Integer.parseInt(data_split[1]);
        int giorno = Integer.parseInt(data_split[2]);

        mese--;

        this.data_nascita = new GregorianCalendar(anno, mese, giorno);
    }

    public String getDataRegistrazione() {

        return data_registrazione.get(Calendar.YEAR) + "-"
                + ((data_registrazione.get(Calendar.MONTH) + 1)) + "-"
                + (data_registrazione.get(Calendar.DAY_OF_MONTH));
    }

    public void setDataRegistrazione(String data) {

        String[] data_split = data.split("-");

        int anno = Integer.parseInt(data_split[0]);
        int mese = Integer.parseInt(data_split[1]);
        int giorno = Integer.parseInt(data_split[2]);

        mese--;

        this.data_registrazione = new GregorianCalendar(anno, mese, giorno);
    }

    public void setDataRegistrazione() {

        this.data_registrazione = new GregorianCalendar();
    }

    public String isActive() {
        return stato;
    }

    public void setStato(String stato) {

        this.stato = stato;
    }

    public String isAdmin() { return admin; }

    public void setAdmin(String admin) {

        this.admin = admin;
    }
}