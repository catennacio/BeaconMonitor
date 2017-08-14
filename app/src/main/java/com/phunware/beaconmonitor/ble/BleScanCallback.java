package com.phunware.beaconmonitor.ble;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Duy Nguyen on 8/4/17.
 */

public class BleScanCallback extends ScanCallback
{
    public static final String TAG = BleScanCallback.class.getSimpleName();

    private HashSet<ParcelUuid> monitoredUuids;
    private volatile List<LiveBeaconReading> readings;

    public BleScanCallback(HashSet<ParcelUuid> monitoredUuids)
    {
        super();
        this.monitoredUuids = monitoredUuids;
        readings = new ArrayList<>();
    }

    public List<LiveBeaconReading> getReadings()
    {
        return readings;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result)
    {
//        Log.d(TAG, "onScanResult: " + result);
        if (result != null && result.getScanRecord() != null)
        {
            LiveBeaconReading liveBeaconReading = new LiveBeaconReading(result);
            readings.add(liveBeaconReading);
            /*if (liveBeaconReading.IBeacon != null && monitoredUuids
                .contains(ParcelUuid.fromString(liveBeaconReading.IBeacon.UUID.toString())))
            {

            }*/
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results)
    {
        Log.d(TAG, "onBatchScanResults: result=" + results);
        super.onBatchScanResults(results);
    }

    @Override
    public void onScanFailed(int errorCode)
    {
        Log.e(TAG, "onScanFailed: err=" + errorCode);
        super.onScanFailed(errorCode);
    }
}
