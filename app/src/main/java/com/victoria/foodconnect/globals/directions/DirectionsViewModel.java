package com.victoria.foodconnect.globals.directions;

import static com.victoria.foodconnect.globals.GlobalRepository.directionsApi;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.app.Application;
import android.text.Layout;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.maps.model.LatLng;
import com.victoria.foodconnect.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DirectionsViewModel extends AndroidViewModel {

    public DirectionsViewModel(@NonNull Application application) {
        super(application);

    }

    @NonNull
    @Override
    public <T extends Application> T getApplication() {
        return super.getApplication();
    }

    private MutableLiveData<Optional<ResponseBody>> getDirectionsMutable(LatLng origin, LatLng destination, List<String> waypoints) {
        MutableLiveData<Optional<ResponseBody>> mutableLiveData = new MutableLiveData<>();

        directionsApi.getDirections(getParams(origin, destination, waypoints)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                mutableLiveData.setValue(response.body() != null ? Optional.of(response.body()) : Optional.empty());
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                t.printStackTrace();
            }
        });

        return mutableLiveData;
    }

    public static String getDirectionsParamValue(LatLng latLng) {
        //origin
        //destination
        return String.valueOf(latLng.latitude).concat(",").concat(String.valueOf(latLng.longitude));
    }

    public static String getAvoidParamValue(boolean tolls, boolean highways, boolean ferries) {
        //avoid
        StringBuilder builder = new StringBuilder();
        List<String> avoid = new ArrayList<>();

        if (tolls) {
            avoid.add("tolls");
        }

        if (highways) {
            avoid.add("highways");
        }

        if (ferries) {
            avoid.add("ferries");
        }


        for (int i = 0; i < avoid.size(); i++) {
            builder.append(i);
            if (i == avoid.size() - 1) {
                builder.append(".");
            } else {
                builder.append("|");
            }
        }

        System.out.println("avoid is "+avoid);

        return builder.toString();
    }

    public static String getMode() {
        //mode
        return "driving";
    }

    public static String getUnits() {
        //units
        return "metric";
    }

    public static String getWayPoints(List<String> waypoints) {

        //waypoints=optimize:true
        StringBuilder builder = new StringBuilder();
        builder.append("optimize:true|");

        for (int i = 0; i < waypoints.size(); i++) {
            builder.append(i);
            if (i != waypoints.size() - 1) {
                builder.append("|");
            }
        }
        return builder.toString();
    }

    private HashMap<String, String> getParams(LatLng origin, LatLng destination, List<String> waypoints) {
        HashMap<String, String> params = new HashMap<>();

        params.put("origin", getDirectionsParamValue(origin));
        params.put("destination", getDirectionsParamValue(destination));
        params.put("key", getApplication().getString(R.string.google_maps_key));
        params.put("mode", getMode());
        params.put("units", getUnits());
        params.put("avoid", getAvoidParamValue(true, false, false));
        if (waypoints != null) {
            params.put("waypoints", getWayPoints(waypoints));
        }

        try {
            System.out.println(getObjectMapper().writeValueAsString(params));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return params;
    }


    //export
    public LiveData<Optional<ResponseBody>> getDirectionsLive(LatLng origin, LatLng destination, List<String> waypoints) {
        return getDirectionsMutable(origin, destination, waypoints);
    }

}
