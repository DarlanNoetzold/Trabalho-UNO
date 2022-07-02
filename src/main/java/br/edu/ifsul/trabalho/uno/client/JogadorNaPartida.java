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

            saida.println("pescarIni;8");
            String linha = entrada.readLine();
            pescar(linha.split(";"));
            linha = "";
            while (true) {
                String[] textoSeparado = linha.split(";");
                if(cartasNaMao.size() == 0){
                    saida.println("ganhou;");
                }
                if(textoSeparado[0].equals("pescar")) {
                    pescar(textoSeparado);
                    saida.println("jogada;" + textoSeparado[textoSeparado.length - 2] + ';' + textoSeparado[textoSeparado.length - 1] + ';');
                }else if(textoSeparado[0].equals("comeca")){
                    System.out.println("Sua vez!");
                    fazerJogada(textoSeparado, saida, teclado, true);
                }else if(textoSeparado[0].equals("jogada")){
                    System.out.println("Sua vez! A carta no topo é: " + textoSeparado[1] + " " + textoSeparado[2]);
                    fazerJogada(textoSeparado, saida, teclado, false);
                }else if(initFlag){
                    System.out.println("No aguardo para o primeiro jogador apertar enter...");
                    teclado.readLine();
                    initFlag=false;
                    saida.println("inicio");
                }

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
        for (int i = 1; i < textoSeparado.length - 3; i = i + 2)
            cartasNaMao.add(new Carta(textoSeparado[i], textoSeparado[i + 1]));
    }

    private void fazerJogada(String[] textoSeparado, PrintStream saida, BufferedReader teclado, boolean ehInicial) throws IOException {
        boolean pescou = false;
        mostrarCartasNaMao();
        System.out.println("Digite o código da carta: ");
        int index = 0;
        do {
            try {
                String codCard = teclado.readLine();
                if(codCard.equals("pescar")){
                    pescou=true;
                    saida.println("pescarIni;2");
                    break;
                }else {
                    index = Integer.parseInt(codCard);
                    if (index > cartasNaMao.size()) {
                        System.out.println("Você não tem tantas cartas assim! Digite novamente..");
                        index = -1;
                    }
                }
            } catch (Exception e) {
                System.out.println("Valor Inválido! >:( Digite um número positivo!");
                index = -1;
            }
        } while (index == -1);
        if(!pescou) {
            Carta cartaEscolhida =  cartasNaMao.get(index);

            if (cartaEscolhida.getNome().equals("PescaQuatro") || cartaEscolhida.getNome().equals("EscolheCor")) {
                if(!ehInicial) textoSeparado[1] = cartaEscolhida.getNome();
                System.out.println("Escolha cor: ");
                cartaEscolhida.setCor(teclado.readLine());
                while(true) {
                    if (cartaEscolhida.getCor().equals("amarelo") || cartaEscolhida.getCor().equals("verde") || cartaEscolhida.getCor().equals("vermelho") || cartaEscolhida.getCor().equals("azul")) {
                        break;
                    } else {
                        System.out.println("Valor inválido, por favor digite: vermelho, verde, amarelo ou azul");
                        cartaEscolhida.setCor(teclado.readLine());
                    }
                }
            }

            if (ehInicial || textoSeparado[1].equals(cartaEscolhida.getNome()) || textoSeparado[2].equals(cartaEscolhida.getCor())) {
                saida.println("jogada;" + cartasNaMao.get(index).toString());
                cartasNaMao.remove(index);
            } else {
                System.out.println("Esta carta não pode ser jogada!");
                fazerJogada(textoSeparado, saida, teclado, false);
            }
        }
    }

    public static void main(String[] args) {
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
