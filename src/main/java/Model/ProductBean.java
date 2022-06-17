package Model;

public class ProductBean {
    private int id, quantita_disponibile, sconto;
    private String nome, descrizione, immagine, categoria;
    private double prezzo;

    public ProductBean() {}

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public int getQuantita_disponibile() {

        return quantita_disponibile;
    }

    public void setQuantita_disponibile(int quantita_disponibile) {

        this.quantita_disponibile = quantita_disponibile;
    }

    public int getSconto() {

        return sconto;
    }

    public void setSconto(int sconto) {

        this.sconto = sconto;
    }

    public String getNome() {

        return nome;
    }

    public void setNome(String nome) {

        this.nome = nome;
    }

    public String getDescrizione() {

        return descrizione;
    }

    public void setDescrizione(String descrizione) {

        this.descrizione = descrizione;
    }

    public String getImmagine() {

        return immagine;
    }

    public void setImmagine(String immagine) {

        this.immagine = immagine;
    }

    public String getCategoria() {

        return categoria;
    }

    public void setCategoria(String categoria) {

        this.categoria = categoria;
    }

    public double getPrezzo() {

        return prezzo;
    }

    public void setPrezzo(double prezzo) {

        this.prezzo = prezzo;
    }
}