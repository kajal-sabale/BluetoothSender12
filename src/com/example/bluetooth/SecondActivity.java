package com.example.bluetooth;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager;

public class SecondActivity extends Activity {

	Switch switch1, switch2;
	TextView listAvialableDevices;
	ListView lv, lva;
	String address;
	ArrayList list, list1;
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_DISCOVERABLE_BT = 2;
	private static final int REQUEST_BT_PAIRING = 3;
	protected static final String TAG = null;
	BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
	private ProgressDialog mProgressDlg;
	BluetoothDevice device2;

	// TextView TextViewPairedDevices;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);
		
		ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#18b0d9"));
	    getActionBar().setBackgroundDrawable(colorDrawable);
		
		lv = (ListView) findViewById(R.id.listPairedDevices);
		lva = (ListView) findViewById(R.id.AvailbeDevices);
		// TextViewPairedDevices=(TextView)findViewById(R.id.TextViewPairedDevices);

		mProgressDlg = new ProgressDialog(this);

		// final IntentFilter filter = new IntentFilter();
		// filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

		// Phone does not support Bluetooth so let the user know and exit.

		switch1 = (Switch) findViewById(R.id.switch1);
		switch2 = (Switch) findViewById(R.id.switch2);

		switch1.setChecked(false);
		switch2.setChecked(false);

