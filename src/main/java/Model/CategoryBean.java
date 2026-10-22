package Model;

import java.io.Serializable;

public class CategoryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nome;

    public CategoryBean() {
        // Costruttore vuoto richiesto dalla specifica JavaBeans per l'istanziazione via reflection
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}