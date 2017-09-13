package com.phunware.beaconmonitor;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

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

    public static Bitmap textAsBitmap(Context context, String text, int dimen, int textColor)
    {
        int pixel = context.getResources().getDimensionPixelSize(dimen);
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(pixel);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    public static String decToHex(int dec)
    {
        return Integer.toHexString(dec);
    }

    public static List<MyBeacon> groupMacAddress(List<MyBeacon> beacons)
    {
        HashMap<String, List<MyBeacon>> map = new HashMap<>();
        for(MyBeacon beacon : beacons)
        {
            if(!map.containsKey(beacon.MacAddress))
            {
                List<MyBeacon> list = new ArrayList<>();
                list.add(beacon);
                map.put(beacon.MacAddress, list);
            }
            else
            {
                map.get(beacon.MacAddress).add(beacon);
            }
        }

        List<MyBeacon> groupBeacons = new ArrayList<>();
        for(String key : map.keySet())
        {
            Log.d(TAG, "groupMacAddress: key=" + key);
            List<MyBeacon> list = map.get(key);
            //calculate average RSSI

            double sumRssi = 0;
            double sumTx = 0;
            for(MyBeacon beacon : list)
            {
                sumRssi += beacon.RSSI;
                sumTx += beacon.TxPower;
            }

            double avgRssi = sumRssi / list.size();
            double avgTx = sumTx / list.size();

            MyBeacon myBeacon = new MyBeacon();
            myBeacon.MacAddress = key;
            myBeacon.UUID = map.get(key).get(0).UUID;
            myBeacon.Major = map.get(key).get(0).Major;
            myBeacon.Minor = map.get(key).get(0).Minor;
            myBeacon.TxPower = avgTx;
            myBeacon.RSSI = avgRssi;
            groupBeacons.add(myBeacon);
        }

        return groupBeacons;
    }
}
