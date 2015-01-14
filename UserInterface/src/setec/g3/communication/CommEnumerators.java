package setec.g3.communication;

public class CommEnumerators {
	public static enum Protocol { PROTOCOL_G5, PROTOCOL_G6 };
	/* Predefined Message Types */
	public static final int NEED_SUPPORT=0;
	public static final int NEED_TO_BACK_DOWN=1;
	public static final int FIRETRUCK_IS_IN_TROUBLE=2;
	public static final int NEED_AERIAL_SUPPORT=3;
	public static final int GO_REST=4;
	public static final int AERIAL_SUPPORT_INCOMMING=5;
	public static final int FIRE_SPREADING=6;
	public static final int WE_ARE_LEAVING=7;
	public static final int FIRE_GETTING_CLOSE_TO_HOUSE=8;
	public static final int HOUSE_BURNED=9;
	
	/* Backend Message Type */
	public static final int FIREFIGHTER_TO_COMMAND_GPS=0;
	public static final int FIREFIGHTER_TO_COMMAND_HEART_RATE=1;
	public static final int FIREFIGHTER_TO_COMMAND_CO=2;
	public static final int FIREFIGHTER_TO_COMMAND_GPS_AND_HEART_RATE=3;
	public static final int FIREFIGHTER_TO_COMMAND_GPS_AND_CO=4;
	public static final int FIREFIGHTER_TO_COMMAND_HEART_RATE_AND_CO=5;
	public static final int FIREFIGHTER_TO_COMMAND_GPS_AND_HEART_RATE_AND_CO=6;
	public static final int FIREFIGHTER_TO_COMMAND_LOGIN=7;
	public static final int FIREFIGHTER_TO_COMMAND_LOGOUT=8;
	public static final int FIREFIGHTER_TO_COMMAND_FIRELINE_GPS=9;
	public static final int FIREFIGHTER_TO_COMMAND_PREDEFINED_MESSAGE=10;
	public static final int FIREFIGHTER_TO_COMMAND_MESSGAGE=11;
	public static final int FIREFIGHTER_TO_COMMAND_SOS=12;
	public static final int FIREFIGHTER_TO_COMMAND_SURROUNDED_BY_FLAMES=13;
	public static final int FIREFIGHTER_TO_COMMAND_FIRELINE_GPS_UPDATE=14;
	public static final int FIREFIGHTER_TO_COMMAND_TEAM_UPDATE=15;
	public static final int FIREFIGHTER_TO_COMMAND_CO_ALERT=16;
	public static final int FIREFIGHTER_TO_COMMAND_HEART_RATE_ALERT=17;
	public static final int FIREFIGHTER_TO_COMMAND_DEAD_MAN_ALERT=18;
	public static final int FIREFIGHTER_TO_COMMAND_ACCEPTS_REQUEST=19;
	public static final int FIREFIGHTER_TO_COMMAND_DENIES_REQUEST=20;
	public static final int FIREFIGHTER_TO_COMMAND_LOW_BATTERY=21;
	public static final int FIREFIGHTER_TO_COMMAND_DENIES_ID=22;
	
	public static final int COMMAND_TO_FIREFIGHTER_PREDEFINED_MESSAGE=128;
	public static final int COMMAND_TO_FIREFIGHTER_MESSAGE=129;
	public static final int COMMAND_TO_FIREFIGHTER_FIRELINE_UPDATE_REQUEST=130;
	public static final int COMMAND_TO_FIREFIGHTER_TEAM_INFORMATION_REQUEST=131;
	public static final int COMMAND_TO_FIREFIGHTER_ID=132;
	public static final int COMMAND_TO_FIREFIGHTER_LOGIN_DENIED=133;
	public static final int COMMAND_TO_FIREFIGHTER_LOGING_ACCEPTED=134;
	public static final int COMMAND_TO_FIREFIGHTER_GO_TO_COORDINATE=135;
	
}
