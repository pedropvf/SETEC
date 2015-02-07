
package protocol_g6_package;

import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;


public class filaEspera {
    
    String dados;
    int destino;
    int nRetransmissoes;
    long time;
    byte socketID;
    
        // Construtor para os elementos da fila de espera 
    public filaEspera(String info,int dest){
        dados=info;
        destino=dest;
        nRetransmissoes=0;
        time=0;
        socketID=0;
    }
    
        // Cria fila de espera 
    public static Queue<filaEspera> criarFila(){        
        Queue<filaEspera> queue = new LinkedList<>();
        return queue;
    }
    
        // Adiciona um elemento Ã  fila de espera 
    public static Queue<filaEspera> adicionarElementoFila(Queue<filaEspera> queue,String dados,int dest){
        filaEspera packet=new filaEspera(dados,dest);
        queue.offer(packet);
        return queue;
    }
    
        // Remove um elemento da fila de espera 
    public static Queue<filaEspera> removerElementoFila(Queue<filaEspera> queue){
        
        if(queue.size()!=0){
            queue.remove();
            return queue;
        }
        else
            return null;
    }
    
        // Devolve o primeiro elemento da fila de espera
    public static filaEspera verElementoCabeçaFila(Queue<filaEspera> queue){
        filaEspera packet;
        packet=queue.peek();
        if (packet!=null)
            return packet;
        else return null;
    }
    
        // Imprime o primeiro elemento da fila de espera
    public static void imprimirCabeçaFilaEspera(Queue<filaEspera> queue){
        filaEspera packet;
        packet=queue.peek();
        if (packet!=null){
            Log.d("filaEspera","Mensagem: "+packet.dados);
            Log.d("filaEspera","Destino: "+Integer.toString(packet.destino));
        }
        else{
             Log.d("filaEspera","Fila vazia");
        }
    }
        
        // Devolve o destino do primeiro elemento da fila de espera
    public static int getDest(Queue<filaEspera> queue){
        filaEspera pckt;
        pckt = queue.peek();
        if(queue.size()!=0 && pckt != null)
            return pckt.destino;
        else
            return -1;
    }
    
        // Devolve os dados do primeiro elemento da fila de espera
    public static String getDados(Queue<filaEspera> queue){
        filaEspera pckt;
        pckt = queue.peek();
        return pckt.dados;
    }
    
        // Devolve nTransmissoes do primeiro elemento da fila de espera
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
    // Se nÃ£o chegar dentro de um determinado tempo enviamos de novo
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
    }
}
