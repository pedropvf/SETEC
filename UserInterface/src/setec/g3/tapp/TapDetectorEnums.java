package setec.g3.tapp;

public class TapDetectorEnums {
	public static enum TapDurationType {SHORT_TAP, LONG_TAP};
	public static enum TapPhase {WAITING_TAP, TAP_1, TAP_2, TAP_3, TAP_SEQUENCE_EVALUATION};
	/* some constants */
	public static final int SAMPLE_PERIOD_MILIS = 40;
	/* dead man */
	public static final float DEAD_MAN_THRESHOLD = (float) 0.7;
	public static final int DEAD_MAN_TIME_MILIS = 30000;
	/* tap detection */
	public static final int TAP_DURATION_TIMEOUT = 2000;
	public static final int TAP_DURATION_THRESHOLD = 900;
}
