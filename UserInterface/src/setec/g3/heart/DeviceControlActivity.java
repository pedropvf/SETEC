/*
 * Copyright (C) 2013 The Android Open Source Project
 * This software is based on Apache-licensed code from the above.
 * 
 * Copyright (C) 2013 APUS
 *
 *     This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.

 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package setec.g3.heart;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
//import android.widget.ExpandableListView;
//import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

import setec.g3.ui.R;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
	private final static String TAG = DeviceControlActivity.class
			.getSimpleName();

	// BLE stuff
	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public BluetoothLeService mBluetoothLeService= null;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;
	public BluetoothGattCharacteristic mNotifyCharacteristic=null;
	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";

	// Various UI stuff
	public static boolean currentlyVisible;
	//private boolean logging = false;
	private TextView mDataField;
	private String mDeviceName;
	private String mDeviceAddress;
	//private ImageButton mButtonStart;
	//private ImageButton mButtonStop;
	
	//Handler para correr aquilo passando x tempo
	private final long interval = 4000;
	
	private Handler handler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    	
	    	Log.d("ennn", "handler");
	    	if((mBluetoothLeService!=null) && mNotifyCharacteristic!=null)
	    	{
	    		Log.d("en", "handler dentro if");
	    		mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
	    	}
	    	Log.d("ennn", "sai do if");
	    	
	    }
	};
		
	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				//updateConnectionState(true);
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				mConnected = false;
				//updateConnectionState(false);
				invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				// Show all the supported services and characteristics on the user interface.
				displayGattServices(mBluetoothLeService.getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};

	private void clearUI() {
		mDataField.setText(R.string.no_data);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		//// TODO: Lars added this
		//this.startService(gattServiceIntent);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		
		Log.i(TAG, "onCreate");
		setContentView(R.layout.heartrate);

		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		mDataField = (TextView) findViewById(R.id.data_value);

		
		/*mButtonStart = (ImageButton) findViewById(R.id.btnStart);
		mButtonStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startLogging();
			}
		});

		mButtonStop = (ImageButton) findViewById(R.id.btnStop);
		mButtonStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				stopLogging();
			}
		});*/

		//getActionBar().setTitle(mDeviceName);
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		//Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		//// TODO: Lars added this
		//this.startService(gattServiceIntent);
		//bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	
		//TEntativa de esperar
		Log.d("en", "vai tentadsar esperar");
		
		handler.sendEmptyMessageDelayed(0, interval);
		
		Log.d("en", "sair da espera");
		//saor da espera
		
		//mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
		//startLogging();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		currentlyVisible = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		currentlyVisible = false;
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
			if (logging) {
				menu.findItem(R.id.menu_start_logging).setVisible(false);
				menu.findItem(R.id.menu_stop_logging).setVisible(true);
				mButtonStart.setVisibility(View.GONE);
				mButtonStop.setVisibility(View.VISIBLE);
			} else {
				menu.findItem(R.id.menu_start_logging).setVisible(true);
				menu.findItem(R.id.menu_stop_logging).setVisible(false);
				mButtonStart.setVisibility(View.VISIBLE);
				mButtonStop.setVisibility(View.GONE);
			}
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
			menu.findItem(R.id.menu_start_logging).setVisible(false);
			menu.findItem(R.id.menu_stop_logging).setVisible(false);
			mButtonStart.setVisibility(View.GONE);
			mButtonStop.setVisibility(View.GONE);
		}

		return true;
	}*/

	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			mBluetoothLeService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
			mBluetoothLeService.disconnect();
			return true;
		case R.id.menu_start_logging:
			startLogging();
			return true;
		case R.id.menu_stop_logging:
			stopLogging();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/

	/*private void updateConnectionState(final boolean connected) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (connected) {
					mButtonStart.setVisibility(View.VISIBLE);
					mButtonStop.setVisibility(View.GONE);
				} else {
					mButtonStart.setVisibility(View.GONE);
					mButtonStop.setVisibility(View.GONE);
				}
			}
		});
	}*/

	private void displayData(String data) {
		if (data != null) {
			mDataField.setText("Pulse: " + data);
        }		
	}

	// Demonstrates how to iterate through the supported GATT
	// Services/Characteristics.
	// In this sample, we populate the data structure that is bound to the
	// ExpandableListView
	// on the UI.
	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null)
			return;
		String uuid = null;
		String unknownServiceString = getResources().getString(R.string.unknown_service);
		String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices) {
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();
			currentServiceData.put(LIST_NAME,
					SampleGattAttributes.lookup(uuid, unknownServiceString));
			currentServiceData.put(LIST_UUID, uuid);
			gattServiceData.add(currentServiceData);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = 
					new ArrayList<HashMap<String, String>>();
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charas = 
					new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

				if (UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT)
						.equals(gattCharacteristic.getUuid())) {
					Log.d(TAG, "Found heart rate");
					mNotifyCharacteristic = gattCharacteristic;
				}

				charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();
				currentCharaData.put(LIST_NAME,
						SampleGattAttributes.lookup(uuid, unknownCharaString));
				currentCharaData.put(LIST_UUID, uuid);
				gattCharacteristicGroupData.add(currentCharaData);
			}
			mGattCharacteristics.add(charas);
			gattCharacteristicData.add(gattCharacteristicGroupData);
		}

	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}


	private void startLogging() {
		//mButtonStop.setVisibility(View.VISIBLE);
		//mButtonStart.setVisibility(View.GONE);
		mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
		invalidateOptionsMenu();
		//logging = true;
	}

	private void stopLogging() {
		//mButtonStop.setVisibility(View.GONE);
		//mButtonStart.setVisibility(View.VISIBLE);
		mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
		invalidateOptionsMenu();
		//logging = false;
	}
}
