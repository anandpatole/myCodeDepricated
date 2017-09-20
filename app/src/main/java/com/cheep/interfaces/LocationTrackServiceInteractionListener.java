package com.cheep.interfaces;

import android.location.Location;

import com.google.android.gms.common.api.Status;

/**
 * Created by bhavesh on 12/10/16.
 */
public interface LocationTrackServiceInteractionListener {
    void onLocationNotAvailable();

    void onLocationFetched(Location mLocation);

    void onLocationSettingsDialogNeedToBeShow(Status locationRequest);

    void gpsEnabled();
}
