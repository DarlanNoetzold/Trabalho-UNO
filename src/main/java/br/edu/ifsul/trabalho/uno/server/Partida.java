package br.edu.ifsul.trabalho.uno.server;

import br.edu.ifsul.trabalho.uno.model.Baralho;
import br.edu.ifsul.trabalho.uno.model.Carta;
import br.edu.ifsul.trabalho.uno.model.Jogador;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 *
 * @author 20201PF.CC0149
 */
public class Partida extends Thread {

    private static List<Jogador> jogadores;

    private Jogador jogador;

    private Socket conexao;

    private String nomeJogador;

    private boolean ehInicial;

    private boolean jaPescou = false;
    
    private List<Jogador> ranking;
    public Partida(Jogador j, boolean ehInicial) {
        this.ehInicial = ehInicial;
        conexao = j.getSocket();
        jogador = j;
    }

    public void run() {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(jogador.getSocket().getInputStream()));
            PrintStream saida = new PrintStream(jogador.getSocket().getOutputStream());
            jogador.setSaida(saida);
            
            nomeJogador = entrada.readLine();

            if (nomeJogador == null) {
                return;
            }
            jogador.setNome(nomeJogador);

            String linha = entrada.readLine();
            while (linha != null && !(linha.trim().equals(""))) {
                StringBuilder streamToSend = new StringBuilder();
                String[] textoSeparado = linha.split(";");

                if(textoSeparado.length >= 1) {
                    if (Objects.equals(textoSeparado[0], "pescarIni")) {
                        streamToSend.append("pescar;");
                        for (Carta c : pescar(Integer.parseInt(textoSeparado[1]))) streamToSend.append(c.toString());
                        sendToJogador(saida, streamToSend.toString(), jogador);
                    }else if (Objects.equals(textoSeparado[0], "pescar")) {
                        streamToSend.append("pescar;");
                        for (Carta c : pescar(Integer.parseInt(textoSeparado[1]))) streamToSend.append(c.toString());
                        sendToNext(saida, streamToSend.toString(), jogador);
                    }else if (Objects.equals(textoSeparado[0], "jogada")) {
                        System.out.println(Arrays.toString(textoSeparado));
                        if ("inverte".equals(textoSeparado[1])) {
                            streamToSend.append("jogada;");
                            streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
                            inverte();
                            if (jogadores.size() == 2) sendToNext(saida, streamToSend.toString(), bloqueia());
                            else sendToNext(saida, streamToSend.toString(), jogador);
                            jogador.setPontuacao(jogador.getPontuacao() + 20);
                        } else if ("bloquear".equals(textoSeparado[1])) {
                            streamToSend.append("jogada;");
                            streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
                            sendToNext(saida, streamToSend.toString(), bloqueia());
                            jogador.setPontuacao(jogador.getPontuacao() + 20);
                        } else if ("maisDois".equals(textoSeparado[1])) {
                            if (!jaPescou) {
                                jogador.setPontuacao(jogador.getPontuacao() + 20);
                                streamToSend.append("pescar;");
                                for (Carta c : pescar(2)) streamToSend.append(c.toString());
                                streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
                                sendToNext(saida, streamToSend.toString(), jogador);
                            } else {
                                streamToSend.append("jogada;");
                                streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
                                sendToNext(saida, streamToSend.toString(), jogador);
                            }
                            jaPescou = !jaPescou;
                        } else if ("PescaQuatro".equals(textoSeparado[1])) {
                            if (!jaPescou) {
                                jogador.setPontuacao(jogador.getPontuacao() + 20);
                                streamToSend.append("pescar;");
                                for (Carta c : pescar(4)) streamToSend.append(c.toString());
                                streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
                                sendToNext(saida, streamToSend.toString(), jogador);
                            } else {
                                streamToSend.append("jogada;");
                                streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
                                sendToNext(saida, streamToSend.toString(), jogador);
                            }
                            jaPescou = !jaPescou;
                        } else if ("EscolheCor".equals(textoSeparado[1])) {
                            streamToSend.append("jogada;");
                            streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
                            sendToNext(saida, streamToSend.toString(), jogador);
                            jogador.setPontuacao(jogador.getPontuacao() + 50);
                        } else {
                            streamToSend.append("jogada;");
                            streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
                            sendToNext(saida, streamToSend.toString(), jogador);
                            jogador.setPontuacao(jogador.getPontuacao() + Integer.parseInt(textoSeparado[1]));
                        }

                    }else if(Objects.equals(textoSeparado[0], "ganhou")){
                        atualizaRanking();
                        System.out.println("O jogador " + jogador.getNome() + " ganhou!!" + " Pontição de: " + jogador.getPontuacao());
                        sendToAll(saida, "","ganhou;O jogador " + jogador.getNome() + " ganhou!!" + " Pontição de: " + jogador.getPontuacao() + ";");
                        ranking.forEach(jogador -> System.out.println(jogador.getNome()));

                        break;
                    }else if(Objects.equals(textoSeparado[0], "PescarEsc")){
                        if(!jaPescou) {
                            streamToSend.append("PescarEsc;");
                            for (Carta c : pescar(Integer.parseInt(textoSeparado[1]))) streamToSend.append(c.toString());
                            streamToSend.append(textoSeparado[2]).append(";").append(textoSeparado[3]).append(";");
                            sendToJogador(saida, streamToSend.toString(), jogador);
                        }else{
                            streamToSend.append("jogada;");
                            streamToSend.append(textoSeparado[2]).append(";").append(textoSeparado[3]).append(";");
                            sendToNext(saida, streamToSend.toString(), jogador);
                        }
                        jaPescou = !jaPescou;
                    }else if(ehInicial){
                        streamToSend.append("jogada;");
                        for (Carta c : pescar(1)) streamToSend.append(c.toString());
                        sendToJogador(saida, streamToSend.toString(), jogador);
                    }
                }
                linha = entrada.readLine();
            }

