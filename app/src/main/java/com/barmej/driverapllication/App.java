package com.barmej.driverapllication;

import android.app.Application;
import com.barmej.driverapllication.domain.TripManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TripManager.init();
    }
}
