package epusp.pcs.os.android;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import epusp.pcs.os.workflow.emcallworkflowendpoint.Emcallworkflowendpoint;
import epusp.pcs.os.workflow.emcallworkflowendpoint.model.EmergencyCallLifecycleStatus;
import epusp.pcs.os.workflow.emcallworkflowendpoint.model.Position;

public class BackgroundService extends Service implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	String SERVICE_TAG = "BackgroundService";
	
	String email = null;
	
	//Indica se a vítima está em perigo
	private static boolean needsAssistance = false;
	//Indica se o dispositivo já foi notificado que o atendimento começou
	private static boolean isNotified = false;
	
	private static String TAG = "AppVitimaBack";

	// A request to connect to Location Services
	private LocationRequest mLocationRequest;

	// Stores the current instantiation of the location client in this object
	private LocationClient mLocationClient;

	// Handle to SharedPreferences for this app
	SharedPreferences mPrefs;

	// Handle to a SharedPreferences editor
	SharedPreferences.Editor mEditor;

	/*
	 * Note if updates have been turned on. Starts out as "false"; is set to "true" in the
	 * method handleRequestSuccess of LocationUpdateReceiver.
	 *
	 */
	boolean mUpdatesRequested = false;
	
	//----------------------------------------------------------------------------------------------------------------------
	//BLUETOOTH
	private static final int REQUEST_ENABLE_BT = 3;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    //private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;
    
    //----------------------------------------------------
    
    private ByteQueue mByteQueue;
    /**
     * Used to temporarily hold data received from the remote process. Allocated
     * once and used permanently to minimize heap thrashing.
     */
    private byte[] mReceiveBuffer;
    
    /**
     * Our message handler class. Implements a periodic callback.
     */
    final Handler btHandler = new Handler() {
        /**
         * Handle the callback message. Call our enclosing class's update
         * method.
         *
         * @param msg The callback message.
         */
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                update();
            }
        }
    };
    
    final Runnable mCheckSize = new Runnable() {
        public void run() {
           // updateSize();
            btHandler.postDelayed(this, 1000);
        }
    };	

	//----------------------------------------------------------------------------------------------------------------------

	/** indicates how to behave if the service is killed */
	int mStartMode;
	/** indicates whether onRebind should be used */
	boolean mAllowRebind;

	// Flag that indicates if a request is underway.
	private boolean mInProgress;

	private Boolean servicesAvailable = false;

	public class LocalBinder extends Binder {
		public BackgroundService getService() {
			return BackgroundService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	/** Called when the service is being created. */
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d("onCreate", SERVICE_TAG);
		
		//--------------------------------------------------------------------------------
		//LOCATION
		
		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();

		//Set the update interval
		mLocationRequest.setInterval(30000);
		//mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Distância considerada para fazer update da posição
		//mLocationRequest.setSmallestDisplacement(10);

		// Set the interval ceiling to one minute
		mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

		// Note that location updates are off until the user turns them on
		mUpdatesRequested = false;

		// Open Shared Preferences
		mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

		// Get an editor
		mEditor = mPrefs.edit();

		servicesAvailable = servicesConnected();

		/*
		 * Create a new location client, using the enclosing class to
		 * handle callbacks.
		 */
		mLocationClient = new LocationClient(this, this, this);
		
		mLocationClient.connect();
		
		//----------------------------------------------------------------------
		//Bluetooth
		
		// Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mByteQueue = new ByteQueue(4 * 1024);
        mReceiveBuffer = new byte[4 * 1024];

        // If the adapter is null, then Bluetooth is not supported
//        if (mBluetoothAdapter == null) {
//            Activity activity = getActivity();
//            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
//            activity.finish();
//        }
        
     // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
           //startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
        
     // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
        btHandler.postDelayed(mCheckSize, 1000);
		
	}

	/**
	 * Verify that Google Play services is available before making a request.
	 *
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			return true;
		} else {
			return false;
		}
	}

	public int onStartCommand (Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);

		if(!servicesAvailable || mLocationClient.isConnected() || mInProgress)
			return START_STICKY;

		setUpLocationClientIfNeeded();
		if(!mLocationClient.isConnected() || !mLocationClient.isConnecting() && !mInProgress)
		{
			//appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Started", Constants.LOG_FILE);
			mInProgress = true;
			mLocationClient.connect();
		}

		return START_STICKY;
	}
	
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.d("BinderService", TAG);
//		return super.onStartCommand(intent, flags, startId);
//	}
	
	@Override
    public void onStart(Intent intent, int startid)
    {
        Intent intents = new Intent(getBaseContext(), MainActivity.class);
        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intents);
        
        setUpLocationClientIfNeeded();
		if(!mLocationClient.isConnected() || !mLocationClient.isConnecting() && !mInProgress)
		{
			//appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Started", Constants.LOG_FILE);
			mInProgress = true;
			mLocationClient.connect();
		}
    }

	/*
	 * Create a new location client, using the enclosing class to
	 * handle callbacks.
	 */
	private void setUpLocationClientIfNeeded()
	{
		if(mLocationClient == null) 
			mLocationClient = new LocationClient(this, this, this);
	}

	// Define the callback method that receives location updates
	@Override
	public void onLocationChanged(Location location) {
		// Report to the UI that the location was updated
		String msg = Double.toString(location.getLatitude()) + "," +
				Double.toString(location.getLongitude());
		Log.d("debug", msg);
		// Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		// appendLog(msg, Constants.LOCATION_FILE);

		if(needsAssistance) {
			Position position = new Position();
			position.setLatitude(location.getLatitude());
			position.setLongitude(location.getLongitude());

			new UpdateVictimPositionAndVerifyStatusAsyncTask(this, "stmidori@gmail.com", position).execute();
		}
	}

	/** A client is binding to the service with bindService() */
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/** Called when all clients have unbound with unbindService() */
	@Override
	public boolean onUnbind(Intent intent) {
		return mAllowRebind;
	}

	/** Called when a client is binding to the service with bindService()*/
	@Override
	public void onRebind(Intent intent) {

	}

	/** Called when The service is no longer used and is being destroyed */	   
	@Override
	public void onDestroy(){
		// Turn off the request flag
		mInProgress = false;
		if(servicesAvailable && mLocationClient != null) {
			mLocationClient.removeLocationUpdates(this);
			// Destroy the current location client
			mLocationClient = null;
		}
		if (mChatService != null) {
            mChatService.stop();
        }
		// Display the connection status
		// Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
		//appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Stopped", Constants.LOG_FILE);
		super.onDestroy();
	}

	/*
	 * Called by Location Services when the request to connect the
	 * client finishes successfully. At this point, you can
	 * request the current location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {

		// Request location updates using static settings
		//mLocationClient.requestLocationUpdates(mLocationRequest, this);
		//appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Connected", Constants.LOG_FILE);

		if (mUpdatesRequested) {
			startPeriodicUpdates();
		}

	}

	/*
	 * Called by Location Services if the connection to the
	 * location client drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		// Turn off the request flag
		mInProgress = false;
		// Destroy the current location client
		mLocationClient = null;
		// Display the connection status
		// Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
		//appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected", Constants.LOG_FILE);
	}

	/*
	 * Called by Location Services if the attempt to
	 * Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		mInProgress = false;

		/*
		 * Google Play services can resolve some errors it detects.
		 * If the error has a resolution, try sending an Intent to
		 * start a Google Play services activity that can resolve
		 * error.
		 */
		if (connectionResult.hasResolution()) {

			// If no resolution is available, display an error dialog
		} else {

		}
	}
	//-------------------------------------------------------------------------------------------------------
	//		@Override
	//		protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	//
	//			// Choose what to do based on the request code
	//			switch (requestCode) {
	//
	//			// If the request code matches the code sent in onConnectionFailed
	//			case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :
	//
	//				switch (resultCode) {
	//				// If Google Play services resolved the problem
	//				case Activity.RESULT_OK:
	//
	//					// Log the result
	//					Log.d(LocationUtils.APPTAG, getString(R.string.resolved));
	//
	//					// Display the result
	//					//                        mConnectionState.setText(R.string.connected);
	//					//                        mConnectionStatus.setText(R.string.resolved);
	//					break;
	//
	//					// If any other result was returned by Google Play services
	//				default:
	//					// Log the result
	//					Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));
	//
	//					//                        // Display the result
	//					//                        mConnectionState.setText(R.string.disconnected);
	//					//                        mConnectionStatus.setText(R.string.no_resolution);
	//
	//					break;
	//				}
	//
	//				// If any other request code was received
	//			default:
	//				// Report that this Activity received an unknown requestCode
	//				Log.d(LocationUtils.APPTAG,
	//						getString(R.string.unknown_activity_request_code, requestCode));
	//
	//				break;
	//			}
	//		}


	/**
	 * Invoked by the "Get Location" button.
	 *
	 * Calls getLastLocation() to get the current location
	 *
	 * Antigo getLocation(View v)
	 */
	public void startEmCall() {
		// If Google Play Services is available
		if (servicesConnected()) {

			// Get the current location
			Location currentLocation = mLocationClient.getLastLocation();

			currentLocation.getLatitude();
			currentLocation.getLongitude();

			Position position = new Position();
			position.setLatitude(currentLocation.getLatitude());
			position.setLongitude(currentLocation.getLongitude());

			new AddEmergencyCallAsyncTask(this, email, position).execute();
		}
	}

	/**
	 * Invoked by the "Start Updates" button
	 * Sends a request to start location updates
	 *
	 * @param v The view object associated with this method, in this case a Button.
	 */
	public void startUpdates() {
		mUpdatesRequested = true;

		if (servicesConnected()) {
			startPeriodicUpdates();
		}
	}

	/**
	 * Invoked by the "Stop Updates" button
	 * Sends a request to remove location updates
	 * request them.
	 *
	 * @param v The view object associated with this method, in this case a Button.
	 */
	public void stopUpdates(View v) {
		mUpdatesRequested = false;

		if (servicesConnected()) {
			stopPeriodicUpdates();
		}
	}



	/*
	 * Called by Location Services if the attempt to
	 * Location Services fails.
	 */
	//		@Override
	//		public void onConnectionFailed(ConnectionResult connectionResult) {
	//
	//			/*
	//			 * Google Play services can resolve some errors it detects.
	//			 * If the error has a resolution, try sending an Intent to
	//			 * start a Google Play services activity that can resolve
	//			 * error.
	//			 */
	//			if (connectionResult.hasResolution()) {
	//				try {
	//
	//					// Start an Activity that tries to resolve the error
	//					connectionResult.startResolutionForResult(
	//							this,
	//							LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
	//
	//					/*
	//					 * Thrown if Google Play services canceled the original
	//					 * PendingIntent
	//					 */
	//
	//				} catch (IntentSender.SendIntentException e) {
	//
	//					// Log the error
	//					e.printStackTrace();
	//				}
	//			} else {
	//
	//				// If no resolution is available, display a dialog to the user with the error.
	//				showErrorDialog(connectionResult.getErrorCode());
	//			}
	//		}



	/**
	 * In response to a request to start updates, send a request
	 * to Location Services
	 */
	private void startPeriodicUpdates() {
		mLocationClient.requestLocationUpdates(mLocationRequest, this, Looper.getMainLooper());
		//        mConnectionState.setText(R.string.location_requested);
	}

	/**
	 * In response to a request to stop updates, send a request to
	 * Location Services
	 */
	private void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
		//        mConnectionState.setText(R.string.location_updates_stopped);
	}
	//		
	//		/**
	//		 * Define a DialogFragment to display the error dialog generated in
	//		 * showErrorDialog.
	//		 */
	//		public static class ErrorDialogFragment extends DialogFragment {
	//
	//			// Global field to contain the error dialog
	//			private Dialog mDialog;
	//
	//			/**
	//			 * Default constructor. Sets the dialog field to null
	//			 */
	//			public ErrorDialogFragment() {
	//				super();
	//				mDialog = null;
	//			}
	//
	//			/**
	//			 * Set the dialog to display
	//			 *
	//			 * @param dialog An error dialog
	//			 */
	//			public void setDialog(Dialog dialog) {
	//				mDialog = dialog;
	//			}
	//
	//			/*
	//			 * This method must return a Dialog to the DialogFragment.
	//			 */
	//			@Override
	//			public Dialog onCreateDialog(Bundle savedInstanceState) {
	//				return mDialog;
	//			}
	//		}

	//-------------------------------------------------------------------------------------------------------

	private class AddEmergencyCallAsyncTask extends AsyncTask<Void, Void, Void>{
		Context context;
		String victimId;
		Position position;

		public AddEmergencyCallAsyncTask(Context context, String victimId, Position position) {
			this.context = context;
			this.victimId = victimId;
			this.position = position;
		}

		protected void onPreExecute(){ 
			super.onPreExecute();

			Log.d(SERVICE_TAG, "Adding waiting call...");
		}

		protected Void doInBackground(Void... params) {
			try {
				Emcallworkflowendpoint.Builder builder = new Emcallworkflowendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Emcallworkflowendpoint service =  builder.build();
				service.addWaitingCall(victimId, position).execute();
				//Iniciar atualizações de posição periódicas
				startUpdates();
				needsAssistance = true;
			} catch (Exception e) {
				Log.d("Could not add waiting call", e.getMessage(), e);
			}
			return null;
		}

	}

	//-------------------------------------------------------------------------------------------------------

	private class UpdateVictimPositionAndVerifyStatusAsyncTask extends AsyncTask<Void, Void, EmergencyCallLifecycleStatus>{
		Context context;
		String victimId;
		Position position;

		public UpdateVictimPositionAndVerifyStatusAsyncTask(Context context, String victimId, Position position) {
			this.context = context;
			this.victimId = victimId;
			this.position = position;
		}

		protected void onPreExecute(){ 
			super.onPreExecute();

			Log.d(SERVICE_TAG, "Updating position/verifying status...");
		}

		protected EmergencyCallLifecycleStatus doInBackground(Void... params) {
			EmergencyCallLifecycleStatus response = null;
			try {
				Emcallworkflowendpoint.Builder builder = new Emcallworkflowendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Emcallworkflowendpoint service =  builder.build();
				response = service.updateVictimPositionAndVerifyStatus(victimId, position).execute();
				//Iniciar atualizações de posição periódicas
				//startPeriodicUpdates();
			} catch (Exception e) {
				Log.d("Could not add waiting call", e.getMessage(), e);
			}
			return response;
		}

		protected void onPostExecute(EmergencyCallLifecycleStatus emCallStatus) {
			if(emCallStatus.getStatus().equals("OnCall") && !isNotified) {
				//TODO
				Log.i("Você está sendo atendido!!", "UpdateVictimPositionAndVerifyStatusAsyncTask");
				//Enviar sinal para o relógio avisando que a chamada já está sendo atendida
				sendMessage("OK");
				isNotified = true;
			}
			else if(emCallStatus.getStatus().equals("Finished") && isNotified) {
				Log.i("Chamada finalizada", "UpdateVictimPositionAndVerifyStatusAsyncTask");
				needsAssistance = false;
				isNotified = false;
				//Parar de atualizar a posição
				if (mLocationClient.isConnected()) {
					stopPeriodicUpdates();
				}
			}
		}
	}

	//---------------------------------------------------------------------------------------------------------------
	//BLUETOOTH
	public void write(byte[] buffer, int length) {
        try {
			mByteQueue.write(buffer, 0, length);

            } catch (InterruptedException e) {
        }
        btHandler.sendMessage( btHandler.obtainMessage(1));
    }
    
    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        //mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            //Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
        	message = message + "\n\r";
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
//    private void setStatus(int resId) {
//        Activity activity = getActivity();
//        if (null == activity) {
//            return;
//        }
//        final ActionBar actionBar = activity.getActionBar();
//        if (null == actionBar) {
//            return;
//        }
//        actionBar.setSubtitle(resId);
//    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
//    private void setStatus(CharSequence subTitle) {
//        Activity activity = getActivity();
//        if (null == activity) {
//            return;
//        }
//        final ActionBar actionBar = activity.getActionBar();
//        if (null == actionBar) {
//            return;
//        }
//        actionBar.setSubtitle(subTitle);
//    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
           //Activity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            //setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            //setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    
                    write(writeBuf, msg.arg1);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    
                    write(readBuf, msg.arg1);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
