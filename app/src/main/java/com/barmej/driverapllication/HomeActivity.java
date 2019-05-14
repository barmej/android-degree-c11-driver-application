package com.barmej.driverapllication;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.barmej.driverapllication.fragment.MapsContainerFragment;
import com.barmej.driverapllication.fragment.StatusInfoFragment;
import com.barmej.driverapllication.callback.DriverActionsDelegates;
import com.barmej.driverapllication.callback.PermissionFailListener;
import com.barmej.driverapllication.callback.StatusCallback;
import com.barmej.driverapllication.domain.TripManager;
import com.barmej.driverapllication.domain.entity.Driver;
import com.barmej.driverapllication.domain.entity.FullStatus;
import com.barmej.driverapllication.domain.entity.Trip;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.model.LatLng;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private MapsContainerFragment mapsContainerFragment;
    private PermissionFailListener permissionFailListener = getPermissionFailListener();
    private LocationCallback locationCallback;
    private FusedLocationProviderClient locationClient;
    private DriverActionsDelegates driverActionsDelegates = getDriverActionDelegates();
    private StatusInfoFragment statusInfoFragment;
    private StatusCallback statusListener = getStatusListener();

    public static Intent getStartIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mapsContainerFragment = (MapsContainerFragment) getSupportFragmentManager().findFragmentById(R.id.map_container_fragment);
        mapsContainerFragment.setPermissionFailListener(permissionFailListener);
        statusInfoFragment = (StatusInfoFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_status_info);
        statusInfoFragment.setDriverActionDelegates(driverActionsDelegates);

    }

    private PermissionFailListener getPermissionFailListener() {

        return new PermissionFailListener() {
            @Override
            public void onPermissionFail() {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setMessage(R.string.location_permission_needed);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            }
        };
    }

    private DriverActionsDelegates getDriverActionDelegates() {
        return new DriverActionsDelegates() {
            @Override
            public void arrivedToPickup() {
                TripManager.getInstance().updateTripToArrivedToPickUp();
            }

            @Override
            public void arrivedToDestination() {
                TripManager.getInstance().updateTripToArrivedToDestination();
                stopLocationUpdates();

            }

            @Override
            public void goOffline() {
                TripManager.getInstance().goOffline();
                startActivity(LoginActivity.getStartIntent(HomeActivity.this));
                finish();
            }
        };
    }

    private StatusCallback getStatusListener() {
        return new StatusCallback() {
            @Override
            public void onUpdate(FullStatus status) {
                String driverStatus = status.getDriver().getStatus();
                if (driverStatus.equals(Driver.Status.AVAILABLE.name())) {
                    showAvailableScreen(status);
                } else if (driverStatus.equals(Driver.Status.ON_TRIP.name())) {
                    showOnTripView(status);
                    trackAndSendLocationUpdates();
                }
            }
        };
    }


    private void trackAndSendLocationUpdates() {
        if (locationCallback == null) {
            locationClient = LocationServices.getFusedLocationProviderClient(this);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location lastLocation = locationResult.getLastLocation();
                    TripManager.getInstance().updateCurrentLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                }
            };
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
                locationClient.requestLocationUpdates(new LocationRequest(), locationCallback, null);
            }
        }
    }

    private void showAvailableScreen(FullStatus status) {
        mapsContainerFragment.reset();
        statusInfoFragment.updateWithStatus(status);

    }

    private void showOnTripView(FullStatus status) {
        Trip trip = status.getTrip();
        mapsContainerFragment.setDestinationMarker(new LatLng(trip.getDestinationLat(), trip.getDestinationLng()));
        mapsContainerFragment.setPickUpMarker(new LatLng(trip.getPickUpLat(), trip.getPickUpLng()));
        statusInfoFragment.updateWithStatus(status);
    }


    @Override
    protected void onResume() {
        super.onResume();
        TripManager.getInstance().startListeningForStatus(statusListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TripManager.getInstance().stopListeningToStatus();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (locationCallback != null && locationClient != null) {
            locationClient.removeLocationUpdates(locationCallback);
            locationCallback=null;
        }
    }
}
