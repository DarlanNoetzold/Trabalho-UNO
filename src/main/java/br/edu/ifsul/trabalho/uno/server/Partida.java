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
 * Classe resposável pelo controle do servidor e das regras do jogo.
 * @author Darlan Noetzold
 * @author Jakelyny Sousa de Araujo
 * @version 1.0
 */
public class Partida extends Thread {

    private static List<Jogador> jogadores;

    private final Jogador jogador;

    private final Socket conexao;

    private final boolean ehInicial;

    private boolean jaPescou = false;

    private static boolean jogoComecou = false;
    
    private List<Jogador> ranking;
    public Partida(Jogador j, boolean ehInicial) {
        this.ehInicial = ehInicial;
        conexao = j.getSocket();
        jogador = j;
    }
    /**
     * <p> Metodo que ira executar a Thread, responsavel por enviar uma jogada para a partida e
     * </p>
     * @since 1.0
     */
    public void run() {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(jogador.getSocket().getInputStream()));
            PrintStream saida = new PrintStream(jogador.getSocket().getOutputStream());
            jogador.setSaida(saida);

            String nomeJogador = entrada.readLine();

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
                        sendToJogador(saida, streamToSend.toString());
                    }else if (Objects.equals(textoSeparado[0], "pescar")) {
                        streamToSend.append("pescar;");
                        for (Carta c : pescar(Integer.parseInt(textoSeparado[1]))) streamToSend.append(c.toString());
                        sendToNext(streamToSend.toString(), jogador);
                    }else if (Objects.equals(textoSeparado[0], "jogada")) {
                        System.out.println(Arrays.toString(textoSeparado));
                        if ("inverte".equals(textoSeparado[1])) {
                            inverter(streamToSend, textoSeparado);
                        } else if ("bloquear".equals(textoSeparado[1])) {
                            bloquear(streamToSend, textoSeparado);
                        } else if ("maisDois".equals(textoSeparado[1])) {
                            maisDois(streamToSend, textoSeparado);
                        } else if ("PescaQuatro".equals(textoSeparado[1])) {
                            pescarQuatro(streamToSend, textoSeparado);
                        } else if ("EscolheCor".equals(textoSeparado[1])) {
                            escolheCor(streamToSend, textoSeparado);
                        } else {
                            jogadaNormal(streamToSend, textoSeparado);
                        }
                    }else if(Objects.equals(textoSeparado[0], "ganhou")){
                        jogadorGanhou(saida);
                        break;
                    }else if(Objects.equals(textoSeparado[0], "PescarEsc")){
                        pescarPorEscolha(streamToSend, saida, textoSeparado, jogador);
                    }else if(ehInicial){
                        streamToSend.append("jogada;");
                        for (Carta c : pescar(1)) streamToSend.append(c.toString());
                        sendToJogador(saida, streamToSend.toString());
                        jogoComecou = true;
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

    /**
     * <p> Metodo que implementa a logica da carta Inverte.
     * </p>
     * @param streamToSend - jogada que sera enviada para o proximo jogador.
     * @param textoSeparado - jogada que chegou de algum jogador
     * @since 1.0
     */
    private void inverter(StringBuilder streamToSend, String[] textoSeparado) throws IOException {
        streamToSend.append("jogada;");
        streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
        Collections.reverse(jogadores);
        if (jogadores.size() == 2) sendToNext(streamToSend.toString(), bloqueia());
        else sendToNext(streamToSend.toString(), jogador);
        jogador.setPontuacao(jogador.getPontuacao() + 20);
    }

    /**
     * <p> Metodo que implementa a logica da carta Bloqueia.
     * </p>
     * @param streamToSend - jogada que sera enviada para o proximo jogador.
     * @param textoSeparado - jogada que chegou de algum jogador
     * @since 1.0
     */
    private void bloquear(StringBuilder streamToSend, String[] textoSeparado) throws IOException {
        streamToSend.append("jogada;");
        streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
        sendToNext(streamToSend.toString(), bloqueia());
        jogador.setPontuacao(jogador.getPontuacao() + 20);
    }

    /**
     * <p> Metodo que implementa a logica da carta Mais Dois.
     * </p>
     * @param streamToSend - jogada que sera enviada para o proximo jogador.
     * @param textoSeparado - jogada que chegou de algum jogador
     * @since 1.0
     */
    private void maisDois(StringBuilder streamToSend, String[] textoSeparado) throws IOException{
        if (!jaPescou) {
            jogador.setPontuacao(jogador.getPontuacao() + 20);
            streamToSend.append("pescar;");
            for (Carta c : pescar(2)) streamToSend.append(c.toString());
            streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
            sendToNext(streamToSend.toString(), jogador);
        } else {
            streamToSend.append("jogada;");
            streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
            sendToNext(streamToSend.toString(), jogador);
        }
        jaPescou = !jaPescou;
    }

    /**
     * <p> Metodo que implementa a logica da carta Pesca Quatro.
     * </p>
     * @param streamToSend - jogada que sera enviada para o proximo jogador.
     * @param textoSeparado - jogada que chegou de algum jogador
     * @since 1.0
     */
    private void pescarQuatro(StringBuilder streamToSend, String[] textoSeparado) throws IOException{
        if (!jaPescou) {
            jogador.setPontuacao(jogador.getPontuacao() + 20);
            streamToSend.append("pescar;");
            for (Carta c : pescar(4)) streamToSend.append(c.toString());
            streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
            sendToNext(streamToSend.toString(), jogador);
        } else {
            streamToSend.append("jogada;");
            streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
            sendToNext(streamToSend.toString(), jogador);
        }
        jaPescou = !jaPescou;
    }

    /**
     * <p> Metodo que implementa a logica da carta Escolhe Cor.
     * </p>
     * @param streamToSend - jogada que sera enviada para o proximo jogador.
     * @param textoSeparado - jogada que chegou de algum jogador
     * @since 1.0
     */
    private void escolheCor(StringBuilder streamToSend, String[] textoSeparado) throws IOException{
        streamToSend.append("jogada;");
        streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
        sendToNext(streamToSend.toString(), jogador);
        jogador.setPontuacao(jogador.getPontuacao() + 50);
    }

    /**
     * <p> Metodo que implementa a logica de uma carta normal numerica.
     * </p>
     * @param streamToSend - jogada que sera enviada para o proximo jogador.
     * @param textoSeparado - jogada que chegou de algum jogador
     * @since 1.0
     */
    private void jogadaNormal(StringBuilder streamToSend, String[] textoSeparado) throws IOException{
        streamToSend.append("jogada;");
        streamToSend.append(textoSeparado[1]).append(";").append(textoSeparado[2]).append(";");
        sendToNext(streamToSend.toString(), jogador);
        jogador.setPontuacao(jogador.getPontuacao() + Integer.parseInt(textoSeparado[1]));
    }

    /**
     * <p> Metodo que implementa a logica da pesca de carta, quando o jogador escolhe pescar.
     * </p>
     * @param streamToSend - jogada que sera enviada para o proximo jogador.
     * @param textoSeparado - jogada que chegou de algum jogador.
     * @param saida - stream usada para enviar uma mensagem para o jogador.
     * @param jogador - jogador que vai receber as cartas pescadas.
     * @since 1.0
     */
    private void pescarPorEscolha(StringBuilder streamToSend, PrintStream saida, String[] textoSeparado, Jogador jogador) throws IOException{
        if(!jaPescou) {
            streamToSend.append("PescarEsc;");
            for (Carta c : pescar(Integer.parseInt(textoSeparado[1]))) streamToSend.append(c.toString());
            streamToSend.append(textoSeparado[2]).append(";").append(textoSeparado[3]).append(";");
            sendToJogador(saida, streamToSend.toString());
        }else{
            streamToSend.append("jogada;");
            streamToSend.append(textoSeparado[2]).append(";").append(textoSeparado[3]).append(";");
            sendToNext(streamToSend.toString(), jogador);
        }
        jaPescou = !jaPescou;
    }

    /**
     * <p> Metodo que implementa a logica de quando um jogador ganha.
     * </p>
     * @param saida - Stream usada para enviar a mensagem do ganhador para os jogadores.
     * @since 1.0
     */
    private void jogadorGanhou(PrintStream saida) throws IOException{
        atualizaRanking();
        System.out.println("O jogador " + jogador.getNome() + " ganhou!!" + " Pontuação de: " + jogador.getPontuacao());
        sendToAll(saida,"ganhou;O jogador " + jogador.getNome() + " ganhou!!" + " Pontuação de: " + jogador.getPontuacao() + ";");
        System.out.println("Ranking: ");
        ranking.forEach(jogador -> System.out.println(jogador.getNome()));
    }

    /**
     * <p> Metodo que envia uma mensagem para o proprio jogador.
     * </p>
     * @param saida - stream usado para enviar a mensagem para o jogador.
     * @param linha - mensagem que sera enviada para o jogador.
     * @since 1.0
     */
    private void sendToJogador(PrintStream saida, String linha){
        saida.println(linha);
    }

    /**
     * <p> Metodo que envia uma mensagem para o todos os jogadores.
     * </p>
     * @param saida - stream usado para enviar a mensagem para os jogadores.
     * @param linha - mensagem que sera enviada para os jogadores.
     * @since 1.0
     */
    private void sendToAll(PrintStream saida, String linha) throws IOException {
        for (Jogador outroCliente : jogadores) {
            PrintStream chat = (PrintStream) outroCliente.getSaida();
            if (chat != saida) chat.println(jogador.getNome() + linha);
        }
    }

    /**
     * <p> Metodo que envia uma mensagem para o proximo jogador.
     * </p>
     * @param jogador - usado para encontrar o proximo jogador.
     * @param linha - mensagem que sera enviada para o proximo jogador.
     * @since 1.0
     */
    private void sendToNext(String linha, Jogador jogador) throws IOException {
        if(jogadores.indexOf(jogador)+1 == jogadores.size()){
            PrintStream jogada = (PrintStream) jogadores.get(0).getSaida();
            jogada.println(linha);
        }else{
            PrintStream jogada = (PrintStream) jogadores.get(jogadores.indexOf(jogador)+1).getSaida();
            jogada.println(linha);
        }
    }

    /**
     * <p> Metodo que pesca as cartas do baralho.
     * </p>
     * @param quant - quantidade a ser pescada
     * @return lista de cartas
     * @since 1.0
     */
    private List<Carta> pescar(int quant){
        List<Carta> cartasPescadas = new ArrayList<>();
        for(int i =0; i<quant;i++) cartasPescadas.add(Baralho.baralho.pop());
        return cartasPescadas;
    }

    /**
     * <p> Metodo que bloqueia o proximo jogador.
     * </p>
     * @return proximo jogador.
     * @since 1.0
     */
    private Jogador bloqueia(){
        if(jogadores.indexOf(jogador)+1 == jogadores.size()){
            return jogadores.get(0);
        }else{
            return jogadores.get(jogadores.indexOf(jogador)+1);
        }
    }

    /**
     * <p> Metodo que atualiza o ranking dos jogadores.
     * </p>
     * @since 1.0
     */
    private void atualizaRanking(){
        ranking = jogadores;
        ranking.sort((a, b) -> Integer.compare(b.getPontuacao(), a.getPontuacao()));
    }

    /**
     * <p> Metodo que inicia o servidor e inicia cada socket da partida.
     * </p>
     * @since 1.0
     */
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
                if(jogoComecou){
                    conexao.close();
                    System.out.println("O jogo já começou!");
                    break;
                }
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