package protocol_g6_package;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

import android.util.Log;

import static protocol_g6_package.rotas.ENDEREÇO_NULO;
import static protocol_g6_package.rotas.CENTRAL;
import static protocol_g6_package.rotas.BROADCAST;
import static protocol_g6_package.rotas.MAXNODES;

import setec.g3.communication.rqst;
import setec.g3.communication.rspns;

public class Protocol_G6 {

    public final static boolean DEBUG_FINAL = false;
    public final static boolean DEBUG = false;
    public final static boolean DEBUG_DETAILED = false;
    
    public static final int NO_SOCKET = -300;

    public static final String LOOPBACK_ADDRESS = "127.0.0.1";
    // Versão do protocolo 
    public static final int VERSAO_PROTOCOLO = 0;
    // True se a aplicação é do backend e false se a aplicação é do android
    boolean application;
    // Identificação da porta do socket
    final int socketPort;
    // Identificação do aparelho
    int deviceID;
    // No em que o protocolo está a ser exectuado
    no node;
    //Just to know when to stop
    boolean running = true;
    // socket for the protocol's requests and the application's responses
    private Socket SocketPro;
    // socket for the protocol's responses and the application's requests
    private Socket SocketApp;
    // Tempo Espera por um Ack ou RRply (em ms)
    long t_espera = 15000;
    // Nº retransmissoes definidas
    int numTransm = 3;
    // caso Backend: array que guarda os Socket IDs dos nós com GSM
    public static int[][] tabelaSocketID = new int[256][2];

    private ObjectInputStream AppIn;
    private ObjectInputStream ProIn;
    private ObjectOutputStream AppOut;
    private ObjectOutputStream ProOut;

    // Inicializa o protocolo e o nó
    public Protocol_G6(boolean machine, int port, byte system_id) {

        application = machine;
        socketPort = port;
        deviceID = (int) system_id;
        running = true;

        // Inicializa nó e as suas respectivas estruturas de informação
        rotas[] tabelaRota = rotas.criaTabela();
        tabelaValidade[] tabValidade = tabelaValidade.criaTabelaValidade();
        Queue<filaEspera> filaout = filaEspera.criarFila(); // Fila de pacotes para saída
        Queue<filaEspera> filain = filaEspera.criarFila();  // Fila de pacotes para ser tratados 
        Queue<filaEspera> fila_dados_in = filaEspera.criarFila(); //Fila para dados recebido do Android
        List<waitingPackets> fila_espera_ACK_RRply = waitingPackets.criarLista(); //Fila para passar pacotes que enviamos para Android e aguardam Ack ou RRply
        Queue<filaEspera> fila_dados_out = filaEspera.criarFila(); // Fila de dados para sair
        Queue<Byte> scktQueue = new LinkedList<>();
        Queue<rqst> filaFrag = new LinkedList<>();

        // Cria um novo nó e adiciona a rota para si mesmo
        node = new no(filaout, filain, fila_dados_in, fila_espera_ACK_RRply, fila_dados_out, filaFrag, tabelaRota, tabValidade, ((int) system_id), scktQueue);
        // adicionaEntradaTabela(rotas[] tabela,int dest,int nHop,int hCount)
        rotas.adicionaEntradaTabela(node.tabRota, ((int) system_id), -1, 0);

        if (machine) {
            for (int i = 0; i < 256; i++) {
                tabelaSocketID[i][0] = NO_SOCKET;
            }
        }
    }
    /*
     public HashMap<Byte, Byte> getTabelaSocket(){
     return this.tabelaSocketID;
     }
    
     public void setTabelaSocket(byte noID, byte socketID){
     tabelaSocketID.put(noID, socketID);
     }
    
     public void removeTabelaSocketID(byte SocketID){
     tabelaSocketID.remove(SocketID);
     }*/

    // Executa um objecto da classe Protocolo_G6
    @SuppressWarnings("empty-statement")
    public void execute() throws IOException, ClassNotFoundException, InterruptedException {

        /*Open the sockets 127.0.0.1 is the current machine's address*/
        try {
            SocketPro = new Socket(LOOPBACK_ADDRESS, socketPort);
        } catch (IOException ex) {
            Log.d("Protocol_G6","Protocol: Could not open SocketPro!\n");
            return;
        }

        try {
            SocketApp = new Socket(LOOPBACK_ADDRESS, socketPort);
        } catch (IOException ex) {
            Log.d("Protocol_G6","Protocol: Could not open SocketApp!\n");
            return;
        }

        /* Transform socket streams into ObjectStreams in order to send objects
         instead of bytes (Out's must always be created first and flushed!)*/
        try {
            AppOut = new ObjectOutputStream(SocketApp.getOutputStream());
            ProOut = new ObjectOutputStream(SocketPro.getOutputStream());
            AppOut.flush();
            ProOut.flush();
            AppOut.reset();
            ProOut.reset();
            AppIn = new ObjectInputStream(SocketApp.getInputStream());
            ProIn = new ObjectInputStream(SocketPro.getInputStream());

        } catch (IOException ex) {
            Log.d("Protocol_G6","Could not create Object Streams!\n");
            return;
        }

        // Thread responsável por estar à escuta dos requests do SocketApp          
        ReadAppSocket AndroidRequests = new ReadAppSocket(Protocol_G6.this);
        new Thread(AndroidRequests).start();

        // Thread responsável por analisar as filas onde são postos os 
        // pacotes/dados enviados pelo SocketApp, e actuar em conformidade
        ReadQueues checkQeues = new ReadQueues(Protocol_G6.this);
        new Thread(checkQeues).start();

        //ReadFilaOneAtTime checkFilaOneAtTime = new ReadFilaOneAtTime(Protocol_G6.this);
        //new Thread(checkFilaOneAtTime).start();
    }

