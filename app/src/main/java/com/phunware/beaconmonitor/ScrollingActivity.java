package com.phunware.beaconmonitor;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.catennacio.simpleblescanner.BleScanner;
import com.catennacio.simpleblescanner.BleScannerListener;
import com.catennacio.simpleblescanner.BleScannerOptions;
import com.catennacio.simpleblescanner.LiveBeaconReading;

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

public class ScrollingActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier, BleScannerListener
{
    public static final String TAG = ScrollingActivity.class.getSimpleName();
    public static final String DEFAULT_UUID = "";
    private static final int REQUEST_PERMISSIONS = 0;
    private static final int REQUEST_ENABLE_BT = 1;

    private AppBarLayout appBarLayout;
    private EditText etUUID;
    private EditText etMajor;
    private EditText etMinor;
    private SwitchCompat switchUseCoreBluetooth;
    private SwitchCompat switchShowInHex;
    private SwitchCompat switchGroupMacAddress;
    private RecyclerView beaconRecycleView;
    private BeaconItemRecycleViewAdapter adapter;
    private FloatingActionButton fabScan;

    private List<MyBeacon> myBeacons;

    private BeaconManager beaconManager;
    private Region region;
    private boolean isRanging;

    private SharedPreferences sharedPreferences;
    private static final String UUID_KEY = "UUID_KEY";
    private static final String MAJOR_KEY = "MAJOR_KEY";
    private static final String MINOR_KEY = "MINOR_KEY";

    private ScanLibrary scanLibrary;
    private boolean isHexChecked;
    private boolean groupMacAddress;

    private boolean isBleScannerScanning  = false;

    private BleScanner bleScanner;
    private BleScannerOptions bleScannerOptions;

