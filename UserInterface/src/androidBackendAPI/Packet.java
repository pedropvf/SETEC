package androidBackendAPI;
import java.io.Serializable;

public class Packet implements Serializable{
	
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean hasProtocolHeader;
	public byte[] packetContent;

}