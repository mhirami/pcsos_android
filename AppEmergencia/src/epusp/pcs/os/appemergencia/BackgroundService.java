package epusp.pcs.os.appemergencia;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
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

		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();

		//Set the update interval
		mLocationRequest.setInterval(5000);
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

			new AddEmergencyCallAsyncTask(this, "stmidori@gmail.com", position).execute();
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
			}
			else if(emCallStatus.getStatus().equals("Finished")) {
				//Parar de atualizar a posição
				if (mLocationClient.isConnected()) {
					stopPeriodicUpdates();
				}
			}
		}
	}

	//---------------------------------------------------------------------------------------------------------------

}
