package com.nain.tienda.activities.Comprador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.SphericalUtil;
import com.nain.tienda.R;
import com.nain.tienda.providers.GoogleApiProvider;
import com.nain.tienda.providers.GoogleProvider;
import com.nain.tienda.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoogleMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    android.app.AlertDialog mDialog;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient mFusedLocation;

    private PlacesClient mPlaces;
    private AutocompleteSupportFragment mAutoComplete;
    private GoogleMap.OnCameraIdleListener mCameraListener;

    private LocationRequest mLocationRequest;
    private LatLng mCurrentLatlng;
    private final static int LOCATION_REQUEST_CODE = 1;
    private boolean mIsFirstTime = true;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private String mOrigin;
    private LatLng mDestinoLatLong;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolygonOptions;

    GoogleProvider googleProvider;
    Polyline polyline;
    String id_google;
    Toolbar mToolbar;

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
        setContentView(R.layout.activity_google_maps);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("UBICACIÓN DE TIENDAS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        googleProvider = new GoogleProvider();
        mGoogleApiProvider = new GoogleApiProvider(GoogleMapsActivity.this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        mPlaces = Places.createClient(this);
        instanceAutocomplete();

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

        boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.estilos_mapa));
        if(!success) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraIdleListener(mCameraListener);

        mLocationRequest = new LocationRequest();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
        onCameraMove();

        getMarketTiendas();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mDestinoLatLong = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);

                if(polyline != null){
                    polyline.remove();
                }

                drawRoute();
                return false;
            }
        });

    }

    private void onCameraMove(){
        mCameraListener = new GoogleMap.OnCameraIdleListener() {

            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder = new Geocoder(GoogleMapsActivity.this);
                    mDestinoLatLong = mMap.getCameraPosition().target;
                    List<Address> addressList = geocoder.getFromLocation(mDestinoLatLong.latitude, mDestinoLatLong.longitude,1);
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

    private void drawRoute(){
        mGoogleApiProvider.getDirections(mCurrentLatlng, mDestinoLatLong).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String poins = polylines.getString("points");

                    mPolylineList = DecodePoints.decodePoly(poins);

                    mPolygonOptions = new PolylineOptions();
                    mPolygonOptions.color(Color.BLUE);
                    mPolygonOptions.width(13f);
                    mPolygonOptions.startCap(new SquareCap());
                    mPolygonOptions.jointType(JointType.ROUND);
                    mPolygonOptions.addAll(mPolylineList);
                    polyline = mMap.addPolyline(mPolygonOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);

                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");

                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");

                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Duración : " + durationText + " , Distancia : " + distanceText, Snackbar.LENGTH_LONG)
                            .setBackgroundTint(parentLayout.getResources().getColor(R.color.colorPrimaryDark))
                            .setActionTextColor(parentLayout.getResources().getColor(R.color.colorWhite))
                            .show();

                } catch (Exception e) {
                    Log.d("Error", "Error encontrado " + e.getMessage() );
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void getMarketTiendas(){
        googleProvider.getGoogleTiendas().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (documentSnapshot.exists()) {
                            String latitud = documentSnapshot.getString("latitud");
                            String longitud = documentSnapshot.getString("longitud");
                            LatLng MarketLatLng = new LatLng(Double.valueOf(latitud), Double.valueOf(longitud));
                            mMap.addMarker(
                                    new MarkerOptions()
                                    .position(MarketLatLng)
                                    .title("Tienda")
                            );
                        }
                    }
                }
            }
        });
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
                                ActivityCompat.requestPermissions(GoogleMapsActivity.this, new String[] {
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                }, LOCATION_REQUEST_CODE );
                            }
                        })
                        .create()
                        .show();
            }else{
                ActivityCompat.requestPermissions(GoogleMapsActivity.this, new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, LOCATION_REQUEST_CODE );
            }
        }
    }

    private void limitSearch(){
        LatLng northSide = SphericalUtil.computeOffset(mCurrentLatlng, 5000,0);
        LatLng sourthSide = SphericalUtil.computeOffset(mCurrentLatlng, 5000,180);
        mAutoComplete.setCountry("PE");
        mAutoComplete.setLocationBias(RectangularBounds.newInstance(sourthSide, northSide));
    }

    private void instanceAutocomplete(){
        mAutoComplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placesAucompleteDestino);
        mAutoComplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutoComplete.setHint("Buscar Local");
        mAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin = place.getName();
                mDestinoLatLong = place.getLatLng();
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(mDestinoLatLong,
                        17);
                mMap.moveCamera(update);

                Log.d("PLACES", "Name "+ mOrigin);
                Log.d("PLACES", "Lat "+ mDestinoLatLong.latitude);
                Log.d("PLACES", "Lon "+ mDestinoLatLong.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }
}
