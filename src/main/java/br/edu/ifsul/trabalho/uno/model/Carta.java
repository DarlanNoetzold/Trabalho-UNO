/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.edu.ifsul.trabalho.uno.model;

/**
 * Classe modelo de uma carta de UNO.
 * @author Darlan Noetzold
 * @author Jakelyny Sousa de Araujo
 * @version 1.0
 */
public class Carta {
    
    private String nome;
    private String cor;

    public Carta(String nome, String cor) {
        this.nome = nome;
        this.cor = cor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }


    @Override
    public String toString() {
        return nome + ";" + cor+";";
    }
}
