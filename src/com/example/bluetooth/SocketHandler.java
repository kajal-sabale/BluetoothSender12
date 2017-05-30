package com.example.bluetooth;

import android.bluetooth.BluetoothSocket;

public class SocketHandler 
{
	private static BluetoothSocket socket;
	 public static BluetoothSocket getSocket(){
	        return socket;
	    }

	    public static void setSocket(BluetoothSocket socket){
	        SocketHandler.socket = socket;
	    }
}