    /*public class ReadFilaOneAtTime implements Runnable {

     public Protocol_G6 threadProtocol;

     public ReadFilaOneAtTime(Protocol_G6 protocol) {
     this.threadProtocol = protocol;
     }

     public void run() {
     boolean existeInfo=false;
     if( !(this.threadProtocol.node.filaFragmentacao.isEmpty())){
     if (!(this.threadProtocol.node.fila_espera_ACK_RRply.isEmpty())){
     for (int i=0;i<this.threadProtocol.node.fila_espera_ACK_RRply.size();i++){
     if (pacote.getTypePacote(waitingPackets.verElementoLista
     (this.threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket)==0){
     existeInfo=true;
     }
     }
                    
     if (!existeInfo){
     this.threadProtocol.node.flag_frag=false;
     try {
     this.threadProtocol.AppOut.writeObject(this.threadProtocol.node.filaFragmentacao.peek());
     this.threadProtocol.node.filaFragmentacao.remove();
     } catch (IOException ex) {
     Logger.getLogger(Protocol_G6.class.getName()).log(Level.SEVERE, null, ex);
     }
     }
     }
     }
     }
     }
     */
    public class ReadAppSocket implements Runnable {

        public Protocol_G6 threadProtocol;

        public ReadAppSocket(Protocol_G6 protocol) {
            this.threadProtocol = protocol;
        }

