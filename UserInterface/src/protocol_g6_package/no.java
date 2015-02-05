package protocol_g6_package;

import java.util.List;

import java.util.Queue;

import android.util.Log;
import static protocol_g6_package.Protocol_G6.VERSAO_PROTOCOLO;
import static protocol_g6_package.rotas.ENDEREÇO_NULO;
import static protocol_g6_package.rotas.CENTRAL;
import static protocol_g6_package.rotas.BROADCAST;
import static protocol_g6_package.rotas.MAXNODES;
import static protocol_g6_package.Protocol_G6.DEBUG;
import static protocol_g6_package.Protocol_G6.DEBUG_DETAILED;
import static protocol_g6_package.Protocol_G6.DEBUG_FINAL;

import setec.g3.communication.rqst;
import setec.g3.communication.rspns;

public class no {

    int nodeIdentification;
    rotas[] tabRota;
    tabelaValidade[] tabValidade;
    volatile Queue<filaEspera> filain;
    volatile Queue<filaEspera> fila_dados_in;
    volatile Queue<filaEspera> filaout;
    volatile List<waitingPackets> fila_espera_ACK_RRply;
    volatile Queue<filaEspera> fila_dados_out;
    volatile Queue<rqst> filaFragmentacao;
    Queue<Byte> socketQueue;
    int Flag_app_Back; //se =0 é App Se =1 é BackEnd
    boolean GSM;
    int espera_rrply; //Quando está à espera de um rrply não envia mais nenhum
    String[][] fragmentacao = new String[255][10];
    boolean rreq = false;
    volatile int flag_frag;

    public no(Queue<filaEspera> queue, Queue<filaEspera> queue2, Queue<filaEspera> queue3, List<waitingPackets> queue4, Queue<filaEspera> queue5, Queue<rqst> queue6, rotas[] table1, tabelaValidade[] table2, int nodeID, Queue<Byte> sckQueue) {

        nodeIdentification = nodeID;
        tabRota = table1;
        tabValidade = table2;
        filaout = queue;
        filain = queue2;
        fila_dados_in = queue3;
        fila_dados_out = queue5;
        fila_espera_ACK_RRply = queue4;
        filaFragmentacao = queue6;
        GSM = false; // Inicialmente é falso
        espera_rrply = 0;
        if (this.nodeIdentification == 1) {
            this.Flag_app_Back = 1;
        } else {
            this.Flag_app_Back = 0;
        }
        socketQueue = sckQueue;
        flag_frag = 1;
        //if(this.Flag_app_Back==1)
        //Crias tabela sockets do backend
    }

    public void reencaminhaPacote(String packet, int dest, int nHop) {

        int ttl = pacote.getTTLPacote(packet) - 1;
        //Log.d("no","NO: " + this.nodeIdentification);
        //Log.d("no","Pacote para reencaminhar:::::");
        //pacote.imprimePacote(packet);
        String Npcket = pacote.setNewHeadersPacote(packet, ttl, this.nodeIdentification, nHop);
        //Log.d("no","Pacote reencaminha:::::");
        //pacote.imprimePacote(Npcket);
        filaEspera.adicionarElementoFila(this.filaout, Npcket, dest);
        /*Log.d("no","\n\nFILA OUT");
         Log.d("no","tamanho filaout: "+filaout.size());
         filaEspera.imprimirCabeçaFilaEspera(this.filaout);
         Log.d("no","\n\n");
         pacote.imprimePacote(filaEspera.getDados(filaout));
         Log.d("no","\n\n");*/
    }

