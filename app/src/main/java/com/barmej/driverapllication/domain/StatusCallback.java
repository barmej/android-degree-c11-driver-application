package com.barmej.driverapllication.domain;


import com.barmej.driverapllication.domain.model.FullStatus;

public interface StatusCallback {
    void onUpdate(FullStatus status);
}
