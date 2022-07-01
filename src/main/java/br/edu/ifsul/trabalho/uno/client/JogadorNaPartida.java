/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.edu.ifsul.trabalho.uno.client;

import br.edu.ifsul.trabalho.uno.model.Carta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 20201PF.CC0149
 */
public class JogadorNaPartida extends Thread {

    private static boolean done = false;

    private Socket conexao;

    private boolean initFlag = true;
    private List<Carta> cartasNaMao;
    public JogadorNaPartida(Socket s) {
        cartasNaMao = new ArrayList<>();
        conexao = s;
    }

    public void run() {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            PrintStream saida = new PrintStream(conexao.getOutputStream());
            BufferedReader teclado= new BufferedReader(new InputStreamReader(System.in));

            saida.println("pescarIni;7");
            String linha = entrada.readLine();
            pescar(linha.split(";"));
            linha = "";
            while (true) {
                String[] textoSeparado = linha.split(";");
                if(textoSeparado[0].equals("pescar")) {
                    pescar(textoSeparado);
                    linha = teclado.readLine();
                    saida.println(linha);
                }else if(textoSeparado[0].equals("comeca")){
                    System.out.println("Sua vez!");
                    fazerJogada(textoSeparado, saida, teclado, true);
                }else if(textoSeparado[0].equals("jogada")){
                    System.out.println("Sua vez!");
                    fazerJogada(textoSeparado, saida, teclado, false);
                }else if(initFlag){
                    System.out.println("No aguardo para o primeiro jogador apertar enter...");
                    teclado.readLine();
                    initFlag=false;
                    saida.println("inicio");
                }

                mostrarCartasNaMao();
                if (done) {
                    break;
                }
                if (linha == null) {
                    System.out.println("Conexão encerrada!");
                    break;
                }

                linha = entrada.readLine();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
        done = true;
    }

    private void mostrarCartasNaMao(){
        for (int i = 0; i < cartasNaMao.size(); i++) {
            System.out.print(" | "+i + " - " + cartasNaMao.get(i).toString());
        }
        System.out.println("\n");
    }

    private void pescar(String[] textoSeparado){
        for (int i = 1; i < textoSeparado.length - 1; i = i + 2)
            cartasNaMao.add(new Carta(textoSeparado[i], textoSeparado[i + 1]));
    }

    private void fazerJogada(String[] textoSeparado, PrintStream saida, BufferedReader teclado, boolean ehInicial) throws IOException {
        mostrarCartasNaMao();
        System.out.println("Digite o código da carta: ");
        int index = 0;
        while(true){
            try {
                index = Integer.parseInt(teclado.readLine());
            }catch (Exception e){
                System.out.println("Valor Inválido! >:( Digite um número positivo!");
                index = -1;
            }
            if(index != -1) break;
        }
        Carta cartaEscolhida =  cartasNaMao.get(index);
        if(textoSeparado[1].equals(cartaEscolhida.getNome()) || textoSeparado[2].equals(cartaEscolhida.getCor()) || ehInicial){
            saida.println("jogada;" + cartasNaMao.get(index).toString());
            cartasNaMao.remove(index);
        }else{
            System.out.println("Esta carta não pode ser jogada!");
            fazerJogada(textoSeparado, saida, teclado, false);
        }
    }

    public static void main(String args[]) {
        try {
            Socket conexao = new Socket("127.0.0.1", 2222);
            PrintStream saida = new PrintStream(conexao.getOutputStream());
            BufferedReader teclado= new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Entre com o seu nome: ");
            String meuNome = teclado.readLine();
            saida.println(meuNome);

            Thread t = new JogadorNaPartida(conexao);
            t.start();
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }
}
