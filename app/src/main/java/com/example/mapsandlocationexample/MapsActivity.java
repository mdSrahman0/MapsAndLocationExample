package com.example.mapsandlocationexample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionManager.IPermissionManager {

    private GoogleMap mMap;
    private PermissionManager permissionManager;
    private LocationRequest locationRequest;
    private Location mLastLocation;
    private LocationManager locationManager;
    Marker mCurrLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        permissionManager = new PermissionManager(this);
        permissionManager.checkPermission();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));

    }

    // create the menu that will hold the list of map types (terrains)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle the map type selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.none:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.satellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.terrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) throws IOException {
        switch (view.getId()) {
            case R.id.btnGoToOffice:
                double lat = 33.908916;
                double lng = -84.478981;
                LatLng latLng = new LatLng(lat, lng);
                // marker is the red pin
                mMap.addMarker(new MarkerOptions().position(latLng).title("MOBILE APPS COMPANY"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                // zoom in closer automatically
                mMap.setMinZoomPreference(15);
                break;
            case R.id.btnFindCoordsGivenAddress:
                // after they clicked, hide their keyboard
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                EditText etAddress = findViewById(R.id.etAddress);
                String inputAddress = etAddress.getText().toString();

                Geocoder gc = new Geocoder(this);
                List<Address> list = gc.getFromLocationName(inputAddress, 1);
                Address add = list.get(0);
                String locality = add.getLocality();
                Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

                double userLat = add.getLatitude();
                double userLng = add.getLongitude();
                Location findLocation = new Location(LocationManager.GPS_PROVIDER);
                findLocation.setLatitude(userLat);
                findLocation.setLongitude(userLng);
                moveToLocation(findLocation);
                break;

                // given coordinates, find the address
            case R.id.btnFindAddressGivenCoords:
                // after they clicked, hide their keyboard
                inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                EditText etCoords = findViewById(R.id.etCoords);
                String inputCoords = etCoords.getText().toString();

                String latString = inputCoords.substring(0, inputCoords.indexOf(","));
                String lngString = inputCoords.substring(inputCoords.indexOf(",")+1);
                double inputLat = Double.parseDouble(latString);
                double inputLng = Double.parseDouble(lngString);

                Location lo = new Location(LocationManager.GPS_PROVIDER);
                lo.setLatitude(inputLat);
                lo.setLongitude(inputLng);
                moveToLocation(lo);
                break;
        }
    }

    @SuppressLint("MissingPermission")
    public void locationChangeFusedSetup() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setMaxWaitTime(5000);
        locationRequest.setFastestInterval(4000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = new SettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        getFusedLocationProviderClient(this).requestLocationUpdates(
                locationRequest,
                new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        onLocationChanged(locationResult.getLocations().get(0));
                    }
                },
                Looper.myLooper());
    }

        public void onLocationChanged(Location location) {
            mLastLocation = location;

            if (mCurrLocationMarker != null) {
                mCurrLocationMarker.remove();
            }

            // Place current location marker
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mCurrLocationMarker = mMap.addMarker(markerOptions);

            // move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));
            moveToLocation(location);
            ////////////////////////
        }


    public void moveToLocation(Location location) {
        LatLng latLongOfLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLongOfLocation).title("Location of current call"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLongOfLocation, 17));
        // zoom in closer automatically
        //mMap.setMinZoomPreference(1);
    }

    @SuppressWarnings("MissingPermission")
    public void getLastKnownLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    moveToLocation(location);
                } else {
                    Log.d("TAG", "location is null");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.checkResult(requestCode, permissions, grantResults);
    }

    public void onPermissionResult(boolean isGranted) {
        if(isGranted) {
            getLastKnownLocation();
        }
    }
}
