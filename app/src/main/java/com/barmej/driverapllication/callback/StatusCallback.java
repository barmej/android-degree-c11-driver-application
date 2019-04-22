package com.barmej.driverapllication.callback;


import com.barmej.driverapllication.domain.entity.FullStatus;

public interface StatusCallback {
    void onUpdate(FullStatus status);
}
