package com.nain.tienda.activities.Vendedor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.SphericalUtil;
import com.nain.tienda.R;
import com.nain.tienda.models.Google;
import com.nain.tienda.providers.AuthProvider;
import com.nain.tienda.providers.GoogleProvider;
import com.nain.tienda.providers.UsersProvider;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class RegisterGoogleActivity extends AppCompatActivity implements OnMapReadyCallback {

    android.app.AlertDialog mDialog;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private PlacesClient mPlaces;
    private AutocompleteSupportFragment mAutoComplete;
    private GoogleMap.OnCameraIdleListener mCameraListener;

    private LocationRequest mLocationRequest;
    private LatLng mCurrentLatlng;
    private FusedLocationProviderClient mFusedLocation;
    private final static int LOCATION_REQUEST_CODE = 1;
    private boolean mIsFirstTime = true;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private String mOrigin;
    private LatLng mOriginLatLong;

    private Button mButtonSave;
    private AuthProvider mAuthProvider;
    private UsersProvider mUsersProvider;
    private GoogleProvider mGoogleMapsProvider;
    Google mGoogleMaps;
    String id_google;
    Toolbar mToolbar;
    String tienda_id;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location: locationResult.getLocations()) {
                if(getApplicationContext() != null) {
                    mCurrentLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                .zoom(16f)
                                .build()
                    ));

                    if(mIsFirstTime) {
                        mIsFirstTime = false;
                        limitSearch();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_google);

        mButtonSave = findViewById(R.id.btnSave);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mGoogleMapsProvider = new GoogleProvider();
        mGoogleMaps = new Google();

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key)); // el api key de google
        }

        mPlaces = Places.createClient(this);
        instanceAutocomplete();
        onCameraMove();

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLocation();
            }
        });

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("UBICACIÓN DE LOCAL");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void saveLocation(){
        if(mOriginLatLong != null) {
            mDialog.show();
            mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()) {
                        if(documentSnapshot.contains("tienda_id")){
                            tienda_id = documentSnapshot.getString("tienda_id");
                            mGoogleMaps.setTienda_id(tienda_id);
                            mGoogleMaps.setLatitud(String.valueOf(mOriginLatLong.latitude));
                            mGoogleMaps.setLongitud(String.valueOf(mOriginLatLong.longitude));
                            mGoogleMapsProvider.getGoogleByTiendaId(tienda_id).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if(queryDocumentSnapshots.isEmpty()){
                                        save(mGoogleMaps);
                                    }else{
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            id_google = documentSnapshot.getId();
                                        }
                                        if(id_google.length() > 3){
                                            mGoogleMaps.setId(id_google);
                                            update(mGoogleMaps);
                                        }else{
                                            mDialog.dismiss();
                                            Toast.makeText(RegisterGoogleActivity.this, "Ocurrio un Error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        }else{
                            mDialog.dismiss();
                        }
                    }else{
                        mDialog.dismiss();
                    }
                }
            });
        }else {
            Toast.makeText(this, "Seleccione su Ubicación", Toast.LENGTH_LONG).show();
        }
    }

    private void save(Google googleMaps){
        mGoogleMapsProvider.create(googleMaps).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mDialog.dismiss();
                    Toast.makeText(RegisterGoogleActivity.this, "Ubicación Guardada", Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    mDialog.dismiss();
                    Toast.makeText(RegisterGoogleActivity.this, "Ocurrio un Error!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void update(Google googleMaps){
        mGoogleMapsProvider.update(googleMaps).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(RegisterGoogleActivity.this, "Ubicación Actualizada", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        mDialog.dismiss();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraIdleListener(mCameraListener);

        mLocationRequest = new LocationRequest();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    private void startLocation(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if(gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }else {
                    showAlertDialogGPS();
                }
            }else{
                checkLocationPermission();
            }
        }else{
            if(gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }else{
                showAlertDialogGPS();
            }
        }
    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void showAlertDialogGPS(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicación para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    private void checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporcionar los permisos para continuar")
                        .setMessage("Esta Aplicación requiere de los permisos de ubicación para continuar")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(RegisterGoogleActivity.this, new String[] {
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                }, LOCATION_REQUEST_CODE );
                            }
                        })
                        .create()
                        .show();
            }else{
                ActivityCompat.requestPermissions(RegisterGoogleActivity.this, new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, LOCATION_REQUEST_CODE );
            }
        }
    }

    private void instanceAutocomplete(){
        mAutoComplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placesAucomplete);
        mAutoComplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutoComplete.setHint("Buscar Local");
        mAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin = place.getName();
                mOriginLatLong = place.getLatLng();
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(mOriginLatLong,
                        16);
                mMap.moveCamera(update);

                Log.d("PLACES", "Name "+ mOrigin);
                Log.d("PLACES", "Lat "+ mOriginLatLong.latitude);
                Log.d("PLACES", "Lon "+ mOriginLatLong.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    private void onCameraMove(){
        mCameraListener = new GoogleMap.OnCameraIdleListener() {

            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder = new Geocoder(RegisterGoogleActivity.this);
                    mOriginLatLong = mMap.getCameraPosition().target;
                    List<Address> addressList = geocoder.getFromLocation(mOriginLatLong.latitude, mOriginLatLong.longitude,1);
                    String city = addressList.get(0).getLocality();
                    String country = addressList.get(0).getCountryName();
                    String address = addressList.get(0).getAddressLine(0);
                    mOrigin = address + " " + city;
                    mAutoComplete.setText(address + " " + city);
                } catch (Exception e){
                    Log.d("Error : ",  "Mensaje de Error => "+ e.getLocalizedMessage());
                }
            }
        };
    }

    private void limitSearch(){
        LatLng northSide = SphericalUtil.computeOffset(mCurrentLatlng, 5000,0);
        LatLng sourthSide = SphericalUtil.computeOffset(mCurrentLatlng, 5000,180);
        mAutoComplete.setCountry("PE");
        mAutoComplete.setLocationBias(RectangularBounds.newInstance(sourthSide, northSide));
    }
}
