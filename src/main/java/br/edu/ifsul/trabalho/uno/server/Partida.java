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
    
    private List<Jogador> ranking;
    public Partida(Jogador j, boolean ehInicial) {
        this.ehInicial = ehInicial;
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
                        //TODO
                    }else if(ehInicial){
                        sendToJogador(saida, "comeca;", jogador);
                    }else{
                        //just testing
                        streamToSend.append("jogada");
                        streamToSend.append(linha);
                        sendToAll(saida, " disse: ",streamToSend.toString());
                    }

                }else sendToAll(saida, " disse: ", linha);
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
                chat.println(jogador.getNome() + acao + linha);
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

    private void atualizaPontuacao(){
        //TODO
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