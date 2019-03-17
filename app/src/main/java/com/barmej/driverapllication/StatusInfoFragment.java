package com.barmej.driverapllication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.barmej.driverapllication.domain.model.Driver;
import com.barmej.driverapllication.domain.model.FullStatus;
import com.barmej.driverapllication.domain.model.Trip;

public class StatusInfoFragment extends Fragment {
    private TextView statusTv;
    private Button arrivedToPickUpBt;
    private Button arrivedToDestinationBt;
    private Button logOutBt;
    private DriverActionsDeltagates driverActionsDelegates;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        statusTv = view.findViewById(R.id.text_view_status);
        arrivedToPickUpBt = view.findViewById(R.id.button_arrived_pickup);
        arrivedToDestinationBt = view.findViewById(R.id.button_arrived_destination);
        logOutBt = view.findViewById(R.id.button_logout);
        logOutBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverActionsDelegates.goOffline();
            }
        });
        arrivedToDestinationBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverActionsDelegates.arrivedToDestination();
            }
        });
        arrivedToPickUpBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverActionsDelegates.arrivedToPickup();
            }
        });
    }


    void setDriverActionDelegates(DriverActionsDeltagates delegates) {
        this.driverActionsDelegates = delegates;
    }

    void updateWithStatus(FullStatus fullStatus) {
        String driverStatus = fullStatus.getDriver().getStatus();
        if (driverStatus.equals(Driver.Status.AVAILABLE.name())) {
            statusTv.setText(R.string.available);
            hideAllButtons();
            logOutBt.setVisibility(View.VISIBLE);
        } else if (driverStatus.equals(Driver.Status.ON_TRIP.name())) {
            String tripStatus = fullStatus.getTrip().getStatus();
            if (tripStatus.equals(Trip.Status.GOING_TO_PICKUP.name())) {
                statusTv.setText(R.string.going_pickup);
                hideAllButtons();
                arrivedToPickUpBt.setVisibility(View.VISIBLE);
            } else if (tripStatus.equals(Trip.Status.GOING_TO_DESTINATION.name())) {
                statusTv.setText(R.string.going_destination);
                hideAllButtons();
                arrivedToDestinationBt.setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideAllButtons() {
        logOutBt.setVisibility(View.GONE);
        arrivedToDestinationBt.setVisibility(View.GONE);
        arrivedToPickUpBt.setVisibility(View.GONE);
    }
}
