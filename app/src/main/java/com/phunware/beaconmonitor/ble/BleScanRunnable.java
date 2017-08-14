package com.phunware.beaconmonitor.ble;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Duy Nguyen on 8/4/17.
 */

public class BleScanRunnable implements Runnable
{
    public static final String TAG = BleScanRunnable.class.getSimpleName();

    private BluetoothLeScanner scanner;
    private BleScannerOptions scannerOptions;
    private BleScanCallback bleScanCallback;
    private HashSet<ParcelUuid> uuids;
    private List<ScanFilter> scanFilters = new ArrayList<>();

    public BleScanRunnable(@NonNull BluetoothLeScanner scanner, @NonNull BleScannerOptions scannerOptions, HashSet<ParcelUuid> uuids)
    {
        this.scanner = scanner;
        this.scannerOptions = scannerOptions;
        this.uuids = uuids;
    }

    private void startScan()
    {
        bleScanCallback = new BleScanCallback(uuids);
        for(ParcelUuid uuid : uuids)
        {
            // Empty data
            byte[] manData = new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

            // Data Mask
            byte[] mask = new byte[]{0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0};

            // Copy UUID into data array and remove all "-"
            System.arraycopy(Utils.hexStringToByteArray(uuid.toString().replace("-","")), 0, manData, 2, 16);

            scanFilters.add(new ScanFilter.Builder().setManufacturerData(76, manData, mask).build());
            Log.d(TAG, "startScan: " + uuid.toString());
        }

        if(scanFilters.size() > 0)
        {
            scanner.startScan(scanFilters, scannerOptions.getScanSettings(), bleScanCallback);
        }
        else
        {
            scanner.startScan(null, scannerOptions.getScanSettings(), bleScanCallback);
        }
    }

    private void stopScan()
    {
        scanner.stopScan(bleScanCallback);
    }

    List<LiveBeaconReading> getReadings()
    {
        return bleScanCallback.getReadings();
    }

    @Override
    public void run()
    {
        startScan();
    }

    void stop()
    {
        stopScan();
    }
}
