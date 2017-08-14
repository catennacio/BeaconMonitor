package com.phunware.beaconmonitor.ble;

import android.bluetooth.le.ScanResult;

import org.parceler.Parcel;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Duy Nguyen on 6/22/17.
 */

@Parcel
public class LiveBeaconReading
{
    public static final String TAG = LiveBeaconReading.class.getSimpleName();
    public long TimeStamp;
    public IBeacon IBeacon;
    public int RSSI;

    public LiveBeaconReading()
    {
        IBeacon = new IBeacon();
    }

    /*
    * From this https://en.wikipedia.org/wiki/IBeacon#Packet_Structure_Byte_Map
    *
    * Byte 0-2: Standard BLE Flags
    * Byte 0: Length :  0x02
    * Byte 1: Type: 0x01 (Flags)
    * Byte 2: Value: 0x06 (Typical Flags)
    * Byte 3-29: Apple Defined iBeacon Data
    * Byte 3: Length: 0x1a
    * Byte 4: Type: 0xff (Custom Manufacturer Packet)
    * Byte 5-6: Manufacturer ID : 0x4c00 (Apple)
    * Byte 7: SubType: 0x2 (iBeacon)
    * Byte 8: SubType Length: 0x15
    * Byte 9-24: Proximity UUIDs
    * Byte 25-26: Major
    * Byte 27-28: Minor
    * Byte 29: Signal Power
    * */
    public LiveBeaconReading(ScanResult scanResult)
    {
        byte[] scanRecord = scanResult.getScanRecord().getBytes();

        //make sure it's iBeacon packet
        if (scanRecord[7] == 0x02 && scanRecord[8] == 0x15)
        {
            //16 bytes from 9 to 24 is UUIDs and et cetera after that
            UUID uuid = Utils.getGuidFromByteArray(Arrays.copyOfRange(scanRecord, 9, 25));
            int major = (scanRecord[25] & 0xff) * 0x100 + (scanRecord[26] & 0xff);
            int minor = (scanRecord[27] & 0xff) * 0x100 + (scanRecord[28] & 0xff);
            int txpw = scanRecord[29];

            TimeStamp = System.currentTimeMillis();
            IBeacon = new IBeacon();
            IBeacon.UUID = Identifier.fromUuid(uuid);
            IBeacon.Major = Integer.toString(major).toUpperCase();
            IBeacon.Minor = Integer.toString(minor).toUpperCase();
            IBeacon.Name = scanResult.getDevice().getName();
            IBeacon.TxPower = txpw;
            IBeacon.Address = scanResult.getDevice().getAddress();
            RSSI = scanResult.getRssi();
        }
        else
        {
//            Log.i(TAG, "LiveBeaconReading: Not an iBeacon packet!");
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TimeStamp);
        sb.append(",");
        if(IBeacon != null)
        {
            sb.append(IBeacon.toString());
            sb.append(",");
        }
        sb.append(RSSI);
        return sb.toString();
    }
}
