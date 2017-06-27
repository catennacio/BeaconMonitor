package com.phunware.beaconmonitor;

import android.support.annotation.NonNull;

/**
 * Created by Duy Nguyen on 7/28/16.
 */
public class MyBeacon implements Comparable<MyBeacon>
{
    public String UUID;
    public String Major;
    public String Minor;
    public int RSSI;
    public int TxPower;

    public MyBeacon() {}

    @Override
    public int compareTo(@NonNull MyBeacon other)
    {
        if(this.equals(other) && RSSI == other.RSSI)
        {
            return 0;
        }
        else return (this.RSSI > other.RSSI)?-1:1;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof MyBeacon))
        {
            return false;
        }
        else
        {
            return (Major.equalsIgnoreCase(((MyBeacon)obj).Major) && Minor.equalsIgnoreCase(((MyBeacon)obj).Minor));
        }
    }
}
