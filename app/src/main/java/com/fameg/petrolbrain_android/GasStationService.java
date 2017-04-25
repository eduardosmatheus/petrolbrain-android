package com.fameg.petrolbrain_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class GasStationService extends Service {

    public GasStationService() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        String PLACES_WEBSERVICE_API_KEY = "AIzaSyD81NQ74zxczdfejdiegET7wtaPQIUmogE";
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
