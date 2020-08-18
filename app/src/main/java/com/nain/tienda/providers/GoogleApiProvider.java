package com.nain.tienda.providers;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.nain.tienda.R;

import java.util.Date;

import retrofit2.Call;
import com.nain.tienda.retrofit.IGoogleApi;
import com.nain.tienda.retrofit.retrofitClient;

public class GoogleApiProvider {

    private Context context;

    public GoogleApiProvider(Context context) {
        this.context = context;
    }

    // 60*60*1000 => una hora
    public Call<String> getDirections(LatLng originLntLng, LatLng destinationLntLng) {
        String baseUrl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                    + "origin=" + originLntLng.latitude + "," + originLntLng.longitude + "&"
                    + "destination=" + destinationLntLng.latitude + "," + destinationLntLng.longitude + "&"
                    + "departure_time=" + (new Date().getTime() + (60*60*1000)) + "&"
                    + "traffic_model=best_guess&"
                    + "key=" + context.getResources().getString(R.string.google_maps_key);

        // pasamos la interfas
        return retrofitClient.getClient(baseUrl).create(IGoogleApi.class).getDirections(baseUrl + query);
    }
}