		if (BTAdapter == null) {
			new AlertDialog.Builder(this)
			.setTitle("Not compatible")
			.setMessage("Your phone does not support Bluetooth")
			.setPositiveButton("Exit",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int which) {
					System.exit(0);
				}
			}).setIcon(android.R.drawable.ic_dialog_alert)
			.show();
		}

		if (!BTAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			// switch1.setChecked(true);
		} else {
			switch1.setChecked(true);
			ArrayAdapter aa = pairedDevices();
			lv.setAdapter(aa);
		}

		switch1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					Log.i("TAg", "In switch1");
					Intent enableBtIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
					if (BTAdapter.isEnabled()) {
						// Listing paired devices
						ArrayAdapter aa = pairedDevices();
						lv.setAdapter(aa);

					}
				} else {
					Log.i("TAg", "In switch1 close");
					BTAdapter.disable();
					lv.setAdapter(null);
					lva.setAdapter(null);
					switch2.setChecked(false);

					// ((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
					Toast.makeText(SecondActivity.this, "Turning off",
							Toast.LENGTH_SHORT).show();
				}

			}

		});

		switch2.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (BTAdapter.isEnabled()) {

					if (isChecked) {

						Log.i("TAg", "In swtich2" + isChecked);
						mProgressDlg.setMessage("Scanning...");
						mProgressDlg.show();
						Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							public void run() {
								mProgressDlg.dismiss();
							}
						}, 2000);

						BTAdapter.startDiscovery();

						IntentFilter filter = new IntentFilter();
						filter.addAction(BluetoothDevice.ACTION_FOUND);
						filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
						filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
						filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
						filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
						registerReceiver(mReceiver, filter);
					} else {
						Log.i("TAg", "In switch2 close" + isChecked);
						Toast.makeText(SecondActivity.this, "Visibility off",
								Toast.LENGTH_SHORT).show();
						lva.setAdapter(null);
						unregisterReceiver(mReceiver);
						// switch2.setChecked(false);
					}
				} else {
					Toast.makeText(SecondActivity.this, "First On Bluetooth",
							Toast.LENGTH_SHORT).show();
					lva.setAdapter(null);
					// switch1.setChecked(false);
					switch2.setChecked(false);
				}
			}

		});

		/*
		 * ArrayAdapter aa=pairedDevices(); lv.setAdapter(aa);
		 */

		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View arg1,
					int position, long arg3) {
				String device = parent.getItemAtPosition(position).toString();
				address = device.substring(device.length() - 17);
				device2 = BTAdapter.getRemoteDevice(address);

				final AlertDialog.Builder builder = new AlertDialog.Builder(
						SecondActivity.this);
				builder.setTitle("Info")

				.setPositiveButton("UnPair",
						new DialogInterface.OnClickListener() {
					public void onClick(
							final DialogInterface dialog,
							final int id) {
						// Toast.makeText(SecondActivity.this,
						// "unpair",Toast.LENGTH_SHORT).show();

						/*
						 * Intent pairingBtIntent = new
						 * Intent(BluetoothDevice
						 * .ACTION_PAIRING_REQUEST);
						 * startActivityForResult
						 * (pairingBtIntent,
						 * REQUEST_BT_PAIRING);
						 */
						unpairDevice(device2);
						Log.d("TAg", "jtuyuiy");
						BTAdapter.startDiscovery();
						ArrayAdapter aa = pairedDevices();
						lv.setAdapter(aa);
						aa.notifyDataSetChanged();

					}
				})
				.setNegativeButton("Send Data",
						new DialogInterface.OnClickListener() {
					public void onClick(
							final DialogInterface dialog,
							final int id) {
						/*Toast.makeText(SecondActivity.this,
								"data", Toast.LENGTH_SHORT)
								.show();*/
						BluetoothDevice a = BTAdapter
								.getRemoteDevice(address);
						ConnectThread ct = new ConnectThread(a);
						Log.i("TAG", "Before connect  thread");
						ct.start();
						Intent intent = new Intent(
								SecondActivity.this,
								ThirdActivity.class);
						startActivity(intent);
					}
				});

				builder.show();

			}
		});

		lva.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View arg1,
					int position, long arg3) {

				String device = parent.getItemAtPosition(position).toString();
				String address = device.substring(device.length() - 17);
				BluetoothDevice device1 = BTAdapter.getRemoteDevice(address);
				try {

					Method method = device1.getClass().getMethod("createBond",
							(Class[]) null);
					method.invoke(device1, (Object[]) null);
					Intent pairingBtIntent = new Intent(
							BluetoothDevice.ACTION_PAIRING_REQUEST);
					startActivityForResult(pairingBtIntent, REQUEST_BT_PAIRING);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		final UUID MY_UUID = UUID
				.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

		public ConnectThread(BluetoothDevice device) {
			Log.i("TAG", "inside cons of connect thread");
			// BTAdapter = BluetoothAdapter.getDefaultAdapter();
			// Use a temporary object that is later assigned to mmSocket
			// because mmSocket is final.
			Log.i("TAG", "inside cons of connect thread..11");
			BluetoothSocket tmp = null;
			mmDevice = device;
			Log.i("TAG", "inside cons of connect thread..22");
			try {
				// Get a BluetoothSocket to connect with the given
				// BluetoothDevice.
				// MY_UUID is the app's UUID string, also used in the server
				// code.
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
				Log.d("TAG", "Inside connect thread........");
			} catch (IOException e) {
				Log.e("TAG", "Socket's create() method failed", e);
			}
			mmSocket = tmp;
			SocketHandler.setSocket(mmSocket);
			if (tmp == null) {
				Log.i("TAG", "dfsfdgdrfg");
			} else {
				Log.i("TAG", "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
			}
		}

		public void run() {

			// Cancel discovery because it otherwise slows down the connection.
			BTAdapter.cancelDiscovery();
			/*
			 * Looper.prepare();
			 * 
			 * Looper.loop();
			 */

			try {
				// Connect to the remote device through the socket. This call
				// blocks
				// until it succeeds or throws an exception.
				Log.d("TAG", "before socket connection");
				mmSocket.connect();

				Log.d("TAG", "After connection establishment successfully");

				// Toast.makeText(MainActivity.this,"Connection established successfully"
				// , Toast.LENGTH_LONG).show();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and return.
				// Toast.makeText(MainActivity.this,"Unable to connect; close the socket and return."
				// , Toast.LENGTH_LONG).show();

				try {
					mmSocket.close();

				} catch (IOException closeException) {
					// Log.e(TAG, "Could not close the client socket",
					// closeException);
					// Toast.makeText(MainActivity.this,"Could not close the client socket.... close exception"
					// , Toast.LENGTH_LONG).show();
				}
				return;
			}

			// The connection attempt succeeded. Perform work associated with
			// the connection in a separate thread.

		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e("TAG", "Could not close the client socket", e);
			}
		}
	}

	private void unpairDevice(BluetoothDevice device) {
		try {
			Log.d("TAg", "in unpair");
			Method m = device.getClass()
					.getMethod("removeBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
			ArrayAdapter aa = pairedDevices();
			lv.setAdapter(aa);
			aa.notifyDataSetChanged();

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		ArrayAdapter aa = pairedDevices();
		lv.setAdapter(aa);
		aa.notifyDataSetChanged();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		ArrayAdapter aa = pairedDevices();
		lv.setAdapter(aa);
		lv.invalidateViews();
		aa.notifyDataSetChanged();
		BTAdapter.startDiscovery();
	}

	private void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
		.show();
	}

	// Create a BroadcastReceiver for ACTION_FOUND.
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				final int state = intent.getIntExtra(
						BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

				if (state == BluetoothAdapter.STATE_ON) {
					ArrayAdapter aa = pairedDevices();
					lv.setAdapter(aa);
					// lv.invalidateViews();
					// aa.notifyDataSetChanged();
					Log.i("TAg", "In state changed");
					showToast("Enabled");

				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				Log.i("TAg", "In action discovery");
				list1 = new ArrayList<BluetoothDevice>();

				
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Log.i("TAg", "In ACTION_FOUND");

				Set<BluetoothDevice> devices = BTAdapter.getBondedDevices();
				// Log.i("TAg"," paired  "+devices.size());

				BluetoothDevice device = (BluetoothDevice) intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Log.i("TAg"," avaliable  "+device.describeContents());
				address = device.getName() + "\n" + device.getAddress();

				if (list1 == null)
					list1 = new ArrayList<BluetoothDevice>();

				if (list1.contains(address))
				{

				} else 
				{
					if (device.getBondState() != BluetoothDevice.BOND_BONDED)
						list1.add(address);
				}

				ArrayAdapter aa = new ArrayAdapter(SecondActivity.this,
						android.R.layout.simple_list_item_1, list1);
				lva.setAdapter(aa);
				//aa.notifyDataSetChanged();

				BTAdapter.startDiscovery();

			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				Log.i(TAG, "In ACTION_DISCOVERY_FINISHED ");
				BTAdapter.startDiscovery();
			}
		}
	};

	@Override
	public void onPause() {
		if (BTAdapter != null) {
			if (BTAdapter.isDiscovering()) {
				BTAdapter.cancelDiscovery();
			}
		}

		super.onPause();
	}

	/*
	 * @Override public void onDestroy() { unregisterReceiver(mReceiver);
	 * 
	 * super.onDestroy(); }
	 */

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen for landscape and portrait
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			//Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
			// setContentView(R.layout.activity_second);

		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			//Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
			// setContentView(R.layout.activity_second);
		}
	}

	public ArrayAdapter pairedDevices() {
		Log.d("TAG", "in paired devices");
		// TODO Auto-generated method stub
		lv = (ListView) findViewById(R.id.listPairedDevices);
		Set<BluetoothDevice> devices = BTAdapter.getBondedDevices();
		list = new ArrayList();

		for (BluetoothDevice bt : devices) {
			list.add(bt.getName() + "\n" + bt.getAddress());

		}

		ArrayAdapter aa = new ArrayAdapter(this,
				android.R.layout.simple_list_item_1, list);

		aa.notifyDataSetChanged();
		BTAdapter.startDiscovery();
		return aa;

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_BT_PAIRING) {
			if (resultCode == RESULT_OK)
			{
				BTAdapter.startDiscovery();
				ArrayAdapter aa = pairedDevices();
				lv.setAdapter(aa);

			} else 
			{
				ArrayAdapter aa = pairedDevices();
				lv.setAdapter(aa);
				BTAdapter.startDiscovery();
			}
		}
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				ArrayAdapter aa = pairedDevices();
				lv.setAdapter(aa);
				switch1.setChecked(true);
			} else {
				Toast.makeText(SecondActivity.this, "Cancelled",
						Toast.LENGTH_LONG).show();
				lv.setAdapter(null);
				switch2.setChecked(false);
				finish();
			}
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
	}
}
