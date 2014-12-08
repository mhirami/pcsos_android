package epusp.pcs.os.android;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import epusp.pcs.os.workflow.emcallworkflowendpoint.Emcallworkflowendpoint;
import epusp.pcs.os.workflow.emcallworkflowendpoint.model.Agent;
import epusp.pcs.os.workflow.emcallworkflowendpoint.model.AgentCollection;
import epusp.pcs.os.workflow.emcallworkflowendpoint.model.EmergencyCall;
import epusp.pcs.os.workflow.emcallworkflowendpoint.model.EmergencyCallLifecycleStatus;
import epusp.pcs.os.workflow.emcallworkflowendpoint.model.Monitor;
import epusp.pcs.os.workflow.emcallworkflowendpoint.model.Position;
import epusp.pcs.os.workflow.emcallworkflowendpoint.model.Victim;

public class MainActivity extends Activity implements LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {

	FragmentManager fragmentManager = getFragmentManager();
	//DetailsListFragment detailsListFragment = (DetailsListFragment) fragmentManager.findFragmentById(R.id.detailsListFragment);

	private static final String MAP_FRAGMENT_TAG = "MAP";
	private static final String DETAILSLIST_FRAGMENT_TAG = "DETAILSLIST";
	private static final String APP_TAG = "PCSOS-MainActivity";

	FrameLayout detailsFrameLayout;
	RelativeLayout mapFrameLayout;
	MapFragment mapFragment;
	TextView busyRibbonTv;
	
	Bitmap monitorPicture = null;

	DetailsListFragment detailsListFragment = new DetailsListFragment();

	Position myPosition = new Position();

	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	// Handle to SharedPreferences for this app
	SharedPreferences mPrefs;
	// Handle to a SharedPreferences editor
	SharedPreferences.Editor mEditor;
	/*
	 * Note if updates have been turned on. Starts out as "false"; is set to "true" in the
	 * method handleRequestSuccess of LocationUpdateReceiver.
	 */
	boolean mUpdatesRequested = true;

	Boolean isOnCall = false;

	AgentCollection agents;
	String vehicleTag = "TAG001";

	EmergencyCall currentEmCall = null;
	Monitor currentMonitor = null;
	Victim currentVictim = null;

	private enum Estado {
		livre, ocupado, atendimento;
	}

	Menu menu;

	//markMap is a hashmap populated with my markers positions.
	HashMap<String, LatLng> mapMarkers = new HashMap<String, LatLng>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		detailsFrameLayout = (FrameLayout)findViewById(R.id.detailsListContainer);
		mapFrameLayout = (RelativeLayout)findViewById(R.id.mapContainer);
		mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		busyRibbonTv = (TextView)findViewById(R.id.busyRibbonTv);

		//		LinearLayout.LayoutParams mapLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);  
		//		mapFrameLayout.setLayoutParams(mapLayoutParams); 
		//		mapFrameLayout.setVisibility(View.VISIBLE);

		//		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		//		fragmentTransaction.add(R.id.mapContainer, mapFragment, MAP_FRAGMENT_TAG);
		//		fragmentTransaction.commit();

		Bundle extras = getIntent().getExtras();
		List<Agent> agentsList = (List<Agent>) extras.getSerializable("Agents");
		agents = new AgentCollection();
		agents.setAgentCollection(agentsList);

		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();

