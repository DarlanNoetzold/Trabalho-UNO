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
                }else if(textoSeparado[0].equals("jogada") || textoSeparado[0].equals("comeca")){
                    System.out.println("Sua vez!");
                    fazerJogada(linha, saida, teclado);
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
        for (int i = cartasNaMao.size(); i >= 0; i--) System.out.print("\t|" + i);
        System.out.println();
        cartasNaMao.forEach(System.out::print);
    }

    private void pescar(String[] textoSeparado){
        for (int i = 1; i < textoSeparado.length - 1; i = i + 2)
            cartasNaMao.add(new Carta(textoSeparado[i], textoSeparado[i + 1]));
    }

    private void fazerJogada(String linha, PrintStream saida, BufferedReader teclado) throws IOException {
        linha = teclado.readLine();
        //TODO
        saida.println(linha);
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