//                    if (null != activity) {
//                        Toast.makeText(activity, "Connected to "
//                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                    }
                    break;
                case Constants.MESSAGE_TOAST:
//                    if (null != activity) {
//                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
//                                Toast.LENGTH_SHORT).show();
//                    }
                    break;
            }
        }
    };
    
    //-----------------------------------------------------------------------
    
    /**
     * Look for new input from the ptty, send it to the terminal emulator.
     */
    private void update() {
        int bytesAvailable = mByteQueue.getBytesAvailable();
        int bytesToRead = Math.min(bytesAvailable, mReceiveBuffer.length);
        try {
            int bytesRead = mByteQueue.read(mReceiveBuffer, 0, bytesToRead);
            String stringRead = new String(mReceiveBuffer, 0, bytesRead);
            //append(mReceiveBuffer, 0, bytesRead);
            
            //FOI RECEBIDA UMA MENSAGEM DO DISPOSITIVO = INICIAR CHAMADA DE EMERGÊNCIA
            //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + stringRead);
            if(!needsAssistance)
            	startEmCall();         
        } catch (InterruptedException e) {
        }
    }
    
    public void setVictimEmail(String email) {
    	this.email = email;
    }
    
    public void BTConnect(String address, boolean secure) {
    	BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
    	mChatService.connect(device, secure);
    }
    
    /**
     * A multi-thread-safe produce-consumer byte array.
     * Only allows one producer and one consumer.
     */

    class ByteQueue {
        public ByteQueue(int size) {
            mBuffer = new byte[size];
        }

        public int getBytesAvailable() {
            synchronized(this) {
                return mStoredBytes;
            }
        }

        public int read(byte[] buffer, int offset, int length)
            throws InterruptedException {
            if (length + offset > buffer.length) {
                throw
                    new IllegalArgumentException("length + offset > buffer.length");
            }
            if (length < 0) {
                throw
                new IllegalArgumentException("length < 0");

            }
            if (length == 0) {
                return 0;
            }
            synchronized(this) {
                while (mStoredBytes == 0) {
                    wait();
                }
                int totalRead = 0;
                int bufferLength = mBuffer.length;
                boolean wasFull = bufferLength == mStoredBytes;
                while (length > 0 && mStoredBytes > 0) {
                    int oneRun = Math.min(bufferLength - mHead, mStoredBytes);
                    int bytesToCopy = Math.min(length, oneRun);
                    System.arraycopy(mBuffer, mHead, buffer, offset, bytesToCopy);
                    mHead += bytesToCopy;
                    if (mHead >= bufferLength) {
                        mHead = 0;
                    }
                    mStoredBytes -= bytesToCopy;
                    length -= bytesToCopy;
                    offset += bytesToCopy;
                    totalRead += bytesToCopy;
                }
                if (wasFull) {
                    notify();
                }
                return totalRead;
            }
        }

        public void write(byte[] buffer, int offset, int length)
        throws InterruptedException {
            if (length + offset > buffer.length) {
                throw
                    new IllegalArgumentException("length + offset > buffer.length");
            }
            if (length < 0) {
                throw
                new IllegalArgumentException("length < 0");

            }
            if (length == 0) {
                return;
            }
            synchronized(this) {
                int bufferLength = mBuffer.length;
                boolean wasEmpty = mStoredBytes == 0;
                while (length > 0) {
                    while(bufferLength == mStoredBytes) {
                        wait();
                    }
                    int tail = mHead + mStoredBytes;
                    int oneRun;
                    if (tail >= bufferLength) {
                        tail = tail - bufferLength;
                        oneRun = mHead - tail;
                    } else {
                        oneRun = bufferLength - tail;
                    }
                    int bytesToCopy = Math.min(oneRun, length);
                    System.arraycopy(buffer, offset, mBuffer, tail, bytesToCopy);
                    offset += bytesToCopy;
                    mStoredBytes += bytesToCopy;
                    length -= bytesToCopy;
                }
                if (wasEmpty) {
                    notify();
                }
            }
        }

        private byte[] mBuffer;
        private int mHead;
        private int mStoredBytes;
    }
	
}
