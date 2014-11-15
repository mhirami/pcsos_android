package epusp.pcs.os.android;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import epusp.pcs.os.workflow.emcallworkflowendpoint.Emcallworkflowendpoint;
import epusp.pcs.os.workflow.emcallworkflowendpoint.model.EmergencyCall;
import epusp.pcs.os.workflow.emcallworkflowendpoint.model.Position;

public class MainActivity extends Activity implements DetailsListFragment.OnUpdateListener, LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {
	
	FragmentManager fragmentManager = getFragmentManager();
    DetailsListFragment detailsListFragment = (DetailsListFragment) fragmentManager.findFragmentById(R.id.detailsListFragment);
    
	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	private Location mCurrentLocation;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Note that location updates are off until the user turns them on
        mUpdatesRequested = false;

        // Open Shared Preferences
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get an editor
        mEditor = mPrefs.edit();

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
	}

    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {

        super.onStart();

        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mLocationClient.connect();

    }
    
	/*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
    @Override
    public void onStop() {

        // If the client is connected
        if (mLocationClient.isConnected()) {
        	//stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();

        super.onStop();
    }

    /*
     * Called when the system detects that this Activity is now visible.
     */
    @Override
    public void onResume() {
        super.onResume();

        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
            mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

        // Otherwise, turn off location updates until requested
        } else {
            mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }

    }
    
 // GooglePlayServicesClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    	/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
        }
    }
 
    // GooglePlayServicesClient.ConnectionCallbacks 
    @Override
    public void onConnected(Bundle arg0) {
         
        if(mLocationClient != null)
            mLocationClient.requestLocationUpdates(mLocationRequest,  this);
 
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
         
        if(mLocationClient != null){
            // get location
            mCurrentLocation = mLocationClient.getLastLocation();
                try{
                    // set TextView(s) 
                    mCurrentLocation.getLatitude();
                }catch(NullPointerException npe){
                     
                    Toast.makeText(this, "Failed to Connect", Toast.LENGTH_SHORT).show();
 
                    // switch on location service intent
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
        }
 
    }
    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected.", Toast.LENGTH_SHORT).show();
     
    }
    
    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // Log the result
                        //Log.d(LocationUtils.APPTAG, getString(R.string.resolved));

                    break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        //Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));
                    break;
                }

            // If any other request code was received
            default:
               // Report that this Activity received an unknown requestCode
               //Log.d(LocationUtils.APPTAG,
                //       getString(R.string.unknown_activity_request_code, requestCode));

               break;
        }
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
         //   Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
              //  errorFragment.setDialog(dialog);
              //  errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
        }
    }

	@Override
	public void onUpdate(String vehicleId) {
		// If Google Play Services is available
        if(servicesConnected()) {
		Location currentLocation = mLocationClient.getLastLocation();
	
			currentLocation.getLatitude();
			currentLocation.getLongitude();
	
			Position position = new Position();
			position.setLatitude(currentLocation.getLatitude());
			position.setLongitude(currentLocation.getLongitude());
			//position.setLatitude(33.80653802509606);//currentLocation.getLatitude());
			//position.setLongitude(-84.15252685546875);//currentLocation.getLongitude());
	
			new updatePositionAndVerifyStatus(this, "PCS-0505", position).execute();
        }
	}

	public void ackEmergencyCall() {
		new AckVehicleOnCallCallAsyncTask(this, "PCS-0505").execute();
	}

	private class updatePositionAndVerifyStatus extends AsyncTask<Void, Void, EmergencyCall>{
		Context context;
		private ProgressDialog pd;
		String vehicleId;
		Position position;

		public updatePositionAndVerifyStatus(Context context, String vehicleId, Position position) {
			this.context = context;
			this.vehicleId = vehicleId;
			this.position = position;
		}

		protected void onPreExecute(){ 
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setMessage("Updating...");
			pd.show();    
		}

		protected EmergencyCall doInBackground(Void... params) {
			EmergencyCall response = null;
			try {
				Emcallworkflowendpoint.Builder builder = new Emcallworkflowendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Emcallworkflowendpoint service =  builder.build();

				response = service.updatePositionAndVerifyStatus(vehicleId, position).execute();

			} catch (Exception e) {
				Log.d("Could not update position/verify status", e.getMessage(), e);
			}
			return response;
		}

		protected void onPostExecute(EmergencyCall emCall) {
			//Clear the progress dialog and the fields
			pd.dismiss();
			if(emCall != null) {
				if (detailsListFragment != null) {
		            detailsListFragment.updateDetail(emCall);
		        }
				//Ve�culo deve atender chamada -> Chama servi�o para acknowledgment
				ackEmergencyCall();
			}
			//Display success message to user
			Toast.makeText(getBaseContext(), "Update position/verify status succesfully", Toast.LENGTH_SHORT).show();
		}

	}

	private class AckVehicleOnCallCallAsyncTask extends AsyncTask<Void, Void, Void>{
		Context context;
		private ProgressDialog pd;
		String vehicleId;

		public AckVehicleOnCallCallAsyncTask(Context context, String vehicleId) {
			this.context = context;
			this.vehicleId = vehicleId;
		}

		protected void onPreExecute(){ 
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setMessage("Ack waiting call...");
			pd.show();    
		}

		protected Void doInBackground(Void... params) {
			try {
				Emcallworkflowendpoint.Builder builder = new Emcallworkflowendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Emcallworkflowendpoint service =  builder.build();
				service.ackVehicleOnCall(vehicleId).execute();
			} catch (Exception e) {
				Log.d("Could not ack waiting call", e.getMessage(), e);
			}
			return null;
		}

		protected void onPostExecute() {
			//Clear the progress dialog and the fields
			pd.dismiss();

			//Display success message to user
			Toast.makeText(getBaseContext(), "Ack waiting call succesfully", Toast.LENGTH_SHORT).show();
		}

	}
	
	/**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }
    
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
    }

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}
}