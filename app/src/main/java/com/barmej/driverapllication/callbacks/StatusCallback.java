package com.barmej.driverapllication.callbacks;


import com.barmej.driverapllication.domain.entities.FullStatus;

public interface StatusCallback {
    void onUpdate(FullStatus status);
}
