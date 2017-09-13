package com.phunware.beaconmonitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duy Nguyen on 7/28/16.
 */
public class BeaconAdapter extends ArrayAdapter<MyBeacon>
{
    private Context context;
    private MyBeacon watchBeacon;

    public BeaconAdapter(Context context, List<MyBeacon> beaconList, MyBeacon watchedBeacon)
    {
        super(context, 0, new ArrayList<MyBeacon>());
        addAll(beaconList);
        this.context = context;
        watchBeacon = watchedBeacon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;
        ViewHolder holder;
        if(v == null)
        {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.beacon_list_item, parent, false);
            holder = new ViewHolder();
            holder.macAddress= (TextView)v.findViewById(R.id.tv_mac_address);
            holder.major = (TextView)v.findViewById(R.id.tv_major);
            holder.minor = (TextView)v.findViewById(R.id.tv_minor);
            holder.rssi = (TextView)v.findViewById(R.id.tv_rssi);
            holder.txPower = (TextView)v.findViewById(R.id.tv_txpower);

            v.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)v.getTag();
        }

        MyBeacon currentBeacon = getItem(position);

        holder.macAddress.setText(currentBeacon.MacAddress);
        holder.major.setText(currentBeacon.Major);
        holder.minor.setText(currentBeacon.Minor);
        holder.rssi.setText(Double.toString(currentBeacon.RSSI));
        holder.txPower.setText(Double.toString(currentBeacon.TxPower));

        /*if(currentBeacon != null && currentBeacon.equals(watchBeacon))
        {
            holder.major.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            holder.major.setPaintFlags(holder.major.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            holder.minor.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            holder.minor.setPaintFlags(holder.major.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            holder.rssi.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            holder.rssi.setPaintFlags(holder.major.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            holder.txPower.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            holder.txPower.setPaintFlags(holder.major.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        }*/

        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getView(position, convertView, parent);
    }

    private static final class ViewHolder
    {
        TextView macAddress;
        TextView major;
        TextView minor;
        TextView rssi;
        TextView txPower;
    }
}
