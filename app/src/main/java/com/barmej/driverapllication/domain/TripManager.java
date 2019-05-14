package com.barmej.driverapllication.domain;


import androidx.annotation.NonNull;
import android.text.TextUtils;
import com.barmej.driverapllication.callback.CallBack;
import com.barmej.driverapllication.callback.StatusCallback;
import com.barmej.driverapllication.domain.entity.Driver;
import com.barmej.driverapllication.domain.entity.FullStatus;
import com.barmej.driverapllication.domain.entity.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TripManager {

    private static final String TRIP_REF_PATH = "trips";
    private static final String DRIVER_REF_PATH = "drivers";

    private static TripManager instance;
    private FirebaseDatabase database;


    private Driver driver;
    private StatusCallback statusListener;
    private ValueEventListener driverStatusListener;
    private Trip trip;


    private TripManager() {
        database = FirebaseDatabase.getInstance();

    }


    public static TripManager getInstance() {
        if (instance == null) {
            instance = new TripManager();
        }
        return instance;
    }


    public void getDriverProfileAndMakeAvailableIfOffline(final String driverId, final CallBack callBack) {
        database.getReference(DRIVER_REF_PATH).child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                driver = dataSnapshot.getValue(Driver.class);
                if (driver != null) {
                    if (driver.getStatus().equals(Driver.Status.OFFLINE.name())) {
                        makeDriverAvailableAndNotify(callBack);
                    } else {
                        callBack.onComplete(true);
                    }
                } else {
                    callBack.onComplete(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makeDriverAvailableAndNotify(final CallBack callBack) {
        driver.setStatus(Driver.Status.AVAILABLE.name());
        database.getReference(DRIVER_REF_PATH).child(driver.getId()).setValue(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                callBack.onComplete(task.isSuccessful());
            }
        });
    }

    public void startListeningForStatus(StatusCallback statusCallback) {
        this.statusListener = statusCallback;
        driverStatusListener = database.getReference(DRIVER_REF_PATH).child(driver.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                driver = dataSnapshot.getValue(Driver.class);
                if (driver != null) {
                    if (driver.getStatus().equals(Driver.Status.ON_TRIP.name()) && !TextUtils.isEmpty(driver.getAssignedTrip())) {
                        getTripAndNotifyStatus();
                    } else {
                        FullStatus fullStatus = new FullStatus();
                        fullStatus.setDriver(driver);
                        notifyListener(fullStatus);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getTripAndNotifyStatus() {
        database.getReference(TRIP_REF_PATH).child(driver.getAssignedTrip()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                trip = dataSnapshot.getValue(Trip.class);
                if (trip != null) {
                    FullStatus status = new FullStatus();
                    status.setDriver(driver);
                    status.setTrip(trip);
                    notifyListener(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void notifyListener(FullStatus fullStatus) {
        if (statusListener != null) {
            statusListener.onUpdate(fullStatus);
        }
    }

    public void updateCurrentLocation(double latitude, double longitude) {

        trip.setCurrentLat(latitude);
        trip.setCurrentLng(longitude);
        database.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);
    }

    public void updateTripToArrivedToPickUp() {
        trip.setStatus(Trip.Status.GOING_TO_DESTINATION.name());
        database.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);
        FullStatus fullStatus = new FullStatus();
        fullStatus.setDriver(driver);
        fullStatus.setTrip(trip);
        notifyListener(fullStatus);
    }

    public void updateTripToArrivedToDestination() {
        trip.setStatus(Trip.Status.ARRIVED.name());
        database.getReference(TRIP_REF_PATH).child(trip.getId()).setValue(trip);
        driver.setStatus(Driver.Status.AVAILABLE.name());
        driver.setAssignedTrip(null);
        trip = null;
        database.getReference(DRIVER_REF_PATH).child(driver.getId()).setValue(driver);
        FullStatus fullStatus = new FullStatus();
        fullStatus.setDriver(driver);
        notifyListener(fullStatus);
    }
    public void stopListeningToStatus() {
        if (driverStatusListener != null) {
            database.getReference().child(driver.getId()).removeEventListener(driverStatusListener);
        }
        statusListener = null;
    }
    public void goOffline() {
        driver.setStatus(Driver.Status.OFFLINE.name());
        driver.setAssignedTrip(null);
        database.getReference(DRIVER_REF_PATH).child(driver.getId()).setValue(driver);
        FirebaseAuth.getInstance().signOut();
    }
}
