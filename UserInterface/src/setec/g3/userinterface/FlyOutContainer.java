package setec.g3.userinterface;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import setec.g3.communication.CommEnumerators;
import setec.g3.communication.Message;
import setec.g3.maincontroller.MainUI;
import setec.g3.ui.R;
import setec.g3.userinterface.InterfaceStatusEnumerators.*;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Vibrator;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Scroller;

public class FlyOutContainer extends RelativeLayout {
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * USER INTERFACE VARIABLES
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/* context */
	Context rootContext;
	Activity rootParent;
	MainUI parentClass;
	
	/* settings */
	private boolean showHelp=true;
	
	/* screen */
	float center[]={0,0};
	float screenWH[]={0,0};
	
	/* References to groups contained in this view. */
	private View lineOfFireView;
	private View messageView;
		/* messages */
		private ListView messageList;
		private ArrayList<MessageItem> messageItemValues;
		private MessageItemArrayAdapter messageAdapter;
		/* pre defined messages */
		private ExpandableListView preDefinedMessageList;
		SparseArray<preDefinedMessageGroup> preDefinedMessages;
		ExpandablePreDefinedMessageListAdapter preDefinedMessagesAdapter;
	private View settingsView;
	private View preDefinedMessageView;
	private View primaryView;
	private View combatModeView;
	
	/* messaging */
	private LinearLayout quickScreenView;

	/* possible view states */
	public enum secondaryViewState { CLOSED, OPEN, CLOSING, OPENING	};

	/* Position information attributes */
	protected int currentPrimaryHorizontalOffset = 0;
	protected int currentPrimaryVerticalOffset = 0;
	protected secondaryViewState lineOfFireCurrentState = secondaryViewState.CLOSED;
	protected secondaryViewState messageCurrentState = secondaryViewState.CLOSED;
	protected secondaryViewState settingsCurrentState = secondaryViewState.CLOSED;
	protected secondaryViewState preDefMessageCurrentState = secondaryViewState.CLOSED;
	protected secondaryViewState combatModeCurrentState = secondaryViewState.CLOSED;
	
	/* movement handling */
	private actioDialStateEnum actioDialCurrentState=actioDialStateEnum.RETURNING;
	private float actionDialBaseX,actionDialBaseY=0.0f;
	private float x,y=0.0f;
	protected Runnable dialAnimationRunnable = new DialAnimationRunnable();
	protected Handler dialAnimationHandler = new Handler();
	
	/* quick screen */
	private ImageView userRank;
	private TextView timeUnderOperation;
	private TextView heartRateValue;
	private ImageView heartRateIcon;
	protected Handler heartbeatAnimationHandler = new Handler();
	protected Runnable heartbeatAnimationRunnable  = new HeartBeatAnimationRunnable();
		private int heartRate;
		private long heartRateDelta;
	private TextView quickScreenMainText;
		
	/* combat mode */
	protected Handler combatModeAnimationHandler = new Handler();
	protected Runnable combatModeAnimationRunnable  = new CombatModeAnimationRunnable();
	public boolean combatMode=false;
	private ImageView combatModeCircle;
	private boolean combatModeCircleAnimationRunnableQeued=false;
	private boolean combatModeCircleAnimationOngoing=false;
	private ImageView combatModeOutImageBtn;
	private ImageView combatModeCircleOut;
	
	/* status indicators */
	private indicatorStates batteryStatus=indicatorStates.MEDIUM;
	private indicatorStates radioBatteryStatus=indicatorStates.MEDIUM;
	private indicatorStates wifiStatus=indicatorStates.MEDIUM;
	private indicatorStates bluetoothStatus=indicatorStates.EMPTY;
	
	/* time under operation */
    private Handler timeUnderOperationHandler = new Handler();
    private timeUnderOperationRunnable timerUpdater = new timeUnderOperationRunnable();
    private Date timeUnderOperationDate;
    private Date timeUnderOperationDateStart;
    	private TimerStates timerUnderOperationState=TimerStates.RESETED;
    	
	/* distance measurement */
    public ImageView lineOfFireSituationSelector;
    public Button lineOfFireSituationSenderSelector;
    private Button btnDistanceThousandsUp, btnDistanceThousandsDown;
    private Button btnDistanceHundredsUp, btnDistanceHundredsDown;
    private Button btnDistanceDozensUp, btnDistanceDozensDown;
    private Button btnDistanceUnitsUp, btnDistanceUnitsDown;
    private TextView thousandsText, hundredsText, dozensText, unitsText;
    private int thousands=0;
    private int hundreds=0;
    private int dozens=0;
    private int units=0;
    private float distanceIndicatorTouchSpot=0.0f;

	/* animation objects */
    protected Scroller secondaryViewHorizontalAnimationScroller = new Scroller(this.getContext(), new SmoothInterpolator());
    protected Scroller secondaryViewVerticalAnimationScroller = new Scroller(this.getContext(), new SmoothInterpolator()); 
	protected Runnable secondaryViewAnimationRunnable = new AnimationRunnable();
	protected Handler secondaryViewAnimationHandler = new Handler();
	private boolean isAnimationVertical;
	private boolean scheduleGoToLineOfFire=false;
	private boolean scheduleGoToMessages=false;
	private boolean scheduleGoToPreDefinedMessages=false;
	private boolean scheduleGoToSettings=false;
	
	/* Status indicators */
	private ImageView indiatorBattery;
	private ImageView indiatorRadioBattery;
	private ImageView indiatorWifi;
	private ImageView indiatorBluetooth;
	private ImageView indiatorGps;
	
	/* vibration */
    private Vibrator vibrator;
	
	/* action dial */
	public ImageView actionDial;
	private float actionDialWidth,actionDialHeight;
	
	/* dial base */
	private ImageView actionDialCircle;
	private float actionDialCircleWidth,actionDialCircleHeight;
	private float actionDialCircleBaseX,actionDialCircleBaseY;
	private float distanceFromCenter;
	
	/* dial line of fire icon (N) */
	private ImageView lineOfFireIcon;
	private float lineOfFireIconWidth, lineOfFireIconHeight;
	private float lineOfFireIconBaseX, lineOfFireIconBaseY;
	
	/* dial settings icon (S) */
	private ImageView settingsIcon;
	private float settingsIconWidth, settingsIconHeight;
	private float settingsIconBaseX, settingsIconBaseY;
	
	/* dial messages icon (E) */
	private ImageView messagesIcon;
	private float messagesIconWidth, messagesIconHeight;
	private float messagesIconBaseX, messagesIconBaseY;
	
	/* right messaging view stuff */
	private Button prioritySelector, sendMessage;
	private PriorityLevel currentPriorityLevel=PriorityLevel.NORMAL;
	private EditText messageTextBox;
	
	/* dial pre defined messages icon (W) */
	private ImageView preDefMessagesIcon;
	private float preDefMessagesIconWidth, preDefMessagesIconHeight;
	private float preDefMessagesIconBaseX, preDefMessagesIconBaseY;
	
	/* selection state */
	private dialDisplayState dialState = dialDisplayState.OFF; 
	private dialSelectionSate dialSelection = dialSelectionSate.UNSELECTED;
	
	/* flow buttons */
	private Button backToMainBtn, backToLineOfFireBtn, backToMessageBtn, backToSettingsBtn, backToPreDefinedMessageBtn;
	private float backBtnX, backBtnY;
	private float backToOption1BtnX, backToOption1BtnY;
	private float backToOption2BtnX, backToOption2BtnY;
	private float backToOption3BtnX, backToOption3BtnY;
	
	/* Dial Text */
	private TextView dialText;
	float dialTextBaseX, dialTextBaseY;
	float dialTextWidth, dialTextHeight;
	
	/* Compass */
	private ImageView compass;
	private boolean targetMode=false;
	private compassState currentCompassState = compassState.DIAL_OFF;
	private float compassBaseX, compassBaseY;
	private boolean compassEnabled=true;
	public ImageView compassEnablerBtn;
	public TextView compassEnablerTxt;
	
	/* sos slider */
	public ImageView sosSliderBase, sosHandle;
	private float sosHandleBaseX, sosHandleBaseY;
	private float sosSliderBaseX, sosSliderBaseY;
	
	 /* to control animation timing */
	public long actionDialStartTime=0;
	public float actionDialStartX, actionDialStartY=0;
	public long iconsStartTime=0;
	public movementInterpolation dialInterpolation = movementInterpolation.OVERSHOOT;
	
	/* text to speech */
	public ImageView voiceOverBtn;
	private TextView voiceOverTxt;
	
