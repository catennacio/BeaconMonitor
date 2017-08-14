package com.phunware.beaconmonitor.ble;

import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * Created by Duy Nguyen on 6/22/17.
 */

public class AdvertisingRecord
{
    public AdvertisingRecord(int length, int type, byte[] data)
    {
        String decodedRecord = "";
        try
        {
            decodedRecord = new String(data, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        Log.d("AdRecord", "Length: " + length + " Type : " + type + " Data : " + Utils.fromByteArrayToString(data));
    }

}