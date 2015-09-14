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

package com.gkxim.android.thanhniennews.location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.SectionActivity;
import com.gkxim.android.thanhniennews.SplashActivity;
import com.gkxim.android.utils.GKIMLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.loopj.android.http.RequestHandle;

/**
 * This the app's main Activity. It provides buttons for requesting the various
 * features of the app, displays the current location, the current address, and
 * the status of the location client and updating services.
 * 
 * {@link #getLocation} gets the current location using the Location Services
 * getLastLocation() function. {@link #getAddress} calls geocoding to get a
 * street address for the current location. {@link #startUpdates} sends a
 * request to Location Services to send periodic location updates to the
 * Activity. {@link #stopUpdates} cancels previous periodic update requests.
 * 
 * The update interval is hard-coded to be 5 seconds.
 */
public class LocationHelper implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private static String TAG = LocationHelper.class.getSimpleName();
	// A request to connect to Location Services
	private LocationRequest mLocationRequest;

	// Stores the current instantiation of the location client in this object
	private LocationClient mLocationClient;

	// Handles to UI widgets

	// Handle to SharedPreferences for this app
	SharedPreferences mPrefs;

	// Handle to a SharedPreferences editor
	SharedPreferences.Editor mEditor;

	/*
	 * Note if updates have been turned on. Starts out as "false"; is set to
	 * "true" in the method handleRequestSuccess of LocationUpdateReceiver.
	 */
	boolean mUpdatesRequested = true;

	private Context mContext;

	private SectionActivity sectionActivity;

	public void setSectionActivity(SectionActivity sectionActivity) {
		this.sectionActivity = sectionActivity;
	}

	public LocationHelper(Context context) {
		mContext = context.getApplicationContext();
	}

	/*
	 * Initialize the Activity
	 */
	public void onCreate() {
		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		mLocationRequest
				.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

		// Set the interval ceiling to one minute
		mLocationRequest
				.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
		// }
		// Note that location updates are off until the user turns them on
		mUpdatesRequested = true;

		// Open Shared Preferences
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		// Get an editor
		mEditor = mPrefs.edit();

		/*
		 * Create a new location client, using the enclosing class to handle
		 * callbacks.
		 */
		mLocationClient = new LocationClient(mContext, this, this);

	}

	/*
	 * Called when the Activity is no longer visible at all. Stop updates and
	 * disconnect.
	 */
	public void onStop() {
		if (mLocationClient != null) {
			// If the client is connected
			if (mLocationClient.isConnected()) {
				if (mUpdatesRequested) {
					stopPeriodicUpdates();
				}
			}
			// After disconnect() is called, the client is considered "dead".
			mLocationClient.disconnect();
		}
		if (mRequestHandle != null) {
			mRequestHandle.cancel(true);
		}
	}

	/*
	 * Called when the Activity is going into the background. Parts of the UI
	 * may be visible, but the Activity is inactive.
	 */
	public void onPause() {
		if (mEditor != null) {
			// Save the current setting for updates
			mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED,
					mUpdatesRequested);
			mEditor.commit();
		}

	}

	/*
	 * Called when the Activity is restarted, even before it becomes visible.
	 */
	public void onStart() {

		/*
		 * Connect the client. Don't re-start any requests here; instead, wait
		 * for onResume()
		 */
		mLocationClient.connect();

	}

	/*
	 * Called when the system detects that this Activity is now visible.
	 */
	public void onResume() {

		// If the app already has a setting for getting location updates, get it
		// if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
		// mUpdatesRequested = mPrefs.getBoolean(
		// LocationUtils.KEY_UPDATES_REQUESTED, false);
		//
		// // Otherwise, turn off location updates until requested
		// } else {
		// mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
		// mEditor.commit();
		// }

	}

	/*
	 * Handle results returned to this Activity by other Activities started with
	 * startActivityForResult(). In particular, the method onConnectionFailed()
	 * in LocationUpdateRemover and LocationUpdateRequester may call
	 * startResolutionForResult() to start an Activity that handles Google Play
	 * services problems. The result of this call returns here, to
	 * onActivityResult.
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		// Choose what to do based on the request code
		switch (requestCode) {

		// If the request code matches the code sent in onConnectionFailed
		case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:

			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK:

				// Log the result
				Log.d(LocationUtils.APPTAG,
						mContext.getString(R.string.resolved));

				// Display the result
				// mConnectionState.setText(R.string.connected);
				// mConnectionStatus.setText(R.string.resolved);
				break;

			// If any other result was returned by Google Play services
			default:
				// Log the result
				Log.d(LocationUtils.APPTAG,
						mContext.getString(R.string.no_resolution));

				// Display the result
				// mConnectionState.setText(R.string.disconnected);
				// mConnectionStatus.setText(R.string.no_resolution);

				break;
			}

			// If any other request code was received
		default:
			// Report that this Activity received an unknown requestCode
			Log.d(LocationUtils.APPTAG, mContext.getString(
					R.string.unknown_activity_request_code, requestCode));

			break;
		}
	}

	/**
	 * Verify that Google Play services is available before making a request.
	 * 
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected(Activity activity) {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(mContext);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d(LocationUtils.APPTAG,
					mContext.getString(R.string.play_services_available));

			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			if (activity != null) {
				// Display an error dialog
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
						resultCode, activity, 0);
				if (dialog != null) {
					ErrorDialogFragment errorFragment = new ErrorDialogFragment();
					errorFragment.setDialog(dialog);
					errorFragment.show(((FragmentActivity) activity)
							.getSupportFragmentManager(), LocationUtils.APPTAG);
				}
			}
			return false;
		}
	}

	/**
	 * Invoked by the "Get Location" button.
	 * 
	 * Calls getLastLocation() to get the current location
	 * 
	 * @param v
	 *            The view object associated with this method, in this case a
	 *            Button.
	 */
	public Location getLocation(View v) {
		Activity activity = null;
		if (v != null) {
			activity = (Activity) v.getContext();
		}
		// If Google Play Services is available
		if (servicesConnected(activity) && mLocationClient != null
				&& mLocationClient.isConnected()) {

			// Get the current location
			Location currentLocation = mLocationClient.getLastLocation();
			if (currentLocation != null) {
				float[] results = new float[2];
				Location.distanceBetween(currentLocation.getLatitude(),
						currentLocation.getLongitude(), 10.79813750,
						106.68855290, results);
				// if (results != null && results.length > 0) {
				// if (results[0] < 50) {
				// pushNotification(activity, null);
				// }
				// }
			} else {
				Toast.makeText(mContext, "detect no location",
						Toast.LENGTH_SHORT).show();
			}
			// Display the current location in the UI
			// mLatLng.setText(LocationUtils.getLatLng(this, currentLocation));
			showDebugToast("get location: "
					+ LocationUtils.getLatLng(mContext, currentLocation));
			return currentLocation;
		}
		return null;
	}

	private void pushNotification(Activity activity, Bundle bundle) {
		// Prepare intent which is triggered if the
		// notification is selected
		if (activity != null) {
			Intent intent = new Intent(activity, activity.getClass());
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pIntent = PendingIntent.getActivity(activity, 0,
					intent, 0);

			// Build notification
			// Actions are just fake
			Notification noti = new NotificationCompat.Builder(activity)
					.setContentTitle("Burd coffe")
					.setContentText("buy coffe and get baby!")
					.setSmallIcon(R.drawable.ic_launcher)
					.setPriority(5)
					.setAutoCancel(true)
					.setDefaults(
							Notification.DEFAULT_SOUND
									| Notification.DEFAULT_VIBRATE
									| Notification.DEFAULT_LIGHTS).setNumber(1)
					.setContentIntent(pIntent).build();
			// .addAction(R.drawable.facebook_android, "Call", pIntent)
			// .addAction(R.drawable.facebook_android, "More", pIntent)
			// .addAction(R.drawable.facebook_android, "And more", pIntent)
			NotificationManager notificationManager = (NotificationManager) activity
					.getSystemService(Context.NOTIFICATION_SERVICE);
			// Hide the notification after its selected
			noti.flags |= Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(0, noti);
		}
	}

	/**
	 * Invoked by the "Start Updates" button Sends a request to start location
	 * updates
	 * 
	 * @param v
	 *            The view object associated with this method, in this case a
	 *            Button.
	 */
	public void startUpdates(View v) {
		mUpdatesRequested = true;
		Activity activity = null;
		if (v != null) {
			activity = (Activity) v.getContext();
		}
		if (servicesConnected(activity)) {
			startPeriodicUpdates();
		}
	}

	public void showToast(String toast) {
		if (mContext != null) {
			Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
		}
	}

	public void showToastLong(String toast) {
		if (mContext != null) {
			Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
		}
	}

	public void showDebugToast(String toast) {
		if (GKIMLog.DEBUG_TOAST_ON) {
			Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Invoked by the "Stop Updates" button Sends a request to remove location
	 * updates request them.
	 * 
	 * @param v
	 *            The view object associated with this method, in this case a
	 *            Button.
	 */
	public void stopUpdates(View v) {
		mUpdatesRequested = true;
		Activity activity = null;
		if (v != null) {
			activity = (Activity) v.getContext();
		}
		if (servicesConnected(activity)) {
			stopPeriodicUpdates();
		}
	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {
		showDebugToast(mContext.getResources().getString(R.string.connected));
		if (mUpdatesRequested) {
			startPeriodicUpdates();
		}
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		showDebugToast(mContext.getResources().getString(R.string.disconnected));
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		showDebugToast("onConnectionFailed");
		Log.d(TAG, connectionResult.toString());
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		// if (connectionResult.hasResolution()) {
		// try {
		//
		// // Start an Activity that tries to resolve the error
		// connectionResult.startResolutionForResult(mContext,
		// LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
		//
		// /*
		// * Thrown if Google Play services canceled the original
		// * PendingIntent
		// */
		//
		// } catch (IntentSender.SendIntentException e) {
		//
		// // Log the error
		// e.printStackTrace();
		// }
		// } else {
		//
		// // If no resolution is available, display a dialog to the user with
		// // the error.
		// showErrorDialog(connectionResult.getErrorCode());
		// }
	}

	/**
	 * Report location updates to the UI.
	 * 
	 * @param location
	 *            The updated location.
	 */
	@Override
	public void onLocationChanged(Location location) {

		// // Report to the UI that the location was updated
		// mConnectionStatus.setText(R.string.location_updated);
		//
		// // In the UI, set the latitude and longitude to the value received
		// mLatLng.setText(LocationUtils.getLatLng(this, location));
		mLocation = location;
		SplashActivity.mLocation = location;
		if (sectionActivity != null) {
			sectionActivity.gpsSessionUpdate();
		}
		String latLng = LocationUtils.getLatLng(mContext, location);
		GKIMLog.l(2, TAG + "==> onLocationChanged:" + latLng);
		showDebugToast(latLng);
	}

	/**
	 * In response to a request to start updates, send a request to Location
	 * Services
	 */
	private void startPeriodicUpdates() {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		showDebugToast(mContext.getResources().getString(
				R.string.location_requested));
	}

	/**
	 * In response to a request to stop updates, send a request to Location
	 * Services
	 */
	public void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
		showDebugToast(mContext.getResources().getString(
				R.string.location_updates_stopped));
	}

	/**
	 * An AsyncTask that calls getFromLocation() in the background. The class
	 * uses the following generic types: Location - A
	 * {@link android.location.Location} object containing the current location,
	 * passed as the input parameter to doInBackground() Void - indicates that
	 * progress units are not used by this subclass String - An address passed
	 * to onPostExecute()
	 */
	protected class GetAddressTask extends AsyncTask<Location, Void, Address> {

		// Store the context passed to the AsyncTask when the system
		// instantiates it.
		Context localContext;

		// Constructor called by the system to instantiate the task
		public GetAddressTask(Context context) {

			// Required by the semantics of AsyncTask
			super();

			// Set a Context for the background task
			localContext = context;
		}

		/**
		 * Get a geocoding service instance, pass latitude and longitude to it,
		 * format the returned address, and return the address to the UI thread.
		 */
		@Override
		protected Address doInBackground(Location... params) {
			/*
			 * Get a new geocoding service instance, set for localized
			 * addresses. This example uses android.location.Geocoder, but other
			 * geocoders that conform to address standards can also be used.
			 */
			Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

			// Get the current location from the input parameter list
			Location location = params[0];

			// Create a list to contain the result address
			List<Address> addresses = null;

			// Try to get an address for the current location. Catch IO or
			// network problems.
			try {

				/*
				 * Call the synchronous getFromLocation() method with the
				 * latitude and longitude of the current location. Return at
				 * most 1 address.
				 */
				addresses = geocoder.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1);

				// Catch network or other I/O problems.
			} catch (IOException exception1) {

				// Log an error and return an error message
				Log.e(LocationUtils.APPTAG, mContext
						.getString(R.string.IO_Exception_getFromLocation));

				// print the stack trace
				exception1.printStackTrace();

				// Return an error message
				// return (mContext
				// .getString(R.string.IO_Exception_getFromLocation));
				return null;

				// Catch incorrect latitude or longitude values
			} catch (IllegalArgumentException exception2) {

				// Construct a message containing the invalid arguments
				String errorString = mContext.getString(
						R.string.illegal_argument_exception,
						location.getLatitude(), location.getLongitude());
				// Log the error and print the stack trace
				Log.e(LocationUtils.APPTAG, errorString);
				exception2.printStackTrace();

				//
				// return errorString;
				return null;
			} catch (Exception exception) {
				return null;
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {

				// Get the first address
				Address address = addresses.get(0);

				// Format the first line of address
				String addressText = mContext.getString(
						R.string.address_output_string,

						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "",

						// Locality is usually a city
						address.getLocality(),

						// The country of the address
						address.getCountryName());

				// Return the text
				// return addressText;
				return address;

				// If there aren't any addresses, post a message
			} else {
				// return mContext.getString(R.string.no_address_found);
				return null;
			}
		}

		/**
		 * A method that's called once doInBackground() completes. Set the text
		 * of the UI element that displays the address. This method runs on the
		 * UI thread.
		 */
		@Override
		protected void onPostExecute(Address address) {

			// if (mainActivity != null) {
			// mainActivity.onPostAddress(address);
			// }

		}
	}

	private String mAddress;

	protected class AutoTrackGetAddressTask extends
			AsyncTask<Location, Void, Address> {
		Context localContext;

		public AutoTrackGetAddressTask(Context context) {
			super();
			localContext = context;
		}

		@Override
		protected Address doInBackground(Location... params) {
			Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());
			Location location = params[0];
			List<Address> addresses = null;
			try {
				addresses = geocoder.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1);

			} catch (IOException exception1) {

				Log.e(LocationUtils.APPTAG, mContext
						.getString(R.string.IO_Exception_getFromLocation));

				exception1.printStackTrace();
				// mAddress = (mContext
				// .getString(R.string.IO_Exception_getFromLocation));
				mAddress = "";
				return null;

			} catch (IllegalArgumentException exception2) {

				String errorString = mContext.getString(
						R.string.illegal_argument_exception,
						location.getLatitude(), location.getLongitude());
				Log.e(LocationUtils.APPTAG, errorString);
				exception2.printStackTrace();
				mAddress = "";

				return null;
			} catch (Exception exception) {
				return null;
			}
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				mAddress = mContext.getString(
						R.string.address_output_string,
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "", address.getLocality(),
						address.getCountryName());
				return address;
			} else {
				mAddress = mContext.getString(R.string.no_address_found);
				return null;
			}
		}

		@Override
		protected void onPostExecute(Address address) {

		}
	}

	/**
	 * Show a dialog returned by Google Play services for the connection error
	 * code
	 * 
	 * @param errorCode
	 *            An error code returned from onConnectionFailed
	 */
	private void showErrorDialog(int errorCode) {

		// // Get the error dialog from Google Play services
		// Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
		// mContext, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
		//
		// // If Google Play services can provide an error dialog
		// if (errorDialog != null) {
		//
		// // Create a new DialogFragment in which to show the error dialog
		// ErrorDialogFragment errorFragment = new ErrorDialogFragment();
		//
		// // Set the dialog in the DialogFragment
		// errorFragment.setDialog(errorDialog);
		//
		// // Show the error dialog in the DialogFragment
		// errorFragment.show(
		// ((FragmentActivity) mContext).getSupportFragmentManager(),
		// LocationUtils.APPTAG);
		// }
	}

	/**
	 * Define a DialogFragment to display the error dialog generated in
	 * showErrorDialog.
	 */
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 * 
		 * @param dialog
		 *            An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	public boolean isProvideEnabled() {
		if (mContext != null) {
			LocationManager service = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);
			boolean enabled = service
					.isProviderEnabled(LocationManager.GPS_PROVIDER)
					|| service
							.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			// check if enabled and if not send user to the GSP settings
			// Better solution would be to display a dialog and suggesting to
			// go to the settings
			// if (!enabled && SettingsFragment.getGPSProvider(mContext)) {
			if (!enabled) {
				Intent intent = new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			}
			return enabled;
		}
		return false;
	}

	public boolean checkGPSProviderEnabled() {
		if (mContext != null) {
			LocationManager service = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);
			boolean enabled = service
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			// check if enabled and if not send user to the GSP settings
			// Better solution would be to display a dialog and suggesting to
			// go to the settings
			// if (!enabled && SettingsFragment.getGPSProvider(mContext)) {
			if (!enabled) {
				Intent intent = new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			}
			return enabled;
		}
		return false;
	}

	public boolean checkAndStartLocationClientConnect() {
		if (mLocationClient != null && mLocationClient.isConnected()) {
			showDebugToast("location service was connected !!!");
			return true;
		} else {
			// stop location client if has exist
			showDebugToast("restart location service !!!");
			onPause();
			onStop();
			// restart
			onCreate();
			onStart();
			onResume();
			return false;
		}
	}

	private RequestHandle mRequestHandle;
	private Location mLocation;

}
