/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package epusp.pcs.os.appemergencia;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.pcsos.appemergencia.R;

/**
 * This the app's main Activity. It provides buttons for requesting the various features of the
 * app, displays the current location, the current address, and the status of the location client
 * and updating services.
 *
 * {@link #getLocation} gets the current location using the Location Services getLastLocation()
 * function. {@link #getAddress} calls geocoding to get a street address for the current location.
 * {@link #startUpdates} sends a request to Location Services to send periodic location updates to
 * the Activity.
 * {@link #stopUpdates} cancels previous periodic update requests.
 *
 * The update interval is hard-coded to be 5 seconds.
 */
public class MainActivity extends Activity {

	/**
	 * Reference to our bound service.
	 */
	BackgroundService mService = null;
	boolean mServiceConnected = false;

	// Bluetooth enable request code
	private final static int REQUEST_ENABLE_BT = 1;
	// Bluetoothe Adapter
	private BluetoothAdapter mBluetoothAdapter;
	private Set<BluetoothDevice> mPairedDevices;
	private ArrayList<BluetoothDevice> mPairedDeviceList = 
			new ArrayList<BluetoothDevice>(); // FIXME Do I really need two structures for paired devices??? Seriously???
	// private Handler mHandler = new ServiceHandler(); // Have no idea what this is...
	ArrayAdapter<String> mArrayAdapter;
	ListView mPairedDevicesListView;
	
	FragmentManager fragmentManager = getFragmentManager();

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d("BinderActivity", "Connected to service.");
			mService = ((BackgroundService.LocalBinder) binder).getService();
			mServiceConnected = true;
		}

		/**
		 * Connection dropped.
		 */
		@Override
		public void onServiceDisconnected(ComponentName className) {
			Log.d("BinderActivity", "Disconnected from service.");
			mService = null;
			mServiceConnected = false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.add(R.id.sample_content_fragment, fragment, "FRAGMENT");
            transaction.commit();
        }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(bReceiver);
		if (mServiceConnected)
			unbindService(mConn);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Bind to LocalService
		Intent intent = new Intent(this, BackgroundService.class);
		this.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Unbind from the service
		if (mServiceConnected) {
			unbindService(mConn);
			mServiceConnected = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search_devices:
			find();
			break;
		case R.id.pair_device:
			// Launch the DeviceListActivity to see devices and do scan
//            Intent serverIntent = new Intent(this, DeviceListActivity.class);
//            startActivityForResult(serverIntent, 2);
            //return true;
			listPairedDevices();
			break;
		default:
            return super.onOptionsItemSelected(item);
		}
		return true;  
	}

	// Função chamada quando o usuário aperta o botão
	public void help(View v) {
		//startService(v);
		if(mServiceConnected)
			mService.startEmCall();
	}

	// Method to start the service
	public void startService(View view) {
		//		Intent intent = new Intent(this, BackgroundService.class);
		//		ComponentName name = intent.getComponent();
		//		name = startService(intent);
		boolean bind = getApplicationContext().bindService(new Intent(this, BackgroundService.class), mConn, Context.BIND_AUTO_CREATE);

	}

	// Method to stop the service
	public void stopService(View view) {
		stopService(new Intent(this, BackgroundService.class));
		//		if (mServiceConnected) {
		//			unbindService(mConn);
		//			stopService(new Intent(getBaseContext(), BackgroundService.class));
		//			mServiceConnected = false;
		//		}
	}

	//----------------------------------------------------------------------------------------------------------------------------

	final BroadcastReceiver bReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// add the name and the MAC address of the object to the arrayAdapter
				mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				mArrayAdapter.notifyDataSetChanged();
			}
		}
	};

	public void find() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.devices_dialog);
		dialog.setTitle("Parear dispositivos");
		mPairedDevicesListView = (ListView) dialog.findViewById(R.id.paired_devices);

		// Bluetooth setting up
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(!mBluetoothAdapter.isEnabled()){
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			//mDeviceStatus.setText(R.string.connected);
		}

		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		mPairedDevicesListView.setAdapter(mArrayAdapter);
		
		if (mBluetoothAdapter.isDiscovering()) {
			// the button is pressed when it discovers, so cancel the discovery
			mBluetoothAdapter.cancelDiscovery();
		}
		else {
			mArrayAdapter.clear();
			Boolean start = mBluetoothAdapter.startDiscovery();
			registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));   
		}   
		dialog.show();
	}



	/**
	 * Invoked by the "List" button.
	 * Instantiates the bluetooth adapter and starts listening to requests.
	 *
	 * @param v The view object associated with this method, in this case a Button.
	 */
	public void listPairedDevices(){
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.devices_dialog);
		dialog.setTitle("Parear dispositivos");
		mPairedDevicesListView = (ListView) dialog.findViewById(R.id.paired_devices);

		// Bluetooth setting up
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(!mBluetoothAdapter.isEnabled()){
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			//mDeviceStatus.setText(R.string.connected);
		}

		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		mPairedDevicesListView.setAdapter(mArrayAdapter);


		mPairedDevices = mBluetoothAdapter.getBondedDevices();

		// If there are paired devices
		for (BluetoothDevice device : mPairedDevices) {
			// Add the name and address to an array adapter to show in a ListView
			mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			mPairedDeviceList.add(device);
		}

		mPairedDevicesListView.setAdapter(mArrayAdapter);

		mPairedDevicesListView.setOnItemClickListener(
				new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent,
							final View view, int position, long id) {

						Log.w("myApp", "onItemClick");
						Log.w("myApp", String.valueOf(position));
						// Log.w("myApp", String.valueOf(id));

						connectDevice(position);
						// Connect to device

						/*
						 * view.animate().setDuration(2000).alpha(0)
						 * .withEndAction(new Runnable() {
						 * 
						 * @Override public void run() { list.remove(item);
						 * adapter.notifyDataSetChanged(); view.setAlpha(1); }
						 * });
						 */
					}

				}); 
		dialog.show();
	}

	public void connectDevice(int position){

		BluetoothDevice bluetoothDevice = mPairedDeviceList.get(position);
		Log.w("myApp", bluetoothDevice.getName());

		ConnectThread connectThread = new ConnectThread(bluetoothDevice);
		Log.w("myApp", "ruuuuun");
		connectThread.run();

	}

	public class ConnectThread extends Thread {
		private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		private final BluetoothSocket mmSocket;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) { }
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			Log.w("", "It's running!");
			mBluetoothAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception            	
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				Toast.makeText(getApplicationContext(), "Unable to connect",
						Toast.LENGTH_SHORT).show();
				try {
					Log.w("myApp", "Not connected! D:");
					connectException.printStackTrace();
					mmSocket.close();
				} catch (IOException closeException) { }
				return;
			}

			// Do work to manage the connection (in a separate thread)
			ConnectedThread connectedThread = new ConnectedThread(mmSocket);
			connectedThread.run();
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}
	}

	public class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			Log.w("myApp", "It's is connected!!!");

			byte[] buffer = new byte[1024];  // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);

					//String message = new String(buffer, "UTF-8"); // kinda works...
					//Log.w("myApp", message);

					// Send the obtained bytes to the UI activity
					byte[] msg = new byte[bytes];
					for(int i = 0; i < bytes; i++){
						String message = new String(buffer, "UTF-8");
					}
					// Log.w("myApp", message);
					// Message message = mHandler.obtainMessage(9999, bytes, -1, buffer);
					//        .sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) { }
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}
	}

}