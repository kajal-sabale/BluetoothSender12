package com.example.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ThirdActivity extends Activity {
	public boolean flag = true;
	protected static final String TAG = null;
	private InputStream mmInStream;
	private OutputStream mmOutStream;
	Button b2, b3;
	static int cnt = 1;
	EditText ed;
	// ConnectedThread mConnectedThread;
	private BluetoothAdapter BTAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_third);
		
		ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#18b0d9"));
	    getActionBar().setBackgroundDrawable(colorDrawable);
		
		b2 = (Button) findViewById(R.id.button2);
		b3 = (Button) findViewById(R.id.button3);
		// ed=(EditText)findViewById(R.id.editText1);
		BTAdapter = BluetoothAdapter.getDefaultAdapter();

		/*
		 * Bundle b = new Bundle(); b = getIntent().getExtras(); String address
		 * = b.getString("Address"); //Toast.makeText(this,name,
		 * Toast.LENGTH_SHORT).show(); BluetoothDevice
		 * a=BTAdapter.getRemoteDevice(address);
		 */

		if (SocketHandler.getSocket() != null) {
			final ConnectedThread ct = new ConnectedThread(
					SocketHandler.getSocket());
			Log.i("TAG", "Before connect  thread");

			b2.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (!ct.isAlive()) {
						b2.setText("Sending");
						ct.start();

					} else {
						
						synchronized(ct) {
							b2.setText("Sending");
							flag = true;
						Log.e(TAG, "Resuming.....");
						ct.notify();
						}
					}
				}
			});

			b3.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					b2.setText("Send");
					flag = false;

					// ct.suspend();
				}
			});
		} else {
			Log.i("TAG", "Socket is null");
		}

	}

	private class ConnectedThread extends Thread {

		protected static final String TAG = " ";
		private Handler mHandler;
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private OutputStream mmOutStream;
		private byte[] mmBuffer = new byte[1024];

		// mmBuffer store for the stream

		public ConnectedThread(BluetoothSocket socket) {
			Log.d(TAG, "Connected thread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = socket.getInputStream();
				Log.d(TAG, "creating input stream");
			} catch (IOException e) {
				Log.e(TAG, "Error occurred when creating input stream", e);
			}
			try {
				tmpOut = socket.getOutputStream();
				Log.d(TAG, "creating output stream");
			} catch (IOException e) {
				Log.e(TAG, "Error occurred when creating output stream", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {

			Log.d(TAG, "In connected run ");

			String msg;
			int x = 0;
			synchronized (this) {

				while (flag) {

					double y = Math.random() * 50;
					int yy = (int) y;
					// (6.0,10)
					// Log.i("TAg","X"+x);
					msg = "(" + x + "," + yy + ")";
					x++;
					try {
						mmOutStream = mmSocket.getOutputStream();
						Log.e(TAG, "o/p= " + mmOutStream);
						// byte[] msgBuffer = msg.getBytes();
						mmOutStream.write(msg.getBytes());
						mmOutStream.flush();
						Log.d(TAG, "data send...." + msg);
						sleep(1000);

						if (flag == false) {
							System.out.println("Going to wait()");
							Log.e(TAG, "Waiting.....");
							this.wait();
						}

					} catch (IOException e) {
						System.out.println(e);
						Log.e(TAG, "Data Not Send.." + e);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}

		}

		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);

			} catch (IOException e) {
				Log.e(TAG, "Error occurred when sending data", e);

				// Send a failure message back to the activity.
				Message writeErrorMsg = mHandler.obtainMessage(2);
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
