package com.phunware.beaconmonitor;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

/**
 * Created by Duy Nguyen on 8/11/17.
 */

public class Utils
{
    public static final String TAG = Utils.class.getSimpleName();

    public static boolean isBluetoothOn()
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
    }

    public static boolean setBluetooth(boolean enable)
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null)
        {
            boolean isEnabled = bluetoothAdapter.isEnabled();
            if (enable && !isEnabled)
            {
                return bluetoothAdapter.enable();
            }
            else if (!enable && isEnabled)
            {
                return bluetoothAdapter.disable();
            }
            // No need to change bluetooth state
            return true;
        }
        else
        {
            Log.e(TAG, "setBluetooth: No bluetooth adapter available!");
            return false;
        }
    }
}
