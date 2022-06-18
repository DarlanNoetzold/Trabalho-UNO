package br.edu.ifsul.trabalho.uno.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class Baralho {
    public static Stack<Carta> baralho;

    public Baralho() {
        baralho = new Stack<>();
        for(int i = 0; i < 9;i++){
            baralho.add(new Carta(String.valueOf(i), "vermelho"));
            if(i==0) continue;
            baralho.add(new Carta(String.valueOf(i), "vermelho"));
        }
        for(int i = 0; i < 9;i++){
            baralho.add(new Carta(String.valueOf(i), "azul"));
            if(i==0) continue;
            baralho.add(new Carta(String.valueOf(i), "azul"));
        }
        for(int i = 0; i < 9;i++){
            baralho.add(new Carta(String.valueOf(i), "verde"));
            if(i==0) continue;
            baralho.add(new Carta(String.valueOf(i), "verde"));
        }
        for(int i = 0; i < 9;i++){
            baralho.add(new Carta(String.valueOf(i), "amarelo"));
            if(i==0) continue;
            baralho.add(new Carta(String.valueOf(i), "amarelo"));
        }


        for(int i = 0; i< 2; i++){
            baralho.add(new Carta("inverte", "amarelo"));
            baralho.add(new Carta("inverte", "verde"));
            baralho.add(new Carta("inverte", "amarelo"));
            baralho.add(new Carta("inverte", "azul"));

            baralho.add(new Carta("bloquear", "amarelo"));
            baralho.add(new Carta("bloquear", "verde"));
            baralho.add(new Carta("bloquear", "amarelo"));
            baralho.add(new Carta("bloquear", "azul"));

            baralho.add(new Carta("maisDois", "amarelo"));
            baralho.add(new Carta("maisDois", "verde"));
            baralho.add(new Carta("maisDois", "amarelo"));
            baralho.add(new Carta("maisDois", "azul"));
        }

        for(int i = 0; i<4; i++){
            baralho.add(new Carta("PescaQuatro", ""));
        }

        for(int i = 0; i<4; i++){
            baralho.add(new Carta("EscolheCor", ""));
        }

        Collections.shuffle(baralho);
    }
}