    public int recebePacote() {

        
        synchronized(this.fila_espera_ACK_RRply){ 
                
        int versao, id, source;
       
        if (filaEspera.verElementoCabeçaFila(this.filain) != null) { //verifica se tem elementos na fila

            /*
             if (nodeIdentification==3){
             Log.d("no","\nXXXXXX");
             pacote.imprimePacote(filaEspera.verElementoCabeçaFila(this.filain).dados);
             }*/
            /*if(this.nodeIdentification==1){  
             Log.d("no","PACOTE RECEBIDO PELA CENTRAL");
        
             pacote.imprimePacote(filaEspera.getDados(this.filain));
             }*/
            String packet = filaEspera.getDados(this.filain);
            versao = pacote.getVersionPacote(packet);

            if (versao != VERSAO_PROTOCOLO) { //se n for o nosso protocolo descarta
                if (DEBUG_FINAL) {
                    if (this.nodeIdentification != 1) {
                        Log.d("no","Pacote recebido pelo nó " + this.nodeIdentification + " não corresponde à nossa versão");
                    } else {
                        Log.d("no","Pacote recebido pelo Backend não corresponde à nossa versão");
                    }
                }
                filaEspera.removerElementoFila(this.filain);
                return -1;
            }
            if (pacote.verificaErros(packet) == -1) { //se tiver erros descarta
                if (DEBUG_FINAL) {
                    if (this.nodeIdentification != 1) {
                        Log.d("no","Pacote recebido pelo nó " + this.nodeIdentification + " contém erros");
                    } else {
                        Log.d("no","Pacote recebido pelo Backend contém erros");
                    }

                }
                filaEspera.removerElementoFila(this.filain);
                return -2;
            }

            id = pacote.getIDPacote(packet);
            source = pacote.getSourcePacote(packet);
            int type = pacote.getTypePacote(packet); 
            //f (pacote.getTypePacote(packet) == 3 && pacote.getDestinoPacote(packet)==this.nodeIdentification)
               
            //Log.d("no","É aqui");
            //pacote.imprimePacote(packet);
            if ((tabelaValidade.verificaValidade(this.tabValidade, id, source) == 0
                    && source != this.nodeIdentification && type != 3) || source == this.nodeIdentification) { //sejativer visto o pacote descarta
                if (DEBUG_FINAL) {
                    if (this.nodeIdentification != 1) {
                        Log.d("no","Pacote recebido pelo nó " + this.nodeIdentification
                                + " é um pacote repetido do tipo " + pacote.getTypePacote(packet)
                                + " proveniente do nó " + source + " e enviada pelo nó " + pacote.getOrigSourcePacote(packet));
                    } else {
                        Log.d("no","Pacote recebido pelo Backend é um pacote repetido do tipo " + pacote.getTypePacote(packet)
                                + " proveniente do nó " + source + " e enviada pelo nó " + pacote.getOrigSourcePacote(packet));
                    }

                    // pacote.imprimePacote(packet);
                    //tabelaValidade.imprimeTabelaValidade(this.tabValidade);
                }
                filaEspera.removerElementoFila(this.filain);
                return -3;
            }

            if (pacote.getNextHopPacote(packet) != this.nodeIdentification
                    && pacote.getNextHopPacote(packet) != BROADCAST) { //se nao for o next hop descarta
                if (DEBUG_FINAL) {
                    if (this.nodeIdentification != 1) {
                        Log.d("no","Pacote recebido pelo nó " + this.nodeIdentification + " não era para si destinado");
                    } else {
                        Log.d("no","Pacote recebido pelo Backend não era para si destinado");
                    }

                }
                filaEspera.removerElementoFila(this.filain);
                return -4;
            }

            ///
            /*
             Log.d("no","Dono tabela -> "+this.nodeIdentification);
             Log.d("no","id= "+id);
             Log.d("no","source= "+source);
             ///*/
            tabelaValidade.addTabelValidade(this.tabValidade, id, source);

            ///
            /*
             Log.d("no","Tabela de Validade do no "+ this.nodeIdentification);
             tabelaValidade.imprimeTabelaValidade(this.tabValidade);
             ///*/
            int dest = pacote.getDestinoPacote(packet);
            
            //int num=0;
            //if(num==0){
            //rotas.adicionaEntradaTabela(this.tabRota, dest, 1,0);
            rotas.adicionaEntradaTabela(this.tabRota, source, pacote.getOrigSourcePacote(packet), 0);
            //num++;
            //}

            int nHop = rotas.getEntradaTabela(this.tabRota, dest);
            if ((pacote.getNextHopPacote(packet) == this.nodeIdentification)
                    && dest != this.nodeIdentification) { //Se for so o next hop

                if (type == 0 || type == 3) { //se for info ou ack reencaminha
                    if (nHop != -1) {
                        reencaminhaPacote(packet, dest, nHop);
                        if (DEBUG) {
                            Log.d("no","Pacote recebido pelo nó " + this.nodeIdentification + " é reencaminhado (O nó é apenas o next hop do pacote)");
                        }
                    } else {

                        for (int i = 0; i < this.fila_espera_ACK_RRply.size(); i++) {
                            String compPacket1 = waitingPackets.verElementoLista(this.fila_espera_ACK_RRply, i).waitPacket;
                            if (pacote.getTypePacote(compPacket1) == 1 && (pacote.getDestinoPacote(packet) == pacote.getDestinoPacote(compPacket1))) {

                                this.rreq = true;
                            }
                        }
                        waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, packet);
                        if (this.rreq == false) {

                            String RReq = pacote.criaRReq(dest, this.nodeIdentification);
                            waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, RReq);
                            espera_rrply = 1;
                        }
                        if (DEBUG) {
                            Log.d("no","Pacote recebido pelo nó " + this.nodeIdentification + " era para ser reencaminhado mas devido à inexistência de rota cria-se RRequest ");
                        }
                        this.rreq = false;
                    }

                } else if (type == 2) {
                    nHop = rotas.getEntradaTabela(this.tabRota, dest);
                    if (nHop == -1) {
                        //Log.d("no","Pacote de RReply recebido pelo nó " + this.nodeIdentification + ", no entanto n existe rota para retransmitir!!");
                    } else {
                        reencaminhaPacote(packet, dest, nHop);
                        if (DEBUG_DETAILED) {
                            Log.d("no","Pacote de RReply recebido pelo nó " + this.nodeIdentification + " é reencaminhado (nó não é o destino do pacote)");
                        }
                    }
                }
            } else if ((pacote.getNextHopPacote(packet) == BROADCAST) && dest != this.nodeIdentification) {
                if (type == 0) { //se for BROADCAST reencaminha
                    reencaminhaPacote(packet, dest, BROADCAST);
                    if (DEBUG) {
                        Log.d("no","Pacote de info (broadcast) recebido pelo nó " + this.nodeIdentification + " é retransmitido em broadcast");
                    }
                } else if (type == 1) { //se for rreq reencaminha e actualiza tabela -> actualizar

                    reencaminhaPacote(packet, dest, BROADCAST);
                    //rotas.imprimeTabela(this.tabRota,this.nodeIdentification);
                    if (DEBUG_DETAILED) {
                        Log.d("no","Pacote de RRequest recebido pelo nó " + this.nodeIdentification + " é enviado em broadcast (nó não é o destino do pacote)");
                    }
                }

            } else if (dest == this.nodeIdentification) {

                if (type == 0) { //se for o destino juntar os pacotes e mandar dados p android

                    if (pacote.getFragmentFlagPacote(packet) == 1) {
                        int fragid = pacote.getFragmentIDPacote(packet);

                        fragmentacao[source][fragid] = pacote.getDadosPacote(packet);

                        //filaEspera.removerElementoFila(this.filain);
                        String Ack = pacote.criaAck(packet, this);
                        //Log.d("no","ACK: "+ Ack);
                        //pacote.imprimePacote(Ack);
                        filaEspera.adicionarElementoFila(filaout, Ack, source);

                        for (int i = 0; i < (pacote.getTotalFragmentsPacote(packet) + 1); i++) {
                            if (fragmentacao[source][i] == null) {
                                filaEspera.removerElementoFila(this.filain);
                                return 1;
                            }
                        }
                        String dados = "";
                        for (int i = 0; i < (pacote.getTotalFragmentsPacote(packet) + 1); i++) {
                            dados += fragmentacao[source][i];

                        }
                        //Log.d("no","dados juntos "+ pacote.binaryStringToText(dados));

                        filaEspera.adicionarElementoFila(this.fila_dados_out, dados, this.nodeIdentification);
                        for (int j = 0; j < (pacote.getTotalFragmentsPacote(packet) + 1); j++) {
                            fragmentacao[source][j] = null;
                        }
                        //Log.d("no","Je " + this.nodeIdentification +" Recebi pacote de info do no "+source);
                        if (DEBUG_FINAL) {
                            if (this.nodeIdentification != 1) {
                                Log.d("no","Nó " + this.nodeIdentification + " recebe Info (com fragmentação)");
                            } else {
                                Log.d("no","Backend recebe Info (com fragmentação)");
                            }

                        }
                    } else {

                        String dados = pacote.getDadosPacote(packet);
                       // Log.d("no","Je " + this.nodeIdentification +" Recebi pacote de info do no "+source);
                        //Log.d("no","\n*****************************");
                        //pacote.imprimePacote(packet);
                        //Log.d("no","*****************************\n");
                        //Log.d("no","*****************************this.nodeIdentification: "+this.nodeIdentification);
                        filaEspera.adicionarElementoFila(this.fila_dados_out, dados, this.nodeIdentification);
                        String Ack = pacote.criaAck(packet, this);
                        filaEspera.adicionarElementoFila(this.filaout, Ack, source);
                        //pacote.imprimePacote(Ack);
                        if (DEBUG_FINAL) {
                            if (this.nodeIdentification != 1) {
                                Log.d("no","Nó " + this.nodeIdentification + " recebe Info (sem fragmentação)");
                            } else {
                                Log.d("no","Backend recebe Info (sem fragmentação)");
                            }

                        }
                    }

                } else if (type == 1) {
                    String RRep = pacote.criaRReply(packet, this);
                    filaEspera.adicionarElementoFila(filaout, RRep, source);
                    //rotas.imprimeTabela(this.tabRota,this.nodeIdentification);
                    if (DEBUG_FINAL) {
                        if (this.nodeIdentification != 1) {
                            Log.d("no","Nó " + this.nodeIdentification + " recebe RRequest");
                        } else {
                            Log.d("no","Backend recebe RRequest");
                        }

                    }
                } else if (type == 2) {
                    //Log.d("no","**************Size"+this.fila_dados_in.size());
                    if (DEBUG_FINAL) {
                        if (this.nodeIdentification != 1) {
                            Log.d("no","Nó " + this.nodeIdentification + " recebe RReply");
                        } else {
                            Log.d("no","Backend recebe RReply");
                        }

                    }
                    //waitingPackets.imprimeFila(this.fila_espera_ACK_RRply);
                    // Log.d("no","**************Size"+this.fila_espera_ACK_RRply.size());
                    for (int i = 0; i < this.fila_espera_ACK_RRply.size(); i++) {
                        
                       
                        String compPacket = this.fila_espera_ACK_RRply.get(i).waitPacket;//waitingPackets.verElementoLista(this.fila_espera_ACK_RRply, i).waitPacket;

                        if (pacote.getTypePacote(compPacket) == 1 && pacote.getDestinoPacote(compPacket) == source) {
                            //Log.d("no","**************Pacote a remover\n");
                            //pacote.imprimePacote(compPacket);
                            this.fila_espera_ACK_RRply.remove(i);
                            //waitingPackets.removerElementoLista(this.fila_espera_ACK_RRply, i);
                            i--;
                            espera_rrply = 0;
                            for (int j = 0; j < this.fila_espera_ACK_RRply.size(); j++) {

                                String compPacket1 = this.fila_espera_ACK_RRply.get(j).waitPacket;//waitingPackets.verElementoLista(this.fila_espera_ACK_RRply, j).waitPacket;

                                if (pacote.getTypePacote(compPacket1) == 0 && pacote.getDestinoPacote(compPacket1) == source) {

                                    //Log.d("no","**************Enviei fragmento" + pacote.getFragmentIDPacote(compPacket1));
                                    //pacote.imprimePacote(compPacket1);
                                    //compPacket1 = pacote.setNewPacketID(compPacket1);
                                    //Log.d("no","Pacote depois :::::");
                                    //pacote.imprimePacote(compPacket1);
                                    reencaminhaPacote(compPacket1, pacote.getDestinoPacote(compPacket1), rotas.getEntradaTabela(tabRota, pacote.getDestinoPacote(compPacket1)));
                                    this.fila_espera_ACK_RRply.remove(j);
                                    //waitingPackets.removerElementoLista(this.fila_espera_ACK_RRply, j);
                                    j--;
                                }
                            }
                            if (DEBUG) {
                                Log.d("no","Pacote RReply recebido pelo nó " + this.nodeIdentification + ". Actualiza tabelas");
                            }

                        }
                    }

                } else if (type == 3) {
                    //Log.d("no","Pacote Ack recebido pelo nó "+this.nodeIdentification);
                    //pacote.imprimePacote(packet);
                     //Log.d("no","Eu "+this.nodeIdentification+ " Recebeu ack do no "+ source);
                    if (DEBUG_FINAL) {
                        if (this.nodeIdentification != 1) {
                            Log.d("no","Nó " + this.nodeIdentification + " recebe Ack");
                        } else {
                            Log.d("no","Backend recebe Ack");
                        }
                    }


                        for (int i = 0; i < this.fila_espera_ACK_RRply.size(); i++) {
                            String compPacket = this.fila_espera_ACK_RRply.get(i).waitPacket;//waitingPackets.verElementoLista(this.fila_espera_ACK_RRply, i).waitPacket;

                            if (pacote.getTypePacote(compPacket) == 0) {
                                if (pacote.getDestinoPacote(compPacket) == source && (pacote.getIDPacote(compPacket) == id)) {
                                    if (pacote.getFragmentFlagPacote(packet) == 1) {
                                        if (pacote.getFragmentIDPacote(packet) == pacote.getFragmentIDPacote(compPacket)) {
                                            this.fila_espera_ACK_RRply.remove(i);
                                            //waitingPackets.removerElementoLista(this.fila_espera_ACK_RRply, i);

                                        }
                                    } else {
                                        this.fila_espera_ACK_RRply.remove(i);
                                        //waitingPackets.removerElementoLista(this.fila_espera_ACK_RRply, i);

                                    }
                                }
                            }
                        }

                        if (this.fila_espera_ACK_RRply.size() > 0) {

                            for (int i = 0; i < this.fila_espera_ACK_RRply.size(); i++) {
                                if (pacote.getTypePacote(this.fila_espera_ACK_RRply.get(i).waitPacket)==0 && (pacote.getFragmentFlagPacote(this.fila_espera_ACK_RRply.get(i).waitPacket) == 1)){//waitingPackets.verElementoLista(this.fila_espera_ACK_RRply, i).waitPacket) == 0 && (pacote.getFragmentFlagPacote(waitingPackets.verElementoLista(this.fila_espera_ACK_RRply, i).waitPacket) == 1)) {
                                    this.flag_frag = 0;
                                    break;
                                } else {
                                    this.flag_frag = 1;
                                }
                            }
                        } else {
                            this.flag_frag = 1;
                        }

                        String pac = null;
                        if (this.flag_frag == 1) {
                            if (!(this.filaFragmentacao.isEmpty())) {
                                pac = pacote.byteArray2binaryString(this.filaFragmentacao.peek().packet);
                                //Log.d("no","pac: " + pacote.binaryStringToText(pac));
                                filaEspera.adicionarElementoFila(this.fila_dados_in, pac, this.filaFragmentacao.peek().spec);
                                this.filaFragmentacao.remove();
                                preparaPacote();
                            }
                        }
                    
                }
            } else if (dest == BROADCAST) {

                if (pacote.getFragmentFlagPacote(packet) == 1) {

                }
                String dados = pacote.getDadosPacote(packet);
                filaEspera.adicionarElementoFila(this.fila_dados_out, dados, this.nodeIdentification);

            }

        }

