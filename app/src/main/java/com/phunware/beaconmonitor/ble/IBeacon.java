package com.phunware.beaconmonitor.ble;

import android.support.annotation.NonNull;

import org.parceler.Parcel;

/**
 * Created by Duy Nguyen on 6/22/17.
 */

@Parcel
public class IBeacon implements Comparable<IBeacon>
{
    public String Address;
    public String Name;
    public Identifier UUID;
    public String Major;
    public String Minor;
    public String FullName;
    public int TxPower;

    public IBeacon()
    {
    }

    public String getFullName()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(UUID);
        sb.append(",");
        sb.append(Major);
        sb.append(",");
        sb.append(Minor);
        sb.append("]");
        FullName = sb.toString();
        return FullName;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getFullName());
        sb.append(" TxPower=");
        sb.append(TxPower);
        sb.append(" Name=");
        sb.append(Name);
        sb.append(" Address=");
        sb.append(Address);
        return sb.toString();
    }

    public String toFingerprintString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(UUID);
        sb.append(",");
        sb.append(Major);
        sb.append(",");
        sb.append(Minor);
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;

        IBeacon IBeacon = (IBeacon) obj;
        return UUID.equals(IBeacon.UUID) && Major.equalsIgnoreCase(IBeacon.Major) && Minor.equalsIgnoreCase(
            IBeacon.Minor);
    }

    @Override
    public int compareTo(@NonNull IBeacon another)
    {
        //The only way to uniquely identify a beacon is via the MAC Address, not the UUIDs, Major and Minor
        //This is because a Cisco AP can be configured to chirp any UUIDs, Major and Minor
        //But we can't compare the MAC address because we don't know it at run time
        //return (Address.equalsIgnoreCase(another.Address))?0:1;

        //So we have to compare the reference, which means client class is responsible for managing the reference of
        //this class
        return (this == another) ? 0 : 1;
    }
}
