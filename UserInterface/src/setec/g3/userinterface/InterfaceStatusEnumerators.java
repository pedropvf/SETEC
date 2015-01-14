package setec.g3.userinterface;

import setec.g3.userinterface.FlyOutContainer.CombatModeAnimationRunnable;
import setec.g3.userinterface.FlyOutContainer.DialAnimationRunnable;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public final class InterfaceStatusEnumerators {
	static public enum userRanks{ COMMANDER, OPERATIONAL};
	static public enum actioDialStateEnum{ STILL, START, MOVING, RETURNING; }
	static public enum utilityStates {UTILITY_ON, UTILITY_OFF};
	static public enum indicatorStates{EMPTY, LOW, MEDIUM, HIGH, FULL};
	static public enum TimerStates {STOPPED, RUNNING, RESETED};
	static public enum PriorityLevel {NORMAL, NORMAL_PLUS, IMPORTANT, CRITICAL};
	static public enum dialDisplayState { OFF, OPENING, ON, CLOSING };
	static public enum dialSelectionSate { UNSELECTED, LINE_OF_FIRE, MESSAGE, SETTINGS, PRE_DEFINED_MESSAGES };
	static public enum LineOfFireSituation { ACTIVE, CONTROLLED, VIGILLANCE, DELETED};
	static public enum compassState { DIAL_OFF, DIAL_ON, TARGET_MODE_DIAL_OFF, TARGET_MODE_DIAL_ON, INVISIBLE };
	static public enum movementInterpolation { LINEAR, OVERSHOOT, EXPONENTIAL};
	static public enum UILanguage { PT, EN};

	/* Constants */
	
	/* user interface flyout margins */
	public static final int dialDiameter = 700;
	public static final int flowIconsDiameter = 150;
	public static final int secondaryMargin = 200;
	public static float downOffset=100;
	
	/* vibration */
	static public int buttonTapVibrationDuration=200;
	static public int buttonSOSVibrationDuration=500;
	static public int combatModeVibrationDuration=1000;
	
	/* Animation constants */
	public static final int secondaryViewAnimationDuration = 400;
	public static final int secondaryViewAnimationPollingInterval = 16;
	
	/* time under operation */
	static public int timerUpdateDelta = 1000; // 5 seconds by default, can be changed later
    
    /* distance measurement */
	static public float incrementDelta=150.0f;
    
    /* combat mode */
	static public long combatModeCircleAnimationStartOffset = 1000;
	static public long combatModeCircleAnimationDuration = 4000;
	static public long combatModeCircleAnimationOutDuration = 250;
	
	/* movement handling */
	public static final int dialAnimationPollingInterval = 10;
	public static final int dialReturningAnimationDuration = 200;
	public static final int dialMovementMaxRadius = dialDiameter/2;
	public static final int iconSnapRadius=100;
	public static final long dialAnimationInDuration = 300;
}