        public void run() {
            rqst request;
            rspns response;

            if (!threadProtocol.application) {
                ///////////////// Caso Dispositivo Android  /////////////////
                while (true) {
                    try {
                        request = (rqst) threadProtocol.AppIn.readObject();

                        if (DEBUG) {
                            //Log.d("Protocol_G6","Protocolo do nó " + this.threadProtocol.deviceID + " recebeu um request da aplicação (Request 0x"+Integer.toHexString(request.id)+")");
                            String pac;
                            if ((request.id == ((byte) 0x00)) || (request.id == ((byte) 0x55))) {
                                pac = pacote.byteArray2binaryString(request.packet);
                            }
                            //Log.d("Protocol_G6","Descrição do pacote: ");
                            //pacote.imprimePacote(pac);
                            //String msg = pacote.binaryStringToText(pacote.getDadosPacote(pacote.byteArray2binaryString(request.packet)));
                            //Log.d("Protocol_G6","Pacote: " + pac);
                            //Log.d("Protocol_G6","Msg do Pacote: " + msg);
                        }

                        switch (request.id) {

                            case (byte) 0x00: // Aplicação recebe um pacote e envia para o protocolo
                                /*if (threadProtocol.node.nodeIdentification==3){
                                 Log.d("Protocol_G6","%%%%%% PACOTE");
                                 pacote.imprimePacote(pacote.byteArray2binaryString(request.packet));
                                 }*/
                                filaEspera.adicionarElementoFila(threadProtocol.node.filain,
                                        pacote.byteArray2binaryString(request.packet), 0);
                                response = new rspns((byte) 0x00);
                                //Log.d("Protocol_G6","Entrei 1");
                                threadProtocol.AppOut.writeObject(response);
                                threadProtocol.node.recebePacote();
                                break;

                            case (byte) 0x33:

                                if (request.spec == 0x00) {
                                    threadProtocol.node.GSM = true;
                                    rotas.adicionaEntradaTabela(threadProtocol.node.tabRota, CENTRAL, CENTRAL, 1);
                                    if (false) {
                                        Log.d("Protocol_G6","Nó " + this.threadProtocol.deviceID + " ganhou GSM");
                                    }
                                } else if (request.spec == 0x11) {
                                    threadProtocol.node.GSM = false;
                                    rotas.removeEntradaTabela(threadProtocol.node.tabRota, CENTRAL);
                                    if (DEBUG) {
                                        Log.d("Protocol_G6","Nó " + this.threadProtocol.deviceID + " perdeu GSM");
                                    }
                                }
                                response = new rspns((byte) 0x00);
                                //Log.d("Protocol_G6","Entrei 2");
                                threadProtocol.AppOut.writeObject(response);
                                //Log.d("Protocol_G6","GSM= "+ threadProtocol.node.GSM);
                                break;

                            case (byte) 0x55: // Aplicação envia para o protocolo dados que quer enviar

                                //Log.d("Protocol_G6","Nó " + threadProtocol.node.nodeIdentification + " recebeu mensagem do Android para enviar para a central: "
                                //+ pacote.binaryStringToText(pacote.byteArray2binaryString(request.packet)));
                                int numFrag = node.calculaNumFragmentos(pacote.byteArray2binaryString(request.packet));
                                if (numFrag == 0) {
                                    numFrag = 1;
                                }
                                //Log.d("Protocol_G6","num frag: " + numFrag);
                               /* filaEspera.adicionarElementoFila(threadProtocol.node.fila_dados_in,
                                 pacote.byteArray2binaryString(request.packet), 0);
                                 response = new rspns((byte) 0x00);
                                 //Log.d("Protocol_G6","Entrei 3");
                                 threadProtocol.AppOut.writeObject(response);
                                 threadProtocol.node.preparaPacote();*/

                                if (numFrag == 1 || ((threadProtocol.node.flag_frag == 1) && numFrag > 1)) {
                                    filaEspera.adicionarElementoFila(threadProtocol.node.fila_dados_in,
                                            pacote.byteArray2binaryString(request.packet), 0);
                                    response = new rspns((byte) 0x00);
                                    threadProtocol.AppOut.writeObject(response);
                                    //threadProtocol.node.flag_frag=0;
                                    //Log.d("Protocol_G6","posso preparar");
                                    threadProtocol.node.preparaPacote();

                                } else {//if ((threadProtocol.node.flag_frag == 0) && numFrag > 1) { 
                                    //Log.d("Protocol_G6","Entrei com frag tenho de esperar");
                                    threadProtocol.node.filaFragmentacao.add(request);
                                    response = new rspns((byte) 0x00);
                                    threadProtocol.AppOut.writeObject(response);
                                }

                                break;

                            default:
                                if (DEBUG) {
                                    Log.d("Protocol_G6","Request Android Inválido " + request.id + " " + request.spec);
                                }
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        Logger.getLogger(Protocol_G6.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            } ////////////////////// Caso Backend //////////////////////
            else if (threadProtocol.application) {

                while (true) {
                    try {
                        request = (rqst) threadProtocol.AppIn.readObject();

                        switch (request.id) {
                            case 0x00:
                                //adiciona o endereco do no (key) e o SocketID (value) na Tabela
                               /*if(threadProtocol.tabelaSocketID.containsValue(request.spec) == false){
                                 Log.d("Protocol_G6","entrei false");
                                 Log.d("Protocol_G6","request.spec: " + request.spec);
                                 threadProtocol.setTabelaSocket(request.packet[7], request.spec);
                                 }*/

                                //?
                                //threadProtocol.tabelaSocketID[pacote.getIDPacote(pacote.byteArray2binaryString(request.packet))]=(int) request.spec;
                                if (threadProtocol.tabelaSocketID[pacote.getSourcePacote(pacote.byteArray2binaryString(request.packet))][0] == NO_SOCKET) {
                                    threadProtocol.tabelaSocketID[pacote.getSourcePacote(pacote.byteArray2binaryString(request.packet))][0] = (int) request.spec;
                                    threadProtocol.tabelaSocketID[pacote.getSourcePacote(pacote.byteArray2binaryString(request.packet))][1] = pacote.getOrigSourcePacote(pacote.byteArray2binaryString(request.packet));
                                }

                                threadProtocol.tabelaSocketID[pacote.getOrigSourcePacote(pacote.byteArray2binaryString(request.packet))][0] = (int) request.spec;
                                threadProtocol.tabelaSocketID[pacote.getOrigSourcePacote(pacote.byteArray2binaryString(request.packet))][1] = pacote.getOrigSourcePacote(pacote.byteArray2binaryString(request.packet));

                                //adiciona socketID à fila com os sockets todos usados
                                //threadProtocol.node.socketQueue.add(request.spec);
                                filaEspera.adicionarElementoFila(threadProtocol.node.filain,
                                        pacote.byteArray2binaryString(request.packet), 0);
                                //Log.d("Protocol_G6","Backhend recebe no protocolo: ");
                                //pacote.imprimePacote(pacote.byteArray2binaryString(request.packet));
                                response = new rspns((byte) 0x00);
                                //Log.d("Protocol_G6","Entrei 4");
                                threadProtocol.AppOut.writeObject(response);
                                threadProtocol.node.recebePacote();

                                break;
                            case 0x44:
                                //Backend informa que perdeu ligaçao com SocketID
                                //threadProtocol.removeTabelaSocketID(request.spec);
                                for (int i = 0; i < 256; i++) {
                                    if (threadProtocol.tabelaSocketID[i][0] == (int) request.spec) {
                                        threadProtocol.tabelaSocketID[i][0] = NO_SOCKET;
                                    }
                                }
                                response = new rspns((byte) 0x00);
                                //Log.d("Protocol_G6","Entrei 5");
                                threadProtocol.AppOut.writeObject(response);
                                break;
                            case 0x55:
                                //Backend quer enviar mensagem
                                //Log.d("Protocol_G6","Nó 1 recebeu mensagem do Backend para enviar para o nó " + ((int) request.spec)
                                //+ ": " + pacote.binaryStringToText(pacote.byteArray2binaryString(request.packet)));

                                int numFrag = node.calculaNumFragmentos(pacote.byteArray2binaryString(request.packet));
                                if (numFrag == 0) {
                                    numFrag = 1;
                                }

                                /* filaEspera.adicionarElementoFila(threadProtocol.node.fila_dados_in,
                                 pacote.byteArray2binaryString(request.packet), 0);
                                 response = new rspns((byte) 0x00);
                                 //Log.d("Protocol_G6","Entrei 3");
                                 threadProtocol.AppOut.writeObject(response);
                                 threadProtocol.node.preparaPacote();*/
                                if (numFrag == 1 || ((threadProtocol.node.flag_frag == 1) && numFrag > 1)) {
                                    //Log.d("Protocol_G6","posso avançar");
                                    filaEspera.adicionarElementoFila(threadProtocol.node.fila_dados_in,
                                            pacote.byteArray2binaryString(request.packet), (int) request.spec);
                                    response = new rspns((byte) 0x00);
                                    threadProtocol.AppOut.writeObject(response);
                                    //threadProtocol.node.flag_frag=0;
                                    //Log.d("Protocol_G6","Entrei prepara!!!");
                                    threadProtocol.node.preparaPacote();

                                } else { //if ((threadProtocol.node.flag_frag == 0) && numFrag > 1) { 
                                    //Log.d("Protocol_G6","Tenho de esperar");
                                    threadProtocol.node.filaFragmentacao.add(request);
                                    response = new rspns((byte) 0x00);
                                    threadProtocol.AppOut.writeObject(response);
                                }

                                //filaEspera.adicionarElementoFila(threadProtocol.node.fila_dados_in,
                                        //pacote.byteArray2binaryString(request.packet), request.spec);
                                // Log.d("Protocol_G6",);
                                //filaEspera.imprimirCabeçaFilaEspera(threadProtocol.node.fila_dados_in);
                                response = new rspns((byte) 0x00);
                                threadProtocol.AppOut.writeObject(response);
                                //rotas.adicionaEntradaTabela(threadProtocol.node.tabRota, 0, 30, 5);
                                //threadProtocol.node.preparaPacote();
                                //filaEspera.imprimirCabeçaFilaEspera(threadProtocol.node.filaout);
                                //pacote.imprimePacote(filaEspera.getDados(threadProtocol.node.filaout));
                                break;
                            default:
                                Log.d("Protocol_G6","Request Backend Inválido" + request.id + " " + request.spec);
                                break;
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        Logger.getLogger(Protocol_G6.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    public class ReadQueues implements Runnable {

        public Protocol_G6 threadProtocol;

        public ReadQueues(Protocol_G6 protocol) {
            this.threadProtocol = protocol;
        }

        public void run() {
            while (true) {
                rqst request;
                rspns response;
                ////////////// Caso Android ////////////////
                if (!threadProtocol.application) {

                    synchronized (threadProtocol.node.fila_espera_ACK_RRply) {
                        //Log.d("Protocol_G6","tamanho fila espera: "+ threadProtocol.node.fila_espera_ACK_RRply.size());
                        if (threadProtocol.node.filaout.size() != 0 || threadProtocol.node.fila_espera_ACK_RRply.size() != 0
                                || threadProtocol.node.fila_dados_out.size() != 0) {

                            try {
                                //Log.d("Protocol_G6","destFilaOut: "+filaEspera.getDest(threadProtocol.node.filaout));
                                //Log.d("Protocol_G6","nodeID: "+threadProtocol.node.nodeIdentification);

                                // Se destino é o próprio nó
                                if (filaEspera.getDest(threadProtocol.node.fila_dados_out) == threadProtocol.node.nodeIdentification) {

                                    byte id = 0x22;
                                    byte spec = 0;
                                    byte[] packet = pacote.binaryString2byteArray(
                                            filaEspera.verElementoCabeçaFila(threadProtocol.node.fila_dados_out).dados);
                                    request = new rqst(id, spec, packet);
                                   
                                    threadProtocol.ProOut.writeObject(request);
                                    response = null;
                                    response = (rspns) threadProtocol.ProIn.readObject();
                                    if (response != null) {
                                        filaEspera.removerElementoFila(threadProtocol.node.fila_dados_out);
                                    }
                                }

                                // Se destino é central e nó tem GSM
                                if (filaEspera.getDest(threadProtocol.node.filaout) == CENTRAL && threadProtocol.node.GSM == true) {
                                    //Log.d("Protocol_G6","Entrei com gsm: " + node.nodeIdentification);
                                    //pacote.imprimePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados);
                                    byte id = 0x11;
                                    byte spec = 0x00; //GSM
                                    byte[] packet = pacote.binaryString2byteArray(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados);
                                    request = new rqst(id, spec, packet);
                                    //Log.d("Protocol_G6","Entrei 8");
                                    threadProtocol.ProOut.writeObject(request);
                                    response = null;
                                    response = (rspns) threadProtocol.ProIn.readObject();
                                    //Log.d("Protocol_G6","Response: "+response.id);
                                    if (response.id != (byte) 0xFF) {
                                        if ((pacote.getTypePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados) == 0 && pacote.getSourcePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados) == threadProtocol.node.nodeIdentification)
                                                || (pacote.getTypePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados) == 1 && pacote.getSourcePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados) == threadProtocol.node.nodeIdentification)) {
                                            //Passa Pacote enviado para Fila espera de Ack ou RRPly e remove pacote da filaout
                                            waitingPackets.adicionarElementoLista(threadProtocol.node.fila_espera_ACK_RRply, filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados);
                                            filaEspera.removerElementoFila(threadProtocol.node.filaout);
                                            //Incrementa numero Transmissoes
                                            waitingPackets.incrementaTransmissoes(threadProtocol.node.fila_espera_ACK_RRply, threadProtocol.node.fila_espera_ACK_RRply.size() - 1);
                                            //Mete tempo atual do sistema para contabilizar quanto tempo demora Ack ou RRply
                                            //Se Ack ou RRply não chegarem dentro de um x de tempo retransmite
                                            waitingPackets.setTime(threadProtocol.node.fila_espera_ACK_RRply, threadProtocol.node.fila_espera_ACK_RRply.size() - 1);
                                        } else {
                                            filaEspera.removerElementoFila(threadProtocol.node.filaout);
                                        }
                                    } else {
                                        this.threadProtocol.node.GSM = false;
                                    }

                                }

                                //Destino é central e nó não tem GSM ou dest!=central
                                if ((filaEspera.getDest(threadProtocol.node.filaout) == CENTRAL && threadProtocol.node.GSM == false) || (filaEspera.getDest(threadProtocol.node.filaout) != CENTRAL && filaEspera.getDest(threadProtocol.node.filaout) >= 0)) {

                                    //Log.d("Protocol_G6","filaEspera.getDest(threadProtocol.node.filaout:"+ filaEspera.getDest(threadProtocol.node.filaout));
                                    //Log.d("Protocol_G6","filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados:"+ filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados);
                                    byte id = 0x11;
                                    byte spec = 0x11; //Radio
                                    //Log.d("Protocol_G6","Entrei sem gsm no: " + node.nodeIdentification);
                                    // Log.d("Protocol_G6","tamanho dadosout funcao"+threadProtocol.node.filaout.size());
                                    //Log.d("Protocol_G6","dados: "+ filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados);
                                    byte[] packet = pacote.binaryString2byteArray(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados);
                                    request = new rqst(id, spec, packet);
                                    //Log.d("Protocol_G6","Entrei 9");
                                    threadProtocol.ProOut.writeObject(request);
                                    response = null;
                                    response = (rspns) threadProtocol.ProIn.readObject();
                                    if (response != null) {
                                        if ((pacote.getTypePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados) == 0 && pacote.getSourcePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados) == threadProtocol.node.nodeIdentification)
                                                || (pacote.getTypePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados) == 1 && pacote.getSourcePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados) == threadProtocol.node.nodeIdentification)) {
                                            //Passa Pacote enviado para Fila espera de Ack ou RRPly e remove pacote da filaout

                                            waitingPackets.adicionarElementoLista(threadProtocol.node.fila_espera_ACK_RRply, filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados);
                                            //if(threadProtocol.node.nodeIdentification==3)
                                            // waitingPackets.imprimeFila(threadProtocol.node.fila_espera_ACK_RRply);
                                            filaEspera.removerElementoFila(threadProtocol.node.filaout);
                                            //Incrementa numero Transmissoes
                                            waitingPackets.incrementaTransmissoes(threadProtocol.node.fila_espera_ACK_RRply, threadProtocol.node.fila_espera_ACK_RRply.size() - 1);
                                            //Mete tempo atual do sistema para contabilizar quanto tempo demora Ack ou RRply
                                            //Se Ack ou RRply não chegarem dentro de um x de tempo retransmite
                                            waitingPackets.setTime(threadProtocol.node.fila_espera_ACK_RRply, threadProtocol.node.fila_espera_ACK_RRply.size() - 1);
                                        } else {
                                            filaEspera.removerElementoFila(threadProtocol.node.filaout);
                                        }
                                    }

                                }

                                for (int i = 0; i < threadProtocol.node.fila_espera_ACK_RRply.size(); i++) {

                                    //Situação em que espera de um Ack ou RRply excede tempo espera, logo retransmite
                                    if (i < threadProtocol.node.fila_espera_ACK_RRply.size()) {

                                        if (waitingPackets.hasIndex(threadProtocol.node.fila_espera_ACK_RRply, i) && waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i) != null) {
                                            if ((System.currentTimeMillis() - waitingPackets.getTime(threadProtocol.node.fila_espera_ACK_RRply, i) > t_espera)) {
                                                if (waitingPackets.hasIndex(threadProtocol.node.fila_espera_ACK_RRply, i) == true) {

                                                    if ((pacote.getSourcePacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket) == threadProtocol.node.nodeIdentification)) {
                                        //waitingPackets.imprimirElementoLista(threadProtocol.node.fila_espera_ACK_RRply,i);

                                                        //Destino é central e nó tem GSM
                                                        if (waitingPackets.getDest(threadProtocol.node.fila_espera_ACK_RRply, i) == CENTRAL && threadProtocol.node.GSM == true) {
                                                            byte id = 0x11;
                                                            byte spec = 0x00; //GSM
                                                            String newPckt = waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket;
                                                            byte[] packet = pacote.binaryString2byteArray(pacote.setNewPacketID(newPckt));
                                                            request = new rqst(id, spec, packet);

                                                            //Log.d("Protocol_G6","no "+node.nodeIdentification +"Retransmite pacote do tipo " + pacote.getTypePacote(newPckt));
                                                            threadProtocol.ProOut.writeObject(request);
                                                            response = null;
                                                            response = (rspns) threadProtocol.ProIn.readObject();
                                                            if (response.id != (byte) 0xFF) {
                                                                //Incrementa numero Transmissoes
                                                                waitingPackets.incrementaTransmissoes(threadProtocol.node.fila_espera_ACK_RRply, i);
                                                                //Mete tempo atual do sistema para contabilizar quanto tempo demora Ack ou RRply
                                                                //Se Ack ou RRply não chegarem dentro de um x de tempo retransmite
                                                                waitingPackets.setTime(threadProtocol.node.fila_espera_ACK_RRply, i);
                                                            } else {
                                                                this.threadProtocol.node.GSM = false;
                                                            }

                                                        } //Destino é central e nó não tem GSM ou dest!=central
                                                        else if (((waitingPackets.getDest(threadProtocol.node.fila_espera_ACK_RRply, i) == CENTRAL && threadProtocol.node.GSM == false)) || (waitingPackets.getDest(threadProtocol.node.fila_espera_ACK_RRply, i) != CENTRAL && waitingPackets.getDest(threadProtocol.node.fila_espera_ACK_RRply, i) >= 0)) {
                                                            if (waitingPackets.hasIndex(threadProtocol.node.fila_espera_ACK_RRply, i)) {
                                                                if (waitingPackets.hasIndex(threadProtocol.node.fila_espera_ACK_RRply, i) && pacote.getNextHopPacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket) != 0) {
                                                                    byte id = 0x11;
                                                                    byte spec = 0x11;
                                                                    String newPckt = waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket;
                                                                    byte[] packet = pacote.binaryString2byteArray(pacote.setNewPacketID(newPckt));//Radio

                                                                    //Log.d("Protocol_G6","no "+node.nodeIdentification +"Retransmite pacote do tipo " + pacote.getTypePacote(newPckt));
                                                                    request = new rqst(id, spec, packet);
                                                                    response = null;
                                                                    //Log.d("Protocol_G6","Entrei 11");
                                                                    threadProtocol.ProOut.writeObject(request);
                                                                    response = (rspns) threadProtocol.ProIn.readObject();
                                                                    if (response != null) {
                                                                        //Incrementa numero Transmissoes
                                                                        waitingPackets.incrementaTransmissoes(threadProtocol.node.fila_espera_ACK_RRply, i);
                                                                        //Mete tempo atual do sistema para contabilizar quanto tempo demora Ack ou RRply
                                                                        //Se Ack ou RRply não chegarem dentro de um x de tempo retransmite
                                                                        waitingPackets.setTime(threadProtocol.node.fila_espera_ACK_RRply, i);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Verifica se Ntrnsmissoes é maior do que o definido
                                    if (waitingPackets.getNumTransmissoes(threadProtocol.node.fila_espera_ACK_RRply, i) >= numTransm && pacote.getSourcePacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket) == threadProtocol.node.nodeIdentification) {
                                        //Elimina rota
                                        rotas.removeEntradaTabela(threadProtocol.node.tabRota, waitingPackets.getDest(threadProtocol.node.fila_espera_ACK_RRply, i));
                                        //Passa dados e destino novamente para fila_dados_in para encapsular de novo
                                        String data = pacote.getDadosPacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket);
                                        int destino = pacote.getDestinoPacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket);
                                        //Retira pacote da fila_espera_Ack
                                        filaEspera.adicionarElementoFila(threadProtocol.node.fila_dados_in, data, destino);
                                        //Log.d("Protocol_G6"," no "+node.nodeIdentification + " Retransmite pacote do tipo " + pacote.getTypePacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket));

                                        if (pacote.getTypePacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket) == pacote.rReqPacketType) {
                                            threadProtocol.node.espera_rrply = 0; //Para permitir crar RReq outra vez
                                        }
                                        waitingPackets.removerElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i);
                                        threadProtocol.node.preparaPacote();

                                    }
                                }

                            } catch (IOException | ClassNotFoundException ex) {
                                Logger.getLogger(Protocol_G6.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    }
                } //caso Backend
                else if (threadProtocol.application) {
                    
                    synchronized (threadProtocol.node.fila_espera_ACK_RRply) {

                    if (threadProtocol.node.filaout.size() != 0) {
                        
                                
                        try {

                        

                                byte id = 0x11;//Envio de pacotes
                                byte spec = (byte) NO_SOCKET;
                                byte[] packet;
                                int nexthop = 0;

                            //Log.d("Protocol_G6","size: " + threadProtocol.node.filaout.size());
                            /*Log.d("Protocol_G6","O meu destino é "+filaEspera.verElementoCabeçaFila(
                                 threadProtocol.node.filaout).destino+" e o conteúdo da tabela GSM é "+threadProtocol.tabelaSocketID[filaEspera.verElementoCabeçaFila(
                                 threadProtocol.node.filaout).destino]);*/
                                //Se nao houver o socketID para o no para onde
                                //se quer enviar o pacote
                                // Primeiro tenta enviar directamente para o nó de destino (pode ser que esse nó tenha ganho GSM entretanto
                            /*for (int i = 0; i < 256; i++) {
                                 Log.d("Protocol_G6","valor da tabela pos "+ i + " valor: " + tabelaSocketID[i]);
                                 }*/
                                if (threadProtocol.tabelaSocketID[filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).destino][0] != NO_SOCKET) {
                                    spec = (byte) threadProtocol.tabelaSocketID[filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).destino][0];
                                    nexthop = threadProtocol.tabelaSocketID[filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).destino][1];
                                    //Log.d("Protocol_G6","entrei 1");
                                } // Segundo tenta enviar pelo next hop do pacote 
                                else if (threadProtocol.tabelaSocketID[pacote.getNextHopPacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados)][0] != NO_SOCKET) {
                                    spec = (byte) threadProtocol.tabelaSocketID[pacote.getNextHopPacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados)][0];
                                    nexthop = threadProtocol.tabelaSocketID[pacote.getNextHopPacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados)][1];
                                    //Log.d("Protocol_G6","entrei 2");
                                } // Se nenhum dos casos anteriores funcionar tenta enviar por um socket qualquer que esteja activo
                                else {
                                    for (int i = 0; i < 256; i++) {
                                        if (threadProtocol.tabelaSocketID[i][0] != NO_SOCKET) {
                                            spec = (byte) threadProtocol.tabelaSocketID[i][0];
                                            nexthop = threadProtocol.tabelaSocketID[i][1];
                                            //Log.d("Protocol_G6","entrei 3");
                                            break;
                                        }
                                    }
                                }

                                //Log.d("Protocol_G6","size2: " + threadProtocol.node.filaout.size());
                                filaEspera auxElement = filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout);

                                /*
                                 auxElement.destino=filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).destino;
                                 auxElement.nRetransmissoes=filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).nRetransmissoes;
                                 auxElement.time=filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).time;
                                 auxElement.socketID=filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).socketID;
                                 */
                                int k = pacote.getNextHopPacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados);

                                //Log.d("Protocol_G6","size2: " + threadProtocol.node.filaout.size());
                                if (k == 0) { // Dados que o backend quer enviar para um Android
                                    // Aqui vai-se alterar somente o nextHop do pacote de acordo com o socket id disponível
                                    auxElement.dados = pacote.setNewHeadersPacote(filaEspera.verElementoCabeçaFila(
                                            threadProtocol.node.filaout).dados, pacote.getTTLPacote(filaEspera.verElementoCabeçaFila(
                                                            threadProtocol.node.filaout).dados), pacote.getOrigSourcePacote(filaEspera.verElementoCabeçaFila(
                                                            threadProtocol.node.filaout).dados), nexthop);

                                } else {
                                    auxElement.dados = filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados;
                                }
                                /*if(!threadProtocol.tabelaSocketID.containsKey(
                                 (byte)filaEspera.verElementoCabeçaFila(
                                 threadProtocol.node.filaout).destino))
                                 {
                                 Log.d("Protocol_G6","Contenho Key");
                                 spec = threadProtocol.node.socketQueue.peek();//envia pelo ultimo Socket por onde recebeu
                                 }
                                 else{
                                 spec = threadProtocol.tabelaSocketID.get(
                                 (byte)filaEspera.verElementoCabeçaFila(
                                 threadProtocol.node.filaout).destino);
                                 }*/
                                //Log.d("Protocol_G6","SPEC: " + spec);
                                if (spec != (byte) NO_SOCKET) {
                                    if (pacote.getTypePacote(filaEspera.verElementoCabeçaFila(
                                            threadProtocol.node.filaout).dados) == 1) {
                                        // Apaga se for Route Request
                                        filaEspera.removerElementoFila(threadProtocol.node.filaout);
                                    } else {
                                        //Log.d("Protocol_G6","pacote alterado:");
                                        //pacote.imprimePacote(auxElement.dados);
                                        packet = pacote.binaryString2byteArray(auxElement.dados);
                                        //Log.d("Protocol_G6","");
                                        //pacote.imprimePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados);
                                        //Log.d("Protocol_G6","\n SPEC: "+spec+"   id: "+id);
                                        //Log.d("Protocol_G6","");

                                        rqst req = new rqst(id, spec, packet);
                                        //Log.d("Protocol_G6","Entrei 12");
                                        threadProtocol.ProOut.writeObject(req);

                                        response = (rspns) threadProtocol.ProIn.readObject();
                                        //Log.d("Protocol_G6","response: "+response.id);
                                        if (response.id == 0xEE) {
                                            for (int i = 0; i < 256; i++) {
                                                if (threadProtocol.tabelaSocketID[i][0] == ((int) spec)) {
                                                    threadProtocol.tabelaSocketID[i][0] = NO_SOCKET;
                                                    break;
                                                }
                                            }
                                        } else { // MODIFICAÇÃO: Só remove se não receber 0xEE.
                                            // remove pacote da fila de espera de pacotes filaout
                                            // adiciona pacote à fila de Acks
                                            // Log.d("Protocol_G6","Fila out: ");
                                            //pacote.imprimePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados);
                                            if ((pacote.getTypePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados) == 0 && pacote.getSourcePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados) == threadProtocol.node.nodeIdentification)
                                                    || (pacote.getTypePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados) == 1 && pacote.getSourcePacote(filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados) == threadProtocol.node.nodeIdentification)) {
                                                waitingPackets.adicionarElementoLista(threadProtocol.node.fila_espera_ACK_RRply, filaEspera.verElementoCabeçaFila(threadProtocol.node.filaout).dados);

                                                waitingPackets.incrementaTransmissoes(threadProtocol.node.fila_espera_ACK_RRply, threadProtocol.node.fila_espera_ACK_RRply.size() - 1);
                                                //Mete tempo atual do sistema para contabilizar quanto tempo demora Ack ou RRply
                                                //Se Ack ou RRply não chegarem dentro de um x de tempo retransmite
                                                waitingPackets.setTime(threadProtocol.node.fila_espera_ACK_RRply, threadProtocol.node.fila_espera_ACK_RRply.size() - 1);
                                            }
                                            filaEspera.removerElementoFila(threadProtocol.node.filaout);
                                        }
                                    }
                                } else {
                                    //Log.d("Protocol_G6","Não existem sockets disponíveis na central");
                                }
                           
                        } catch (IOException ex) {
                            Logger.getLogger(Protocol_G6.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(Protocol_G6.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    //Enviar dados para backend
                    if (threadProtocol.node.fila_dados_out.size() != 0 && (filaEspera.getDest(threadProtocol.node.fila_dados_out) == threadProtocol.node.nodeIdentification)) {
                        try {
                            byte id = 0x22;
                            byte spec = 0x00;
                            byte[] packet;

                            packet = pacote.binaryString2byteArray(
                                    filaEspera.verElementoCabeçaFila(
                                            threadProtocol.node.fila_dados_out).dados);
                            rqst req = new rqst(id, spec, packet);
                            //Log.d("Protocol_G6","Entrei 13");
                            threadProtocol.ProOut.writeObject(req);
                            filaEspera.removerElementoFila(threadProtocol.node.fila_dados_out);
                        } catch (IOException ex) {
                            Logger.getLogger(Protocol_G6.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    

                        if (threadProtocol.node.fila_espera_ACK_RRply.size() > 0) {

                            for (int i = 0; i < threadProtocol.node.fila_espera_ACK_RRply.size(); i++) {

                                if (waitingPackets.hasIndex(threadProtocol.node.fila_espera_ACK_RRply, i)) {
                                    if (threadProtocol.node.fila_espera_ACK_RRply.get(i) != null) {//waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i) != null) {
                                        //Situação em que espera de um Ack ou RRply excede tempo espera, logo retransmite
                                        if (!threadProtocol.node.fila_espera_ACK_RRply.isEmpty()
                                                && (System.currentTimeMillis() - waitingPackets.getTime(threadProtocol.node.fila_espera_ACK_RRply, i) > t_espera) && waitingPackets.hasIndex(threadProtocol.node.fila_espera_ACK_RRply, i) && pacote.getSourcePacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket) == threadProtocol.node.nodeIdentification) {

                                            if (waitingPackets.hasIndex(threadProtocol.node.fila_espera_ACK_RRply, i) == true) {
                                                if (pacote.getNextHopPacote(threadProtocol.node.fila_espera_ACK_RRply.get(i).waitPacket) != 0) {

                                                    //waitingPackets.imprimeFila(threadProtocol.node.fila_espera_ACK_RRply);
                                                    byte id = 0x11;
                                                    byte spec = -120;
                                                    String newPckt = waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket;
                                                    byte[] packet = pacote.binaryString2byteArray(pacote.setNewPacketID(newPckt));
                                                    
                                                    //Log.d("Protocol_G6","no " +node.nodeIdentification +" retransmite pacote do tipo: " + pacote.getSourcePacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket));
                                                            
                                                    //////
                                                    if (threadProtocol.tabelaSocketID[waitingPackets.getDest(threadProtocol.node.fila_espera_ACK_RRply, i)][0] != NO_SOCKET) {
                                                        spec = (byte) threadProtocol.tabelaSocketID[waitingPackets.getDest(threadProtocol.node.fila_espera_ACK_RRply, i)][0];
                                                    } else if (threadProtocol.tabelaSocketID[pacote.getNextHopPacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket)][0] != NO_SOCKET) {
                                                        spec = (byte) threadProtocol.tabelaSocketID[pacote.getNextHopPacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket)][0];
                                                    } else {
                                                        for (int x = 0; x < 256; x++) {
                                                            if (threadProtocol.tabelaSocketID[x][0] != NO_SOCKET) {
                                                                spec = (byte) threadProtocol.tabelaSocketID[x][0];
                                                                break;
                                                            }
                                                        }
                                                    }

                                    //////
                            /*
                                                     if (!threadProtocol.tabelaSocketID.containsKey(
                                                     (byte) waitingPackets.getDest(threadProtocol.node.fila_espera_ACK_RRply,i))) {
                                                     spec = threadProtocol.node.socketQueue.peek();//envia pelo ultimo Socket por onde recebeu
                                                     } else {
                                                     spec = threadProtocol.tabelaSocketID.get(
                                                     (byte) waitingPackets.getDest(threadProtocol.node.fila_espera_ACK_RRply,i));
                                                     }
                                                     */
                                                    request = new rqst(id, spec, packet);
                                                    try {
                                                        //Log.d("Protocol_G6","Entrei 14");
                                                        threadProtocol.ProOut.writeObject(request);
                                                    } catch (IOException ex) {
                                                        Logger.getLogger(Protocol_G6.class.getName()).log(Level.SEVERE, null, ex);
                                                    }

                                                    try {
                                                        response = (rspns) threadProtocol.ProIn.readObject();

                                                        if (response.id != (byte) 0xEE) {
                                                            //Incrementa numero Transmissoes
                                                            waitingPackets.incrementaTransmissoes(threadProtocol.node.fila_espera_ACK_RRply, i);
                                                            //Mete tempo atual do sistema para contabilizar quanto tempo demora Ack ou RRply
                                                            //Se Ack ou RRply não chegarem dentro de um x de tempo retransmite
                                                            waitingPackets.setTime(threadProtocol.node.fila_espera_ACK_RRply, i);
                                                        } else {
                                                            for (int z = 0; z < 256; z++) {
                                                                if (threadProtocol.tabelaSocketID[z][0] == ((int) spec)) {
                                                                    threadProtocol.tabelaSocketID[z][0] = NO_SOCKET;
                                                                    break;
                                                                }
                                                            }
                                                        }

                                                    } catch (IOException ex) {
                                                        Logger.getLogger(Protocol_G6.class.getName()).log(Level.SEVERE, null, ex);
                                                    } catch (ClassNotFoundException ex) {
                                                        Logger.getLogger(Protocol_G6.class.getName()).log(Level.SEVERE, null, ex);
                                                    }

                                                }

                                                // Verifica se Ntrnsmissoes é maior do que o definido
                                                if (waitingPackets.getNumTransmissoes(threadProtocol.node.fila_espera_ACK_RRply, i) >= numTransm && pacote.getSourcePacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket) == threadProtocol.node.nodeIdentification) {
                                    //Elimina rota
                                                    //rotas.removeEntradaTabela(threadProtocol.node.tabRota, waitingPackets.getDest(threadProtocol.node.fila_espera_ACK_RRply, i));
                                                    //Passa dados e destino novamente para fila_dados_in para encapsular de novo
                                                    String data = pacote.getDadosPacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket);
                                                    int destino = pacote.getDestinoPacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket);
                                                    //Retira pacote da fila_espera_Ack
                                                    filaEspera.adicionarElementoFila(threadProtocol.node.fila_dados_in, data, destino);
                                                        
                                                    //Log.d("Protocol_G6","retransmite pacote do tipo: " + pacote.getSourcePacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket));
                                                    //Log.d("Protocol_G6","no " +node.nodeIdentification + " retransmite pacote do tipo: " + pacote.getSourcePacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket));

                                                    
                                    //if (pacote.getTypePacote(waitingPackets.verElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i).waitPacket) == pacote.rReqPacketType) {
                                                    //    threadProtocol.node.espera_rrply = 0; //Para permitir crar RReq outra vez
                                                    //}
                                                    waitingPackets.removerElementoLista(threadProtocol.node.fila_espera_ACK_RRply, i);
                                                    threadProtocol.node.preparaPacote();

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    
                    }
                }
            }
        }
    }

    public void Stop() {
        running = false;
    }
}
