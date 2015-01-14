package setec.g3.communication;

import java.io.Serializable;

public class packet implements Serializable{
	
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean hasProtocolHeader;
	public byte[] packetContent;

}