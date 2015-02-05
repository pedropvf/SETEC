
package protocol_g6_package;

import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;


public class filaEsperaSimulador {
    
    byte[] pac;
    int  idAplicacao;    
    byte rqstID;
    byte spec;
    // Para efeitos de teste do simulador
    
    //////////////////////////////////////
    
    public filaEsperaSimulador(){
        pac=null;
        idAplicacao=-1;
        rqstID=0x00;
        spec=0x00;     
    }
    
        // Construtor para os elementos da fila de espera 
    public filaEsperaSimulador(byte[] info,int dest,byte rID, byte Spe){
        pac=info;
        idAplicacao=dest;
        rqstID=rID;
        spec=Spe;     
    }
    
        // Cria fila de espera 
    public static Queue<filaEsperaSimulador> criarFila(){        
        Queue<filaEsperaSimulador> queue = new LinkedList<>();
        return queue;
    }
    
        // Adiciona um elemento à fila de espera 
    public static Queue<filaEsperaSimulador> adicionarElementoFila(Queue<filaEsperaSimulador> queue,byte[] pacote,int id,byte rID, byte Spe){
        filaEsperaSimulador packet=new filaEsperaSimulador(pacote,id,rID,Spe);
        queue.offer(packet);
        return queue;
    }
    
        // Remove um elemento da fila de espera 
    public static Queue<filaEsperaSimulador> removerElementoFila(Queue<filaEsperaSimulador> queue){
        queue.remove();
        return queue;
    }
    
        // Devolve o primeiro elemento da fila de espera
    public static filaEsperaSimulador verElementoCabeçaFila(Queue<filaEsperaSimulador> queue){
        filaEsperaSimulador packet;
        packet=queue.peek();
        if (packet!=null)
            return packet;
        else return null;
    }
    
    public static int getSize(Queue<filaEsperaSimulador> queue){
        return queue.size();
    }
    
        // Imprime o primeiro elemento da fila de espera
    public static void imprimeFilaEsperaSimulador(Queue<filaEsperaSimulador> queue){
        filaEsperaSimulador packet;
        packet=queue.peek();
        if (packet!=null){
            pacote.imprimePacote(pacote.byteArray2binaryString(packet.pac));
            //Log.d("filaEsperaSimulador","Mensagem: "+pacote.binaryStringToText(pacote.));
            Log.d("filaEsperaSimulador","\nDestino: "+Integer.toString(packet.idAplicacao));
        }
        else{
             Log.d("filaEsperaSimulador","Fila vazia");
        }
    }
        
        // Devolve o idAplicacao do primeiro elemento da fila de espera
    public static int getDest(Queue<filaEsperaSimulador> queue){
        filaEsperaSimulador pckt;
        pckt = queue.peek();
        if(queue.size()!=0)
            return pckt.idAplicacao;
        else
            return -1;
    }
    
        // Devolve os pacote do primeiro elemento da fila de espera
    public static byte[] getPacote(Queue<filaEsperaSimulador> queue){
        filaEsperaSimulador pckt;
        pckt = queue.peek();
        return pckt.pac;
    }
    
    // Devolve os pacote do primeiro elemento da fila de espera
    public static byte getRID(Queue<filaEsperaSimulador> queue){
        filaEsperaSimulador pckt;
        pckt = queue.peek();
        return pckt.rqstID;
    }
    
        // Devolve os pacote do primeiro elemento da fila de espera
    public static byte getSpe(Queue<filaEsperaSimulador> queue){
        filaEsperaSimulador pckt;
        pckt = queue.peek();
        return pckt.spec;
    }

   
    public static int getIDApp(Queue<filaEsperaSimulador> queue){
        filaEsperaSimulador pckt;
        pckt = queue.peek();
        return pckt.idAplicacao;
    }
}

