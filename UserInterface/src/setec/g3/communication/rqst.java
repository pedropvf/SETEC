package setec.g3.communication;

import java.io.Serializable;

/**
 * @author Luis Ungaro
 */

/*These class must be Serializable!*/
public class rqst implements Serializable {
    public final byte id;
    public final byte spec;
    public boolean priority;
    
    public final byte[] packet;
    
    public rqst(byte id, byte spec, byte[] packet){
        this.id = id;
        this.spec = spec;
        this.packet = packet;

    }
}
