package com.phunware.beaconmonitor.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelFormatException;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Duy Nguyen on 7/31/17.
 */

public class BleScanner
{
    public static final String TAG = BleScanner.class.getSimpleName();

    private BleScannerOptions scannerOptions;
    private boolean isScanning = false;

    private BeaconReadingContinuousDispatcher beaconReadingContinuousDispatcher;
    private BleScannerListener bleScannerListener;

    private BluetoothAdapter bluetoothAdapter;

    //need to run on another thread, if not, start/stop bluetooth adapter can block UI thread
    private BleScanRunnable bleScanRunnable;

    private Handler periodicScanHandler;
    private Handler continuousScanHandler;

    private final Handler runOnUiThreadHandler;
    private HashSet<ParcelUuid> uuids = new HashSet<>();
    private List<ScanFilter> scanFilters = new ArrayList<>();

    public BleScanner(@NonNull Context context, @NonNull BleScannerOptions scannerOptions, @NonNull final BleScannerListener listener) throws Exception
    {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if(bluetoothManager == null || bluetoothManager.getAdapter() == null)
        {
            throw new Exception("No bluetooth adapter.");
        }
        else
        {
            bluetoothAdapter = bluetoothManager.getAdapter();
            this.scannerOptions = scannerOptions;
            this.bleScannerListener = listener;
            periodicScanHandler = new Handler();
            continuousScanHandler=  new Handler();
            runOnUiThreadHandler = new Handler(context.getMainLooper());
        }
    }

    public ParcelUuid addUuidToMonitor(@NonNull String uuid) throws ParcelFormatException
    {
        ParcelUuid ret = null;
        try
        {
            Identifier identifier = Identifier.parse(uuid);
            ret = ParcelUuid.fromString(identifier.toString());
            uuids.add(ret);
        }
        catch(ParcelFormatException ex)
        {
            throw new ParcelFormatException("UUIDs not in valid format");
        }

        return ret;
    }

    public void removeUuidToMonitor(@NonNull String uuid)
    {
        uuids.remove(uuid);
    }

    public void start()
    {
        if(!isScanning)
        {
            Log.d(TAG, "start: ");
            if(bleScanRunnable != null)
            {
                bleScanRunnable.stop();
            }

            bleScanRunnable = new BleScanRunnable(bluetoothAdapter.getBluetoothLeScanner(), scannerOptions, uuids);
            switch(scannerOptions.getScanStrategy())
            {
                case SCAN_STRATEGY_CONTINUOUS:
                {
                    doContinuousNotify();
                    break;
                }
                default:
                case SCAN_STRATEGY_PERIODIC:
                {
                    doPeriodicScan();
                    break;
                }
            }

            isScanning = true;
        }
    }

    public void stop()
    {
        if(isScanning)
        {
            Log.d(TAG, "stop: ");
            bleScanRunnable.stop();
            switch(scannerOptions.getScanStrategy())
            {
                case SCAN_STRATEGY_CONTINUOUS:
                {
                    continuousScanHandler.removeCallbacks(continuousNotifyRunnable);
                    break;
                }
                default:
                case SCAN_STRATEGY_PERIODIC:
                {
                    periodicScanHandler.removeCallbacks(periodicScanRunnable);
                    break;
                }
            }
            isScanning = false;
        }
    }

    public boolean isScanning()
    {
        return isScanning;
    }

    private Runnable continuousNotifyRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            notifyListener(bleScanRunnable.getReadings());
            continuousScanHandler.postDelayed(continuousNotifyRunnable, scannerOptions.getContinuousDispatchInterval());
        }
    };

    private void doContinuousNotify()
    {
        //start the scan on another thread
        bleScanRunnable.run();

        //Notify the listener at every interval
        continuousScanHandler.postDelayed(continuousNotifyRunnable, scannerOptions.getContinuousDispatchInterval());
    }

    private Runnable periodicScanRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            bleScanRunnable.stop();
            notifyListener(bleScanRunnable.getReadings());

            //start the next scan cycle
            doPeriodicScan();
        }
    };

    private void doPeriodicScan()
    {
        //start the scan on another thread
        bleScanRunnable.run();

        //stop scanning and start new scan cycle right away
        periodicScanHandler.postDelayed(periodicScanRunnable, scannerOptions.getPeriodicScanLength());
    }

    private void runOnUiThread(Runnable runnable)
    {
        runOnUiThreadHandler.post(runnable);
    }

    private void notifyListener(final List<LiveBeaconReading> readings)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d(TAG, "notifyListener: dispatching " + readings.size() + " readings...");
                bleScannerListener.onBleScannerResult(readings);
                readings.clear();
            }
        });
    }
}
