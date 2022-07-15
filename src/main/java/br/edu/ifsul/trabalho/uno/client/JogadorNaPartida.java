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
 * Classe resposável por controlar cada Jogador e conectar ao servidor.
 * @author Darlan Noetzold
 * @author Jakelyny Sousa de Araujo
 * @version 1.0
 */
public class JogadorNaPartida extends Thread {

    private static boolean done = false;

    private final Socket conexao;
    private boolean initFlag = true;
    private final List<Carta> cartasNaMao;
    public JogadorNaPartida(Socket s) {
        cartasNaMao = new ArrayList<>();
        conexao = s;
    }
    /**
     * <p> Metodo que fica escutando jogadas do servidor, alem de enviar jogadas para o servidor.
     * </p>
     * @since 1.0
     */
    public void run() {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            PrintStream saida = new PrintStream(conexao.getOutputStream());
            BufferedReader teclado= new BufferedReader(new InputStreamReader(System.in));

            saida.println("pescarIni;2;");
            String linha = entrada.readLine();
            pescar(linha.split(";"));
            linha = "";
            while (true) {
                if(linha == null) break;
                String[] textoSeparado = linha.split(";");

                if(textoSeparado[0].equals("PescarEsc")){
                    pescar(textoSeparado);
                    saida.println("PescarEsc;jogada;" + textoSeparado[textoSeparado.length - 2] + ';' + textoSeparado[textoSeparado.length - 1] + ';');
                }else if(textoSeparado[0].equals("pescar")) {
                    pescar(textoSeparado);
                    saida.println("jogada;" + textoSeparado[textoSeparado.length - 2] +'t'+ ';' + textoSeparado[textoSeparado.length - 1] + ';');
                }else if(textoSeparado[0].equals("jogada")){
                    if(textoSeparado.length == 2) System.out.println("Sua vez! A carta no topo é: " + textoSeparado[1]);
                    else System.out.println("Sua vez! A carta no topo é: " + textoSeparado[1] + " " + textoSeparado[2]);
                    fazerJogada(textoSeparado, saida, teclado);
                }else if(initFlag){
                    System.out.println("No aguardo para o primeiro jogador apertar enter...");
                    teclado.readLine();
                    initFlag=false;
                    saida.println("inicio");
                }else if(textoSeparado[0].equals("ganhou")){
                    System.out.println("Alguem ganhou: "+textoSeparado[1]);
                    break;
                }

                if (done) {
                    break;
                }

                linha = entrada.readLine();
            }
        } catch (IOException e) {
            System.out.println("A PARTIDA ACABOU! Foi forçado seu término por algum jogador ou falha do sistema!");
        }
        done = true;
    }
    /**
     * <p> Metodo que mostra as cartas da mao do jogador.
     * </p>
     * @since 1.0
     */
    private void mostrarCartasNaMao(){
        for (int i = 0; i < cartasNaMao.size(); i++) {
            System.out.print(" | "+i + " - " + cartasNaMao.get(i).toString());
        }
        System.out.println("\n");
    }
    /**
     * <p> Metodo que pesca as cartas enviadas do baralho do servidor.
     * </p>
     * @param textoSeparado - cartas enviadas pelo servidor.
     * @since 1.0
     */
    private void pescar(String[] textoSeparado){
        for (int i = 1; i < textoSeparado.length - 2; i = i + 2)
            cartasNaMao.add(new Carta(textoSeparado[i], textoSeparado[i + 1]));
    }
    /**
     * <p> Metodo resposavel pelas escolhas do jogador na jogada.
     * </p>
     * @param textoSeparado - cartas enviadas pelo servidor.
     * @param saida - stream para enviar a jogada.
     * @param teclado - reader que escuta o que o jogador escreve.
     * @since 1.0
     */
    private void fazerJogada(String[] textoSeparado, PrintStream saida, BufferedReader teclado) throws IOException {
        boolean pescou = false;
        boolean falouUno= false;
        mostrarCartasNaMao();
        System.out.println("Digite o código da carta: ");
        int index = -1;
        do {
            try {
                String codCard = teclado.readLine();
                if(codCard.equals("pescar")) {
                    pescou = true;
                    saida.println("PescarEsc;1;" + textoSeparado[1] + ";" + textoSeparado[2] + ";");
                    break;
                }else if(codCard.equals("uno")){
                    falouUno = true;
                    System.out.println("Digite o codigo da carta: ");
                }else if(codCard.equals("sair")){
                    saida.println("sair;");
                    conexao.close();
                    pescou = true;
                    break;
                }else {
                    index = Integer.parseUnsignedInt(codCard);
                    if (index >= cartasNaMao.size()) {
                        System.out.println("Você não tem tantas cartas assim! Digite novamente..");
                        index = -1;
                    }
                }
            } catch (Exception e) {
                System.out.println("Valor Inválido! >:( Digite um número positivo!" + e.getMessage());
                index = -1;
            }
        } while (index == -1);
        if(!pescou) {
            Carta cartaEscolhida =  cartasNaMao.get(index);
            escolheCor(textoSeparado, teclado, cartaEscolhida);

            verificaCarta(textoSeparado, index, cartaEscolhida, saida, teclado, falouUno);
        }
    }

    /**
     * <p> Metodo que verifica a carta escolhida pelo jogador e envia a jogada ou volta para o jogador refazer a jogada.
     * </p>
     * @param textoSeparado - cartas enviadas pelo servidor.
     * @param saida - stream para enviar a jogada.
     * @param teclado - reader que escuta o que o jogador escreve.
     * @param cartaEscolhida - carta escolhida pelo jogador.
     * @param index - indice da carta escolhida.
     * @param falouUno - flag que verifica se o usuario falou uno.
     * @since 1.0
     */
    public void verificaCarta(String[] textoSeparado, int index, Carta cartaEscolhida, PrintStream saida, BufferedReader teclado, boolean falouUno) throws IOException {
        String ultimoCaractere = textoSeparado[1].substring(textoSeparado[1].length() - 1);
        if(ultimoCaractere.equals("t")){
            textoSeparado[1] = textoSeparado[1].substring(0, textoSeparado[1].length()-1);
        }

        if ((textoSeparado.length == 2) || (textoSeparado[1].equals(cartaEscolhida.getNome()) || textoSeparado[2].equals(cartaEscolhida.getCor()))) {
            if(cartasNaMao.size() == 1){
                System.out.println("PARABÉNS VOCÊ GANHOU!!");
                saida.println("ganhou;");
            }else if((cartasNaMao.size() == 2) && !falouUno){
                System.out.println("Você não falou UNO, pesque duas cartas.");
                saida.println("PescarEsc;2;" + cartasNaMao.get(index).toString());
            }else {
                saida.println("jogada;" + cartasNaMao.get(index).toString());
            }
            cartasNaMao.remove(index);

        } else {
            System.out.println("Esta carta não pode ser jogada!");
            fazerJogada(textoSeparado, saida, teclado);
        }
    }

    /**
     * <p> Metodo que implementa a logica da escolha de cor do jogador.
     * </p>
     * @param textoSeparado - cartas enviadas pelo servidor.
     * @param teclado - reader que escuta o que o jogador escreve.
     * @param cartaEscolhida - carta escolhida pelo jogador.
     * @since 1.0
     */
    public void escolheCor(String[] textoSeparado, BufferedReader teclado, Carta cartaEscolhida) throws IOException {
        if (cartaEscolhida.getNome().equals("PescaQuatro") || cartaEscolhida.getNome().equals("EscolheCor")) {
            textoSeparado[1] = cartaEscolhida.getNome();
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
    }
    /**
     * <p> Metodo resposavel pela inicializacao do jogador, conexao com o servidor e criacao da thread que ira escutar e enviar jogadas.
     * </p>
     * @since 1.0
     */
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