            jogadores.remove(saida);
            conexao.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    private void sendToJogador(PrintStream saida, String linha, Jogador jogador){
        PrintStream jogada = (PrintStream) jogador.getSaida();
        if (jogada == saida) {
            jogada.println(linha);
        }
    }
    private void sendToAll(PrintStream saida, String acao,
                           String linha) throws IOException {
        Iterator<Jogador> iter = jogadores.iterator();
        while (iter.hasNext()) {
            Jogador outroCliente = iter.next();
            PrintStream chat = (PrintStream) outroCliente.getSaida();
            if (chat != saida) {
                chat.println(jogador.getNome() + linha);
            }
        }
    }
    private void sendToNext(PrintStream saida, String linha, Jogador jogador) throws IOException {
        if(jogadores.indexOf(jogador)+1 == jogadores.size()){
            PrintStream jogada = (PrintStream) jogadores.get(0).getSaida();
            jogada.println(linha);
        }else{
            PrintStream jogada = (PrintStream) jogadores.get(jogadores.indexOf(jogador)+1).getSaida();
            jogada.println(linha);
        }
    }
    private List<Carta> pescar(int quant){
        List<Carta> cartasPescadas = new ArrayList<>();
        for(int i =0; i<quant;i++) cartasPescadas.add(Baralho.baralho.pop());
        return cartasPescadas;
    }
    private void inverte(){
        Collections.reverse(jogadores);
    }
    private Jogador bloqueia(){
        if(jogadores.indexOf(jogador)+1 == jogadores.size()){
            return jogadores.get(0);
        }else{
            return jogadores.get(jogadores.indexOf(jogador)+1);
        }
    }
    private void atualizaRanking(){
        ranking = jogadores;
        ranking.sort((a, b) -> Integer.compare(b.getPontuacao(), a.getPontuacao()));
    }

    public static void main(String args[]) {
        jogadores = new ArrayList<Jogador>();
        Baralho baralho = new Baralho();
        try {
            ServerSocket s = new ServerSocket(2222);
            int cont=0;
            while (true) {
                System.out.print("Esperando alguem se conectar...");
                Socket conexao = s.accept();
                Jogador jogador = new Jogador(new ArrayList<>());
                jogador.setId(conexao.getRemoteSocketAddress().toString());
                jogador.setIp(conexao.getRemoteSocketAddress().toString());
                jogador.setSocket(conexao);

                jogadores.add(jogador);

                System.out.println(" Conectou!: " + conexao.getRemoteSocketAddress());
                if(cont==0) {
                    Thread t = new Partida(jogador, true);
                    t.start();
                }else{
                    Thread t = new Partida(jogador, false);
                    t.start();
                }
                cont++;


            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

}