    private static final String[] PERMISSIONS_NEEDED = {
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean allPermissionGranted;

    private String uuid;
    private String major;
    private String minor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        hasPermissions();
/*
        if (!Utils.isBluetoothOn())
        {
            Toast.makeText(this, "Bluetooth is OFF. Turning bluetooth ON...", Toast.LENGTH_SHORT).show();
            Utils.setBluetooth(true);
        }*/

        BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        sharedPreferences = this.getSharedPreferences("com.phunware.beaconmonitor", Context.MODE_PRIVATE);
        String savedUUID = sharedPreferences.getString(UUID_KEY, DEFAULT_UUID);
        String savedMajor = sharedPreferences.getString(MAJOR_KEY, "");
        String savedMinor = sharedPreferences.getString(MINOR_KEY, "");

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        etUUID = (EditText) findViewById(R.id.et_UUID);
        etUUID.setText(savedUUID);
        etMajor = (EditText) findViewById(R.id.et_Major);
        etMajor.setText(savedMajor);
        etMinor = (EditText) findViewById(R.id.et_Minor);
        etMinor.setText(savedMinor);
        switchUseCoreBluetooth = (SwitchCompat) findViewById(R.id.switch_core_bluetooth);
        switchUseCoreBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(hasPermissions())
                {
                    if(isScanning())
                    {
                        stopScan();
                    }
                    if(isChecked)
                    {
                        scanLibrary = ScanLibrary.SimpleBleScan;
                        Toast.makeText(ScrollingActivity.this, "Using core bluetooth to scan", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        scanLibrary = ScanLibrary.AltBeacon;
                        Toast.makeText(ScrollingActivity.this, "Using AltBeacon library to scan", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        switchShowInHex = (SwitchCompat) findViewById(R.id.switch_show_hex);
        switchShowInHex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                isHexChecked = isChecked;
            }
        });

        switchGroupMacAddress = (SwitchCompat) findViewById(R.id.switch_group_mac_address);
        switchGroupMacAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                groupMacAddress = isChecked;
            }
        });

        beaconRecycleView = (RecyclerView)findViewById(R.id.beacon_recycle_view);
        myBeacons = new ArrayList<>();
        adapter = new BeaconItemRecycleViewAdapter(myBeacons);
        beaconRecycleView.setAdapter(adapter);
        beaconRecycleView.setLayoutManager(new LinearLayoutManager(this));
        fabScan = (FloatingActionButton) findViewById(R.id.fab_scan);
        setFabText(getString(R.string.action_start_scan));
        fabScan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!isScanning())
                {
                    startScan();
                }
                else
                {
                    stopScan();
                }
            }
        });

        scanLibrary = ScanLibrary.SimpleBleScan;

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.setForegroundScanPeriod(1100L);
        beaconManager.setForegroundBetweenScanPeriod(0L);
        beaconManager.bind(this);

        BleScannerOptions.ScanStrategy scanStrategy;
        try
        {
            //As of Android N DP4 ble scanner cannot be started more than 5 times per 30 seconds, so only use SCAN_STRATEGY_CONTINUOUS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                scanStrategy = BleScannerOptions.ScanStrategy.SCAN_STRATEGY_CONTINUOUS;
            }
            else
            {
                scanStrategy = BleScannerOptions.ScanStrategy.SCAN_STRATEGY_PERIODIC;
            }
            Log.d(TAG, "onCreate: scanStrategy = " + scanStrategy);

            bleScannerOptions = new BleScannerOptions(BleScannerOptions.ScanMode.SCAN_MODE_BALANCE, scanStrategy);
        }
        catch (Exception ex)
        {
            Log.e(TAG, "onCreate: " + ex.toString(), ex);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    protected void onPause()
    {
        sharedPreferences.edit().putString(UUID_KEY, etUUID.getText().toString()).commit();
        sharedPreferences.edit().putString(MAJOR_KEY, etMajor.getText().toString()).commit();
        sharedPreferences.edit().putString(MINOR_KEY, etMinor.getText().toString()).commit();
        stopScan();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        beaconManager.unbind(this);
        super.onDestroy();
    }

    private boolean hasPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat
                .checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat
                .checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
            {

                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_ADMIN},
                    REQUEST_PERMISSIONS
                );
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_PERMISSIONS:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "onRequestPermissionsResult: all permissions granted");
                }
                else
                {
                    Log.d(TAG, "onRequestPermissionsResult: " + permissions);
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case REQUEST_ENABLE_BT:
            {
                Log.d(TAG, "onActivityResult: " + resultCode);
                break;
            }
        }
    }

    private void startScan()
    {
        Log.d(TAG, "startScan: scanLibrary=" + scanLibrary);
        appBarLayout.setExpanded(false, true);
        hideKeyBoard();

        if(etUUID.getText() != null && !etUUID.getText().toString().isEmpty())
        {
            uuid = etUUID.getText().toString();
            Log.d(TAG, "startScan: scanning uuid " + etUUID.getText().toString());
        }
        else
        {
            uuid = null;
            Log.d(TAG, "startScan: scan all uuids");
        }

        if(etMajor.getText() != null && !etMajor.getText().toString().isEmpty())
        {
            major = etMajor.getText().toString();
        }
        else
        {
            major = null;
        }

        if(etMinor.getText() != null && !etMinor.getText().toString().isEmpty())
        {
            minor = etMinor.getText().toString();
        }
        else
        {
            minor = null;
        }

        Log.d(TAG, "startScan: major = " + major + " minor = " + minor);

        switch (scanLibrary)
        {
            case AltBeacon:
            {
                startAltScan();
                break;
            }
            case SimpleBleScan:
            default:
            {
                startSimpleBleScanner();
                break;
            }
        }

        if(fabScan != null)
        {
            setFabText(getString(R.string.action_stop_scan));
        }
    }

    private void stopScan()
    {
        Log.d(TAG, "stopScan: scanLibrary=" + scanLibrary);
        switch (scanLibrary)
        {
            case AltBeacon:
            {
                stopAltScan();
                break;
            }
            case SimpleBleScan:
            default:
            {
                stopSimpleBleScanner();
                break;
            }
        }

        if(fabScan != null)
        {
            setFabText(getString(R.string.action_start_scan));
        }
    }

    private void startSimpleBleScanner()
    {
        Log.d(TAG, "startSimpleBleScanner: ");

        if(!isBleScannerScanning)
        {
            try
            {
                if(bleScanner != null)
                {
                    bleScanner.stop();
                }

                bleScanner = new BleScanner(this, bleScannerOptions, this);

                if(etUUID.getText() != null && !etUUID.getText().toString().isEmpty())
                {
                    bleScanner.addUuidToMonitor(etUUID.getText().toString());
                    Log.d(TAG, "startSimpleBleScanner: scanning uuid " + etUUID.getText().toString());
                }

                bleScanner.start();
                isBleScannerScanning = true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void stopSimpleBleScanner()
    {
        Log.d(TAG, "stopSimpleBleScanner: ");
        if(bleScanner != null)
        {
            bleScanner.stop();
            isBleScannerScanning = false;
        }
    }

    private void startAltScan()
    {
        Log.d(TAG, "startAltScan: ");
        if(beaconManager != null && beaconManager.isBound(this))
        {
            if(!isRanging)
            {
                try
                {
                    View view = this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    if(myBeacons != null)
                    {
                        myBeacons.clear();
                    }

                    Identifier identifier = null;
                    if(uuid != null)
                    {
                        identifier = Identifier.parse(etUUID.getText().toString().toUpperCase());
                    }

                    region = new Region("PwBeaconMonitor", identifier, null, null);
                    beaconManager.startRangingBeaconsInRegion(region);
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
        }
        else
        {
            Toast.makeText(this, "Beacon Manager is not bound yet. Kill the app and relaunch.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAltScan()
    {
        Log.d(TAG, "stopAltScan: ");
        if(beaconManager != null)
        {
            try
            {
                beaconManager.stopRangingBeaconsInRegion(region);
                isRanging = false;
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }
    }

    private boolean isScanning()
    {
        return isRanging || isBleScannerScanning;
    }

    private void setFabText(String text)
    {
        fabScan.setImageBitmap(Utils.textAsBitmap(this, text, R.dimen.fab_text_size, ContextCompat.getColor(this, R.color.colorFab) ));
    }

    private void hideKeyBoard()
    {
        View view = getCurrentFocus();
        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBleScannerResult(List<LiveBeaconReading> readings)
    {
        myBeacons = new ArrayList<>();
        for(LiveBeaconReading liveBeaconReading : readings)
        {
            if(liveBeaconReading.IBeacon != null)
            {
                MyBeacon myBeacon = new MyBeacon();
                String uuid = (liveBeaconReading.IBeacon.UUID != null)?liveBeaconReading.IBeacon.UUID.toString():"";
                String major = liveBeaconReading.IBeacon.Major;
                String minor = liveBeaconReading.IBeacon.Minor;

                myBeacon.UUID = uuid;
                myBeacon.Major = major;
                myBeacon.Minor = minor;
                myBeacon.RSSI = liveBeaconReading.RSSI;
                myBeacon.TxPower = liveBeaconReading.IBeacon.TxPower;
                myBeacon.MacAddress = liveBeaconReading.IBeacon.Address;

                if(this.major == null && this.minor == null)
                {
                    if(isHexChecked)
                    {
                        myBeacon.Major = Utils.decToHex(Integer.parseInt(myBeacon.Major));
                        myBeacon.Minor = Utils.decToHex(Integer.parseInt(myBeacon.Minor));
                    }
                    myBeacons.add(myBeacon);
                }
                else if(this.major != null && this.minor == null)
                {
                    if(isHexChecked)
                    {
                        myBeacon.Major = Utils.decToHex(Integer.parseInt(myBeacon.Major));
                        myBeacon.Minor = Utils.decToHex(Integer.parseInt(myBeacon.Minor));
                    }
                    myBeacons.add(myBeacon);
                }
                else if(this.major == null && this.minor != null)
                {
                    if(isHexChecked)
                    {
                        myBeacon.Major = Utils.decToHex(Integer.parseInt(myBeacon.Major));
                        myBeacon.Minor = Utils.decToHex(Integer.parseInt(myBeacon.Minor));
                    }
                    myBeacons.add(myBeacon);
                }
                else if(this.major != null && this.minor != null)
                {
                    if(this.major.equalsIgnoreCase(myBeacon.Major) && this.minor.equalsIgnoreCase(myBeacon.Minor))
                    {
                        if(isHexChecked)
                        {
                            myBeacon.Major = Utils.decToHex(Integer.parseInt(myBeacon.Major));
                            myBeacon.Minor = Utils.decToHex(Integer.parseInt(myBeacon.Minor));
                        }
                        myBeacons.add(myBeacon);
                    }
                }
            }
        }

        bindData();
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
            myBeacons = new ArrayList<>();

            for(Beacon beacon : collection)
            {
                MyBeacon myBeacon = new MyBeacon();
                myBeacon.UUID = beacon.getId1().toString();
                if(isHexChecked)
                {
                    myBeacon.Major = Utils.decToHex(Integer.parseInt(beacon.getId2().toString()));
                    myBeacon.Minor= Utils.decToHex(Integer.parseInt(beacon.getId3().toString()));
                }
                else
                {
                    myBeacon.Major = beacon.getId2().toString();
                    myBeacon.Minor= beacon.getId3().toString();
                }

                myBeacon.RSSI = beacon.getRssi();
                myBeacon.TxPower = beacon.getTxPower();
                myBeacons.add(myBeacon);
            }

            Log.d(TAG, "didRangeBeaconsInRegion: size=" + myBeacons.size());

            bindData();
        }
    }

    private void bindData()
    {
        if(groupMacAddress && !isRanging)
        {
            myBeacons = Utils.groupMacAddress(myBeacons);
        }

        Collections.sort(myBeacons);

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                adapter = new BeaconItemRecycleViewAdapter(myBeacons);
                beaconRecycleView.swapAdapter(adapter, true);
                beaconRecycleView.getRecycledViewPool().clear();
            }
        });
    }
}
