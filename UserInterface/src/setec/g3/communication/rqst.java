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
    
    rqst(byte id, byte spec, boolean priority, byte[] packet){
        this.id = id;
        this.spec = spec;
        this.packet = packet;
        this.priority = priority;
    }
}
