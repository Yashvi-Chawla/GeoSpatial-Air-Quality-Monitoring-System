package com.example.iotssc_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iotssc_app.adapter.BluetoothReceiver;
import com.example.iotssc_app.adapter.DiscoveredBluetoothDevice;
import com.example.iotssc_app.adapter.ScannerDevicesAdapter;
import com.example.iotssc_app.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;


import java.lang.Math;
import java.util.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements LocationListener{
    // Activity Tags
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 1022; // random number
    private static final String TAG = "MainActivityTag";



    //decryption parameters

    public static int t = (5-1)*(13-1);
    public static int n = 5*13;
    public static int x = 5;
    public static int y = 13;
    public static long[] e = new long[50];
    public static long[] d = new long[50];




    // Application Scope
    private ApplicationData app;

    // Android Serial Library
    private Context context;
    private BluetoothManager bluetoothManager;
    private SimpleBluetoothDeviceInterface deviceInterface;
    private BluetoothReceiver bluetoothReceiver = new BluetoothReceiver();

    // Bluetooth Scanner
    private ScannerDevicesAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;
    private Boolean hasBluetoothScanStarted = false;

    //location
    protected double latitude;
    protected double longitude;
    // Permissions Views
    @BindView(R.id.no_devices)
    View mEmptyView;
    @BindView(R.id.no_location_permission)
    View mNoLocationPermissionView;
    @BindView(R.id.action_grant_location_permission)
    Button mGrantPermissionButton;
    @BindView(R.id.action_permission_settings)
    Button mPermissionSettingsButton;
    @BindView(R.id.action_enable_location)
    Button enableLocation;
    @BindView(R.id.no_location)
    View mNoLocationView;
    @BindView(R.id.bluetooth_off)
    View mNoBluetoothView;

    // Activity Connection views
    @BindView(R.id.activityTitleTextView)
    TextView activityTitleTextView;
    @BindView(R.id.selectDeviceButton)
    Button selectDeviceButton;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.appLoadingProgressBar)
    ProgressBar appLoadingProgressBar;
    @BindView(R.id.bluetoothScanningProgressBar)
    ProgressBar bluetoothScanningProgressBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    // Activity bluetooth views
    @BindView(R.id.connectedDeviceMessageCommunication)
    ConstraintLayout connectedDeviceComms;
    // TODO add bindings for your added message communication views here
    @BindView(R.id.textView2)
    TextView connectedDeviceCommunication;
    @BindView(R.id.textView3)
    TextView connectedToDevice;
    @BindView(R.id.textView4)
    TextView deviceNumber;
    @BindView(R.id.textView5)
    TextView lastMessageFromDevice;
    @BindView(R.id.textView6)
    TextView noCurrentMessageReceived;
    @BindView(R.id.textView7)
    TextView noLastMessageTimeKnown;
    @BindView(R.id.textView8)
    TextView sendMessageToDevice;
    @BindView(R.id.edittext1)
    EditText messageToBeSent;
    @BindView(R.id.sendMessageButton)
    Button sendMessageButton;

    public static long cd(long a)
    {
        long k = 1;
        while(true)
        {
            k = k + t;
            if(k % a == 0)
                return(k/a);
        }
    }

    public static int prime(long pr)
    {
        double j;
        j = Math.sqrt(pr);
        for(int i = 2; i <= j; i++)
        {
            if(pr % i == 0)
                return 0;
        }
        return 1;
    }
    public static void encryption_key()
    {long flag;
        int k;
        k = 0;
        for(int i = 2; i < t; i++)
        {
            if(t % i == 0)
                continue;
            flag = prime(i);
            if(flag == 1 && i != x && i != y)
            {
                e[k] = i;
                flag = cd(e[k]);
                if(flag > 0)
                {
                    d[k] = flag;
                    k++;
                }
                if(k == 99)
                    break;
            }
        }
    }
    public static String decrypt(long[] en)
    {
        long pt, ct, k;
        long key = d[0];
        int i = 0;
        char[] m = new char[en.length];
   /*while(en[i]!=-1){
   	ct = en[i]-96;
   	m[i] = ((pow(ct,key)%n)+96);
   	i++;
   }*/

        while(i < en.length)
        {
            ct = en[i];
            k = 1;
            long j;
            for(j = 0; j < key; j++)
            {
                k = k * ct;
                k = k % n;
            }
            pt = k + 96;
            //pt = k;
            m[i] = (char)pt;
            i++;
        }
        //m[i] = (char)-1;
        System.out.println("\n\nTHE DECRYPTED MESSAGE IS\n");
        String decr = "";
        for(int q = 0; q<m.length; q++){
            decr = decr.concat(String.valueOf(m[q]));
        //    System.out.println(m[q]);
        }
        return decr;


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Initialise application context
        context = getApplicationContext();
        app = (ApplicationData) context;

        // TODO initialise the Bluetooth Adapter here
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Setup our BluetoothSerialManager
        bluetoothManager = BluetoothManager.getInstance();
        if (bluetoothManager == null) {
            // Bluetooth unavailable on this device :( tell the user
            Toast.makeText(getApplicationContext(), "Bluetooth not available.", Toast.LENGTH_LONG).show(); // Replace context with your context instance.
            finish();
        }

        // setup app toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);


        // Configure the recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        adapter = new ScannerDevicesAdapter(this, bluetoothReceiver.getDiscoveredBluetoothDevices());
        recyclerView.setAdapter(adapter);

        encryption_key();
        // initialise select device button functionality
        selectDeviceButton();
        sendMessageButton();
    }
    @Override
    public void onLocationChanged(Location location) {
      //  latitude = String.valueOf(location.getLatitude());
        //longitude = String.valueOf(location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    /* Bluetooth scanning and options menu instantiation */

    /**
     * Inflates the menu; this adds items to the action bar if it is present.
     * @param menu - menu object reference
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handle action bar item clicks here. The action bar will automatically handle clicks on the
     * Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
     *
     * Clicking the menu item begins/stops the process of scanning for bluetooth devices
     * (depending on application state). This updates the visibility of views as needed.
     * @param item - menu item object reference
     * @return true
     */

    public boolean onOptionsItemSelected(MenuItem item) {

        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    R.string.initialise_the_bluetooth_adapter,
                    Toast.LENGTH_LONG).show();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.bluetooth_scan) {

            if (!hasBluetoothScanStarted) {
                Log.i(TAG, "Bluetooth scan started");

                // disconnected from any bluetooth devices
                bluetoothManager.close();

                // update visibility of views
                appLoadingProgressBar.setVisibility(View.GONE);
                connectedDeviceComms.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                selectDeviceButton.setVisibility(View.GONE);

                checkPermissionsAndStartScan(bluetoothAdapter);

            } else {
                Log.i(TAG, "Bluetooth scan stopped");

                // remove the receiver and cancel device discovery
                this.unregisterReceiver(bluetoothReceiver.getBroadcastReceiver());
                bluetoothAdapter.cancelDiscovery();

                // update app context with discovered devices
                List<DiscoveredBluetoothDevice> bluetoothDeviceList =
                        bluetoothReceiver.getDiscoveredBluetoothDevices();
                app.setDevices(bluetoothDeviceList);

                // update recycler view with discovered devices
                adapter = new ScannerDevicesAdapter(this, bluetoothDeviceList);
                recyclerView.setAdapter(adapter);
                bluetoothReceiver.resetDiscoveredBluetoothDevices();

                Toast.makeText(MainActivity.this, "Bluetooth scan stopped", Toast.LENGTH_SHORT).show();

                // set views to visible and progress bar to gone
                activityTitleTextView.setText(R.string.connect_to_bluetooth_device);
                recyclerView.setVisibility(View.VISIBLE);
                selectDeviceButton.setVisibility(View.VISIBLE);
                bluetoothScanningProgressBar.setVisibility(View.GONE);
                selectDeviceButton();

            }

            // updates the device scanning state
            hasBluetoothScanStarted = !hasBluetoothScanStarted;

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Check permissions have been sent for Bluetooth scanning and start scanning. Alternatively,
     * displays a view to users requesting permissions access.
     * @param state used to query what state the bluetooth adapter is in e.g. whether its enabled
     *              or not
     */
    private void checkPermissionsAndStartScan(final BluetoothAdapter state) {
        // First, check the Location permission. This is required on Marshmallow onwards in order
        // to scan for Bluetooth LE devices.
        if (Utils.isLocationPermissionsGranted(this)) {
            mNoLocationPermissionView.setVisibility(View.GONE);

            // Bluetooth must be enabled
            if (state.isEnabled()) {
                mNoBluetoothView.setVisibility(View.GONE);

                // register bluetooth receiver and start scanning for bluetooth devices
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                this.registerReceiver(bluetoothReceiver.getBroadcastReceiver(), filter);
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
                bluetoothAdapter.startDiscovery();

                // Inform the user a bluetooth scan has started
                Toast.makeText(MainActivity.this, "Bluetooth scan started", Toast.LENGTH_LONG).show();

                // Sleep to allow device to being discovering
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Sleep during scanning threw following InterruptedException: " + e);
                }

                if (!state.isDiscovering()) {
                    mEmptyView.setVisibility(View.VISIBLE);

                    if (!Utils.isLocationRequired(this) || Utils.isLocationEnabled(this)) {
                        mNoLocationView.setVisibility(View.INVISIBLE);
                    } else {
                        mNoLocationView.setVisibility(View.VISIBLE);
                        activityTitleTextView.setVisibility(View.GONE);
                    }
                } else {
                    mEmptyView.setVisibility(View.GONE);

                    // update views for scanning
                    activityTitleTextView.setText(R.string.scanning_for_devices);
                    bluetoothScanningProgressBar.setVisibility(View.VISIBLE);
                }
            } else {
                mNoBluetoothView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
                activityTitleTextView.setVisibility(View.GONE);
            }
        } else {
            mNoLocationPermissionView.setVisibility(View.VISIBLE);
            mNoBluetoothView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.GONE);
            activityTitleTextView.setVisibility(View.GONE);

            final boolean deniedForever = Utils.isLocationPermissionDeniedForever(this);
            mGrantPermissionButton.setVisibility(deniedForever ? View.GONE : View.VISIBLE);
            mPermissionSettingsButton.setVisibility(deniedForever ? View.VISIBLE : View.GONE);
        }
    }


    /* Transition to Communication functionality: button handling */


    /**
     * Initialises the select data button by assigning an OnClickListener. The select data button
     * ensures that only one device has been selected before connecting to a device and updating
     * the views to a connection state.
     */
    private void selectDeviceButton() {

        selectDeviceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Log.e(TAG, "onClick: here");

                // storing the application data
                List<DiscoveredBluetoothDevice> selectedDevices = retrieveSelectedDevices();

                // Ensure a device is selected
                if (selectedDevices.size() == 1) {

                    DiscoveredBluetoothDevice deviceForConnection = selectedDevices.get(0);
                    Log.i(TAG, "Connection with device initiating for device address: "
                            + deviceForConnection.getMacAddress());

                    activityTitleTextView.setText(R.string.connecting_to_device);
                    recyclerView.setVisibility(View.GONE);
                    selectDeviceButton.setVisibility(View.GONE);
                    bluetoothScanningProgressBar.setVisibility(View.VISIBLE);

                    connectDevice(deviceForConnection.getMacAddress());
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Please choose a (single) device",
                            Toast.LENGTH_LONG);
                    toast.show();
                }

            }
        });
    }


    private void sendMessageButton() {
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sentMessage = String.valueOf(messageToBeSent.getText());
                deviceInterface.sendMessage(sentMessage);


            }
        });
    }


    /**
     * Retrieves the selected devices to update app data with. Checks the checked status of each of
     * the DiscoveredBluetoothDevices in the RecyclerView adapter. It groups these to return the
     * selected devices
     * @return a list of the selected bluetooth devices
     */
    private List<DiscoveredBluetoothDevice> retrieveSelectedDevices() {

        List<DiscoveredBluetoothDevice> mDevices = new ArrayList<>();

        // update devices from application settings if set
        if (app.getDevices() != null) {
            mDevices = app.getDevices();
        }

        List<DiscoveredBluetoothDevice> selectedDevices = new ArrayList<>();

        Log.d(TAG, "mdevicesSize: " + String.valueOf(mDevices.size()));

        for (int i = 0; i < mDevices.size(); i++) {

            Log.d(TAG, "checked" + mDevices.get(i).getChecked().toString());
            if (mDevices.get(i).getChecked()) {
                selectedDevices.add(mDevices.get(i));
            }
        }
        Log.i(TAG, "Finished checked, devices selected: "
                + String.valueOf(selectedDevices.size()));

        return selectedDevices;
    }



    /* Android permissions Code */

    /**
     * A callback for a permissions request
     * @param requestCode the code indicating the type of permission requested
     * @param permissions the string names of permissions
     * @param grantResults the results of the permission request
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION:
                break;
        }
    }

    /**
     * A callback for accepting the location permission on the cannot see any bluetooth devices
     * view {@link layout/info_no_devices.xml}.
     */
    @OnClick(R.id.action_enable_location)
    public void onEnableLocationClicked() {
        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    /**
     * A callback for accepting the location permission on the enable bluetooth view
     * {@link layout/info_no_bluetooth.xml}.
     */
    @OnClick(R.id.action_enable_bluetooth)
    public void onEnableBluetoothClicked() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableIntent);
    }

    /**
     * A callback for accepting the location permission on the enable location view
     * {@link layout/info_no_permission.xml}.
     */
    @OnClick(R.id.action_grant_location_permission)
    public void onGrantLocationPermissionClicked() {
        Utils.markLocationPermissionRequested(this);
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_ACCESS_COARSE_LOCATION);
    }

    /**
     * A callback for accessing the phone settings when this is clicked on the location permission
     * view {@link layout/info_no_permission.xml}.
     */
    @OnClick(R.id.action_permission_settings)
    public void onPermissionSettingsClicked() {
        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }


    /* Bluetooth Serial Code */

    /**
     * Code used to connect to the device: initialising callbacks whilst observing the device
     * connection through bluetooth serial.
     * @param mac mac address of the devices
     */
    private void connectDevice(String mac) {
        bluetoothManager.openSerialDevice(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnected, this::onError);
    }


    /**
     * Callback which gets called when the device and phone connect. Used to update the views,
     * send a initial connection message and set listeners for handling messages to/from the device.
     * @param connectedDevice the connected bluetooth serial device instance
     */
    private void onConnected(BluetoothSerialDevice connectedDevice) {

        // TODO update the visibility of relevant activity views here
        activityTitleTextView.setVisibility(View.GONE);
        bluetoothScanningProgressBar.setVisibility(View.GONE);
        connectedDeviceComms.setVisibility(View.VISIBLE);
        sendMessageButton.setVisibility(View.VISIBLE);
        connectedDeviceCommunication.setVisibility(View.VISIBLE);
        connectedToDevice.setVisibility(View.VISIBLE);
        deviceNumber.setVisibility(View.VISIBLE);
        deviceNumber.setText(connectedDevice.getMac());
        lastMessageFromDevice.setVisibility(View.VISIBLE);
        noCurrentMessageReceived.setVisibility(View.VISIBLE);
        noLastMessageTimeKnown.setVisibility(View.VISIBLE);
        sendMessageToDevice.setVisibility(View.VISIBLE);
        messageToBeSent.setVisibility(View.VISIBLE);
        sendMessageButton.setVisibility(View.VISIBLE);
        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        deviceInterface = connectedDevice.toSimpleDeviceInterface();

        // Listen to bluetooth events
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError);

        // Let's send a message:
        deviceInterface.sendMessage("Hello world!");

    }

    /**
     * Callback for specifying additional functionality on the app when a message is sent to the
     * device.
     * @param message the message sent to the connected device
     */
    private void onMessageSent(String message) {
        // We sent a message! Handle it here.
        // TODO functionality for sending messages here
        Toast.makeText(context, "Message sent was: " + message, Toast.LENGTH_LONG).show();

    }

    /**
     * Callback for specifying additional functionality on the app when a message is received from
     * the device.
     * @param message the message sent to the connected device
     */

    private void onMessageReceived(String message) {
        // We received a message! Handle it here.
        // TODO functionality for receiving messages here
        noCurrentMessageReceived.setText(message);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy.MM.dd");
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss");
        String strTime = timeformat.format(calendar.getTime());
        String strDate = mdformat.format(calendar.getTime());
        noLastMessageTimeKnown.setText(strTime);
        boolean isGPSEnabled = false;
        boolean isNetworkEnabled = false;
        Location location = null;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Log.d(TAG,"HI hi hi hi hi");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG,"not HI hi hi hi hi");
            return;
        }
        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
            // Log.e(“Network-GPS”, “Disable”);
            Log.d(TAG,"Not enabled both");
        } else {
            Log.d(TAG,"enabled one");
            // First get location from Network Provider
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                // Log.e(“Network”, “Network”);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            } else
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                        //Log.e(“GPS Enabled”, “GPS Enabled”);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
        }
        String cityName = null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(latitude, longitude,1);
            if (addresses.size() > 0) {
                Log.d(TAG, String.valueOf(addresses));
                cityName = addresses.get(0).getThoroughfare();
                Log.d(TAG, String.valueOf(cityName));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //Criteria criteria = new Criteria();
        //String provider = locationManager.getBestProvider(criteria, false);
        //Location location = locationManager.getLastKnownLocation(provider);
        //if (location != null) {
        //  onLocationChanged(location);
        //} else {
        //  locationManager.requestSingleUpdate(provider, this, null);
        //}
        Log.d(TAG, String.valueOf(latitude)+" "+String.valueOf(longitude));

        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, (LocationListener) this);

        Map<String, Object> user = new HashMap<>();
        System.out.println(message);
        message = message.replace("{","").replace("}","");
        String[] arr = message.split(",");

        for(String str : arr){
            String[] splited = str.split(":");
            String[] values1 = splited[0].split("a");
            String[] values2 = splited[1].split("a");
            long[] long_values1 = new long[values1.length];
            long[] long_values2 = new long[values2.length];
            System.out.println(long_values1.length);
            for(int i = 0; i < values1.length; i++) {
                System.out.println(values1[i]);
                long_values1[i] = Long.parseLong(values1[i].trim());
            }
            for(int j = 0; j < values2.length; j++) {
                System.out.println(values2[j]);
                long_values2[j] = Long.parseLong(values2[j].trim());
            }
            //char[] ch1 = splited[0].toCharArray();
            //System.out.println(str);
            //System.out.println(ch1);

            String decrypted_string = decrypt(long_values1);
            String decrypted_value = decrypt(long_values2);
            if(decrypted_string == "Dust")
                decrypted_string = "Dust_Concentration";
            Log.d(TAG,decrypted_string);
            Log.d(TAG,splited[1]);
            Log.d(TAG,decrypted_value);
            //char[] ch2 = splited[1].toCharArray();
            //String decrypted_string2 = decrypt(ch2);
            //Log.d(TAG,decrypted_string2);
            user.put(decrypted_string,decrypted_value);

        }

        user.put("latitude",latitude);
        user.put("longitude", longitude);
        user.put("date",strDate);
        user.put("time",strTime);
        user.put("location",cityName);
        String path = "sensors_data/"+String.valueOf(strDate)+"_"+String.valueOf(strTime);
        DocumentReference db = FirebaseFirestore.getInstance().document(path);
        db.set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG,"Document is saved");
            }


        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }

    /**
     * Callback for specifying how errors errors should be handled when connecting to the device.
     * @param error the error message
     */
    private void onError(Throwable error) {
        // Handle the error
    }

    //encryption


}
