package com.phunware.beaconmonitor;

import android.support.annotation.NonNull;

import java.util.Comparator;

import javax.crypto.Mac;

/**
 * Created by Duy Nguyen on 7/28/16.
 */
public class MyBeacon implements Comparable<MyBeacon>
{
    public String UUID;
    public String Major;
    public String Minor;
    public double RSSI;
    public double TxPower;
    public String MacAddress;

    public MyBeacon()
    {
        MacAddress = "N/A";
    }

    @Override
    public int compareTo(@NonNull MyBeacon other)
    {
        if (this.equals(other) && RSSI == other.RSSI)
        {
            return 0;
        }
        else
        {
            return (this.RSSI > other.RSSI) ? -1 : 1;
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof MyBeacon))
        {
            return false;
        }
        else
        {
            return (Major.equalsIgnoreCase(((MyBeacon) obj).Major) && Minor.equalsIgnoreCase(((MyBeacon) obj).Minor));
        }
    }

    public static class Comparators
    {

        public static Comparator<MyBeacon> RSSI = new Comparator<MyBeacon>()
        {
            @Override
            public int compare(MyBeacon o1, MyBeacon o2)
            {
                if(o1.RSSI > o2.RSSI)
                {
                    return 1;
                }
                else if(o1.RSSI < o2.RSSI)
                {
                    return -1;
                }
                return 0;
            }
        };

        public static Comparator<MyBeacon> UUID = new Comparator<MyBeacon>()
        {
            @Override
            public int compare(MyBeacon o1, MyBeacon o2)
            {
                return o1.UUID.compareTo(o2.UUID);
            }
        };

        public static Comparator<MyBeacon> MAJOR = new Comparator<MyBeacon>()
        {
            @Override
            public int compare(MyBeacon o1, MyBeacon o2)
            {
                return o1.Major.compareTo(o2.Major);
            }
        };

        public static Comparator<MyBeacon> MINOR = new Comparator<MyBeacon>()
        {
            @Override
            public int compare(MyBeacon o1, MyBeacon o2)
            {
                return o1.Minor.compareTo(o2.Minor);
            }
        };

        public static Comparator<MyBeacon> UUID_MAJOR_MINOR = new Comparator<MyBeacon>()
        {
            @Override
            public int compare(MyBeacon o1, MyBeacon o2)
            {

                int uuid = o1.UUID.compareTo(o2.UUID);
                if(uuid > 0)
                {
                    return 1;
                }
                else if(uuid < 0)
                {
                    return -1;
                }
                else
                {
                    int major = o1.Major.compareTo(o2.Major);
                    if(major > 0)
                    {
                        return 1;
                    }
                    else if(major < 0)
                    {
                        return -1;
                    }
                    else
                    {
                        int minor = o1.Minor.compareTo(o2.Minor);
                        if(minor > 0)
                        {
                            return 1;
                        }
                        else if(minor < 0)
                        {
                            return -1;
                        }
                        else
                        {
                            return 0;
                        }
                    }
                }
            }
        };

        public static Comparator<MyBeacon> MAJOR_MINOR_RSSI_ASC = new Comparator<MyBeacon>()
        {
            @Override
            public int compare(MyBeacon o1, MyBeacon o2)
            {

                int major = o1.Major.compareTo(o2.Major);
                if(major < 0)
                {
                    return 1;
                }
                else if(major > 0)
                {
                    return -1;
                }
                else
                {
                    int minor = o1.Minor.compareTo(o2.Minor);
                    if(minor < 0)
                    {
                        return 1;
                    }
                    else if(minor > 0)
                    {
                        return -1;
                    }
                    else
                    {
                        double rssi = o1.RSSI - o2.RSSI;
                        if (rssi < 0)
                        {
                            return 1;
                        }
                        else if (rssi > 0)
                        {
                            return -1;
                        }
                        else
                        {
                            return 0;
                        }
                    }
                }
            }
        };

        public static Comparator<MyBeacon> UUID_MAJOR_MINOR_RSSI = new Comparator<MyBeacon>()
        {
            @Override
            public int compare(MyBeacon o1, MyBeacon o2)
            {

                int uuid = o1.UUID.compareTo(o2.UUID);
                if(uuid > 0)
                {
                    return 1;
                }
                else if(uuid < 0)
                {
                    return -1;
                }
                else
                {
                    int major = o1.Major.compareTo(o2.Major);
                    if(major > 0)
                    {
                        return 1;
                    }
                    else if(major < 0)
                    {
                        return -1;
                    }
                    else
                    {
                        int minor = o1.Minor.compareTo(o2.Minor);
                        if(minor > 0)
                        {
                            return 1;
                        }
                        else if(minor < 0)
                        {
                            return -1;
                        }
                        else
                        {
                            double rssi = o1.RSSI - o2.RSSI;
                            if (rssi > 0)
                            {
                                return 1;
                            }
                            else if (rssi < 0)
                            {
                                return -1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                    }
                }
            }
        };
    }
}