        filaEspera.removerElementoFila(this.filain);
        return 0;
        }
    }

    public int preparaPacote() {

        if (filaEspera.verElementoCabeçaFila(this.fila_dados_in) != null) {
            int numFrag = calculaNumFragmentos(filaEspera.getDados(this.fila_dados_in));
            if (this.flag_frag == 1 || numFrag == 0) {

                if (this.Flag_app_Back == 0) {
                    //Prepara Pacote para dados recebidos do Android
                    if (filaEspera.verElementoCabeçaFila(this.fila_dados_in) != null) {
                        //dest e sempre central
                        int dest = CENTRAL;
                        int ttl = 255;
                        String dados = filaEspera.getDados(this.fila_dados_in);

                        //rotas.adicionaEntradaTabela(this.tabRota, CENTRAL, 2, 0);
                        if (rotas.getEntradaTabela(this.tabRota, dest) != -1) {

                            if (DEBUG_DETAILED) {
                                Log.d("no","Nó " + this.nodeIdentification + " quer enviar informação e tem rota.");
                            }
                            int nHop = rotas.getEntradaTabela(this.tabRota, dest);
                            int TotalFragments = this.calculaNumFragmentos(dados);
                            int FragmentFlag;
                            if (TotalFragments == 0) {
                                FragmentFlag = 0;
                                filaEspera.adicionarElementoFila(filaout, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments), dest);

                            } else {
                                FragmentFlag = 1;
                                for (int i = 0; i < TotalFragments; i++) {
                                    filaEspera.adicionarElementoFila(filaout, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1), dest);
                                }
                                this.flag_frag = 0;
                            }
                            filaEspera.removerElementoFila(this.fila_dados_in);

                            espera_rrply = 0;

                        } else if (espera_rrply == 0) {

                            if (DEBUG_DETAILED) {
                                Log.d("no","Nó " + this.nodeIdentification + " quer enviar informação mas não tem rota. Envia RRequest e espera RReply ");
                            }
                            int nHop = 0;
                            int TotalFragments = this.calculaNumFragmentos(dados);
                            int FragmentFlag;
                            String RReq = pacote.criaRReq(dest, this.nodeIdentification);
                            filaEspera.adicionarElementoFila(filaout, RReq, dest);
                            if (TotalFragments == 0) {
                                FragmentFlag = 0;
                                waitingPackets packet = new waitingPackets(pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments));
                                this.fila_espera_ACK_RRply.add(packet);
                                //waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments));
                            } else {

                                FragmentFlag = 1;
                                for (int i = 0; i < TotalFragments; i++) {
                                    
                                    waitingPackets packet = new waitingPackets(pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1));
                                    this.fila_espera_ACK_RRply.add(packet);
                                    //waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1));
                                }
                                this.flag_frag = 0;
                            }
                            //Log.d("no","FILAA\n");
                            //waitingPackets.imprimeFila(this.fila_espera_ACK_RRply);
                            filaEspera.removerElementoFila(this.fila_dados_in);

                            //Envia para Android->Adiciona filaout e passa para Protocol_G6
                            espera_rrply = 1;
                        } else {

                            int nHop = 0;
                            int TotalFragments = this.calculaNumFragmentos(dados);
                            int FragmentFlag;
                            if (TotalFragments == 0) {
                                FragmentFlag = 0;
                                    waitingPackets packet = new waitingPackets(pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments));
                                    this.fila_espera_ACK_RRply.add(packet);
                                    //waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments));
                            } else {
                                FragmentFlag = 1;
                                for (int i = 0; i < TotalFragments; i++) {
                                    waitingPackets packet = new waitingPackets(pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1));
                                    this.fila_espera_ACK_RRply.add(packet);
                                   //waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1));
                                }
                                this.flag_frag = 0;
                            }
                            //Log.d("no","FILAA\n");
                            //waitingPackets.imprimeFila(this.fila_espera_ACK_RRply);
                            filaEspera.removerElementoFila(this.fila_dados_in);

                        }
                    }

                    return 1;
                } else { //Central
                    //Prepara Pacote para dados recebidos do Backhend
                    if (filaEspera.verElementoCabeçaFila(this.fila_dados_in) != null) {

                        int dest = filaEspera.getDest(this.fila_dados_in);
                        //dest e sempre central
                        int ttl = 255;
                        String dados = filaEspera.getDados(this.fila_dados_in);

                        // MODIFICAÇÃO: Para a central põe-se directamente na filaout a central não tem tabela de rotas
                        int nHop = 0;
                        int TotalFragments = this.calculaNumFragmentos(dados);
                        int FragmentFlag;
                        if (TotalFragments == 0) {
                            FragmentFlag = 0;
                            filaEspera.adicionarElementoFila(filaout, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments), dest);

                        } else {
                            FragmentFlag = 1;
                            for (int i = 0; i < TotalFragments; i++) {
                                filaEspera.adicionarElementoFila(filaout, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1), dest);
                            }
                            this.flag_frag = 0;
                        }
                        filaEspera.removerElementoFila(this.fila_dados_in);
                        espera_rrply = 0;

                        /*
                         //rotas.adicionaEntradaTabela(this.tabRota, CENTRAL, 2, 0);
                         if (rotas.getEntradaTabela(this.tabRota, dest) != -1) {
                         if (DEBUG) {
                         Log.d("no","Nó " + this.nodeIdentification + " quer enviar informação e tem rota.");
                         }
                         int nHop = rotas.getEntradaTabela(this.tabRota, dest);
                         int TotalFragments = this.calculaNumFragmentos(dados);
                         int FragmentFlag;
                         if (TotalFragments == 0) {
                         FragmentFlag = 0;
                         filaEspera.adicionarElementoFila(filaout, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments), dest);

                         } else {
                         FragmentFlag = 1;
                         for (int i = 0; i < TotalFragments; i++) {
                         filaEspera.adicionarElementoFila(filaout, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1), dest);
                         }
                         this.flag_frag = 0;
                         }
                         filaEspera.removerElementoFila(this.fila_dados_in);
                         espera_rrply = 0;

                         } else if (espera_rrply == 0) {
                         if (DEBUG_DETAILED) {
                         Log.d("no","Nó " + this.nodeIdentification + " quer enviar informação mas não tem rota. Envia RRequest e espera RReply ");
                         }

                         int nHop = 0;
                         int TotalFragments = this.calculaNumFragmentos(dados);
                         int FragmentFlag;
                         String RReq = pacote.criaRReq(dest, this.nodeIdentification);
                         filaEspera.adicionarElementoFila(filaout, RReq, dest);
                         if (TotalFragments == 0) {
                         FragmentFlag = 0;
                         waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, 0, TotalFragments));
                         } else {
                         FragmentFlag = 1;
                         for (int i = 0; i < TotalFragments; i++) {
                         waitingPackets.adicionarElementoLista(this.fila_espera_ACK_RRply, pacote.encapsula(dest, nHop, dados, ttl, nodeIdentification, FragmentFlag, i, TotalFragments - 1));
                         }
                         }

                         filaEspera.removerElementoFila(this.fila_dados_in);
                         espera_rrply = 1;
                         } */
                    }
                    return 1;
                }
            }
        }
        return 1;
    }

    //Calcula Nº Fragmentos para uns determinados dados
    public int calculaNumFragmentos(String dados) {

        int numFragmentos;
        double tamanhoTotalDadosPacote = 16;
        String dados2 = pacote.binaryStringToText(dados);

        if (dados2.length() <= tamanhoTotalDadosPacote) {
            return 0;
        } else {
            numFragmentos = (int) Math.ceil(((double) dados2.length() / (double) tamanhoTotalDadosPacote));
            //Log.d("no","num fragmentos: " + numFragmentos );
            return numFragmentos;
        }
    }
}
