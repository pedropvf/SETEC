
package protocol_g6_package;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;

/**
 *
 * @author ee10060
 */
public class waitingPackets {
    
    String waitPacket; // Supostamente isto será o pacote em binário
    int nRetransmissoes;
    long time;
    //byte socketID;
    
        // Construtor para os elementos da lista 
    public waitingPackets(String pack){
        waitPacket=pack;
        nRetransmissoes=0;
        time=0;
    }
    
        // Cria lista de waitingPackets
    public static List<waitingPackets> criarLista(){        
        List<waitingPackets> listaRREQ = Collections.synchronizedList(new ArrayList<waitingPackets>());
        return listaRREQ;
    }
    
        // Adiciona um elemento à lista
    public static List<waitingPackets> adicionarElementoLista(List<waitingPackets> listaRREQ,String pack){
        //synchronized (mutex){
            waitingPackets packet=new waitingPackets(pack);
            listaRREQ.add(packet);
            return listaRREQ;
        //}
    }
    
    public static int getDest(List<waitingPackets> lista, int i){
        //synchronized (lista){
            if (lista.size()>i){
                waitingPackets P=lista.get(i);
                if(P != null)
                    return pacote.getDestinoPacote(P.waitPacket);
                return 0;
            }
            else
                return -1;
        //}
    }
    
    public static int incrementaTransmissoes(List<waitingPackets> lista, int i){
        //synchronized (lista){
            if (lista.size()>i){
                waitingPackets P=lista.get(i);
                if(P!=null){
                    P.nRetransmissoes++;
                    return 1;
                }
                return 0;
            }
            else
                return 0;
        //}
    }
    
    public static int setTime(List<waitingPackets> lista, int i){
        //synchronized (lista){
            if (lista.size()>i){
                waitingPackets P=lista.get(i);
                if(P!=null){
                    P.time=System.currentTimeMillis();
                    return 1;
                }
                return 0;
            }
            else
                return 0;
        //}
    }
    
    public static void imprimeFila(List<waitingPackets> lista){
        //synchronized (lista){
            for(int i=0; i<lista.size();i++){
                Log.d("waitingPackets","Pacote n " + i);
                pacote.imprimePacote(waitingPackets.verElementoLista(lista, i).waitPacket);
            }
        //}
        
    }
    
    public static long getTime(List<waitingPackets> lista, int i){
        //synchronized (lista){
            if (lista.size()>i){
                waitingPackets P=lista.get(i);
                return P.time;
            }
            else{
                return -1;
            }
        //}
    }
    
    public static  int getNumTransmissoes(List<waitingPackets> lista, int i){
        //synchronized (lista){
            if(lista.size()>i){
                waitingPackets P=lista.get(i);
                if(P!=null){
                    return P.nRetransmissoes;
                }
                else
                    return -1;
            }
            else
                return -1;
        //}
    }
        // Remove o elemento i da lista 
    public static List<waitingPackets> removerElementoLista(List<waitingPackets> listaRREQ,int i){
        //synchronized (listaRREQ){
            waitingPackets packet;
            int nRemocoes=0;

            if (i<listaRREQ.size())
                listaRREQ.remove(i);
            else
                Log.d("waitingPackets","Não foi removido nenhum elemento da lista");


            return listaRREQ;
        //}
    }
    
        // Devolve o elemento i lista 
    public static waitingPackets verElementoLista(List<waitingPackets> listaRREQ,int i){
        /*Log.d("waitingPackets","SIZE "+listaRREQ.size());
        for(int k=0; k<listaRREQ.size(); k++){
            if(listaRREQ.get(k)==null){
                Log.d("waitingPackets","POS NULA");
                Log.d("waitingPackets",k);
            }
        }*/
        //synchronized (listaRREQ){
            if (i<listaRREQ.size())
                return listaRREQ.get(i);
            else{
                //Log.d("waitingPackets","O elemento especificado não existe verElemento()");
                return null;
            }
        //}
    }
    
    public static boolean hasIndex(List<waitingPackets> listaRREQ,int i){
        //synchronized (listaRREQ){
            return i < listaRREQ.size();
        //}
    }
    
       
        // Imprime o elemento i da lista 
    public static void imprimirElementoLista(List<waitingPackets> listaRREQ,int i){
        waitingPackets packet=null;
        //synchronized (listaRREQ){
            if (i<listaRREQ.size()){
                packet=listaRREQ.get(i);
                //Log.d("waitingPackets","Mensagem: "+packet.waitPacket);
                //Log.d("waitingPackets","Destino: "+Integer.toString(packet.nRetransmissoes));
                //Log.d("waitingPackets","Tempo: "+packet.time);
            }
            else
                Log.d("waitingPackets","O elemento especificado não existe imprimirelemento");
        //}
    }
        
    /*    // Devolve o destino do primeiro elemento da fila de espera
    public static int getDest(Queue<filaEspera> queue){
        filaEspera pckt;
        pckt = queue.peek();
        if(queue.size()!=0)
            return pckt.destino;
        else
            return -1;
    }*/
    
    /*    // Devolve os dados do primeiro elemento da fila de espera
    public static String getDados(Queue<filaEspera> queue){
        filaEspera pckt;
        pckt = queue.peek();
        return pckt.dados;
    }*/
    
    /*    // Devolve nTransmissoes do primeiro elemento da fila de espera
    public static int getNumTransmissoes(Queue<filaEspera> queue){
        filaEspera pckt;
        pckt = queue.peek();
        if(queue.size()!=0)
            return pckt.nRetransmissoes;
        else
            return -1;
    }
    
        // Incrementa nRetransmissoes
    public static int incrementaNumTransmissoes(Queue<filaEspera> queue){
        filaEspera packet;
        packet=queue.peek();
        if (packet!=null){
            packet.nRetransmissoes++;
            return 1;
        }
        else return 0;
    }
    // Devolve tempo inicio do primeiro elemento da fila de espera
    // Usado quando enviamos um pacote e estamos a espera do ack ou rrply
    // Se não chegar dentro de um determinado tempo enviamos de novo
    public static long getTime(Queue<filaEspera> queue){
        filaEspera pckt;
        pckt = queue.peek();
        if(queue.size()!=0)
            return pckt.time;
        else
            return -1;
    }
    
    // Atualiza tempo
    public static int setTime(Queue<filaEspera> queue){
        filaEspera packet;
        packet=queue.peek();
        
        if (packet!=null){
            packet.time=System.currentTimeMillis();
            return 1;
        }
        else return 0;
    }
    
     // Para Backend -> Atualiza socketID
    public static int setSocketID(Queue<filaEspera> queue, byte ID){
        filaEspera packet;
        packet=queue.peek();
        
        if (packet!=null){
            packet.socketID=ID;
            return 1;
        }
        else return 0;
    }
    
    public static long getSocketID(Queue<filaEspera> queue){
        filaEspera pckt;
        pckt = queue.peek();
        return pckt.socketID;
    }*/
}
