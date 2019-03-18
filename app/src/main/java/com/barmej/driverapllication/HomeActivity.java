package com.barmej.driverapllication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import com.barmej.driverapllication.domain.StatusCallback;
import com.barmej.driverapllication.domain.TripManager;
import com.barmej.driverapllication.domain.model.Driver;
import com.barmej.driverapllication.domain.model.FullStatus;
import com.barmej.driverapllication.domain.model.Trip;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.model.LatLng;

public class HomeActivity extends AppCompatActivity {
    private MapsContainerFragment mapsContainerFragment;
    private PermissionFailListenr permissionFailListenr = getPermissionFailListener();
    private LocationCallback locationCallback;
    private FusedLocationProviderClient locationClient;
    private DriverActionsDeltagates driverActionsDeltagates = getDriverActionDelegates();
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
        mapsContainerFragment.setPermissionFailListenr(permissionFailListenr);
        statusInfoFragment = (StatusInfoFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_status_info);
        statusInfoFragment.setDriverActionDelegates(driverActionsDeltagates);

    }

    private PermissionFailListenr getPermissionFailListener() {

        return new PermissionFailListenr() {
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

    private DriverActionsDeltagates getDriverActionDelegates() {
        return new DriverActionsDeltagates() {
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


    @SuppressLint("MissingPermission")
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
            locationClient.requestLocationUpdates(new LocationRequest(), locationCallback, null);
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
