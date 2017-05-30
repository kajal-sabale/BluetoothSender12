package com.example.bluetooth;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements OnClickListener,OnItemClickListener{
	private BluetoothAdapter BA;
	private static final String TAG = "MyActivity";
	private Set<BluetoothDevice>pairedDevices;
	ListView lv;
	Button b1,b2;
	EditText ed;
	BluetoothSocket socket;
	Handler bt_handler;
	int handlerState;
	ConnectedThread mConnectedThread;
	String ed1;
	byte[] theByteArray ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		b1 = (Button) findViewById(R.id.button1);
		b1.setOnClickListener(this);
		b2 = (Button) findViewById(R.id.button2);
		
		
		
		
		lv=(ListView)findViewById(R.id.listView1);
		lv.setOnItemClickListener(this);
		ed=(EditText) findViewById(R.id.editText);
		ed1=ed.getText().toString();
		theByteArray= ed1.getBytes();
		
		

		bt_handler=new Handler()
		{
			@Override
			public void handleMessage(Message msg) 
			{
				if (msg.what==handlerState)
				{
					String readMessage=(String)msg.obj;
					Log.v(TAG, readMessage);
				}
			}
		};
	}




	@Override
	public void onClick(View v) 
	{
		
		Toast.makeText(this, "inside onclick of button", Toast.LENGTH_SHORT).show();

		BA = BluetoothAdapter.getDefaultAdapter();
		pairedDevices = BA.getBondedDevices();
		Toast.makeText(getApplicationContext(), "Showing no of Paired Devices "+pairedDevices.size(),Toast.LENGTH_SHORT).show();
		ArrayList list = new ArrayList();

		for(BluetoothDevice bt : pairedDevices)
		{
			list.add(bt);

		}

		final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);

		lv.setAdapter(adapter);
		

	}
	
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		//	String deviceHardwareAddress = device.getAddress()
		//	Toast.makeText(this,"getting clcd", Toast.LENGTH_SHORT).show();
		//	Toast.makeText(this,parent.getItemAtPosition(position).toString()+parent.getItemAtPosition(position).getClass(), Toast.LENGTH_LONG).show();
		// parent.getItemAtPosition(position).getClass();
		BluetoothDevice a=(BluetoothDevice)parent.getItemAtPosition(position);
		ConnectThread ct=new ConnectThread(a);
		ct.start();
	}




	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		final UUID MY_UUID = UUID
				.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket
			// because mmSocket is final.
			BluetoothSocket tmp = null;
			mmDevice = device;

			try {
				// Get a BluetoothSocket to connect with the given BluetoothDevice.
				// MY_UUID is the app's UUID string, also used in the server code.
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
				Log.d(TAG,"Inside connect thread");
			} catch (IOException e) {
				Log.e(TAG, "Socket's create() method failed", e);
			}
			mmSocket = tmp;
		}

		public void run() 
		{
		
			// Cancel discovery because it otherwise slows down the connection.
			BA.cancelDiscovery();
			/*Looper.prepare();
			
			Looper.loop();*/

			try
			{
				// Connect to the remote device through the socket. This call blocks
				// until it succeeds or throws an exception.
				mmSocket.connect();
				Log.d(TAG, "After connection");

				//Toast.makeText(MainActivity.this,"Connection established successfully" , Toast.LENGTH_LONG).show();
			} catch (IOException connectException)
			{
				// Unable to connect; close the socket and return.
				//Toast.makeText(MainActivity.this,"Unable to connect; close the socket and return." , Toast.LENGTH_LONG).show();

				try 
				{
					mmSocket.close();


				} catch (IOException closeException) 
				{
					//    Log.e(TAG, "Could not close the client socket", closeException);
					//Toast.makeText(MainActivity.this,"Could not close the client socket.... close exception" , Toast.LENGTH_LONG).show();
				}
				return;
			}

			// The connection attempt succeeded. Perform work associated with
			// the connection in a separate thread.


			mConnectedThread = new ConnectedThread(mmSocket);
			mConnectedThread.start();


			




		}
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "Could not close the client socket", e);
			}
		}
	}


	/*public class MyBluetoothService {
		private static final String TAG = "MY_APP_DEBUG_TAG";
		private Handler mHandler; // handler that gets info from Bluetooth service

		// Defines several constants used when transmitting messages between the
		// service and the UI.
		private interface MessageConstants {
			public static final int MESSAGE_READ = 0;
			public static final int MESSAGE_WRITE = 1;
			public static final int MESSAGE_TOAST = 2;

			// ... (Add other message types here as needed.)
		}*/



		private class ConnectedThread extends Thread 
		{
			
			private Handler mHandler;
			private final BluetoothSocket mmSocket;
			private final InputStream mmInStream;
			private OutputStream mmOutStream;
			 // mmBuffer store for the stream


			public ConnectedThread(BluetoothSocket socket)
			{
				Log.d(TAG, "Connected thread");
				mmSocket = socket;
				InputStream tmpIn = null;
				OutputStream tmpOut = null;

				try {
					tmpIn = socket.getInputStream();
				} catch (IOException e) {
					Log.e(TAG, "Error occurred when creating input stream", e);
				}
				try {
					tmpOut = socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "Error occurred when creating output stream", e);
				}

				mmInStream = tmpIn;
				mmOutStream = tmpOut;
			}

			public void run()
			{	
				Log.d(TAG, "In connected run ");
				b2.setOnClickListener(new View.OnClickListener() 
				{
	    			public void onClick(View v)
	    			{ 				  
	    				String msg = ed.getText().toString();
	    			    msg += "\n";
	    			    Log.d(TAG,"data send...."+msg);
	    			    Log.d(TAG,"data send...."+msg.getBytes());
	    			    try
	    			    {
	    			    	mmOutStream = mmSocket.getOutputStream();
	    					Log.e(TAG,"o/p= "+mmOutStream);	    					
	    					 byte[] msgBuffer = msg.getBytes();
	    					 mmOutStream.write(msgBuffer);
	    					 mmOutStream.flush();							 
							Log.d(TAG,"data send...."+msg);
						} 
	    			    catch (IOException e) 
						{						
							System.out.println(e);
							Log.e(TAG,"Data Not Send.."+e);
						}
	    			}
	    		});  

				/*Log.d("TAG", "In run");
				int numBytes; // bytes returned from read()

				// Keep listening to the InputStream until an exception occurs.
				while (true) {
					try {
						// Read from the InputStream.
						numBytes = mmInStream.read(theByteArray);
						write(theByteArray);
						// Send the obtained bytes to the UI activity.
						Message readMsg = mHandler.obtainMessage(
								0, numBytes,-1,
								theByteArray);
						readMsg.sendToTarget();
						
					} catch (IOException e) {
						Log.d(TAG, "Input stream was disconnected", e);
						break;
					}
				}*/
			}

			public void write(byte[] bytes)
			{
				try {
					mmOutStream.write(bytes);

					// Share the sent message with the UI activity.
					/*Message writtenMsg = mHandler.obtainMessage(
							1, -1, -1, theByteArray);
					writtenMsg.sendToTarget();*/
				} catch (IOException e) {
					Log.e(TAG, "Error occurred when sending data", e);

					// Send a failure message back to the activity.
					Message writeErrorMsg =
							mHandler.obtainMessage(2);
					Bundle bundle = new Bundle();
					bundle.putString("toast",
							"Couldn't send data to the other device");
					writeErrorMsg.setData(bundle);
					mHandler.sendMessage(writeErrorMsg);
				}
			}
			public void cancel() {
				try {
					mmSocket.close();
				} catch (IOException e) {
					Log.e(TAG, "Could not close the connect socket", e);
				}
			}
		}
	}

	//create new class for connect thread
	/*private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes; 

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

              }
            }
        }
	 */


	// Closes the client socket and causes the thread to finish.










	/*import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int REQUEST_ENABLE_BT = 0;  
	private static final int REQUEST_DISCOVERABLE_BT = 0;
	TextView pairedDevices;
	//getting bluetooth adapter

	@Override  
	protected void onCreate(Bundle savedInstanceState) 
	{  
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.activity_main);  
		final TextView out=(TextView)findViewById(R.id.out);  
		pairedDevices=(TextView)findViewById(R.id.pairedDevices);
		final EditText getData=(EditText)findViewById(R.id.editText);
		final Button turnOn = (Button) findViewById(R.id.turnOn);  
		final Button turnOff = (Button) findViewById(R.id.turnOff);  
		final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


		if (mBluetoothAdapter == null)
		{  
			out.append("device not supported");  
		}


		turnOn.setOnClickListener(new View.OnClickListener() 
		{  
			public void onClick(View v) 
			{  
				if (!mBluetoothAdapter.isEnabled()) 
				{  
					Toast.makeText(getApplicationContext(), "TURNING ON",  
							Toast.LENGTH_SHORT);  
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);  
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);  

					if (!mBluetoothAdapter.isDiscovering()) {  
						//out.append("MAKING YOUR DEVICE DISCOVERABLE");  
						Toast.makeText(getApplicationContext(), "MAKING YOUR DEVICE DISCOVERABLE",  
								Toast.LENGTH_SHORT).show();  

						Intent enableBtIntent1 = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);  
						startActivityForResult(enableBtIntent1, REQUEST_DISCOVERABLE_BT);  
					}
				}  
			}  
		});

		turnOff.setOnClickListener(new View.OnClickListener() {  
			 @Override  
		        public void onClick(View arg0) {     
		            mBluetoothAdapter.disable();  
		            //out.append("TURN_OFF BLUETOOTH");  
		            Toast.makeText(getApplicationContext(), "TURNING_OFF BLUETOOTH", Toast.LENGTH_SHORT).show();  

		            }  
		}); 

		//CheckBluetoothState();  
	}  

	 It is called when an activity completes.  
	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		super.onActivityResult(requestCode, resultCode, data);  
		if (requestCode == REQUEST_ENABLE_BT) {  
			CheckBluetoothState();  
		}  
	}  



	private void CheckBluetoothState() 
	{  
	if (mBluetoothAdapter.isEnabled())
		{  

			// Listing paired devices   
			Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();  
			for (BluetoothDevice device : devices) {  
				pairedDevices.append("\n  Device: " + device.getName() + ", " + device);  
			}  
		} 

		// Checks for the Bluetooth support and then makes sure it is turned on  
		// If it isn't turned on, request to turn it on  
		// List paired devices  
		if(mBluetoothAdapter==null)
		{   
			pairedDevices.append("\nBluetooth NOT supported. Aborting.");  
			return;  
		}
		else 
		{  
			if (mBluetoothAdapter.isEnabled())
			{  
				pairedDevices.append("\nBluetooth is enabled...");  

				// Listing paired devices  
				pairedDevices.append("\nPaired Devices are:");  
				Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();  
				for (BluetoothDevice device : devices) {  
					pairedDevices.append("\n  Device: " + device.getName() + ", " + device);  
				}  
			} 
			else
			{  
				//Prompt user to turn on Bluetooth  
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);  
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);  
			}  
		}  
	}  

	 */