	/* UI language */
	public ImageView languageSelector;
	public TextView languageSelectorText;
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	
	
	
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * USER INTERFACE INITIALISATION
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/*
	 * Do not alter
	 */
	public FlyOutContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public FlyOutContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public FlyOutContainer(Context context) {
		super(context);
	}
	public int getSideMargin(){
		return InterfaceStatusEnumerators.secondaryMargin;
	}
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		rootContext=this.getContext();
	}
	/*
	 * for this view to access the current activity. Set by the controller
	 */
	public void setParentActivity(Activity parent){
		rootParent=parent;
		parentClass=(MainUI) parent;
	}	
	/*
	 * to find the views components from xml
	 */
	public void attachComponents(){
		/* the views */
		this.messageView = this.getChildAt(0);
		this.settingsView = this.getChildAt(1);
		this.preDefinedMessageView = this.getChildAt(2);
		this.lineOfFireView = this.getChildAt(3);
		this.combatModeView = this.getChildAt(4);
		
		this.primaryView = this.getChildAt(5);
		this.primaryView.setBackgroundResource(R.drawable.background);
		this.primaryView.bringToFront();
		
		/* action dial */
		dialText = (TextView)  findViewById(R.id.main_text);
		actionDial = (ImageView) findViewById(R.id.action_dial_iv);
		actionDialCircle = (ImageView) findViewById(R.id.action_dial_circle_iv);
		
		/* action dial icons */
		lineOfFireIcon = (ImageView) findViewById(R.id.line_of_fire_iv);
		messagesIcon = (ImageView) findViewById(R.id.messages_iv);
		settingsIcon = (ImageView) findViewById(R.id.settings_iv);
		preDefMessagesIcon = (ImageView) findViewById(R.id.pre_defined_messages_iv);
		
		/* back buttons */
		backToMainBtn = (Button) findViewById(R.id.back_selector);
		backToLineOfFireBtn = (Button) findViewById(R.id.line_of_fire_selector);
		backToSettingsBtn = (Button) findViewById(R.id.settings_selector);
		backToPreDefinedMessageBtn = (Button) findViewById(R.id.pre_defined_message_selector);
		backToMessageBtn = (Button) findViewById(R.id.message_selector);
		
		/* sos slider */
		sosSliderBase = (ImageView) findViewById(R.id.sos_slider_iv);
		sosHandle = (ImageView) findViewById(R.id.sos_slider_handle_iv);
		
		/* user rank icon */
		userRank = (ImageView) findViewById(R.id.fireman_rank);
		
		/* quick screen */
		timeUnderOperation = (TextView)  findViewById(R.id.time_under_operation);
		quickScreenMainText = (TextView)  findViewById(R.id.quick_screen_main_text);
		
		/* heart rate */
		heartRateValue = (TextView)  findViewById(R.id.heart_rate);
		heartRateIcon = (ImageView) findViewById(R.id.heart_rate_icon);
		
		/* compass */
		compass = (ImageView) findViewById(R.id.compass_iv);
	    compassEnablerBtn = (ImageView) findViewById(R.id.compass_enabler_button);
	    compassEnablerTxt = (TextView) findViewById(R.id.compass_enabler_text);
		
		/* messages */
		quickScreenMainText = (TextView)  findViewById(R.id.quick_screen_main_text);
		quickScreenView = (LinearLayout) findViewById(R.id.quick_screen_view);
		messageList = (ListView) findViewById(R.id.listview);
		messageItemValues = new ArrayList<MessageItem>();
		messageAdapter = new MessageItemArrayAdapter(this.getContext(), messageItemValues);
		messageList.setAdapter(messageAdapter);
		prioritySelector = (Button) findViewById(R.id.priority_selector);
		sendMessage = (Button) findViewById(R.id.btn_send_message);
		messageTextBox = (EditText) findViewById(R.id.message_text_box);
		
		/* pre defined message */
		preDefinedMessageList = (ExpandableListView) findViewById(R.id.pre_defined_messages_list_view);
		preDefinedMessages = new SparseArray<preDefinedMessageGroup>();
		preDefinedMessagesAdapter = new ExpandablePreDefinedMessageListAdapter(this.getContext(), preDefinedMessages);
		preDefinedMessageList.setAdapter(preDefinedMessagesAdapter);
		
		/* status indicators */
		indiatorBattery = (ImageView) findViewById(R.id.indicator_battery);
		indiatorRadioBattery = (ImageView) findViewById(R.id.indicator_radio_battery);
		indiatorWifi = (ImageView) findViewById(R.id.indicator_wifi);
		indiatorBluetooth = (ImageView) findViewById(R.id.indicator_bluetooth);
		indiatorGps = (ImageView) findViewById(R.id.indicator_gps);
		
		/* combat mode */
		combatModeCircle = (ImageView) findViewById(R.id.combat_mode_circle);
		combatModeCircleOut = (ImageView) findViewById(R.id.combat_mode_circle_animation_out);
		combatModeOutImageBtn = (ImageView) findViewById(R.id.combat_mode_out);
		
		/* distance to line of fire */
		btnDistanceThousandsUp = (Button) findViewById(R.id.thousands_up);
		btnDistanceThousandsDown = (Button) findViewById(R.id.thousands_down);
	    btnDistanceHundredsUp = (Button) findViewById(R.id.hundreds_up);
	    btnDistanceHundredsDown = (Button) findViewById(R.id.hundreds_down);
	    btnDistanceDozensUp = (Button) findViewById(R.id.dozens_up);
	    btnDistanceDozensDown = (Button) findViewById(R.id.dozens_down);
	    btnDistanceUnitsUp = (Button) findViewById(R.id.units_up);
	    btnDistanceUnitsDown = (Button) findViewById(R.id.units_down);
	    thousandsText = (TextView)  findViewById(R.id.thousands);
	    hundredsText = (TextView)  findViewById(R.id.hundreds);
	    dozensText = (TextView)  findViewById(R.id.dozens);
	    unitsText = (TextView)  findViewById(R.id.units);
	    lineOfFireSituationSelector = (ImageView) findViewById(R.id.btn_line_of_fire_situation);
	    lineOfFireSituationSenderSelector = (Button) findViewById(R.id.btn_line_of_fire_situation_send);
	    
	    /* text to speech */
	    voiceOverBtn = (ImageView) findViewById(R.id.text_to_speech_enabler_button);
		voiceOverTxt = (TextView)  findViewById(R.id.text_to_speech_enabler_text);
		
		/* language selection */
		languageSelector = (ImageView) findViewById(R.id.language_selector);
		languageSelectorText = (TextView) findViewById(R.id.language_selector_text);
	}
	/*
	 * to set the UI
	 */
	 public void prepareUI(){
		
		/* dial base */
		RelativeLayout.LayoutParams paramsDialer = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsDialer.addRule(RelativeLayout.CENTER_HORIZONTAL);
		paramsDialer.addRule(RelativeLayout.CENTER_VERTICAL);
		paramsDialer.height = InterfaceStatusEnumerators.dialDiameter;
		paramsDialer.width = InterfaceStatusEnumerators.dialDiameter;
	    actionDialCircle.setLayoutParams(paramsDialer);
	    actionDialCircle.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	    actionDialCircle.setImageResource(R.drawable.dialer_base);
	    actionDialCircleWidth=actionDialCircle.getLayoutParams().width;
		actionDialCircleHeight=actionDialCircle.getLayoutParams().height;
		actionDialCircleBaseX = center[0] - actionDialCircleWidth/2.0f;
		actionDialCircleBaseY = center[1]+InterfaceStatusEnumerators.downOffset - actionDialCircleHeight/2.0f;
		actionDialCircle.setVisibility(View.INVISIBLE);
		distanceFromCenter=paramsDialer.width/2;
		
		/* action dial */
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    params.addRule(RelativeLayout.CENTER_VERTICAL);
	    params.height = 160;
	    params.width = 160;
	    actionDial.setLayoutParams(params);
		actionDial.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		actionDial.setImageResource(R.drawable.dial_off);
		actionDialWidth=actionDial.getLayoutParams().width;
		actionDialHeight=actionDial.getLayoutParams().height;
		actionDialBaseX = center[0] - actionDialWidth/2.0f;
		actionDialBaseY = center[1]+InterfaceStatusEnumerators.downOffset - actionDialHeight/2.0f;
		x=actionDialBaseX;
		y=actionDialBaseY;
		
		/* icons parameters and Images */
		RelativeLayout.LayoutParams paramsDialerIcons = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	    float scaleFactor=Math.abs((float)paramsDialer.width)/500f;
		paramsDialerIcons.height = (int)(100f*scaleFactor);
		paramsDialerIcons.width = (int)(100f*scaleFactor);
		lineOfFireIcon.setLayoutParams(paramsDialerIcons);
		lineOfFireIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		lineOfFireIcon.setImageResource(R.drawable.option_line_fire);
		lineOfFireIcon.setVisibility(View.INVISIBLE);
		
		messagesIcon.setLayoutParams(paramsDialerIcons);
		messagesIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		messagesIcon.setImageResource(R.drawable.option_message);
		messagesIcon.setVisibility(View.INVISIBLE);
		
		settingsIcon.setLayoutParams(paramsDialerIcons);
		settingsIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		settingsIcon.setImageResource(R.drawable.option_settings);
		settingsIcon.setVisibility(View.INVISIBLE);
		
		preDefMessagesIcon.setLayoutParams(paramsDialerIcons);
		preDefMessagesIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		preDefMessagesIcon.setImageResource(R.drawable.option_def_message);
		preDefMessagesIcon.setVisibility(View.INVISIBLE);
		
		/* flow buttons parameters */
		RelativeLayout.LayoutParams paramsFlowIcons = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsFlowIcons.height = InterfaceStatusEnumerators.flowIconsDiameter;
		paramsFlowIcons.width = InterfaceStatusEnumerators.flowIconsDiameter;
		backToMainBtn.setLayoutParams(paramsFlowIcons);
		backToMainBtn.setVisibility(View.INVISIBLE);
		
		backToLineOfFireBtn.setLayoutParams(paramsFlowIcons);
		backToLineOfFireBtn.setVisibility(View.INVISIBLE);
		
		backToSettingsBtn.setLayoutParams(paramsFlowIcons);
		backToSettingsBtn.setVisibility(View.INVISIBLE);
		
		backToPreDefinedMessageBtn.setLayoutParams(paramsFlowIcons);
		backToPreDefinedMessageBtn.setVisibility(View.INVISIBLE);
		
		backToMessageBtn.setLayoutParams(paramsFlowIcons);
		backToMessageBtn.setVisibility(View.INVISIBLE);
		
		backBtnX=0;
		backBtnY=0;
		backToOption1BtnX=0;
		backToOption1BtnY=0;
		backToOption2BtnX=0;
		backToOption2BtnY=0;
		backToOption3BtnX=0;
		backToOption3BtnY=0;
		
		/* right message view */
		prioritySelector.setLayoutParams(paramsFlowIcons);
		prioritySelector.setVisibility(View.INVISIBLE);
		//sendMessage.setLayoutParams(paramsFlowIcons);
		
		/* dial text parameters */
		RelativeLayout.LayoutParams paramsDialerText = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsDialerText.height = 400;
		paramsDialerText.width = 400;
		dialText.setLayoutParams(paramsDialerText);
		dialText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		dialTextBaseX=center[0] - (paramsDialerText.width/2);
		dialTextBaseY=center[1]+InterfaceStatusEnumerators.downOffset - (paramsDialerText.height/2);
		dialTextWidth=paramsDialerText.width;
		dialTextHeight=paramsDialerText.height;
		dialText.setVisibility(View.INVISIBLE);
		
		/* icons positions, widths and height */
		lineOfFireIconBaseX=actionDialBaseX + ( (params.width-paramsDialerIcons.width) / 2 );
		lineOfFireIconBaseY=actionDialBaseY-distanceFromCenter + ( (params.height-paramsDialerIcons.height) / 2 );
		lineOfFireIconWidth=paramsDialerIcons.width;
		lineOfFireIconHeight=paramsDialerIcons.height;
		
		settingsIconBaseX=actionDialBaseX + ( (params.width-paramsDialerIcons.width) / 2 );
		settingsIconBaseY=actionDialBaseY+distanceFromCenter + ( (params.height-paramsDialerIcons.height) / 2 );
		settingsIconWidth=paramsDialerIcons.width;
		settingsIconHeight=paramsDialerIcons.height;
		
		messagesIconBaseX=actionDialBaseX+distanceFromCenter + ( (params.width-paramsDialerIcons.width) / 2 );
		messagesIconBaseY=actionDialBaseY + ( (params.height-paramsDialerIcons.height) / 2 );
		messagesIconWidth=paramsDialerIcons.width;
		messagesIconHeight=paramsDialerIcons.height;
		
		preDefMessagesIconBaseX=actionDialBaseX-distanceFromCenter + ( (params.width-paramsDialerIcons.width) / 2 );
		preDefMessagesIconBaseY=actionDialBaseY + ( (params.height-paramsDialerIcons.height) / 2 );
		preDefMessagesIconWidth=paramsDialerIcons.width;
		preDefMessagesIconHeight=paramsDialerIcons.height;
		
		/* sos slider */
		RelativeLayout.LayoutParams sliderBase = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		sliderBase.height = 170;
		sliderBase.width = 560;
		sosSliderBase.setLayoutParams(sliderBase);
		sosSliderBase.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		sosSliderBaseX=center[0] - (sliderBase.width/2);
		sosSliderBaseY=center[1]*2 - sliderBase.height*2f;
		RelativeLayout.LayoutParams paramsSosHandle = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsSosHandle.height = sliderBase.height-10;
		paramsSosHandle.width = sliderBase.height-10;
		sosHandle.setLayoutParams(paramsSosHandle);
		sosHandle.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		sosHandleBaseX=sosSliderBaseX + (sliderBase.height-paramsSosHandle.height)/2;
		sosHandleBaseY=sosSliderBaseY + (sliderBase.height-paramsSosHandle.height)/2;
		sosSliderBase.setX(sosSliderBaseX);
		sosSliderBase.setY(sosSliderBaseY);
		sosSliderBase.setImageResource(R.drawable.sos_slider_off);
		sosHandle.setX(sosHandleBaseX);
		sosHandle.setY(sosHandleBaseY);
		sosHandle.setImageResource(R.drawable.sos_slider_handler_off);
		
		/* combat mode circle */
		combatModeCircle.setLayoutParams(paramsDialer);
		combatModeCircle.setImageResource(R.drawable.combat_mode_circle);
		combatModeCircle.setScaleType(ImageView.ScaleType.FIT_CENTER);
		combatModeCircle.setVisibility(View.INVISIBLE);

		/* ordering on the z axis */
		actionDial.bringToFront();
		lineOfFireIcon.bringToFront();
		messagesIcon.bringToFront();
		settingsIcon.bringToFront();
		preDefMessagesIcon.bringToFront();	
		dialText.bringToFront();
		
		/* compass */
		RelativeLayout.LayoutParams paramsCompass = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramsCompass.height = 550;
		paramsCompass.width = 550;
		compass.setLayoutParams(paramsCompass);
		compass.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		compassBaseX=center[0] - (paramsCompass.width/2);
		compassBaseY=center[1]+InterfaceStatusEnumerators.downOffset - (paramsCompass.height/2);
		updateCompassAppearance();
		setCompassAngle(90.0f);
		
		/* heart rate */
		setHeartRate(120);
		heartbeatAnimationHandler.postDelayed(heartbeatAnimationRunnable, heartRateDelta);
	}
	 /*
	  * to hide currently unnecessary views
	  */
	  public void resetViewsVisibility(){
			this.messageView.setVisibility(View.GONE);
			this.settingsView.setVisibility(View.GONE);
			this.preDefinedMessageView.setVisibility(View.GONE);
			this.lineOfFireView.setVisibility(View.GONE);
			this.combatModeView.setVisibility(View.GONE);
	 }	
	 /*
	  * to reset UI state
	  */
	 public void resetUI(){
			
		/* reseting positions */
		actioDialCurrentState=actioDialStateEnum.RETURNING;
		dialAnimationHandler.postDelayed(dialAnimationRunnable, InterfaceStatusEnumerators.dialAnimationPollingInterval);
	} 
	/*
	 * Method to set screen measurements
	 */
	public void setScreenData(float []wh, float []centerCoords){
		center[0]=centerCoords[0];
		center[1]=centerCoords[1];
		screenWH[0]=wh[0];
		screenWH[1]=wh[1];
	}
	/*
	 * to set the components listeners
	 */
	public void setListeners(){
		actionDial.setOnTouchListener(
	       		new RelativeLayout.OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction()){
							case MotionEvent.ACTION_DOWN:
								vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
								actioDialCurrentState=actioDialStateEnum.START;
								x=event.getRawX();
								y=event.getRawY();
								if(compassEnabled){
									setCompassMode(compassState.DIAL_ON);
								} else {
									setCompassMode(compassState.INVISIBLE);
								}
								combatModeAnimationHandler.postDelayed(combatModeAnimationRunnable, InterfaceStatusEnumerators.combatModeCircleAnimationStartOffset);
								combatModeCircleAnimationRunnableQeued=true;
								break;
							case MotionEvent.ACTION_MOVE:
								if(actioDialCurrentState==actioDialStateEnum.MOVING){
									x=event.getRawX();
									y=event.getRawY();
								}
								break;
							case MotionEvent.ACTION_UP:
								actioDialCurrentState=actioDialStateEnum.RETURNING;
								setAnimationStartConditions();
								actionDialClean();
								dialState=dialDisplayState.CLOSING;
								switch (dialSelection){
									case LINE_OF_FIRE:
										toggleLineOfFireView();
										vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
										setCompassMode(compassState.INVISIBLE);
										actionDial.setVisibility(View.INVISIBLE);
										break;
									case MESSAGE:
										toggleMessagesView();
										vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
										setCompassMode(compassState.INVISIBLE);
										actionDial.setVisibility(View.INVISIBLE);
										break;
									case SETTINGS:
										toggleSettingsView();
										vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
										setCompassMode(compassState.INVISIBLE);
										actionDial.setVisibility(View.INVISIBLE);
										break;
									case PRE_DEFINED_MESSAGES:
										togglePreDefinedMessagesView();
										vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
										setCompassMode(compassState.INVISIBLE);
										actionDial.setVisibility(View.INVISIBLE);
										break;
									case UNSELECTED:
										actionDial.setVisibility(View.VISIBLE);
										if(compassEnabled){
											setCompassMode(compassState.DIAL_OFF);
										} else {
											setCompassMode(compassState.INVISIBLE);
										}
										break;
									default:
										break;
								}
								updateSideButtonsPositionAndVisibility();
								disableCombatModeAnimation();
								break;
						}
						dialAnimationHandler.postDelayed(dialAnimationRunnable, InterfaceStatusEnumerators.dialAnimationPollingInterval);
						return true;
					}
	       		}
	    );
		backToMainBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				actionDial.setVisibility(View.VISIBLE);
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		    	switch (dialSelection){
				case LINE_OF_FIRE:
					toggleLineOfFireView();
					break;
				case MESSAGE:
					toggleMessagesView();
					hideKeyboard();
					break;
				case SETTINGS:
					toggleSettingsView();
					break;
				case PRE_DEFINED_MESSAGES:
					togglePreDefinedMessagesView();
					break;
				case UNSELECTED:
					// not supposed to happen
					break;
				default:
					// say what?
					break;
				}
		    	dialSelection=dialSelectionSate.UNSELECTED;
		    	if(compassEnabled){
		    		setCompassMode(compassState.DIAL_OFF);
		    	} else {
					setCompassMode(compassState.INVISIBLE);
		    	}
		    	updateSideButtonsPositionAndVisibility();
		    }
		});
		backToLineOfFireBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		    	switch (dialSelection){
				case LINE_OF_FIRE:
					// not supposed to happen
					break;
				case MESSAGE:
					toggleMessagesView();
					dialSelection=dialSelectionSate.UNSELECTED;
			    	//updateSideButtonsPositionAndVisibility();
					hideKeyboard();
					break;
				case SETTINGS:
					toggleSettingsView();
					dialSelection=dialSelectionSate.UNSELECTED;
			    	//updateSideButtonsPositionAndVisibility();
					break;
				case PRE_DEFINED_MESSAGES:
					togglePreDefinedMessagesView();
					dialSelection=dialSelectionSate.UNSELECTED;
			    	//updateSideButtonsPositionAndVisibility();
					break;
				case UNSELECTED:
					// not supposed to happen
					break;
				default:
					// say what?
					break;
				}
				dialSelection=dialSelectionSate.LINE_OF_FIRE;
		    	updateSideButtonsPositionAndVisibility();
		    	scheduleGoToLineOfFireAfterwards();
		    }
		});
		backToSettingsBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		    	switch (dialSelection){
				case LINE_OF_FIRE:
					toggleLineOfFireView();
					dialSelection=dialSelectionSate.UNSELECTED;
			    	//updateSideButtonsPositionAndVisibility();
					break;
				case MESSAGE:
					toggleMessagesView();
					dialSelection=dialSelectionSate.UNSELECTED;
			    	//updateSideButtonsPositionAndVisibility();
					hideKeyboard();
					break;
				case SETTINGS:
					// not supposed to happen
					break;
				case PRE_DEFINED_MESSAGES:
					togglePreDefinedMessagesView();
					dialSelection=dialSelectionSate.UNSELECTED;
			    	//updateSideButtonsPositionAndVisibility();
					break;
				case UNSELECTED:
					// not supposed to happen
					break;
				default:
					// say what?
					break;
				}
				dialSelection=dialSelectionSate.SETTINGS;
		    	updateSideButtonsPositionAndVisibility();
		    	scheduleGoToSettingsAfterwards();
		    }
		});
		backToPreDefinedMessageBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		    	switch (dialSelection){
				case LINE_OF_FIRE:
					toggleLineOfFireView();
					dialSelection=dialSelectionSate.UNSELECTED;
			    	//updateSideButtonsPositionAndVisibility();
					break;
				case MESSAGE:
					toggleMessagesView();
					dialSelection=dialSelectionSate.UNSELECTED;
			    	//updateSideButtonsPositionAndVisibility();
					hideKeyboard();
					break;
				case SETTINGS:
					toggleSettingsView();
					dialSelection=dialSelectionSate.UNSELECTED;
			    	//updateSideButtonsPositionAndVisibility();
					break;
				case PRE_DEFINED_MESSAGES:
					// not supposed to happen
					break;
				case UNSELECTED:
					// not supposed to happen
					break;
				default:
					// say what?
					break;
				}
				dialSelection=dialSelectionSate.PRE_DEFINED_MESSAGES;
		    	updateSideButtonsPositionAndVisibility();
		    	scheduleGoToPreDefinedMessagesAfterwards();
		    }
		});
		backToMessageBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		    	switch (dialSelection){
				case LINE_OF_FIRE:
					toggleLineOfFireView();
					dialSelection=dialSelectionSate.UNSELECTED;
			    	//updateSideButtonsPositionAndVisibility();
					break;
				case MESSAGE:
					// not supposed to happen
					break;
				case SETTINGS:
					toggleSettingsView();
					dialSelection=dialSelectionSate.UNSELECTED;
			    	//updateSideButtonsPositionAndVisibility();
					break;
				case PRE_DEFINED_MESSAGES:
					togglePreDefinedMessagesView();
					dialSelection=dialSelectionSate.UNSELECTED;
			    	//updateSideButtonsPositionAndVisibility();
					break;
				case UNSELECTED:
					// not supposed to happen
					break;
				default:
					// say what?
					break;
				}
				dialSelection=dialSelectionSate.MESSAGE;
		    	updateSideButtonsPositionAndVisibility();
		    	scheduleGoToMessagesAfterwards();
		    }
		});
		
		prioritySelector.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		    	switch(currentPriorityLevel){
		    		case NORMAL:
		    			prioritySelector.setBackgroundResource(R.drawable.selector_priority_normal_plus);
				    	currentPriorityLevel=PriorityLevel.NORMAL_PLUS;
				    	if(parentClass.language==UILanguage.EN){
				    		toastMessage("Priority: Above Normal",Toast.LENGTH_SHORT, 0, 0);
				    	} else if (parentClass.language==UILanguage.PT){
				    		toastMessage("Prioridade: Acima do Normal",Toast.LENGTH_SHORT, 0, 0);
				    	}
		    			break;
		    		case NORMAL_PLUS:
		    			prioritySelector.setBackgroundResource(R.drawable.selector_priority_important);
				    	currentPriorityLevel=PriorityLevel.IMPORTANT;
				    	if(parentClass.language==UILanguage.EN){
				    		toastMessage("Priority: Important",Toast.LENGTH_SHORT, 0, 0);
				    	} else if (parentClass.language==UILanguage.PT){
				    		toastMessage("Prioridade: Importante",Toast.LENGTH_SHORT, 0, 0);
				    	}
		    			break;
		    		case IMPORTANT:
		    			prioritySelector.setBackgroundResource(R.drawable.selector_priority_critical);
				    	currentPriorityLevel=PriorityLevel.CRITICAL;
				    	if(parentClass.language==UILanguage.EN){
				    		toastMessage("Priority: Critical",Toast.LENGTH_SHORT, 0, 0);
				    	} else if (parentClass.language==UILanguage.PT){
				    		toastMessage("Prioridade: Crítica",Toast.LENGTH_SHORT, 0, 0);
				    	}
		    			break;
					case CRITICAL:
						prioritySelector.setBackgroundResource(R.drawable.selector_priority_normal);
				    	currentPriorityLevel=PriorityLevel.NORMAL;
				    	if(parentClass.language==UILanguage.EN){
				    		toastMessage("Priority: Normal",Toast.LENGTH_SHORT, 0, 0);
				    	} else if (parentClass.language==UILanguage.PT){
				    		toastMessage("Prioridade: Normal",Toast.LENGTH_SHORT, 0, 0);
				    	}
						break;
					default:
						// inexistent case 
						break;
		    	}
		    }
		});
		sendMessage.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
			    if(messageTextBox.getText().toString().trim().length()>0){
			    	String msg = messageTextBox.getText().toString().trim();
				    messageTextBox.setText("");
				    sendMessage(msg);
			    }
		    }
		});
		timeUnderOperation.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		    	toggleUnderOperationTimer();
		    }
		});
		sosHandle.setOnTouchListener(
	       		new RelativeLayout.OnTouchListener() {
	       			public boolean onTouch(View v, MotionEvent m) {
	       				switch (m.getAction()){
		       				case MotionEvent.ACTION_DOWN:
								vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		       					x=m.getRawX();
		       					x=clampValue(x-sosHandle.getLayoutParams().width/2, sosHandleBaseX, sosHandleBaseX + sosSliderBase.getLayoutParams().width - sosHandle.getLayoutParams().width - (sosSliderBase.getLayoutParams().height-sosHandle.getLayoutParams().height));
		       					sosHandle.setX(x);
		       					sosHandle.setImageResource(R.drawable.sos_slider_handler_on);
		       					sosSliderBase.setImageResource(R.drawable.sos_slider_on);
		       					if(showHelp){
		       						if(parentClass.language==UILanguage.EN){
		       							toastMessage("Slide right to send SOS.",Toast.LENGTH_SHORT, 0, (int)(center[1]/2));
		    				    	} else if (parentClass.language==UILanguage.PT){
		    				    		toastMessage("Deslize para a direita para enviar SOS.",Toast.LENGTH_SHORT, 0, (int)(center[1]/2));
		    				    	}
		       					}
		       					break;
		       				case MotionEvent.ACTION_MOVE:
		       						x=m.getRawX();
		       						x=clampValue(x-sosHandle.getLayoutParams().width/2, sosHandleBaseX, sosHandleBaseX  + sosSliderBase.getLayoutParams().width - sosHandle.getLayoutParams().width - (sosSliderBase.getLayoutParams().height-sosHandle.getLayoutParams().height));
			       					sosHandle.setX(x);
		       					break;
		       				case MotionEvent.ACTION_UP:
		       					sosHandle.animate().translationX(sosHandleBaseX).setDuration(200).setInterpolator(new SmoothInterpolator());
		       					sosHandle.setImageResource(R.drawable.sos_slider_handler_off);
		       					sosSliderBase.setImageResource(R.drawable.sos_slider_off);
		       					if((sosHandle.getX())==sosHandleBaseX  + sosSliderBase.getLayoutParams().width - sosHandle.getLayoutParams().width - (sosSliderBase.getLayoutParams().height-sosHandle.getLayoutParams().height)){
		       						//launch SOS
		       						if(parentClass.language==UILanguage.EN){
		       							postMessage("You", "< [SOS] >", PriorityLevel.CRITICAL, true);
		       							toastMessage("SOS sent.",Toast.LENGTH_SHORT, 0, (int)(center[1]/2));
		    				    	} else if (parentClass.language==UILanguage.PT){
		    				    		postMessage("Utilizador", "< [SOS] >", PriorityLevel.CRITICAL, true);
		    				    		toastMessage("SOS enviado.",Toast.LENGTH_SHORT, 0, (int)(center[1]/2));
		    				    	}
		       						vibrate(InterfaceStatusEnumerators.buttonSOSVibrationDuration);
	       							Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_SOS);
		       					} else {
		       						/* TODO for testing purposes only*/
			       					parentClass.toggleCompassTargetMode();
		       					}
		       					break;
		       			}
		       			dialAnimationHandler.postDelayed(dialAnimationRunnable, InterfaceStatusEnumerators.dialAnimationPollingInterval);    				
	       			    return true;
	       			}
	       		}
	       );
		combatModeOutImageBtn.setOnTouchListener(
	       		new RelativeLayout.OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction()){
							case MotionEvent.ACTION_DOWN:
								//vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
								combatModeAnimationHandler.postDelayed(combatModeAnimationRunnable, InterfaceStatusEnumerators.combatModeCircleAnimationStartOffset);
								combatModeCircleAnimationRunnableQeued=true;
								if (showHelp){
									toastMessage("Hold to exit Combat Mode.",Toast.LENGTH_SHORT, 0, (int)(center[1]*2-500));
								}
								break;
							case MotionEvent.ACTION_MOVE:
								break;
							case MotionEvent.ACTION_UP:
								disableCombatModeAnimation();
								break;
						}
						return true;
					}
	       		}
	    );
		
		btnDistanceThousandsUp.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
				thousands=(thousands+1)%10;
				thousandsText.setText(Integer.toString(thousands));
		    }
		});
		btnDistanceThousandsDown.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	if(thousands>0){
					vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		    		thousands=(thousands-1)%10;
					thousandsText.setText(Integer.toString(thousands));
		    	}
		    }
		});
	    btnDistanceHundredsUp.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
				hundreds=(hundreds+1)%10;
				hundredsText.setText(Integer.toString(hundreds));
		    }
		});
	    btnDistanceHundredsDown.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	if(hundreds>0){
					vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		    		hundreds=(hundreds-1)%10;
		    		hundredsText.setText(Integer.toString(hundreds));
		    	}
		    }
		});
	    btnDistanceDozensUp.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
				dozens=(dozens+1)%10;
				dozensText.setText(Integer.toString(dozens));
		    }
		});
	    btnDistanceDozensDown.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	if(dozens>0){
					vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		    		dozens=(dozens-1)%10;
		    		dozensText.setText(Integer.toString(dozens));
		    	}
		    }
		});
	    btnDistanceUnitsUp.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
				units=(units+1)%10;
				unitsText.setText(Integer.toString(units));
		    }
		});
	    btnDistanceUnitsDown.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	if(units>0){
					vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		    		units=(units-1)%10;
		    		unitsText.setText(Integer.toString(units));
		    	}
		    }
		});
	    thousandsText.setOnTouchListener(
	       		new RelativeLayout.OnTouchListener() {
	       			public boolean onTouch(View v, MotionEvent m) {
	       				float increment=0;
	       				int res=0;
	       				switch (m.getAction()){
		       				case MotionEvent.ACTION_DOWN:
								vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
								distanceIndicatorTouchSpot=m.getRawY();
		       					break;
		       				case MotionEvent.ACTION_MOVE:
		       						increment=(distanceIndicatorTouchSpot-m.getRawY());
		       						res=thousands+(int)(increment/InterfaceStatusEnumerators.incrementDelta);
		       						if(res>=9){
		       							res=9;
		       						} else if(res<0){
		       							res=0;
		       						}
		       						thousandsText.setText(Integer.toString(res));
		       					break;
		       				case MotionEvent.ACTION_UP:
		       					increment=(distanceIndicatorTouchSpot-m.getRawY());
		       					thousands=thousands+(int)(increment/InterfaceStatusEnumerators.incrementDelta);
		       					if(thousands>=9){
		       						thousands=9;
	       						} else if(thousands<0){
	       							thousands=0;
	       						}
		       					thousandsText.setText(Integer.toString(thousands));
		       					break;
		       			}
		       			//dialAnimationHandler.postDelayed(dialAnimationRunnable, dialAnimationPollingInterval);    				
	       			    return true;
	       			}
	       		}
	       );
	    hundredsText.setOnTouchListener(
	       		new RelativeLayout.OnTouchListener() {
	       			public boolean onTouch(View v, MotionEvent m) {
	       				float increment=0;
	       				int res=0;
	       				switch (m.getAction()){
		       				case MotionEvent.ACTION_DOWN:
								vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
								distanceIndicatorTouchSpot=m.getRawY();
		       					break;
		       				case MotionEvent.ACTION_MOVE:
		       						increment=(distanceIndicatorTouchSpot-m.getRawY());
		       						res=hundreds+(int)(increment/InterfaceStatusEnumerators.incrementDelta);
		       						if(res>=9){
		       							res=9;
		       						} else if(res<0){
		       							res=0;
		       						}
		       						hundredsText.setText(Integer.toString(res));
		       					break;
		       				case MotionEvent.ACTION_UP:
		       					increment=(distanceIndicatorTouchSpot-m.getRawY());
		       					hundreds=hundreds+(int)(increment/InterfaceStatusEnumerators.incrementDelta);
		       					if(hundreds>=9){
		       						hundreds=9;
	       						} else if(hundreds<0){
	       							hundreds=0;
	       						}
		       					hundredsText.setText(Integer.toString(hundreds));
		       					break;
		       			}
		       			//dialAnimationHandler.postDelayed(dialAnimationRunnable, dialAnimationPollingInterval);    				
	       			    return true;
	       			}
	       		}
	       );
	    dozensText.setOnTouchListener(
	       		new RelativeLayout.OnTouchListener() {
	       			public boolean onTouch(View v, MotionEvent m) {
	       				float increment=0;
	       				int res=0;
	       				switch (m.getAction()){
		       				case MotionEvent.ACTION_DOWN:
								vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
								distanceIndicatorTouchSpot=m.getRawY();
		       					break;
		       				case MotionEvent.ACTION_MOVE:
		       						increment=(distanceIndicatorTouchSpot-m.getRawY());
		       						res=dozens+(int)(increment/InterfaceStatusEnumerators.incrementDelta);
		       						if(res>=9){
		       							res=9;
		       						} else if(res<0){
		       							res=0;
		       						}
		       						dozensText.setText(Integer.toString(res));
		       					break;
		       				case MotionEvent.ACTION_UP:
		       					increment=(distanceIndicatorTouchSpot-m.getRawY());
		       					dozens=dozens+(int)(increment/InterfaceStatusEnumerators.incrementDelta);
		       					if(dozens>=9){
		       						dozens=9;
	       						} else if(dozens<0){
	       							dozens=0;
	       						}
		       					dozensText.setText(Integer.toString(dozens));
		       					break;
		       			}
		       			//dialAnimationHandler.postDelayed(dialAnimationRunnable, dialAnimationPollingInterval);    				
	       			    return true;
	       			}
	       		}
	       );
	    unitsText.setOnTouchListener(
	       		new RelativeLayout.OnTouchListener() {
	       			public boolean onTouch(View v, MotionEvent m) {
	       				float increment=0;
	       				int res=0;
	       				switch (m.getAction()){
		       				case MotionEvent.ACTION_DOWN:
								vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
								distanceIndicatorTouchSpot=m.getRawY();
		       					break;
		       				case MotionEvent.ACTION_MOVE:
		       						increment=(distanceIndicatorTouchSpot-m.getRawY());
		       						res=units+(int)(increment/InterfaceStatusEnumerators.incrementDelta);
		       						if(res>=9){
		       							res=9;
		       						} else if(res<0){
		       							res=0;
		       						}
		       						unitsText.setText(Integer.toString(res));
		       					break;
		       				case MotionEvent.ACTION_UP:
		       					increment=(distanceIndicatorTouchSpot-m.getRawY());
		       					units=units+(int)(increment/InterfaceStatusEnumerators.incrementDelta);
		       					if(units>=9){
		       						units=9;
	       						} else if(units<0){
	       							units=0;
	       						}
		       					unitsText.setText(Integer.toString(units));
		       					break;
		       			}
		       			//dialAnimationHandler.postDelayed(dialAnimationRunnable, dialAnimationPollingInterval);    				
	       			    return true;
	       			}
	       		}
	       );
	    
	    languageSelector.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
	    		if (parentClass.language==UILanguage.PT){
	    			parentClass.language=UILanguage.EN;
	    			languageSelector.setImageResource(R.drawable.language_en);
	    			toastMessage("Language selected: EN",Toast.LENGTH_SHORT, 0, 0);
	    			languageSelectorText.setText("Language selected: EN");
	    		} else if(parentClass.language==UILanguage.EN){
	    			parentClass.language=UILanguage.PT;
	    			toastMessage("Língua selecionada: PT",Toast.LENGTH_SHORT, 0, 0);
	    			languageSelector.setImageResource(R.drawable.language_pt);
	    			languageSelectorText.setText("Língua selecionada: PT");
	    		}
	    		invalidate();
		    }
		});
	}
	/*
	 * to initialize the message views, both normal and predefined
	 */
	public void initialiseMessagingInterface(){
		/* messages example */
		if(parentClass.language==UILanguage.EN){
			postMessage("Message Log", "No new messages yet.", PriorityLevel.CRITICAL, true);
    	} else if (parentClass.language==UILanguage.PT){
    		postMessage("Log de Mensagens", "Sem mensagens novas.", PriorityLevel.CRITICAL, true);
    	}
	
		/* predefined messages initialization */
		createPredefinedMessagesList();
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * DISTANCE TO LINE OF FIRE
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/*
	 * to reset the distance to line of fire indication
	 */
	public void resetDistance(){
		units=0;
    	dozens=0;
    	hundreds=0;
    	thousands=0;
    	unitsText.setText(Integer.toString(units));
    	dozensText.setText(Integer.toString(dozens));
    	hundredsText.setText(Integer.toString(hundreds));
    	thousandsText.setText(Integer.toString(thousands));
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	

	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * VIBRATION FEATURE
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/*
	 * set by the MainUI class to allow the user interface to vibrate
	 */
	public void setVibrator(Vibrator v){
		this.vibrator=v;
	}
	/*
	 * issues a vibration
	 */
	public void vibrate(long duration){
		vibrator.vibrate(duration);
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * VIEW TOGGLE ANIMATION
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/*
	 * This method is used by the animation runnable to adjust the view position
	 */
	private void adjustContentPosition(boolean isAnimationOngoing) {
		int scrollerHorizontalOffset = this.secondaryViewHorizontalAnimationScroller.getCurrX();
		int scrollerVerticalOffset = this.secondaryViewVerticalAnimationScroller.getCurrY();

		this.primaryView.offsetLeftAndRight(scrollerHorizontalOffset - this.currentPrimaryHorizontalOffset);
		this.primaryView.offsetTopAndBottom(scrollerVerticalOffset - this.currentPrimaryVerticalOffset);

		this.currentPrimaryHorizontalOffset = scrollerHorizontalOffset;
		this.currentPrimaryVerticalOffset = scrollerVerticalOffset;

		this.invalidate();
		
		if (isAnimationOngoing)
			this.secondaryViewAnimationHandler.postDelayed(this.secondaryViewAnimationRunnable, InterfaceStatusEnumerators.secondaryViewAnimationPollingInterval);
		else
			this.onSecondaryViewTransitionComplete();
	}
	/*
	 * The procedures to execute on a transition animation completion
	 */
	private void onSecondaryViewTransitionComplete() {
		switch (this.lineOfFireCurrentState) {
		case OPEN:
			break;
		case CLOSED:
			break;
		case OPENING:
			this.lineOfFireCurrentState = secondaryViewState.OPEN;
			break;
		case CLOSING:
			this.lineOfFireCurrentState = secondaryViewState.CLOSED;
			this.lineOfFireView.setVisibility(View.GONE);
			break;
		default:
			return;
		}
		
		switch (this.messageCurrentState) {
		case OPEN:
			break;
		case CLOSED:
			break;
		case OPENING:
			this.messageCurrentState = secondaryViewState.OPEN;
			//postMessage();
			break;
		case CLOSING:
			this.messageCurrentState = secondaryViewState.CLOSED;
			this.messageView.setVisibility(View.GONE);
			break;
		default:
			return;
		}
		
		switch (this.settingsCurrentState) {
		case OPEN:
			break;
		case CLOSED:
			break;
		case OPENING:
			this.settingsCurrentState = secondaryViewState.OPEN;
			break;
		case CLOSING:
			this.settingsCurrentState = secondaryViewState.CLOSED;
			this.settingsView.setVisibility(View.GONE);
			break;
		default:
			return;
		}
		
		switch (this.preDefMessageCurrentState) {
		case OPEN:
			break;
		case CLOSED:
			break;
		case OPENING:
			this.preDefMessageCurrentState = secondaryViewState.OPEN;
			break;
		case CLOSING:
			this.preDefMessageCurrentState = secondaryViewState.CLOSED;
			this.preDefinedMessageView.setVisibility(View.GONE);
			break;
		default:
			return;
		}
		
		switch (this.combatModeCurrentState) {
		case OPEN:
			break;
		case CLOSED:
			break;
		case OPENING:
			this.combatModeCurrentState = secondaryViewState.OPEN;
			break;
		case CLOSING:
			this.combatModeCurrentState = secondaryViewState.CLOSED;
			this.combatModeView.setVisibility(View.GONE);
			break;
		default:
			return;
		}
		 
		if(scheduleGoToLineOfFire==true){
			scheduleGoToLineOfFire=false;
			this.toggleLineOfFireView();
		} else if(scheduleGoToMessages==true){
			scheduleGoToMessages=false;
			this.toggleMessagesView();
		} else if(scheduleGoToPreDefinedMessages==true){
			scheduleGoToPreDefinedMessages=false;
			this.togglePreDefinedMessagesView();
		} else if(scheduleGoToSettings==true){
			scheduleGoToSettings=false;
			this.toggleSettingsView();
		}
	}
	/*
	 * These methods schedule an animation to go directly into another view
	 * without stopping on themain view
	 */
	public void scheduleGoToMessagesAfterwards(){
		scheduleGoToMessages=true;
		quickScreenView.setVisibility(View.INVISIBLE);
	}
	public void scheduleGoToPreDefinedMessagesAfterwards(){
		scheduleGoToPreDefinedMessages=true;
		quickScreenView.setVisibility(View.INVISIBLE);
	}
	public void scheduleGoToLineOfFireAfterwards(){
		scheduleGoToLineOfFire=true;
		quickScreenView.setVisibility(View.INVISIBLE);
	}
	public void scheduleGoToSettingsAfterwards(){
		scheduleGoToSettings=true;
		quickScreenView.setVisibility(View.INVISIBLE);
	}
	/*
	 * the interpolator that calculates the position according to the elapsed time
	 */
	static public class SmoothInterpolator implements Interpolator{
		@Override
		public float getInterpolation(float t) {
			return (float)Math.pow(t-1, 5) + 1;
		}
	}
	/*
	 * the runnable that adjusts the views position
	 */
	protected class AnimationRunnable implements Runnable {
		@Override
		public void run() {
			if(isAnimationVertical){
				FlyOutContainer.this.adjustContentPosition(FlyOutContainer.this.secondaryViewVerticalAnimationScroller.computeScrollOffset());
			} else {
				FlyOutContainer.this.adjustContentPosition(FlyOutContainer.this.secondaryViewHorizontalAnimationScroller.computeScrollOffset());
			}
		}
	}
	/*
	 * The view toggling methods
	 */
	public void toggleCombatModeView(){
		switch (this.combatModeCurrentState) {
		case CLOSED:
			this.combatModeCurrentState = secondaryViewState.OPENING;
			this.combatModeView.setVisibility(View.VISIBLE);
			this.secondaryViewVerticalAnimationScroller.startScroll(0, 0, 0, this.combatModeView.getLayoutParams().height, InterfaceStatusEnumerators.secondaryViewAnimationDuration);	
			break;
		case OPEN:
			this.combatModeCurrentState = secondaryViewState.CLOSING;
			this.secondaryViewVerticalAnimationScroller.startScroll(0, this.currentPrimaryVerticalOffset, 0, -this.currentPrimaryVerticalOffset, InterfaceStatusEnumerators.secondaryViewAnimationDuration);
			break;
		default:
			return;
		}
		isAnimationVertical=true;
		this.secondaryViewAnimationHandler.postDelayed(this.secondaryViewAnimationRunnable, InterfaceStatusEnumerators.secondaryViewAnimationPollingInterval);
		
		this.invalidate();
	}
	public void toggleLineOfFireView() {
		switch (this.lineOfFireCurrentState) {
		case CLOSED:
			this.lineOfFireCurrentState = secondaryViewState.OPENING;
			this.lineOfFireView.setVisibility(View.VISIBLE);
			quickScreenView.setVisibility(View.INVISIBLE);
			this.secondaryViewVerticalAnimationScroller.startScroll(0, 0, 0, this.lineOfFireView.getLayoutParams().height, InterfaceStatusEnumerators.secondaryViewAnimationDuration);	
			break;
		case OPEN:
			this.lineOfFireCurrentState = secondaryViewState.CLOSING;
			quickScreenView.setVisibility(View.VISIBLE);
			this.secondaryViewVerticalAnimationScroller.startScroll(0, this.currentPrimaryVerticalOffset, 0, -this.currentPrimaryVerticalOffset, InterfaceStatusEnumerators.secondaryViewAnimationDuration);
			break;
		default:
			return;
		}
		isAnimationVertical=true;
		this.secondaryViewAnimationHandler.postDelayed(this.secondaryViewAnimationRunnable, InterfaceStatusEnumerators.secondaryViewAnimationPollingInterval);
		
		this.invalidate();
	}
	public void toggleMessagesView() {
		switch (this.messageCurrentState) {
		case CLOSED:
			this.messageCurrentState = secondaryViewState.OPENING;
			this.messageView.setVisibility(View.VISIBLE);
			quickScreenView.setVisibility(View.INVISIBLE);
			this.secondaryViewHorizontalAnimationScroller.startScroll(0, 0, -this.messageView.getLayoutParams().width, 0, InterfaceStatusEnumerators.secondaryViewAnimationDuration);	
			break;
		case OPEN:
			this.messageCurrentState = secondaryViewState.CLOSING;
			quickScreenView.setVisibility(View.VISIBLE);
			this.secondaryViewHorizontalAnimationScroller.startScroll(this.currentPrimaryHorizontalOffset, 0, -this.currentPrimaryHorizontalOffset, 0, InterfaceStatusEnumerators.secondaryViewAnimationDuration);
			break;
		default:
			return;
		}
		isAnimationVertical=false;
		this.secondaryViewAnimationHandler.postDelayed(this.secondaryViewAnimationRunnable, InterfaceStatusEnumerators.secondaryViewAnimationPollingInterval);
		
		this.invalidate();
	}
	public void toggleSettingsView() {
		switch (this.settingsCurrentState) {
		case CLOSED:
			this.settingsCurrentState = secondaryViewState.OPENING;
			this.settingsView.setVisibility(View.VISIBLE);
			quickScreenView.setVisibility(View.INVISIBLE);
			this.secondaryViewVerticalAnimationScroller.startScroll(0, 0, 0, -this.settingsView.getLayoutParams().height, InterfaceStatusEnumerators.secondaryViewAnimationDuration);	
			break;
		case OPEN:
			this.settingsCurrentState = secondaryViewState.CLOSING;
			quickScreenView.setVisibility(View.VISIBLE);
			this.secondaryViewVerticalAnimationScroller.startScroll(0, this.currentPrimaryVerticalOffset, 0, -this.currentPrimaryVerticalOffset, InterfaceStatusEnumerators.secondaryViewAnimationDuration);
			break;
		default:
			return;
		}
		isAnimationVertical=true;
		this.secondaryViewAnimationHandler.postDelayed(this.secondaryViewAnimationRunnable, InterfaceStatusEnumerators.secondaryViewAnimationPollingInterval);
		
		this.invalidate();
	}
	public void togglePreDefinedMessagesView() {
		switch (this.preDefMessageCurrentState) {
		case CLOSED:
			this.preDefMessageCurrentState = secondaryViewState.OPENING;
			this.preDefinedMessageView.setVisibility(View.VISIBLE);
			quickScreenView.setVisibility(View.INVISIBLE);
			this.secondaryViewHorizontalAnimationScroller.startScroll(0, 0, this.preDefinedMessageView.getLayoutParams().width, 0, InterfaceStatusEnumerators.secondaryViewAnimationDuration);	
			break;
		case OPEN:
			this.preDefMessageCurrentState = secondaryViewState.CLOSING;
			quickScreenView.setVisibility(View.VISIBLE);
			this.secondaryViewHorizontalAnimationScroller.startScroll(this.currentPrimaryHorizontalOffset, 0, -this.currentPrimaryHorizontalOffset, 0, InterfaceStatusEnumerators.secondaryViewAnimationDuration);
			break;
		default:
			return;
		}
		isAnimationVertical=false;
		this.secondaryViewAnimationHandler.postDelayed(this.secondaryViewAnimationRunnable, InterfaceStatusEnumerators.secondaryViewAnimationPollingInterval);
		
		this.invalidate();
	}	
	/*
	 * to calculate the views sizes
	 */
	private void calculateChildDimensions() {
		this.primaryView.getLayoutParams().height = this.getHeight();
		this.primaryView.getLayoutParams().width = this.getWidth();

		this.lineOfFireView.getLayoutParams().width = this.getWidth();
		this.lineOfFireView.getLayoutParams().height = this.getHeight() - InterfaceStatusEnumerators.secondaryMargin;
		
		this.messageView.getLayoutParams().width = this.getWidth() - InterfaceStatusEnumerators.secondaryMargin;
		this.messageView.getLayoutParams().height = this.getHeight();
		
		this.settingsView.getLayoutParams().width = this.getWidth();
		this.settingsView.getLayoutParams().height = this.getHeight() - InterfaceStatusEnumerators.secondaryMargin;
		
		this.preDefinedMessageView.getLayoutParams().width = this.getWidth() - InterfaceStatusEnumerators.secondaryMargin;
		this.preDefinedMessageView.getLayoutParams().height = this.getHeight();
		
		this.combatModeView.getLayoutParams().width = this.getWidth();
		this.combatModeView.getLayoutParams().height = this.getHeight();
	}
	/*
	 * Method to compute the positions and sizes of each view
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (changed){
			this.calculateChildDimensions();
		}
		
		this.lineOfFireView.layout(left /*+ this.currentPrimaryHorizontalOffset*/, top /*+ this.currentPrimaryVerticalOffset*/, right /*+ this.currentPrimaryHorizontalOffset*/, bottom -InterfaceStatusEnumerators.secondaryMargin /*+ this.currentPrimaryVerticalOffset*/);
		this.messageView.layout(left + InterfaceStatusEnumerators.secondaryMargin /*+ this.currentPrimaryHorizontalOffset*/, top /*+ this.currentPrimaryVerticalOffset*/, right /*+ this.currentPrimaryHorizontalOffset*/, bottom /*+ this.currentPrimaryVerticalOffset*/);
		this.settingsView.layout(left /*+ this.currentPrimaryHorizontalOffset*/, top + InterfaceStatusEnumerators.secondaryMargin  /*+ this.currentPrimaryVerticalOffset*/, right /*+ this.currentPrimaryHorizontalOffset*/, bottom /*+ this.currentPrimaryVerticalOffset*/);
		this.preDefinedMessageView.layout(left /*+ this.currentPrimaryHorizontalOffset*/, top /*+ this.currentPrimaryVerticalOffset*/, right - InterfaceStatusEnumerators.secondaryMargin /*+ this.currentPrimaryHorizontalOffset*/, bottom /*+ this.currentPrimaryVerticalOffset*/);
		this.primaryView.layout(left + this.currentPrimaryHorizontalOffset, top + this.currentPrimaryVerticalOffset, right + this.currentPrimaryHorizontalOffset, bottom + this.currentPrimaryVerticalOffset);
		this.combatModeView.layout(left, top, right, bottom);
	}
	/*
	 * sets the side buttons accordingly to the state of the UI
	 */
	public void updateSideButtonsPositionAndVisibility(){
		float widthAndHeight[] = {this.screenWH[0],this.screenWH[1]};
		int statusBarHeight = (int)Math.ceil(25 * rootContext.getResources().getDisplayMetrics().density);
		float minimumBorderSpace = ( (float) InterfaceStatusEnumerators.secondaryMargin - lineOfFireIconWidth ) / 2.0f;
		switch (dialSelection){
		case LINE_OF_FIRE:
			backBtnX=minimumBorderSpace;
			backBtnY=minimumBorderSpace;
			
			backToOption1BtnX=backBtnX + lineOfFireIconWidth + minimumBorderSpace*2;
			backToOption1BtnY=backBtnY;
			
			backToOption2BtnX=backToOption1BtnX + lineOfFireIconWidth + minimumBorderSpace/2;
			backToOption2BtnY=backBtnY;
			
			backToOption3BtnX=backToOption2BtnX + lineOfFireIconWidth + minimumBorderSpace/2;
			backToOption3BtnY=backBtnY;
			
			backToMainBtn.setX(backBtnX);
			backToMainBtn.setY(backBtnY);
			
			backToMessageBtn.setX(backToOption1BtnX);
			backToMessageBtn.setY(backToOption1BtnY);
			
			backToPreDefinedMessageBtn.setX(backToOption2BtnX);
			backToPreDefinedMessageBtn.setY(backToOption2BtnY);
			
			backToSettingsBtn.setX(backToOption3BtnX);
			backToSettingsBtn.setY(backToOption3BtnY);
			
			backToMainBtn.setVisibility(View.VISIBLE);
			/*backToLineOfFireBtn.setVisibility(View.VISIBLE);*/
			backToSettingsBtn.setVisibility(View.VISIBLE);
			backToPreDefinedMessageBtn.setVisibility(View.VISIBLE);
			backToMessageBtn.setVisibility(View.VISIBLE);
			/* sos handle */
			sosSliderBase.setVisibility(View.INVISIBLE);
			sosHandle.setVisibility(View.INVISIBLE);
			prioritySelector.setVisibility(View.INVISIBLE);
			break;
		case MESSAGE:
			backBtnX=widthAndHeight[0]-minimumBorderSpace-lineOfFireIconWidth;
			backBtnY=minimumBorderSpace;
			
			backToOption1BtnX=backBtnX;
			backToOption1BtnY=backBtnY + lineOfFireIconHeight + minimumBorderSpace*2;
			
			backToOption2BtnX=backBtnX;
			backToOption2BtnY=backToOption1BtnY + lineOfFireIconHeight + minimumBorderSpace/2;
			
			backToOption3BtnX=backBtnX;
			backToOption3BtnY=backToOption2BtnY + lineOfFireIconHeight + minimumBorderSpace/2;
			
			backToMainBtn.setX(backBtnX);
			backToMainBtn.setY(backBtnY);
			
			backToLineOfFireBtn.setX(backToOption1BtnX);
			backToLineOfFireBtn.setY(backToOption1BtnY);
			
			backToPreDefinedMessageBtn.setX(backToOption2BtnX);
			backToPreDefinedMessageBtn.setY(backToOption2BtnY);
			
			backToSettingsBtn.setX(backToOption3BtnX);
			backToSettingsBtn.setY(backToOption3BtnY);
			
			/* priority selector */
			prioritySelector.setX(backBtnX);
			prioritySelector.setY(backToOption3BtnY + lineOfFireIconHeight + minimumBorderSpace*4);
			
			backToMainBtn.setVisibility(View.VISIBLE);
			backToLineOfFireBtn.setVisibility(View.VISIBLE);
			backToSettingsBtn.setVisibility(View.VISIBLE);
			backToPreDefinedMessageBtn.setVisibility(View.VISIBLE);
			/*backToMessageBtn.setVisibility(View.VISIBLE);*/
			prioritySelector.setVisibility(View.VISIBLE);
			/* sos handle */
			sosSliderBase.setVisibility(View.INVISIBLE);
			sosHandle.setVisibility(View.INVISIBLE);
			prioritySelector.setVisibility(View.VISIBLE);
			break;
		case SETTINGS:
			backBtnX=minimumBorderSpace;
			backBtnY=widthAndHeight[1]-minimumBorderSpace-statusBarHeight-lineOfFireIconHeight;
			
			backToOption1BtnX=backBtnX + lineOfFireIconWidth + minimumBorderSpace*2;
			backToOption1BtnY=backBtnY;
			
			backToOption2BtnX=backToOption1BtnX + lineOfFireIconWidth + minimumBorderSpace/2;
			backToOption2BtnY=backBtnY;
			
			backToOption3BtnX=backToOption2BtnX + lineOfFireIconWidth + minimumBorderSpace/2;
			backToOption3BtnY=backBtnY;
			
			backToMainBtn.setX(backBtnX);
			backToMainBtn.setY(backBtnY);
			
			backToLineOfFireBtn.setX(backToOption1BtnX);
			backToLineOfFireBtn.setY(backToOption1BtnY);
			
			backToMessageBtn.setX(backToOption2BtnX);
			backToMessageBtn.setY(backToOption2BtnY);
			
			backToPreDefinedMessageBtn.setX(backToOption3BtnX);
			backToPreDefinedMessageBtn.setY(backToOption3BtnY);
			
			backToMainBtn.setVisibility(View.VISIBLE);
			backToLineOfFireBtn.setVisibility(View.VISIBLE);
			/*backToSettingsBtn.setVisibility(View.VISIBLE);*/
			backToPreDefinedMessageBtn.setVisibility(View.VISIBLE);
			backToMessageBtn.setVisibility(View.VISIBLE);
			/* sos handle */
			sosSliderBase.setVisibility(View.INVISIBLE);
			sosHandle.setVisibility(View.INVISIBLE);
			prioritySelector.setVisibility(View.INVISIBLE);
			break;
		case PRE_DEFINED_MESSAGES:
			backBtnX=minimumBorderSpace;
			backBtnY=minimumBorderSpace;
			
			backToOption1BtnX=backBtnX;
			backToOption1BtnY=backBtnY + lineOfFireIconHeight + minimumBorderSpace*2;
			
			backToOption2BtnX=backBtnX;
			backToOption2BtnY=backToOption1BtnY + lineOfFireIconHeight + minimumBorderSpace/2;
			
			backToOption3BtnX=backBtnX;
			backToOption3BtnY=backToOption2BtnY + lineOfFireIconHeight + minimumBorderSpace/2;
			
			backToMainBtn.setX(backBtnX);
			backToMainBtn.setY(backBtnY);
			
			backToLineOfFireBtn.setX(backToOption1BtnX);
			backToLineOfFireBtn.setY(backToOption1BtnY);
			
			backToMessageBtn.setX(backToOption2BtnX);
			backToMessageBtn.setY(backToOption2BtnY);
			
			backToSettingsBtn.setX(backToOption3BtnX);
			backToSettingsBtn.setY(backToOption3BtnY);
			
			backToMainBtn.setVisibility(View.VISIBLE);
			backToLineOfFireBtn.setVisibility(View.VISIBLE);
			backToSettingsBtn.setVisibility(View.VISIBLE);
			/*backToPreDefinedMessageBtn.setVisibility(View.VISIBLE);*/
			backToMessageBtn.setVisibility(View.VISIBLE);
			/* sos handle */
			sosSliderBase.setVisibility(View.INVISIBLE);
			sosHandle.setVisibility(View.INVISIBLE);
			prioritySelector.setVisibility(View.INVISIBLE);
			break;
		case UNSELECTED:
			backToMainBtn.setVisibility(View.INVISIBLE);
			backToLineOfFireBtn.setVisibility(View.INVISIBLE);
			backToSettingsBtn.setVisibility(View.INVISIBLE);
			backToPreDefinedMessageBtn.setVisibility(View.INVISIBLE);
			backToMessageBtn.setVisibility(View.INVISIBLE);
			prioritySelector.setVisibility(View.INVISIBLE);
			/* sos handle */
			sosSliderBase.setVisibility(View.VISIBLE);
			sosHandle.setVisibility(View.VISIBLE);
			prioritySelector.setVisibility(View.INVISIBLE);
			break;
		default:
			break;
		}
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	

	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * NORMAL MESSAGES 
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/* 
	 * A wrapper to send messages in MessageView 
	 */
	public void sendMessage(String msg){
		String sender = new String ("You");
		if(parentClass.language==UILanguage.EN){
			sender = new String ("You");
    	} else if (parentClass.language==UILanguage.PT){
    		sender = new String ("Utilizador");
    	}
		switch(currentPriorityLevel){
			case NORMAL:
				postMessage(sender, msg, PriorityLevel.NORMAL, true);
				break;
			case NORMAL_PLUS:
				postMessage(sender, msg, PriorityLevel.NORMAL_PLUS, true);
				break;
			case IMPORTANT:
				postMessage(sender, msg, PriorityLevel.IMPORTANT, true);
				break;
			case CRITICAL:
				postMessage(sender, msg, PriorityLevel.CRITICAL, true);
				break;
			default:
				break;
		}
		Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_MESSGAGE, msg);
	}
	/*
	 * Messages for the MessageView (right)
	 */
	public void postMessage(String sender, String message, PriorityLevel priority, boolean sentByMe){
		StringBuilder sb = new StringBuilder("Last Message\n").append((sentByMe==true)?("You: "):("Command: ")).append(message);
		if(parentClass.language==UILanguage.EN){
			sb = new StringBuilder("Last Message\n").append((sentByMe==true)?("You: "):("Command: ")).append(message);
    	} else if (parentClass.language==UILanguage.PT){
    		sb = new StringBuilder("Última Mensagem\n").append((sentByMe==true)?("Utilizador: "):("Comando: ")).append(message);
    	}
		
		quickScreenMainText.setText(sb.toString());
		//quickScreenMainText.
		messageItemValues.add(new MessageItem(sender, message, priority, sentByMe));
		messageList.setSelection(messageAdapter.getCount() - 1);
	}
	/*
	 * displays the last message sent / received on the quick screen
	 */
	public void postLastMessage(){
		String msgg=messageItemValues.get(messageItemValues.size()-1).msg;
		quickScreenMainText.setText(msgg);
	}
	/*
	 * The Message CLass
	 */
	protected class MessageItem{
		public String sndr, msg;
		public PriorityLevel priority;
		boolean sentByOwner;
		
		public MessageItem(String sender, String message, PriorityLevel priorityLevel, boolean sentByMe){
			this.sndr=new String(sender);
			this.msg=new String(message);
			this.priority=priorityLevel;
			this.sentByOwner=sentByMe;
		}
	}
	/*
	 * The adapter for the list view(right)
	 */
	public class MessageItemArrayAdapter extends ArrayAdapter<MessageItem> {
		  private final Activity context;
		  private final ArrayList<MessageItem> values;

		  class ViewHolder {
			    public TextView text;
		  }

		  public MessageItemArrayAdapter(Context context, ArrayList<MessageItem> values) {
		    super(context, R.layout.row_layout, values);
		    this.context = (Activity)context;
		    this.values = values;
		  }

		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		    View rowView = convertView;
		    /* reuse views */
		    if (rowView == null) {
		      LayoutInflater inflater = context.getLayoutInflater();
		      rowView = inflater.inflate(R.layout.row_layout, null);
		      
		      /* configure view holder */
		      ViewHolder viewHolder = new ViewHolder();
		      viewHolder.text = (TextView) rowView.findViewById(R.id.message_text_view);
		      rowView.setTag(viewHolder);
		    }

		    /* process test */
		    ViewHolder holder = (ViewHolder) rowView.getTag();
		    SpannableString textToProcess = new SpannableString((new StringBuilder(values.get(position).sndr).append(":\n").append(values.get(position).msg)).toString());
		    textToProcess.setSpan(new ForegroundColorSpan((values.get(position).sentByOwner)?(Color.GRAY):(Color.WHITE)), 0, values.get(position).sndr.length()+1, 0);
		    textToProcess.setSpan(new RelativeSizeSpan(0.7f), 0, values.get(position).sndr.length()+1, 0);
		    
		    /* fill text view */
		    holder.text.setText(textToProcess, BufferType.SPANNABLE);

		    /* send speech balloon to left or right, accordingly to the sender */
		    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		    p.addRule((values.get(position).sentByOwner == true)?(RelativeLayout.ALIGN_PARENT_RIGHT):(RelativeLayout.ALIGN_PARENT_LEFT));
		    if(values.get(position).sentByOwner == true){
		    	p.rightMargin=25;
		    } else {
		    	p.leftMargin=25;
		    }
		    holder.text.setLayoutParams(p);
		      
		    /* see which priority has the message */
		    switch (values.get(position).priority){
		    	case NORMAL:
		    		holder.text.setBackgroundResource(R.drawable.speech_buble_normal);
		    		break;
		    	case NORMAL_PLUS:
		    		holder.text.setBackgroundResource(R.drawable.speech_buble_normal_plus);
		    		break;
		    	case IMPORTANT:
		    		holder.text.setBackgroundResource(R.drawable.speech_buble_important);
		    		break;
		    	case CRITICAL:
		    		holder.text.setBackgroundResource(R.drawable.speech_buble_critical);
		    		break;
	    		default:
	    			holder.text.setBackgroundResource(R.drawable.speech_buble_normal);
	    			break;
		    }
		    
		    return rowView;
		  }
		}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * PRE DEFINED MESSAGES 
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/* 
	 * To add the predefined messages(left screen)
	 */
	public void addPredefinedMessage(String Message, String Description){
		preDefinedMessages.append(preDefinedMessages.size(), new preDefinedMessageGroup(new preDefinedMessageItem(Message,Description)));
	}
	/*
	 * the predefined message item class
	 */
	protected class preDefinedMessageItem{
		public String msg, msgDetails;
		
		public preDefinedMessageItem(String message, String details){
			this.msg=new String(message);
			this.msgDetails=new String(details);
		}
	}
	/*
	 * the group for the predefined message, meaning, a single predefined message with its bullet points
	 */
	public class preDefinedMessageGroup {
		  public String string;
		  public Button btn;
		  public final List<String> children = new ArrayList<String>();

		  public preDefinedMessageGroup(preDefinedMessageItem data) {
		    this.string = data.msg;
		    children.add(data.msgDetails);
		  }
	}
	/*
	 * the list adapter for the predefined messages (left)
	 */
	public class ExpandablePreDefinedMessageListAdapter extends BaseExpandableListAdapter {

		  private final SparseArray<preDefinedMessageGroup> groups;
		  public LayoutInflater inflater;
		  public Activity activity;

		  public ExpandablePreDefinedMessageListAdapter(Context context, SparseArray<preDefinedMessageGroup> groups) {
		    activity = (Activity)context;
		    this.groups = groups;
		    inflater = ((Activity)context).getLayoutInflater();
		  }

		  @Override
		  public Object getChild(int groupPosition, int childPosition) {
		    return groups.get(groupPosition).children.get(childPosition);
		  }

		  @Override
		  public long getChildId(int groupPosition, int childPosition) {
		    return 0;
		  }

		  @Override
		  public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		    final String children = (String) getChild(groupPosition, childPosition);
		    TextView text = null;
		    if (convertView == null) {
		      convertView = inflater.inflate(R.layout.pre_defined_messages_row_details, null);
		    }
		    text = (TextView) convertView.findViewById(R.id.pre_defined_message_details_text);
		    text.setText(children);
		    convertView.setOnClickListener(new OnClickListener() {
		      @Override
		      public void onClick(View v) {
		        Toast.makeText(activity, children,
		            Toast.LENGTH_SHORT).show();
		      }
		    });
		    return convertView;
		  }

		  @Override
		  public int getChildrenCount(int groupPosition) {
		    return groups.get(groupPosition).children.size();
		  }

		  @Override
		  public Object getGroup(int groupPosition) {
		    return groups.get(groupPosition);
		  }

		  @Override
		  public int getGroupCount() {
		    return groups.size();
		  }

		  @Override
		  public void onGroupCollapsed(int groupPosition) {
		    super.onGroupCollapsed(groupPosition);
		  }

		  @Override
		  public void onGroupExpanded(int groupPosition) {
		    super.onGroupExpanded(groupPosition);
		  }

		  @Override
		  public long getGroupId(int groupPosition) {
		    return 0;
		  }

		  @Override
		  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			CheckedTextView text = null;
			Button send = null;
			final int index=groupPosition+1;
			// because the list on the user side doesn't have all predefined messages
			final int pdmCode;
				if(index-1<4){
					pdmCode=index-1;
				} else {
					pdmCode=index-1+2;
				}
		    if (convertView == null) {
		      convertView = inflater.inflate(R.layout.pre_defined_messages_row_group, null);
		    }
		    final preDefinedMessageGroup group = (preDefinedMessageGroup) getGroup(groupPosition);
		    text = (CheckedTextView) convertView.findViewById(R.id.pre_defined_message_group_text);
		    text.setText(group.string);
		    text.setChecked(isExpanded);
		    send = (Button) convertView.findViewById(R.id.btn_send_pre_defined_message);
		    send.setFocusable(false);
		    send.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View arg0) {
	            	String prefix=new String("PDM ");
	            	String toast=new String("Predefined Message sent.");
	            	if(parentClass.language==UILanguage.EN){
	            		prefix=new String("[PDM ");
	            		toast=new String("Predefined Message sent.");
			    	} else if (parentClass.language==UILanguage.PT){
			    		prefix=new String("[MPD ");
			    		toast=new String("Mensagem predefinida enviada.");
			    	}
	            	postMessage("You", new StringBuilder(prefix).append(pdmCode).append("][ ").append(group.string).append(" ]").toString(), PriorityLevel.CRITICAL, true);
	            	vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
	            	toastMessage("Predefined Message sent.",Toast.LENGTH_SHORT, 0, 0);
	            	Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_PREDEFINED_MESSAGE, pdmCode);
	            	parentClass.parseOutgoingPredefinedMessage(pdmCode);
	            }
		    });
		    return convertView;
		  }

		  @Override
		  public boolean hasStableIds() {
		    return false;
		  }

		  @Override
		  public boolean isChildSelectable(int groupPosition, int childPosition) {
		    return false;
		  }
		}
	/*
	 * To add a predefined message to the list, formated accordingly to the System Design specifications
	 */
	public void addPredefinedSystemDesignMessage(String _pdm, String _description, int code){
		String prefix1=new String("Code: ");
		String prefix2=new String("Description: ");
		if(parentClass.language==UILanguage.EN){
			prefix1=new String("Code: ");
			prefix2=new String("Description: ");
    	} else if (parentClass.language==UILanguage.PT){
    		prefix1=new String("Código: ");
			prefix2=new String("Descrição: ");
    	}
		StringBuilder sb=new StringBuilder(prefix1).append(code);
		preDefinedMessageGroup pdm = new preDefinedMessageGroup(new preDefinedMessageItem(_pdm, sb.toString()));
		sb=new StringBuilder(prefix2).append(_description);
		pdm.children.add(sb.toString());
		preDefinedMessages.append(preDefinedMessages.size(), pdm);
	}
	/*
	 * To create the predefined messages stated in the System Design
	 */
	private void createPredefinedMessagesList(){
		if(parentClass.language==UILanguage.EN){
			addPredefinedSystemDesignMessage("Need support", "Notify Backend about the need for operational support.", 0);
			addPredefinedSystemDesignMessage("Need to back down", "Notify Backend about the need for retreat.", 1);
			addPredefinedSystemDesignMessage("Firetruck is in trouble", "Notify Backend that the firetruck is compromised.", 2);
			addPredefinedSystemDesignMessage("Need aerial support", "Notify Backend about the need for operational aerial support.", 3);
			addPredefinedSystemDesignMessage("Fire spreading", "Notify Backend about the undergoing firespread.", 6);
			addPredefinedSystemDesignMessage("We are leaving", "Notify Backend about the intention to leave the area of operation.", 7);
			addPredefinedSystemDesignMessage("Fire getting close to house", "Notify Backend about the close proximity of the fire to a building.", 8);
			addPredefinedSystemDesignMessage("House burned", "Notify Backend about a scorched building.", 9);
    	} else if (parentClass.language==UILanguage.PT){
    		addPredefinedSystemDesignMessage("Preciso de apoio", "Avisar o commando da necessidade de apoio.", 0);
    		addPredefinedSystemDesignMessage("Preciso de me retirar", "Avisar o comando da necessidade de retirada.", 1);
    		addPredefinedSystemDesignMessage("Veículo em perigo", "Avisar o comando acerca do veículo exposto a perigo.", 2);
    		addPredefinedSystemDesignMessage("Preciso de apoio aéreo", "Avisar o comando da necessidade de apoio aéreo.", 3);
    		addPredefinedSystemDesignMessage("Fogo a espalhar-se", "Avisar o comando da dispersão do fogo.", 6);
    		addPredefinedSystemDesignMessage("Equipa em retirada", "Avisar o comando da retirada da equipa.", 7);
    		addPredefinedSystemDesignMessage("Fogo a aproximar-se de casa", "Avisar o comando da proximidade do fogo a casa.", 8);
    		addPredefinedSystemDesignMessage("Casa queimada", "Avisar o comando de casa quiemada.", 9);
    	}
		
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * TOASTS
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/*
	 * To display Toast Messages
	 */
	public void toastMessage(String Message, int DurationMilis, int PositionX, int PositionY){
		Toast t=Toast.makeText(this.getContext(), Message, DurationMilis);
		t.setGravity(Gravity.BOTTOM|Gravity.CENTER_VERTICAL, PositionX, PositionY);
		t.show();
		t=null;
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * APP STATUS INDICATORS
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/*
	 * updates battery indicator appearance and state variable
	 */
	public void updateBatteryIndicator(indicatorStates batteryStatus){
		this.batteryStatus=batteryStatus;
		switch(batteryStatus){
			case EMPTY:
				indiatorBattery.setImageResource(R.drawable.battery_empty);
				break;
			case LOW:
				indiatorBattery.setImageResource(R.drawable.battery_low);
				break;
			case MEDIUM:
				indiatorBattery.setImageResource(R.drawable.battery_medium);
				break;
			case HIGH:
				indiatorBattery.setImageResource(R.drawable.battery_high);
				break;
			case FULL:
				indiatorBattery.setImageResource(R.drawable.battery_full);
				break;
		}
	}
	/*
	 * updates radio battery indicator appearance and state variable
	 */
	public void updateRadioIndicator(indicatorStates radioBatteryStatus){
		this.radioBatteryStatus=radioBatteryStatus;
		switch(radioBatteryStatus){
			case EMPTY:
				indiatorRadioBattery.setImageResource(R.drawable.radio_empty);
				break;
			case LOW:
				indiatorRadioBattery.setImageResource(R.drawable.radio_low);
				break;
			case MEDIUM:
				indiatorRadioBattery.setImageResource(R.drawable.radio_medium);
				break;
			case HIGH:
				indiatorRadioBattery.setImageResource(R.drawable.radio_high);
				break;
			case FULL:
				indiatorRadioBattery.setImageResource(R.drawable.radio_full);
				break;
		}
	}
	/*
	 * updates wifi indicator appearance and state variable
	 */
	public void updateWifiIndicator(indicatorStates wifiStatus){
		this.wifiStatus=wifiStatus;
		switch(wifiStatus){
			case EMPTY:
				indiatorWifi.setImageResource(R.drawable.wifi_empty);
				break;
			case LOW:
				indiatorWifi.setImageResource(R.drawable.wifi_low);
				break;
			case MEDIUM:
				indiatorWifi.setImageResource(R.drawable.wifi_medium);
				break;
			case HIGH:
				indiatorWifi.setImageResource(R.drawable.wifi_high);
				break;
			case FULL:
				indiatorWifi.setImageResource(R.drawable.wifi_full);
				break;
		}
	}
	/*
	 * updates bluetooth indicator appearance and state variable
	 */
	public void updateBluetoothIndicator(indicatorStates bluetoothStatus){
		this.bluetoothStatus=bluetoothStatus;
		switch(bluetoothStatus){
			case EMPTY:
				indiatorBluetooth.setImageResource(R.drawable.bluetooth_empty);
				break;
			case FULL:
				indiatorBluetooth.setImageResource(R.drawable.bluetooth_full);
				break;
			default:
				indiatorBluetooth.setImageResource(R.drawable.bluetooth_empty);
				break;
		}
	}
	/*
	 * updates gps indicator appearance and state variable
	 */
	public void updateGpsIndicator(indicatorStates gpsStatus){
		switch(gpsStatus){
			case EMPTY:
				indiatorGps.setImageResource(R.drawable.gps_indicator_off);
				break;
			case FULL:
				indiatorGps.setImageResource(R.drawable.gps_indicator_on);
				break;
			default:
				indiatorGps.setImageResource(R.drawable.gps_indicator_off);
				break;
		}
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * HEARTRATE INDICATOR
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/*
	 * to control heart rate animation
	 */
	public void setHeartRate(int newHR){
		this.heartRate=newHR;
		heartRateValue.setText(Integer.toString(heartRate));
		if(heartRate>0){
			heartRateDelta=(long)60000.0 / (long)heartRate;
		}
	}
	/*
	 * The runnable that beats the heart
	 */
	protected class HeartBeatAnimationRunnable implements Runnable {		
				
		@Override
		public void run() {
			heartRateIcon.setAlpha(1.0f);
			heartRateIcon.animate().alpha(0.0f).setDuration(heartRateDelta);
			heartbeatAnimationHandler.postDelayed(this, heartRateDelta);
		}
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * COMPASS
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/* 
	 * Triggers a compass animation to a new angle
	 */
	public void setCompassAngle(float angle){
		compass.animate().rotation((float)(angle)).setDuration((long) (10.0*(long) Math.abs((compass.getRotation()-angle)))).setInterpolator(new SmoothInterpolator());
		compass.setX(compassBaseX);
		compass.setY(compassBaseY);
	}
	/* 
	 * Sets the compass into a new angle instantaneously
	 */
	public void setCompassAngleNow(float angle){
		compass.setRotation(angle);
		compass.setX(compassBaseX);
		compass.setY(compassBaseY);
	}
	/* 
	 * Enables or disables target mode
	 */
	public void setCompassTargetMode(boolean targetON){
		targetMode=targetON;
		if(targetMode){
			if(parentClass.language==UILanguage.EN){
				toastMessage("Target Mode enabled", Toast.LENGTH_SHORT, 0, 0);
	    	} else if (parentClass.language==UILanguage.PT){
	    		toastMessage("Modo de Objetivo ativo", Toast.LENGTH_SHORT, 0, 0);
	    	}
		} else {
			if(parentClass.language==UILanguage.EN){
				toastMessage("Target Mode disabled", Toast.LENGTH_SHORT, 0, 0);
	    	} else if (parentClass.language==UILanguage.PT){
	    		toastMessage("Modo de Objetivo desativado", Toast.LENGTH_SHORT, 0, 0);
	    	}
		}
	}
	/* 
	 * Sets the compass state to either opened or closed
	 */
	public void setCompassMode(compassState newState){
		currentCompassState=newState;
		updateCompassAppearance();
	}
	/* 
	 * updates the compass appearance accordingly to its state and mode
	 */
	public void updateCompassAppearance(){
		switch (currentCompassState){
			case DIAL_OFF:
				compass.setVisibility(View.VISIBLE);
				if(targetMode){
					compass.setImageResource(R.drawable.compass_target_off);
				} else {
					compass.setImageResource(R.drawable.compass_off);
				}
				break;
			case DIAL_ON:
				compass.setVisibility(View.VISIBLE);
				if(targetMode){
					compass.setImageResource(R.drawable.compass_target_on);
				} else {
					compass.setImageResource(R.drawable.compass_on);
				}
				break;
			case TARGET_MODE_DIAL_OFF:
				break;
			case TARGET_MODE_DIAL_ON:
				break;
			case INVISIBLE:
				compass.setVisibility(View.INVISIBLE);
				break;
		}
	}
	public void setCompassEnabledState(boolean isCompassToOperate){
		compassEnabled=isCompassToOperate;
		vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		if(compassEnabled){
			compassEnablerBtn.setImageResource(R.drawable.compass_enabler_on);
			if (parentClass.language==UILanguage.EN){
				toastMessage("Compass: enabled", Toast.LENGTH_SHORT, 0, 0);
				compassEnablerTxt.setText("Compass: Enabled");
			} else {
				toastMessage("Bússola: ativa", Toast.LENGTH_SHORT, 0, 0);
				compassEnablerTxt.setText("Bússola: ativa");
			}
		} else {
			compassEnablerBtn.setImageResource(R.drawable.compass_enabler_off);
			if (parentClass.language==UILanguage.EN){
				toastMessage("Compass: disabled", Toast.LENGTH_SHORT, 0, 0);
				compassEnablerTxt.setText("Compass: Disabled");
			} else {
				toastMessage("Bússola: desativada", Toast.LENGTH_SHORT, 0, 0);
				compassEnablerTxt.setText("Bússola: desativada");
			}
		}
			
	}
	/* 
	 * posts in the quick screen, the distance to the target mode objective
	 */
	public void postDistanceToObjective(double distance){
		StringBuilder sb = new StringBuilder("Distance to Objective:\n").append(Double.toString(distance));
		if(parentClass.language==UILanguage.EN){
			sb = new StringBuilder("Distance to Objective:\n").append(Double.toString(distance));
    	} else if (parentClass.language==UILanguage.PT){
    		sb = new StringBuilder("Distância ao objetivo:\n").append(Double.toString(distance));
    	}
		quickScreenMainText.setText(sb.toString());
	}
	public void reportNoGPSSignal(){
		if(parentClass.language==UILanguage.EN){
			quickScreenMainText.setText("Target Mode:\nNo GPS Signal");
    	} else if (parentClass.language==UILanguage.PT){
    		quickScreenMainText.setText("Modo de Objetivo:\nSem sinal GPS");
    	}
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/

	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * ACTION DIAL HANDLER
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/* 
	 * To control the animation of the radial handler
	 */
	protected class DialAnimationRunnable implements Runnable {
		@Override
		public void run() {
			if( dialState==dialDisplayState.OFF ){
				
			} else if( dialState==dialDisplayState.OPENING ) {
				
			} else if( dialState==dialDisplayState.ON ) {
				
			} else if( dialState==dialDisplayState.CLOSING ) {
				/* action dial */
				actionDial.setImageResource(R.drawable.dial_off);
				
				/* action dial circle */
				actionDialCircle.setVisibility(View.INVISIBLE);
				
				/* selection icons */
				lineOfFireIcon.setVisibility(View.INVISIBLE);
				messagesIcon.setVisibility(View.INVISIBLE);
				settingsIcon.setVisibility(View.INVISIBLE);
				preDefMessagesIcon.setVisibility(View.INVISIBLE);
			} else {
				
			}
			
			if ( actioDialCurrentState==actioDialStateEnum.START ){
				/* first set all to transparent */
				lineOfFireIcon.setAlpha(0.0f);
				messagesIcon.setAlpha(0.0f);
				settingsIcon.setAlpha(0.0f);
				preDefMessagesIcon.setAlpha(0.0f);
				actionDialCircle.setScaleX(0.0f);
				actionDialCircle.setScaleY(0.0f);
				actionDialCircle.setAlpha(0.0f);
				
				/* action dial */
				actionDial.setImageResource(R.drawable.dial_on);
				
				/* then schedule the animation */
				lineOfFireIcon.animate().alpha(1.0f).setDuration(InterfaceStatusEnumerators.dialAnimationInDuration);
				messagesIcon.animate().alpha(1.0f).setDuration(InterfaceStatusEnumerators.dialAnimationInDuration);
				settingsIcon.animate().alpha(1.0f).setDuration(InterfaceStatusEnumerators.dialAnimationInDuration);
				preDefMessagesIcon.animate().alpha(1.0f).setDuration(InterfaceStatusEnumerators.dialAnimationInDuration);
				actionDialCircle.animate().scaleX(1.0f).scaleY(1.0f).setDuration(InterfaceStatusEnumerators.dialAnimationInDuration);
				actionDialCircle.animate().alpha(1.0f).setDuration(InterfaceStatusEnumerators.dialAnimationInDuration);
				
				/* action dial circle position and visibility */
				actionDialCircle.setX(actionDialCircleBaseX);
				actionDialCircle.setY(actionDialCircleBaseY);
				actionDialCircle.setVisibility(View.VISIBLE);
				
				/* icons position and visibility */
				lineOfFireIcon.setX(lineOfFireIconBaseX);
				lineOfFireIcon.setY(lineOfFireIconBaseY);
				messagesIcon.setX(messagesIconBaseX);
				messagesIcon.setY(messagesIconBaseY);
				settingsIcon.setX(settingsIconBaseX);
				settingsIcon.setY(settingsIconBaseY);
				preDefMessagesIcon.setX(preDefMessagesIconBaseX);
				preDefMessagesIcon.setY(preDefMessagesIconBaseY);
				lineOfFireIcon.setVisibility(View.VISIBLE);
				messagesIcon.setVisibility(View.VISIBLE);
				settingsIcon.setVisibility(View.VISIBLE);
				preDefMessagesIcon.setVisibility(View.VISIBLE);
				
				actioDialCurrentState=actioDialStateEnum.MOVING;
				dialState=dialDisplayState.OPENING;
			} else if ( actioDialCurrentState==actioDialStateEnum.MOVING ){
				float pos[]=trimRadialMovement((x - actionDialWidth/2.0f), (y - actionDialHeight/*/2.0f*/), actionDialBaseX, actionDialBaseY, (float)InterfaceStatusEnumerators.dialMovementMaxRadius);
				if(assertSnapDistance( pos[0], pos[1], lineOfFireIconBaseX, lineOfFireIconBaseY, InterfaceStatusEnumerators.iconSnapRadius)){
					pos[0]=lineOfFireIconBaseX - ( (actionDialWidth-lineOfFireIconWidth) / 2 );
					pos[1]=lineOfFireIconBaseY - ( (actionDialHeight-lineOfFireIconHeight) / 2 );
					dialSelection=dialSelectionSate.LINE_OF_FIRE;
					if(parentClass.language==UILanguage.EN){
						actionDialPostText("Report Line of Fire", PriorityLevel.NORMAL);
			    	} else if (parentClass.language==UILanguage.PT){
						actionDialPostText("Reportar Linha de Fogo", PriorityLevel.NORMAL);
			    	}
					setCompassAngle(0.0f);
					actionDial.setImageResource(R.drawable.dial_selected);
					disableCombatModeAnimation();
				} else if(assertSnapDistance( pos[0], pos[1], messagesIconBaseX, messagesIconBaseY, InterfaceStatusEnumerators.iconSnapRadius)){
					pos[0]=messagesIconBaseX - ( (actionDialWidth-messagesIconWidth) / 2 );
					pos[1]=messagesIconBaseY - ( (actionDialHeight-messagesIconHeight) / 2 );
					dialSelection=dialSelectionSate.MESSAGE;
					if(parentClass.language==UILanguage.EN){
						actionDialPostText("Send Message", PriorityLevel.NORMAL);
			    	} else if (parentClass.language==UILanguage.PT){
						actionDialPostText("Enviar Mensagem", PriorityLevel.NORMAL);
			    	}
					setCompassAngle(90.0f);
					actionDial.setImageResource(R.drawable.dial_selected);
					disableCombatModeAnimation();
				} else if(assertSnapDistance( pos[0], pos[1], settingsIconBaseX, settingsIconBaseY, InterfaceStatusEnumerators.iconSnapRadius)){
					pos[0]=settingsIconBaseX - ( (actionDialWidth-settingsIconWidth) / 2 );
					pos[1]=settingsIconBaseY - ( (actionDialHeight-settingsIconHeight) / 2 );
					dialSelection=dialSelectionSate.SETTINGS;
					if(parentClass.language==UILanguage.EN){
						actionDialPostText("Change Settings", PriorityLevel.NORMAL);
			    	} else if (parentClass.language==UILanguage.PT){
						actionDialPostText("Definições", PriorityLevel.NORMAL);
			    	}
					setCompassAngle(180.0f);
					actionDial.setImageResource(R.drawable.dial_selected);
					disableCombatModeAnimation();
				} else if(assertSnapDistance( pos[0], pos[1], preDefMessagesIconBaseX, preDefMessagesIconBaseY, InterfaceStatusEnumerators.iconSnapRadius)){
					pos[0]=preDefMessagesIconBaseX - ( (actionDialWidth-preDefMessagesIconWidth) / 2 );
					pos[1]=preDefMessagesIconBaseY - ( (actionDialHeight-preDefMessagesIconHeight) / 2 );
					dialSelection=dialSelectionSate.PRE_DEFINED_MESSAGES;
					if(parentClass.language==UILanguage.EN){
						actionDialPostText("Pre Defined Messages", PriorityLevel.NORMAL);
			    	} else if (parentClass.language==UILanguage.PT){
						actionDialPostText("Mensagens Pré Definidas", PriorityLevel.NORMAL);
			    	}
					setCompassAngle(270.0f);
					actionDial.setImageResource(R.drawable.dial_selected);
					disableCombatModeAnimation();
				} else {
					dialSelection=dialSelectionSate.UNSELECTED;
					actionDialPostText("", PriorityLevel.NORMAL);
					actionDial.setImageResource(R.drawable.dial_on);
				}
				actionDial.setX( pos[0] );
				actionDial.setY( pos[1] );
			} else if ( actioDialCurrentState==actioDialStateEnum.RETURNING ){
				float pos[]=getInterpolatedPos( dialInterpolation, actionDialStartX, actionDialStartY, actionDialBaseX, actionDialBaseY, Calendar.getInstance().getTimeInMillis()-actionDialStartTime, (long)InterfaceStatusEnumerators.dialReturningAnimationDuration, 0.2f, 5.0f, 20.0f );
				actionDial.setX( pos[0] );
				actionDial.setY( pos[1] );
				if( (pos[0]==actionDialBaseX) && (pos[1]==actionDialBaseY) ){
					actioDialCurrentState=actioDialStateEnum.STILL;
				} else {
					dialAnimationHandler.postDelayed(this, InterfaceStatusEnumerators.dialAnimationPollingInterval);
				}
			} else if ( actioDialCurrentState==actioDialStateEnum.STILL ){
				actionDial.setX( actionDialBaseX );
				actionDial.setY( actionDialBaseY );
			} else {
				//do nothing
			}		
		}
	}
	/*
	 * to set the radial handler state
	 */
	public void setRadialHandlerState(actioDialStateEnum state){
		this.actioDialCurrentState=state;
	}
	/*
	 * To save the animation starting time
	 */
	private void setAnimationactionDialStartTime(){
		actionDialStartTime=Calendar.getInstance().getTimeInMillis();
	}
	/*
	 * To save the animation starting positions
	 */
	private void setAnimationStartPositions(){
		actionDialStartX=x - actionDialWidth/2.0f;
		actionDialStartY=y - actionDialHeight * 3/2f;
	}
	/*
	 * Saves animation starting conditions
	 */
	private void setAnimationStartConditions(){
		setAnimationactionDialStartTime();
		setAnimationStartPositions();
	}
	/*
	 * To Help in the animations computations.
	 */
	private float[] getInterpolatedPos(movementInterpolation mode, float actionDialStartX, float actionDialStartY, float endX, float endY, long currentTimeElapsedMilis, long durationMilis, float linearFraction, float frequency, float decay){
		float pos[]={0,0};
		
		if (mode==movementInterpolation.LINEAR){
			float t=(float)currentTimeElapsedMilis/1000.0f;
			float vX = (endX-actionDialStartX)/((durationMilis)/1000.0f);
			float vY = (endY-actionDialStartY)/((durationMilis)/1000.0f);
			
			if (currentTimeElapsedMilis<(durationMilis)){
				pos[0]=actionDialStartX+vX*t;
				pos[1]=actionDialStartY+vY*t;	
			} else {
				pos[0]=actionDialBaseX;
				pos[1]=actionDialBaseY;	
			}
		} else if (mode==movementInterpolation.OVERSHOOT){
			float t=(float)currentTimeElapsedMilis/1000.0f;
			float w=frequency*(float)Math.PI*2;
			float vX = (endX-actionDialStartX)/((durationMilis*linearFraction)/1000.0f);
			float vY = (endY-actionDialStartY)/((durationMilis*linearFraction)/1000.0f);
			
			if(currentTimeElapsedMilis<(durationMilis*linearFraction)){
				pos[0]=actionDialStartX+vX*t;
				pos[1]=actionDialStartY+vY*t;	
			} else if ( (currentTimeElapsedMilis>=(durationMilis*linearFraction)) && (currentTimeElapsedMilis<durationMilis) ){
				pos[0]=endX + vX*((float)Math.sin(t*w)/(float)Math.exp(decay*t)/w);
				pos[1]=endY + vY*((float)Math.sin(t*w)/(float)Math.exp(decay*t)/w);
			} else {
				pos[0]=actionDialBaseX;
				pos[1]=actionDialBaseY;	
			}
		} else if (mode==movementInterpolation.EXPONENTIAL){
			float tau=durationMilis*0.2f;
			if (currentTimeElapsedMilis<(durationMilis)){
				pos[0]=endX + (actionDialStartX - endX)*(float)Math.exp(-currentTimeElapsedMilis/tau);
				pos[1]=endY + (actionDialStartY - endY)*(float)Math.exp(-currentTimeElapsedMilis/tau);
			} else {
				pos[0]=actionDialBaseX;
				pos[1]=actionDialBaseY;	
			}
		} else {
			pos[0]=actionDialBaseX;
			pos[1]=actionDialBaseY;	
		}
		return pos;
	}
	/*
	 * To ensure the handler never leaves the radial base
	 */
	private float[] trimRadialMovement(float xx, float yy, float x0, float y0, float maxRadius){
		float pos[]={0, 0};
		
		float dx = xx-x0;
		float dy = yy-y0;
		
		float radius=(float)Math.sqrt( (Math.pow((double)dx, 2)) + (Math.pow((double)dy, 2)) );
		
		if (radius<=maxRadius){
			pos[0]=xx;
			pos[1]=yy;
		} else {
			double angle = Math.atan2((double)dy, (double)dx);
			
			pos[0]=x0+maxRadius*(float)Math.cos(angle);
			pos[1]=y0+maxRadius*(float)Math.sin(angle);
		}
		
		return pos;
	}
	/*
	 * To check if the handler should snap to an option
	 */
	private boolean assertSnapDistance(float xFrom, float yFrom, float xTo, float yTo, float snapRadius){		
		float dx = xTo-xFrom;
		float dy = yTo-yFrom;
		
		float radius=(float)Math.sqrt( (Math.pow((double)dx, 2)) + (Math.pow((double)dy, 2)) );
		
		if (radius<=snapRadius){
			return true;
		} else {
			return false;
		}		
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * ACTION DIAL TEXT DESCRIPTIONS
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/	
	/* 
	 * action dial Text descriptions 
	 */
	private void actionDialPostText(String text, PriorityLevel priority){
		dialText.setText(text);
		dialText.setVisibility(View.VISIBLE);
		dialText.setX(dialTextBaseX);
		dialText.setY(dialTextBaseY);
		
		if(priority==PriorityLevel.NORMAL){
			dialText.setTextSize(20);
		} else if(priority==PriorityLevel.NORMAL_PLUS) {
			dialText.setTextSize(24);
		} else if(priority==PriorityLevel.IMPORTANT) {
			dialText.setTextSize(30);
		} else if(priority==PriorityLevel.CRITICAL) {
			dialText.setTextSize(40);
		}
	}
	/*
	 * to clean the action dial text
	 */
	public void actionDialClean(){
		dialText.setText("");
		dialText.setVisibility(View.INVISIBLE);
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * SOS HANDLE
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/	
	/* 
	 * sos handle method to ensure a limited range of motion
	 */
	private float clampValue(float toBeClamped, float min, float max){
		if(toBeClamped<=min){
			return min;
		} else if (toBeClamped>=max){
			return max;
		} else {
			return toBeClamped;
		}
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * COMBAT MODE ANIMATION
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/	
	/* 
	 * The runnable to control the combat mode animation 
	 */
	protected class CombatModeAnimationRunnable implements Runnable {		
				
		@Override
		public void run() {
			if(combatMode==false){
				if(combatModeCircleAnimationOngoing==true) {
					vibrate(InterfaceStatusEnumerators.combatModeVibrationDuration);
					combatModeCircle.animate().alpha(0.0f).setDuration(InterfaceStatusEnumerators.combatModeCircleAnimationOutDuration);
					combatModeCircle.animate().scaleX(1.3f).scaleY(1.3f).setDuration(InterfaceStatusEnumerators.combatModeCircleAnimationOutDuration);
					combatModeCircleAnimationOngoing=false;
					combatMode=true;
					toggleCombatModeView();
					parentClass.setTapDetectorOperation(combatMode);
				} else {
					combatModeCircle.setX(center[0] - combatModeCircle.getLayoutParams().width/2.0f);
					combatModeCircle.setY(center[1]+InterfaceStatusEnumerators.downOffset - combatModeCircle.getLayoutParams().height/2.0f);
					combatModeCircle.setVisibility(View.VISIBLE);
					combatModeCircle.setScaleX(0.0f);
					combatModeCircle.setScaleY(0.0f);
					combatModeCircle.setAlpha(1.0f);
					combatModeCircle.animate().scaleX(1.0f).scaleY(1.0f).setDuration(InterfaceStatusEnumerators.combatModeCircleAnimationDuration);
					combatModeCircleAnimationOngoing=true;
					combatModeAnimationHandler.postDelayed(this, InterfaceStatusEnumerators.combatModeCircleAnimationDuration);
				}
			} else {
				if(combatModeCircleAnimationOngoing==true) {
					vibrate(InterfaceStatusEnumerators.combatModeVibrationDuration);
					combatModeCircleOut.animate().alpha(0.0f).setDuration(InterfaceStatusEnumerators.combatModeCircleAnimationOutDuration);
					combatModeCircleOut.animate().scaleX(1.3f).scaleY(1.3f).setDuration(InterfaceStatusEnumerators.combatModeCircleAnimationOutDuration);
					combatModeCircleAnimationOngoing=false;
					combatMode=false;
					parentClass.setTapDetectorOperation(combatMode);
					toggleCombatModeView();
				} else {
					combatModeCircleOut.setVisibility(View.VISIBLE);
					combatModeCircleOut.setScaleX(0.0f);
					combatModeCircleOut.setScaleY(0.0f);
					combatModeCircleOut.setAlpha(1.0f);
					combatModeCircleOut.animate().scaleX(1.0f).scaleY(1.0f).setDuration(InterfaceStatusEnumerators.combatModeCircleAnimationDuration);
					combatModeCircleAnimationOngoing=true;
					combatModeAnimationHandler.postDelayed(this, InterfaceStatusEnumerators.combatModeCircleAnimationDuration);
				}
			}
		}
	}
	/* 
	 * To disable the combat mode animation 
	 */
	public void disableCombatModeAnimation(){
		if (combatModeCircleAnimationRunnableQeued==true){
			combatModeCircleAnimationRunnableQeued=false;
			combatModeAnimationHandler.removeCallbacks(combatModeAnimationRunnable);
		}
		if (combatModeCircleAnimationOngoing==true){
			if (combatMode==false){
				combatModeCircle.animate().cancel();
				combatModeCircle.setVisibility(View.INVISIBLE);
			} else {
				combatModeCircleOut.animate().cancel();
				combatModeCircleOut.setVisibility(View.INVISIBLE);
			}
			combatModeCircleAnimationOngoing=false;
		}
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * UNDER OPERATION TIMER
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/	
	/* 
	 * The runnable that periodically updates the timer
	 */
	protected class timeUnderOperationRunnable implements Runnable {				
		@Override
		public void run() {
		    timeUnderOperationDate =  Calendar.getInstance().getTime();
		    long different = timeUnderOperationDate.getTime() - timeUnderOperationDateStart.getTime();

		    DecimalFormat formatter = new DecimalFormat("00");
		    
	        long secondsInMilli = 1000;
	        long minutesInMilli = secondsInMilli * 60;
	        long hoursInMilli = minutesInMilli * 60;
	        long daysInMilli = hoursInMilli * 24;

	        different = different % daysInMilli;

	        int elapsedHours = (int)different / (int)hoursInMilli;
	        different = different % hoursInMilli;
	        int elapsedMinutes = (int)different / (int)minutesInMilli;
	        different = different % minutesInMilli;
	        int elapsedSeconds = (int)different / (int)secondsInMilli;

		    timeUnderOperation.setText((new StringBuilder(formatter.format(elapsedHours)).append(":").append(formatter.format(elapsedMinutes)).append(":").append(formatter.format(elapsedSeconds)).toString()));
		    
		    timeUnderOperationHandler.postDelayed(this, InterfaceStatusEnumerators.timerUpdateDelta);
		}
	}
	/* 
	 * Starts the timer
	 */
	private synchronized void startTimeUnderOperationUpdates(){
		timeUnderOperationDateStart =  Calendar.getInstance().getTime();
		timerUpdater.run();
    }
	/* 
	 * Stops the timer
	 */
	private synchronized void stopTimeUnderOperationUpdates(){
		timeUnderOperationHandler.removeCallbacks(timerUpdater);
    }
	/* 
	 * Advances between timer states
	 */
	public void toggleUnderOperationTimer(){
		switch(timerUnderOperationState){
			case STOPPED:
				timeUnderOperation.setText("00:00:00");
				timerUnderOperationState=TimerStates.RESETED;
				if(parentClass.language==UILanguage.EN){
					toastMessage("Timer reseted.",Toast.LENGTH_SHORT, 0, (int)center[1]);
		    	} else if (parentClass.language==UILanguage.PT){
					toastMessage("Temporizador restabelecido.",Toast.LENGTH_SHORT, 0, (int)center[1]);
		    	}
				break;
			case RESETED:
				startTimeUnderOperationUpdates();
				timerUnderOperationState=TimerStates.RUNNING;
				if(parentClass.language==UILanguage.EN){
					toastMessage("Timer started.",Toast.LENGTH_SHORT, 0, (int)center[1]);
		    	} else if (parentClass.language==UILanguage.PT){
					toastMessage("Temporizador iniciado.",Toast.LENGTH_SHORT, 0, (int)center[1]);
		    	}		
				break;
			case RUNNING:
				stopTimeUnderOperationUpdates();
				timerUnderOperationState=TimerStates.STOPPED;
				if(parentClass.language==UILanguage.EN){
					toastMessage("Timer paused.",Toast.LENGTH_SHORT, 0, (int)center[1]);
		    	} else if (parentClass.language==UILanguage.PT){
					toastMessage("Temporizador em pausa.",Toast.LENGTH_SHORT, 0, (int)center[1]);
		    	}
				break;
		}
	}	
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * TEXT TO SPEECH
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	public void setTextToSpeech(boolean textToSpeechEnabled){
		vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		if(textToSpeechEnabled){			
			voiceOverBtn.setImageResource(R.drawable.text_to_speech_enabler_on);
			if(parentClass.language==UILanguage.EN){
				voiceOverTxt.setText("Voice Over: Enabled");
			} else {
				voiceOverTxt.setText("Voz: Ativa");
			}
		} else {
			voiceOverBtn.setImageResource(R.drawable.text_to_speech_enabler_off);
			if(parentClass.language==UILanguage.EN){
				voiceOverTxt.setText("Voice Over: Disabled");
			} else {
				voiceOverTxt.setText("Voz: Desativada");
			}
		}
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * MISCELANEOUS
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/*
	 * To ensure that, when user leaves the message view(right), the keyboard disappears
	 */
	private void hideKeyboard() {
	    InputMethodManager inputManager = (InputMethodManager) rootContext.getSystemService(Context.INPUT_METHOD_SERVICE);

	    // check if no view has focus:
	    View view = rootParent.getCurrentFocus();
	    if (view != null) {
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/
}

