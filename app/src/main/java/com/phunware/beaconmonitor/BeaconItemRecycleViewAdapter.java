package com.phunware.beaconmonitor;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * Created by Duy Nguyen on 8/15/17.
 */

public class BeaconItemRecycleViewAdapter extends RecyclerView.Adapter<BeaconItemRecycleViewAdapter.ViewHolder>
{

    public static final String TAG = BeaconItemRecycleViewAdapter.class.getSimpleName();

    private List<MyBeacon> beacons;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView macAdress;
        private final TextView major;
        private final TextView minor;
        private final TextView rssi;
        private final TextView txPower;

        public ViewHolder(View v)
        {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");
                }
            });
            macAdress = (TextView) v.findViewById(R.id.tv_mac_address);
            major = (TextView) v.findViewById(R.id.tv_major);
            minor = (TextView) v.findViewById(R.id.tv_minor);
            rssi = (TextView) v.findViewById(R.id.tv_rssi);
            txPower = (TextView) v.findViewById(R.id.tv_txpower);
        }

        public TextView getMacAdressTextView()
        {
            return macAdress;
        }

        public TextView getMajorTextView()
        {
            return major;
        }

        public TextView getMinorTextView()
        {
            return minor;
        }

        public TextView getRssiTextView()
        {
            return rssi;
        }

        public TextView getTxPowerTextView()
        {
            return txPower;
        }
    }

    public BeaconItemRecycleViewAdapter(List<MyBeacon> beacons)
    {
        this.beacons = beacons;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // Create a new view.
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.beacon_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        MyBeacon myBeacon = beacons.get(position);
        holder.getMacAdressTextView().setText(myBeacon.MacAddress);
        holder.getMajorTextView().setText(myBeacon.Major);
        holder.getMinorTextView().setText(myBeacon.Minor);
        holder.getRssiTextView().setText(String.format(Locale.getDefault(), "%.2f", myBeacon.RSSI));
        holder.getTxPowerTextView().setText(String.format(Locale.getDefault(), "%.2f", myBeacon.TxPower));
    }

    @Override
    public int getItemCount()
    {
        return beacons.size();
    }

}
