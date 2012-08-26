package za.co.house4hack.bluercr;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import za.co.house4hack.bluercr.Instruction.InstructionCommand;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BlueRcrActivity extends Activity {
	private static final String TAG = "BlueTemp";
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothService mService = null;
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	private static final float DEFAULT_DURATION = 3;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	private TextView mTitle;
	private List<String> mReceived;
	private boolean mRunning;
	private ArrayList<Instruction> mInstructionList;
	private InstructionAdapter mListAdapter;
	private ListView mInstructionListView;
	
	private int mCurrentItem=0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		setContentView(R.layout.main);
		
		mInstructionList = new ArrayList<Instruction>();
		mListAdapter = new InstructionAdapter(mInstructionList,this.getLayoutInflater());
		mInstructionListView = (ListView) findViewById(R.id.listInstruction);
		mInstructionListView.setAdapter(mListAdapter);
		

		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the BluetoothChatService to perform bluetooth connections
		mService = new BluetoothService(this, mHandlerBT, mReceived);

	}

	@Override
	public synchronized void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mService != null)
			mService.stop();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case REQUEST_CONNECT_DEVICE:

			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mService.connect(device);
			}
			break;

		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode != Activity.RESULT_OK) {

				finishDialogNoBluetooth();
			}
		}
	}

	public void finishDialogNoBluetooth() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.alert_dialog_no_bt)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.app_name)
				.setCancelable(false)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								finish();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}


	public void connectDevice(final BluetoothDevice device) {
		if (mService != null) {
			mService.connect(device);
		} else {
			Log.d(TAG, "mService null, can't connect to " + device.getName());
		}
	}

	// The Handler that gets information back from the BluetoothService
	private final Handler mHandlerBT = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					if (mMenuItemConnect != null) {
						mMenuItemConnect
								.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
						mMenuItemConnect.setTitle(R.string.disconnect);
					}

					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					setRunning(true);
					break;

				case BluetoothService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;

				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					if (mMenuItemConnect != null) {
						mMenuItemConnect
								.setIcon(android.R.drawable.ic_menu_search);
						mMenuItemConnect.setTitle(R.string.connect);
					}

					mTitle.setText(R.string.title_not_connected);
					setRunning(false);

					break;
				}
				break;
			case MESSAGE_WRITE:

				break;

			case MESSAGE_READ:
				//addData();
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};
	private MenuItem mMenuItemConnect;
	private MenuItem mMenuItemRun;
	private Timer mTimer;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		mMenuItemConnect = menu.getItem(0);
		mMenuItemRun = menu.getItem(1);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.connect_scan:
			if (mService.getState() == BluetoothService.STATE_NONE) {
				// Launch the DeviceListActivity to see devices and do scan
				Intent serverIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			} else if (mService.getState() == BluetoothService.STATE_CONNECTED) {
				mService.stop();
				mService.start();
			}
			return true;
		case R.id.run:
			
				setRunning(!mRunning);
			
		}
		return false;
	}
	public void startTimer() {
		mTimer = new Timer();
		double starttime = 0;
		for(int i=0; i< mInstructionList.size(); i++){
			final Instruction cur = mInstructionList.get(i);
			final int item = i;
			TimerTask t = new TimerTask() {
				@Override
				public void run() {
					if (mService.getState() == BluetoothService.STATE_CONNECTED) {
						mService.write(cur.lookupBT(cur.command).getBytes());
					}
					
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mInstructionListView.setSelection(item);
							DecimalFormat df = new DecimalFormat("#.#");
							Toast.makeText(BlueRcrActivity.this, Integer.toString(item+1)+" - "+cur.lookupCommandText(cur.command)+" for "+df.format(cur.duration)+"s", (int) cur.duration*1000).show();							
						}
					});

				}

			};
			mTimer.schedule(t, (long) (starttime*1000) );
			starttime += cur.duration;
		}
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						setRunning(false);
						
					}
				});
				
			}
		}, (long)starttime*1000);
		

	}
	
	public void stopTimer(){
		if(mTimer !=null) mTimer.cancel();
	}

	public boolean ismRunning() {
		return mRunning;
	}

	public void setRunning(boolean mRunning) {
		this.mRunning = mRunning;
		if(mMenuItemRun!=null){
			if(mRunning){
				mMenuItemRun.setTitle(getString(R.string.stop));
				mMenuItemRun.setIcon(android.R.drawable.ic_media_pause);
				mInstructionListView.setEnabled(false);
				
				//updateData();
				//updateData();
				startTimer();
			} else{
				mMenuItemRun.setTitle(getString(R.string.run));				
				mMenuItemRun.setIcon(android.R.drawable.ic_media_play);
				stopTimer();
				Toast.makeText(BlueRcrActivity.this, "Stopped", Toast.LENGTH_LONG).show();
				mInstructionListView.setEnabled(true);
			}
		}
	}
	
	public void leftClick(View v){
		mInstructionList.add(new Instruction(InstructionCommand.LEFT,DEFAULT_DURATION));
		mListAdapter.notifyDataSetChanged();
		
	}
	public void rightClick(View v){
		mInstructionList.add(new Instruction(InstructionCommand.RIGHT,DEFAULT_DURATION));
		mListAdapter.notifyDataSetChanged();
		
	}
	public void forwardClick(View v){
		mInstructionList.add(new Instruction(InstructionCommand.FORWARD,DEFAULT_DURATION));
		mListAdapter.notifyDataSetChanged();

	}
	public void stopClick(View v){
		mInstructionList.add(new Instruction(InstructionCommand.STOP,DEFAULT_DURATION));
		mListAdapter.notifyDataSetChanged();
		
	}
}