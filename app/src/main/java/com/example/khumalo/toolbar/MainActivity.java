package com.example.khumalo.toolbar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.khumalo.toolbar.Utils.PermissionUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    GoogleApiClient mGoogleApiClient;
    private static final String Tag = "Tag";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final  int PLACE_PICKER_REQUEST = 2;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 3;
    private boolean mPermissionDenied = false;
    TextView diplayTimeToArrive;
    ViewGroup cover;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildGoogleClient();
        Toolbar topToolBar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        diplayTimeToArrive = (TextView)findViewById(R.id.toolbar_title);
        topToolBar.setLogo(R.drawable.ic_toobar_icon);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        cover = (ViewGroup)findViewById(R.id.destination_fragment);
        FloatingActionButton fab =  (FloatingActionButton)findViewById(R.id.find_me_a_ride);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getBaseContext(),"Searching for your ride",Toast.LENGTH_LONG).show();
               // buildPlacePickerAutoCompleteDialog();
                cover.setVisibility(View.VISIBLE);
                PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                        getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
                autocompleteFragment.setHint("Your Destination");
                autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        // TODO: Get info about the selected place.
                        Log.i("Tag", "Place: " + place.getName());
                        cover.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Status status) {
                        // TODO: Handle the error.
                        Log.i("Tag", "An error occurred: " + status);
                    }
                });
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       int id =  item.getItemId();
        if(id == R.id.action_edit_list_name){
            Toast.makeText(this,"Sending Message",Toast.LENGTH_LONG).show();
        }
        return true;
    }


    ////////////
    protected synchronized void buildGoogleClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(Tag, "Connection has failed");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(Tag, "The client has been connected");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(Tag, "The connection has been suspended");
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(Tag, "The Location Access has been Granted");
            buildPlacePickerAutoCompleteDialog();

        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }
    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    //The Rusult returned when the place is picked
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                String toastMsg = String.format("Searching for your ride to " + place.getName());
                diplayTimeToArrive.setText("10 mins away" );
                Toast.makeText(this,toastMsg,Toast.LENGTH_LONG).show();

            }
        }
    }

 /// Building a Place Picker dialog with a map
    private void buildPlacePickerMapDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);

        }else{
            Log.d(Tag, "The Location Access has been Granted");
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }

        }

    }

    ///Building Place AutoComplete without a Dialog
    private void buildPlacePickerAutoCompleteDialog(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);

        }else{
            Log.d(Tag, "The Location Access has been Granted");
            try {
                Intent intent =  new PlaceAutocomplete
                                .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                              .build(this);
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            } catch (GooglePlayServicesRepairableException e) {
                // TODO: Handle the error.
            } catch (GooglePlayServicesNotAvailableException e) {
                // TODO: Handle the error.
            }

        }
    }

}
