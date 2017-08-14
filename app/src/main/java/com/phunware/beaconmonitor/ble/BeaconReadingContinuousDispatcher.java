package com.phunware.beaconmonitor.ble;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duy Nguyen on 8/1/17.
 */

/*
* This class is responsible for accumulating (holding) real-time iBeacon readings and dispatching them every
* DISPATCH_INTERVAL interval. It is meant to only be accessed from within this package.
*
* When the time comes, it will fire the callback onBleScannerResult to the BleScannerListener listener to deliver
* a list of accumulated readings.
*
* It needs to run on its own thread because the listener can perform subsequent actions which could potentially block
* this class's next execution (i.e. accepting new incoming packets), if this class and the listener run on
* the same (main) thread. By making this class thread-independent, after it fires the onBleScannerResult callback,
* the call is returned immediately so the instance of this class is free to do its own tasks.
*
* To stopScan this thread, send a stopScan signal using interrupt(), and not stop() - deprecated because of the danger of
* holding locks and shared variables. The thread will decide when to stop when it receives the interrupt() signal.
* */
class BeaconReadingContinuousDispatcher extends Thread
{
    private static final String TAG = BeaconReadingContinuousDispatcher.class.getSimpleName();

    private long dispatchInterval;

    private BleScannerListener bleScannerListener;

    //This variable needs to be stored in the main memory so that it is immediately accessible with the updated
    //value for all other running threads, hence the use of 'volatile'.
    //This is because I cannot guarantee the callbacks from the core bluetooth of the OS are on the same thread
    //every single time.
    private volatile List<LiveBeaconReading> readings;

    private Handler handler;
    private Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            dispatch();
            handler.postDelayed(runnable, dispatchInterval);
        }
    };

    BeaconReadingContinuousDispatcher(BleScannerListener listener, long dispatchInterval)
    {
        Log.d(TAG, "BeaconReadingDispatcher: ");
        handler = new Handler();
        bleScannerListener = listener;
        readings = new ArrayList<>();
        this.dispatchInterval = dispatchInterval;
    }

    void addReading(LiveBeaconReading liveBeaconReading)
    {
        readings.add(liveBeaconReading);
    }

    synchronized void dispatch()
    {
        Log.d(TAG, "dispatch: Dispatching " + readings.size() + " readings...");
        bleScannerListener.onBleScannerResult(readings);
        readings.clear();
    }

    void stopThread()
    {
        if(!this.isInterrupted())
        {
            interrupt();
        }
    }

    @Override
    public void run()
    {
        Log.d(TAG, "run: ");
        if (!Thread.currentThread().isInterrupted())
        {
            handler.postDelayed(runnable, dispatchInterval);
        }
        else
        {
            Log.d(TAG, "run: stopScan thread.");
        }
    }
}
