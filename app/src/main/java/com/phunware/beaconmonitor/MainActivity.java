package com.phunware.beaconmonitor;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier
{
    public static final String TAG = MainActivity.class.getSimpleName();
//    public static final String DEFAULT_UUID = "11C7F1F8-3058-B733-CB03-110EA9C11254";//Senion New
//    public static final String DEFAULT_UUID = "d3f6aa9c-59bb-11e6-929a-02e208b2d34f";//Mist
//    public static final String DEFAULT_UUID = "30F10CA5-8D45-4AF3-BDCF-8387CE548A71";//Cisco
    public static final String DEFAULT_UUID = "";
    private BeaconManager beaconManager;
    private Region region;
    private EditText editTextUUID;
    private boolean isRanging;
    private ListView listviewBeacon;
    private List<MyBeacon> myBeacons;
    private MenuItem actionScanMenuItem;
    private MyBeacon watchedBeacon;
    private SharedPreferences sharedPreferences;
    private static final String UUID_KEY = "UUID_KEY";
    private static final String[] INITIAL_PERMS = {
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int INITIAL_REQUEST = 0;

    private boolean allPermissionGranted;
    private boolean isHexChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });*/

        sharedPreferences = this.getSharedPreferences("com.phunware.beaconmonitor", Context.MODE_PRIVATE);
        String savedUUID = sharedPreferences.getString(UUID_KEY, DEFAULT_UUID);

        editTextUUID = (EditText) findViewById(R.id.et_UUID);
        editTextUUID.setText(savedUUID);
        editTextUUID.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean b)
            {
                if(!b)//if not focus
                {
                    Toast.makeText(MainActivity.this, "Start scanning", Toast.LENGTH_SHORT).show();
                }
            }
        });

        editTextUUID.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    startScan();
                    return true;
                }
                return false; // pass on to other listeners.
            }
        });

        listviewBeacon = (ListView)findViewById(R.id.lv_beacons);
        listviewBeacon.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                watchedBeacon = myBeacons.get(position);
            }
        });

        /*Toast.makeText(this, this.getResources().getString(R.string.grant_permissions), Toast.LENGTH_SHORT).show();
        checkAllPermissions();*/

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.setForegroundScanPeriod(1000L);
        beaconManager.setForegroundBetweenScanPeriod(0L);
        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy()
    {
        stopScan();
        beaconManager.unbind(this);
        sharedPreferences.edit().putString(UUID_KEY, editTextUUID.getText().toString()).apply();
        super.onDestroy();
    }

    private void checkAllPermissions()
    {
        if(!canAccessCoarseLocation() || !canAccessFineLocation())
        {
            ActivityCompat.requestPermissions(this, INITIAL_PERMS, INITIAL_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case INITIAL_REQUEST:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    allPermissionGranted = true;
                }
                else
                {
                    allPermissionGranted = false;
                }
            }
        }
    }

    private boolean canAccessCoarseLocation()
    {
        return hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private boolean canAccessFineLocation()
    {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean hasPermission(String perm)
    {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        actionScanMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id)
        {
            case R.id.action_scan:
            {
                if(!isRanging)
                {
                /*if(!allPermissionGranted)
                {
                    checkAllPermissions();
                }
                if(allPermissionGranted)
                {
                    startScan();
                }*/
                    startScan();
                }
                else
                {
                    stopScan();
                }

                return true;
            }

            case R.id.action_switch_hex_dec:
            {
                item.setChecked(!item.isChecked());
                isHexChecked = item.isChecked();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void startScan()
    {
        if(beaconManager.isBound(this))
        {
            try
            {
                watchedBeacon = null;
                if(isRanging)
                {
                    stopScan();
                }

                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                if(myBeacons != null)
                {
                    myBeacons.clear();
                }

                myBeacons = new ArrayList<>();
                listviewBeacon.setAdapter(new BeaconAdapter(MainActivity.this, myBeacons, watchedBeacon));

                Identifier identifier = null;
                if(editTextUUID.getText() == null || editTextUUID.getText().toString().equalsIgnoreCase(""))
                {
//                    Toast.makeText(MainActivity.this, this.getResources().getString(R.string.hint), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    identifier = Identifier.parse(editTextUUID.getText().toString().toUpperCase());
                }

                region = new Region("PwBeaconMonitor", identifier, null, null);

                beaconManager.startRangingBeaconsInRegion(region);
                actionScanMenuItem.setTitle(this.getResources().getString(R.string.action_stop_scan));
                isRanging = true;
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Invalid UUID", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(MainActivity.this, "Beacon Manager is not bound yet. Kill the app and relaunch.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScan()
    {
        if(isRanging)
        {
            try
            {
                beaconManager.stopRangingBeaconsInRegion(region);
                isRanging = false;
                actionScanMenuItem.setTitle(this.getResources().getString(R.string.action_scan));
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBeaconServiceConnect()
    {
        beaconManager.setRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region)
    {
//        Log.d(TAG, "didRangeBeaconsInRegion: beacons seen = " + collection.size());
        if(collection.size() > 0)
        {
            if(myBeacons != null)
            {
                myBeacons.clear();
            }

            myBeacons = new ArrayList<>();

            for(Beacon beacon : collection)
            {
                MyBeacon myBeacon = new MyBeacon();
                myBeacon.UUID = beacon.getId1().toString();
                if(isHexChecked)
                {
                    myBeacon.Major = decToHex(Integer.parseInt(beacon.getId2().toString()));
                    myBeacon.Minor= decToHex(Integer.parseInt(beacon.getId3().toString()));
                }
                else
                {
                    myBeacon.Major = beacon.getId2().toString();
                    myBeacon.Minor= beacon.getId3().toString();
                }

                myBeacon.RSSI = beacon.getRssi();
                myBeacon.TxPower = beacon.getTxPower();
//                Log.d(TAG, "didRangeBeaconsInRegion:" + "UUID_KEY:" + myBeacon.UUID + " Major=" + myBeacon.Major + " Minor=" + myBeacon.Minor + " RSSI=" + myBeacon.RSSI + " Tx=" + myBeacon.TxPower);
                myBeacons.add(myBeacon);
            }

            Collections.sort(myBeacons);

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    listviewBeacon.setAdapter(new BeaconAdapter(MainActivity.this, myBeacons, watchedBeacon));
                }
            });
        }
    }

    private String decToHex(int dec)
    {
        return Integer.toHexString(dec);
    }

    private int hexToDec(String hex)
    {
        return Integer.parseInt(hex, 16);
    }
}