		/*
		 * Set the update interval
		 */
		mLocationRequest.setInterval(30000);
		//mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

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
	/******************************************************************/
	/*Menu*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_actions, menu);
		this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_turnFree:
			turnFree();
			changeView(Estado.livre);
			return true;
		case R.id.action_turnBusy:
			turnBusy();
			changeView(Estado.ocupado);
			return true;
		case R.id.action_monitor:
			openMonitor();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void changeView(Estado estado) {
		ActionBar aBar = getActionBar();
		MenuItem turnFreeItem = menu.findItem(R.id.action_turnFree);
		MenuItem turnBusyItem = menu.findItem(R.id.action_turnBusy);
		MenuItem monitorItem = menu.findItem(R.id.action_monitor);
		LinearLayout.LayoutParams detailsLayoutParams;
		LinearLayout.LayoutParams mapLayoutParams;
		FragmentTransaction fragmentTransaction;

		switch(estado) {
		case livre:
			//Atualizando fragments 
			detailsFrameLayout.setVisibility(View.GONE);


			//			mapLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1);  
			//			mapFrameLayout.setLayoutParams(mapLayoutParams); 
			//			mapFrameLayout.setVisibility(View.VISIBLE);

			busyRibbonTv.setVisibility(View.GONE);

			fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.remove(detailsListFragment);
			fragmentTransaction.commit();

			mapMarkers.remove("Victim");
			updateMapAndCamera();
			
			aBar.setTitle(Html.fromHtml("<font color='#ffffff'>" + getResources().getString(R.string.app_name) + "</font>"));
			aBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background_color)));
			turnFreeItem.setVisible(false);
			turnBusyItem.setVisible(true);
			monitorItem.setVisible(false);
			break;
		case ocupado:
			//Atualizando fragments
			detailsFrameLayout.setVisibility(View.GONE);

			//			mapLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1);  
			//			mapFrameLayout.setLayoutParams(mapLayoutParams); 
			//			mapFrameLayout.setVisibility(View.VISIBLE);

			busyRibbonTv.setVisibility(View.VISIBLE);

			fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.remove(detailsListFragment);
			fragmentTransaction.commit();

			//			if (mapFragment != null) {
			//				googleMap = mapFragment.getMap();
			//				//googleMap.addMarker(new MarkerOptions().position(victimPosition).title("Vítima"));
			//			}

			aBar.setTitle(Html.fromHtml("<font color='#ffffff'>" + getResources().getString(R.string.app_name) + "</font>"));
			aBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background_color)));
			turnFreeItem.setVisible(true);
			turnBusyItem.setVisible(false);
			monitorItem.setVisible(false);
			break;
		case atendimento:
			//Atualizando fragments
			if(currentEmCall != null) {
				//mapLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0.3f);
				mapFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 2f));
				detailsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
				detailsFrameLayout.setVisibility(View.VISIBLE);
				//mapFrameLayout.setLayoutParams(mapLayoutParams); 
				//mapFrameLayout.setVisibility(View.VISIBLE);

				busyRibbonTv.setVisibility(View.GONE);

				fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.add(R.id.detailsListContainer, detailsListFragment, DETAILSLIST_FRAGMENT_TAG);
				fragmentTransaction.commit();

				LatLng victimPosition = new LatLng(currentEmCall.getLastVictimPosition().getLatitude(), currentEmCall.getLastVictimPosition().getLongitude());

				//Atualizando mapa
				mapMarkers.put("Victim", victimPosition);
				updateMapAndCamera();
			}

			//Atualizando action bar			
			if(currentVictim != null) {
				String name = currentVictim.getName();
				name += currentVictim.getSecondName() != null ? " " + currentVictim.getSecondName() : " ";
				name += currentVictim.getSurname() != null ? " " + currentVictim.getSurname() : " ";
				aBar.setTitle(Html.fromHtml("<font color='#ffffff'>" + name + "</font>"));
			}
			else
				aBar.setTitle(Html.fromHtml("<font color='#ffffff'>" + getResources().getString(R.string.app_name) + "</font>"));
			aBar.setBackgroundDrawable(new ColorDrawable(0xffff0000));
			turnFreeItem.setVisible(false);
			turnBusyItem.setVisible(false);
			monitorItem.setVisible(true);

			break;
		default:
			break;
		}
	}

	private void updateMap() {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		if (mapFragment != null) {
			GoogleMap map = mapFragment.getMap();
			map.clear();
			for (Entry<String, LatLng> entry : mapMarkers.entrySet()) {                                                           
				map.addMarker(new MarkerOptions().position(entry.getValue()));
				builder.include(entry.getValue());                                      
			}
		}
	}

	private void updateMapAndCamera() {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		LatLngBounds bounds;
		int padding = 100;
		if (mapFragment != null) {
			GoogleMap map = mapFragment.getMap();
			map.clear();
			for (Entry<String, LatLng> entry : mapMarkers.entrySet()) {                                                           
				map.addMarker(new MarkerOptions().position(entry.getValue()));
				builder.include(entry.getValue());                                      
			}
			bounds = builder.build();
			CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, padding);
			map.animateCamera(update);
		}
	}

	public void openMonitor() {
		// custom dialog
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.monitor_dialog);
		dialog.setTitle("Informações do Monitor");

		if(currentMonitor != null) {
			// set the custom dialog components - text, image and button
			ImageView imageView = (ImageView) dialog.findViewById(R.id.image);
			if(monitorPicture != null)
				imageView.setImageBitmap(monitorPicture);
			
			ListView listView = (ListView) dialog.findViewById(R.id.monitorDetailsList);
			
			ArrayList<Item> arrayList = new ArrayList<Item>();
			DetailsListAdapter adapter;
			//TextView text = (TextView) dialog.findViewById(R.id.text);
			String name = currentMonitor.getName();
			name += currentMonitor.getSecondName() != null ? " " + currentMonitor.getSecondName() : "";
			name += currentMonitor.getSurname() != null ? " " + currentMonitor.getSurname() : "";

			arrayList.add(new Item("Nome:", name));

			adapter = new DetailsListAdapter(this, arrayList);

			listView.setAdapter(adapter);
			//text.setText("Nome: " + name);
			Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
			// if button is clicked, close the custom dialog
			dialogButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.show();
		}
	}

	//	private void addFragments(EmergencyCall emCall) {
	//		LinearLayout.LayoutParams detailsLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0.7f);  
	//		detailsFrameLayout.setLayoutParams(detailsLayoutParams); 
	//		detailsFrameLayout.setVisibility(View.VISIBLE);
	//
	//		LinearLayout.LayoutParams mapLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0.3f);  
	//		mapFrameLayout.setLayoutParams(mapLayoutParams); 
	//		mapFrameLayout.setVisibility(View.VISIBLE);
	//
	//		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	//		fragmentTransaction.add(R.id.detailsListContainer, detailsListFragment, DETAILSLIST_FRAGMENT_TAG);
	//		fragmentTransaction.commit();
	//		
	//		LatLng victimPosition = new LatLng(emCall.getLastVictimPosition().getLatitude(), emCall.getLastVictimPosition().getLongitude());
	//
	//		if (mapFragment != null) {
	//			googleMap = mapFragment.getMap();
	//			googleMap.addMarker(new MarkerOptions().position(victimPosition).title("Vítima"));
	//		}
	//	}
	//	
	//	private void removeFragments() {
	//		LinearLayout.LayoutParams detailsLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0);  
	//		detailsFrameLayout.setLayoutParams(detailsLayoutParams); 
	//		detailsFrameLayout.setVisibility(View.GONE);
	//
	//		LinearLayout.LayoutParams mapLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1);  
	//		mapFrameLayout.setLayoutParams(mapLayoutParams); 
	//		mapFrameLayout.setVisibility(View.VISIBLE);
	//
	//		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	//		fragmentTransaction.add(R.id.detailsListContainer, detailsListFragment, DETAILSLIST_FRAGMENT_TAG);
	//		fragmentTransaction.commit();
	//		
	//	}

	/******************************************************************/

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
			stopPeriodicUpdates();
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
			Location currentLocation = mLocationClient.getLastLocation();
			try{
				LatLng myPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
				mapMarkers.put("myPosition", myPosition);
				updateMapAndCamera();
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

			// If any other request code wasem received
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

	/**
	 * In response to a request to start updates, send a request
	 * to Location Services
	 */
	private void startPeriodicUpdates() {
		mUpdatesRequested = true;
		if (servicesConnected())
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	private void stopPeriodicUpdates() {
		mUpdatesRequested = false;
		if (servicesConnected())
			mLocationClient.removeLocationUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		Position position = new Position();
		position.setLatitude(location.getLatitude());
		position.setLongitude(location.getLongitude());

		LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());

		mapMarkers.put("myPosition", myPosition);
		updateMap();

		if(isOnCall) {
			new UpdatePositionAndVerifyCallStatusAsyncTask(this, vehicleTag, position, currentEmCall.getVictimEmail()).execute();
		}
		else {
			new UpdatePositionAndVerifyStatusAsyncTask(this, vehicleTag, position).execute();
		}
	}

	//-------------------------------------------------------------------------------------------------------------

	//	@Override
	//	public void onUpdate(String vehicleId) {
	//		// If Google Play Services is available
	//		if(servicesConnected()) {
	//			Location currentLocation = mLocationClient.getLastLocation();
	//
	//			currentLocation.getLatitude();
	//			currentLocation.getLongitude();
	//
	//			myPosition.setLatitude(currentLocation.getLatitude());
	//			myPosition.setLongitude(currentLocation.getLongitude());
	//			//position.setLatitude(33.80653802509606);//currentLocation.getLatitude());
	//			//position.setLongitude(-84.15252685546875);//currentLocation.getLongitude());
	//
	//			//FIXME
	//			new UpdatePositionAndVerifyStatusAsyncTask(this, vehicleTag, myPosition).execute();
	//		}
	//	}

	//------------------------------------------------------------------------------------------------------------------------

	public void ackEmergencyCall() {
		new AckVehicleOnCallAsyncTask(this, vehicleTag).execute();
	}

	public void ackFinishedCall() {
		new AckVehicleFinishedCallAsyncTask(this, vehicleTag).execute();
	}

	public void getVictimInfo() {
		new GetVictimAsyncTask(this, currentEmCall.getVictimEmail()).execute();
	}
	
	public void getVictimPicture() {
		new LoadVictimPicture().execute(currentVictim.getPictureURL());
	}

	public void getMonitorInfo() {
		new GetMonitorAsyncTask(this, currentEmCall.getMonitor()).execute();
	}
	
	public void getMonitorPicture() {
		new LoadMonitorPicture().execute(currentMonitor.getPictureURL());
	}

	public void finishCall() {
		new AckVehicleFinishedCallAsyncTask(this, vehicleTag).execute();
	}

	public void turnFree() {
		new addFreeVehicleAsyncTask(this, vehicleTag, agents).execute();
	}

	public void turnBusy() {
		new vehicleLeavingAsyncTask(this, vehicleTag).execute();
	}
	
	//------------------------------------------------------------------------------------------------------------------------

		private class LoadVictimPicture extends AsyncTask<String, String, Bitmap> {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				Toast.makeText(MainActivity.this, "Loading Image ....", Toast.LENGTH_SHORT).show();
			}
			protected Bitmap doInBackground(String... args) {
				Bitmap bitmap = null;
				try {
					bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return bitmap;
			}
			protected void onPostExecute(Bitmap image) {
				if(image != null){
					detailsListFragment.updatePicture(image);
				} else {
					Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
				}
			}
		}
		
	//------------------------------------------------------------------------------------------------------------------------

	private class LoadMonitorPicture extends AsyncTask<String, String, Bitmap> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Toast.makeText(MainActivity.this, "Loading Image ....", Toast.LENGTH_SHORT).show();
		}
		protected Bitmap doInBackground(String... args) {
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return bitmap;
		}
		protected void onPostExecute(Bitmap image) {
			if(image != null){
				monitorPicture = image;
			} else {
				Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
			}
		}
	}

	//------------------------------------------------------------------------------------------------------------------------

	private class addFreeVehicleAsyncTask extends AsyncTask<Void, Void, Void> {
		Context context;
		String vehicleId;
		AgentCollection agents;

		public addFreeVehicleAsyncTask(Context context, String vehicleId, AgentCollection agents) {
			this.context = context;
			this.vehicleId = vehicleId;
			this.agents = agents;
		}

		protected void onPreExecute(){ 
			super.onPreExecute();
			Toast.makeText(getBaseContext(), "Adding free vehicle...", Toast.LENGTH_SHORT).show();
		}

		protected Void doInBackground(Void... params) {
			try {
				Emcallworkflowendpoint.Builder builder = new Emcallworkflowendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Emcallworkflowendpoint service =  builder.build();
				service.addFreeVehicle(vehicleId, agents).execute();
			} catch (Exception e) {
				Log.d("Could not add free vehicle", e.getMessage(), e);
			}
			return null;
		}
	}

	//------------------------------------------------------------------------------------------------------------------------

	private class vehicleLeavingAsyncTask extends AsyncTask<Void, Void, Void> {
		Context context;
		String vehicleId;

		public vehicleLeavingAsyncTask(Context context, String vehicleId) {
			this.context = context;
			this.vehicleId = vehicleId;
		}

		protected void onPreExecute(){ 
			super.onPreExecute(); 
			Toast.makeText(getBaseContext(), "Removing vehicle from resources list...", Toast.LENGTH_SHORT).show();
		}

		protected Void doInBackground(Void... params) {
			try {
				Emcallworkflowendpoint.Builder builder = new Emcallworkflowendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Emcallworkflowendpoint service =  builder.build();
				service.vehicleLeaving(vehicleId).execute();
			} catch (Exception e) {
				Log.d("Could not turn vehicle to busy state", e.getMessage(), e);
			}
			return null;
		}
	}

	//------------------------------------------------------------------------------------------------------------------------

	private class UpdatePositionAndVerifyStatusAsyncTask extends AsyncTask<Void, Void, EmergencyCall> {
		Context context;
		String vehicleId;
		Position position;

		public UpdatePositionAndVerifyStatusAsyncTask(Context context, String vehicleId, Position position) {
			this.context = context;
			this.vehicleId = vehicleId;
			this.position = position;
		}

		protected void onPreExecute(){ 
			super.onPreExecute();
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

		protected void onPostExecute(EmergencyCall emergencyCall) {
			//Clear the progress dialog and the fields
			if(emergencyCall != null) {
				currentEmCall = emergencyCall;
				isOnCall = true;
				changeView(Estado.atendimento);

				//FIXME
				//changeView(Estado.atendimento);
				/*ActionBar ab = getActionBar();
				ab.setTitle(Html.fromHtml("<font color='#ffffff'>Nome da Vítima</font>"));
				ab.setBackgroundDrawable(new ColorDrawable(0xffff0000));*/

				//Veículo deve atender chamada -> Chama serviço para acknowledgment
				ackEmergencyCall();

				//Pegar informações da vítima
				getVictimInfo();

				//Pegar informações do monitor
				getMonitorInfo();

				Toast.makeText(context, "A chamada foi iniciada!", Toast.LENGTH_SHORT).show();

			}
		}

	}

	//------------------------------------------------------------------------------------------------------------------------

	private class AckVehicleOnCallAsyncTask extends AsyncTask<Void, Void, Void> {
		Context context;
		String vehicleId;

		public AckVehicleOnCallAsyncTask(Context context, String vehicleId) {
			this.context = context;
			this.vehicleId = vehicleId;
		}

		protected void onPreExecute(){ 
			super.onPreExecute();
			Toast.makeText(context, "Ack waiting call...", Toast.LENGTH_SHORT).show();
			Log.d(APP_TAG, "Ack waiting call..."); 
		}

		protected Void doInBackground(Void... params) {
			try {
				Emcallworkflowendpoint.Builder builder = new Emcallworkflowendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Emcallworkflowendpoint service =  builder.build();
				service.ackVehicleOnCall(vehicleId).execute();
			} catch (Exception e) {
				Log.d(APP_TAG, e.getMessage(), e);
			}
			return null;
		}

	}
	//--------------------------------------------------------------------------------------------------------------------------

	private class UpdatePositionAndVerifyCallStatusAsyncTask extends AsyncTask<Void, Void, EmergencyCallLifecycleStatus>{
		Context context;
		String vehicleId;
		Position position;
		String victimEmail;

		public UpdatePositionAndVerifyCallStatusAsyncTask(Context context, String vehicleId, Position position, String victimEmail) {
			this.context = context;
			this.vehicleId = vehicleId;
			this.position = position;
			this.victimEmail = victimEmail;
		}

		protected void onPreExecute(){ 
			super.onPreExecute();
			Log.i(APP_TAG, "Updating and verifying call status...");
		}

		protected EmergencyCallLifecycleStatus doInBackground(Void... params) {
			EmergencyCallLifecycleStatus response = null;
			try {
				Emcallworkflowendpoint.Builder builder = new Emcallworkflowendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Emcallworkflowendpoint service =  builder.build();

				response = service.updatePositionAndVerifyCallStatus(vehicleId, victimEmail, position).execute();

			} catch (Exception e) {
				Log.i(APP_TAG, "Could not update position/verify status");
				Log.d(APP_TAG, e.getMessage(), e);
			}
			return response;
		}

		protected void onPostExecute(EmergencyCallLifecycleStatus emergencyCallStatus) {
			String emCallStatus = emergencyCallStatus.getStatus();
			if(emCallStatus != null) {
				//A chamada foi finalizada
				if(emCallStatus.equals("Finished")) {
					isOnCall = false;
					changeView(Estado.livre);
					finishCall();
					currentEmCall = null;
					currentVictim = null;
					currentMonitor = null;
					monitorPicture = null;
					
					Log.i(APP_TAG, "Call finished");
					Toast.makeText(context, "A chamada foi finalizada!", Toast.LENGTH_SHORT).show();
				}
			}
		}

	}

	//--------------------------------------------------------------------------------------------------------------------------

	private class GetVictimAsyncTask extends AsyncTask<Void, Void, Victim> {
		Context context;
		String victimEmail;

		public GetVictimAsyncTask(Context context, String victimEmail) {
			this.context = context;
			this.victimEmail = victimEmail;
		}

		protected void onPreExecute(){ 
			super.onPreExecute();
			Log.i(APP_TAG, "Getting victim info...");
		}

		protected Victim doInBackground(Void... params) {
			Victim response = null;
			try {
				Emcallworkflowendpoint.Builder builder = new Emcallworkflowendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Emcallworkflowendpoint service =  builder.build();

				response = service.getVictim(victimEmail).execute();

			} catch (Exception e) {
				Log.d("Could not get victim information", e.getMessage(), e);
			}
			return response;
		}

		protected void onPostExecute(Victim victim) {
			ActionBar aBar = getActionBar();
			if(victim != null) {
				//Conseguiu pegar as informações da vítima
				currentVictim = victim;
				getVictimPicture();
				//Atualizando fragment com infos da vítima
				if (detailsListFragment != null) {
					String name = currentVictim.getName();
					name += currentVictim.getSecondName() != null ? " " + currentVictim.getSecondName() : "";
					name += currentVictim.getSurname() != null ? " " + currentVictim.getSurname() : "";
					aBar.setTitle(Html.fromHtml("<font color='#ffffff'>" + name + "</font>"));
					ArrayList<Item> list = new ArrayList<Item>();
					list.add(new Item("Nome:", name));
					detailsListFragment.updateDetails(list);
				}
			}
			else {
				Toast.makeText(getBaseContext(), "Erro ao pegar as informações da vítima!", Toast.LENGTH_SHORT).show();
			}
		}

	}

	//--------------------------------------------------------------------------------------------------------------------------

	private class GetMonitorAsyncTask extends AsyncTask<Void, Void, Monitor>{
		Context context;
		String monitorId;

		public GetMonitorAsyncTask(Context context, String monitorId) {
			this.context = context;
			this.monitorId = monitorId;
		}

		protected void onPreExecute(){ 
			super.onPreExecute();
			Log.i(APP_TAG, "Getting monitor info...");
		}

		protected Monitor doInBackground(Void... params) {
			Monitor response = null;
			try {
				Emcallworkflowendpoint.Builder builder = new Emcallworkflowendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Emcallworkflowendpoint service =  builder.build();

				response = service.getMonitor(monitorId).execute();

			} catch (Exception e) {
				Log.d("Could not get monitor information", e.getMessage(), e);
			}
			return response;
		}

		protected void onPostExecute(Monitor monitor) {
			if(monitor != null) {
				//Conseguiu pegar as informações do monitor
				currentMonitor = monitor;
				getMonitorPicture();
			}
			else {
				Toast.makeText(context, "Erro ao pegar as informações do monitor!", Toast.LENGTH_SHORT).show();
			}
		}

	}

	//--------------------------------------------------------------------------------------------------------------------------

	private class AckVehicleFinishedCallAsyncTask extends AsyncTask<Void, Void, Void>{
		Context context;
		String vehicleId;

		public AckVehicleFinishedCallAsyncTask(Context context, String vehicleId) {
			this.context = context;
			this.vehicleId = vehicleId;
		}

		protected void onPreExecute(){ 
			super.onPreExecute();
			Toast.makeText(context, "Ack finished call...", Toast.LENGTH_SHORT).show(); 
		}

		protected Void doInBackground(Void... params) {
			try {
				Emcallworkflowendpoint.Builder builder = new Emcallworkflowendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Emcallworkflowendpoint service =  builder.build();
				service.ackVehicleFinishedCall(vehicleId).execute();
			} catch (Exception e) {
				Log.d("Could not ack finished call", e.getMessage(), e);
			}
			return null;
		}
	}

}