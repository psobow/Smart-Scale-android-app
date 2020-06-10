package com.sobow.smartscale.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sobow.smartscale.R;
import com.sobow.smartscale.activities.adapter.DeviceListAdapter;
import com.sobow.smartscale.services.BluetoothConnectionService;

import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BluetoothActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
  private static final String TAG = "BluetoothActivity";
  
  private BluetoothAdapter mBluetoothAdapter;
  private BluetoothConnectionService mBluetoothConnection;
  private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  private BluetoothDevice mBTDevice;
  private ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
  private DeviceListAdapter mDeviceListAdapter;
  
  private String userEmail = "";
  private String userPassword = "";
  
  // GUI components
  @BindView(R.id.lv_devices)
  ListView lv_devices;
  
  @BindView(R.id.btn_startConnection)
  Button btn_startConnection;
  
  @BindView(R.id.btn_bluetoothOnOff)
  Button btn_bluetoothOnOff;
  
  @BindView(R.id.btn_discoverableOnOff)
  Button btn_discoverableOnOff;
  
  @BindView(R.id.btn_discoverDevices)
  Button btn_discoverDevices;
  
  @BindView(R.id.tv_dataFromTheDevice)
  TextView tv_dataFromTheDevice;
  
  @BindView(R.id.btn_saveData)
  Button btn_saveData;
  
  @BindView(R.id.btn_backToMainActivity)
  Button btn_backToMainActivity;
  
  
  // BroadCast Receiver
  
  // Enabling bluetooth
  private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      String action = intent.getAction();
      if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED))
      {
        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
        
        switch (state)
        {
          case BluetoothAdapter.STATE_OFF:
            Log.d(TAG, "onReceive : STATE OFF");
            break;
          case BluetoothAdapter.STATE_TURNING_OFF:
            Log.d(TAG, "mBroadcastReceiver1 : STATE TURNING OFF");
            break;
          case BluetoothAdapter.STATE_ON:
            Log.d(TAG, "mBroadcastReceiver1 : STATE ON");
            break;
          case BluetoothAdapter.STATE_TURNING_ON:
            Log.d(TAG, "mBroadcastReceiver1 : STATE TURNING ON");
            break;
        }
      }
    }
  };
  
  // Searching for devices
  private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      String action = intent.getAction();
      if (action.equals(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
      {
        int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, mBluetoothAdapter.ERROR);
        
        switch (mode)
        {
          case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
            Log.d(TAG, "mBroadcastReceiver2 : Discoverability Enable.");
            break;
          case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
            Log.d(TAG, "mBroadcastReceiver2 : Discoverability Disable. Able to receive connections.");
            break;
          case BluetoothAdapter.SCAN_MODE_NONE:
            Log.d(TAG, "mBroadcastReceiver2 : Discoverability Disable. Not able to receive connections.");
            break;
          case BluetoothAdapter.STATE_CONNECTING:
            Log.d(TAG, "mBroadcastReceiver2 : Connecting ...");
            break;
          case BluetoothAdapter.STATE_CONNECTED:
            Log.d(TAG, "mBroadcastReceiver2 : Connected.");
            break;
        }
      }
    }
  };
  
  // If device has been found add it to list view
  private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      String action = intent.getAction();
      Log.d(TAG, "mBroadcastReceiver3: ACTION FOUND.");
      if (action.equals(BluetoothDevice.ACTION_FOUND))
      {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        mBTDevices.add(device);
        Log.d(TAG, "mBroadcastReceiver3: " + device.getName() + ": " + device.getAddress());
        mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
        lv_devices.setAdapter(mDeviceListAdapter);
      }
    }
  };
  
  // Pairing devices
  private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      final String action = intent.getAction();
      
      if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
      {
        BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED)
        {
          Log.d(TAG, "mBroadcastReceiver4: BOND BONDED.");
          mBTDevice = mDevice;
        }
        if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING)
        {
          Log.d(TAG, "mBroadcastReceiver4: BOND BONDING.");
        }
        if (mDevice.getBondState() == BluetoothDevice.BOND_NONE)
        {
          Log.d(TAG, "mBroadcastReceiver4: BOND NONE.");
        }
      }
    }
  };
  
  BroadcastReceiver mReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      tv_dataFromTheDevice.setText("");
      String text = intent.getStringExtra("theMessage");
      tv_dataFromTheDevice.setText(text);
    }
  };
  
  @Override
  protected void onDestroy()
  {
    Log.d(TAG, "onDestroy: called.");
    super.onDestroy();
    
    try
    {
      unregisterReceiver(mBroadcastReceiver1);
    }
    catch (Exception e)
    {
    }
    try
    {
      unregisterReceiver(mBroadcastReceiver2);
    }
    catch (Exception e)
    {
    }
    
    try
    {
      unregisterReceiver(mBroadcastReceiver3);
    }
    catch (Exception e)
    {
    
    }
    
    try
    {
      unregisterReceiver(mBroadcastReceiver4);
    }
    catch (Exception e)
    {
    
    }
    
    try
    {
      unregisterReceiver(mReceiver);
    }
    catch (Exception e)
    {
    
    }
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_bluetooth);
    ButterKnife.bind(this);
    
    mBTDevices = new ArrayList<>();
    
    LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
    
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    
    registerReceiver(mBroadcastReceiver4, filter);
    
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  
    // get email and password from main activity
    Bundle bundle = getIntent().getExtras();
    userEmail = bundle.getString("email");
    userPassword = bundle.getString("password");
    
    // OnClickListeners
    btn_bluetoothOnOff.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Log.d(TAG, "onClick: enabling/disabling bluetooth.");
        enableDisableBT();
      }
    });
    
    btn_discoverableOnOff.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Log.d(TAG, "onClick: making device discoverable for 300 seconds.");
        
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        
        IntentFilter IntentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, IntentFilter);
      }
    });
    
    btn_discoverDevices.setOnClickListener(new View.OnClickListener()
    {
      @RequiresApi(api = Build.VERSION_CODES.M)
      @Override
      public void onClick(View v)
      {
        Log.d(TAG, "onClick: looking for unpaired devices.");
        mBTDevices.clear();
        if (mBluetoothAdapter.isDiscovering())
        {
          mBluetoothAdapter.cancelDiscovery();
          Log.d(TAG, "onClick: canceling discovery.");
          
          mBluetoothAdapter.startDiscovery();
          IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
          registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        else
        {
          checkBTPermissions();
          
          mBluetoothAdapter.startDiscovery();
          IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
          registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
      }
    });
    
    lv_devices.setOnItemClickListener(BluetoothActivity.this);
    
    btn_startConnection.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
      }
    });
    
    btn_saveData.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
      
      }
    });
    
    btn_backToMainActivity.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
      }
    });
    
  }
  
  public void startBTConnection(BluetoothDevice device, UUID uuid)
  {
    Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
    
    mBluetoothConnection.startClient(device, uuid);
    
  }
  
  public void enableDisableBT()
  {
    if (mBluetoothAdapter == null)
    {
      Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
      Toast.makeText(getBaseContext(), "Device does not have Bluetooth capabilities", Toast.LENGTH_LONG).show();
      
    }
    else if (! mBluetoothAdapter.isEnabled())
    {
      Log.d(TAG, "enableDisableBT: enabling BT.");
      Toast.makeText(getBaseContext(), "Enabling Bluetooth", Toast.LENGTH_LONG).show();
      Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivity(enableBTIntent);
      
      IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
      registerReceiver(mBroadcastReceiver1, BTIntent);
    }
    else if (mBluetoothAdapter.isEnabled())
    {
      Log.d(TAG, "enableDisableBT: disabling BT.");
      Toast.makeText(getBaseContext(), "Disabling Bluetooth", Toast.LENGTH_LONG).show();
      mBluetoothAdapter.disable();
      
      IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
      registerReceiver(mBroadcastReceiver1, BTIntent);
    }
  }
  
  @RequiresApi(api = Build.VERSION_CODES.M)
  private void checkBTPermissions()
  {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
    {
      int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
      permissionCheck += this.checkSelfPermission("Manifest.permissions.ACCESS_COARSE_LOCATION");
      if (permissionCheck != 0)
      {
        this.requestPermissions(
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
      }
      else
      {
        Log.d(TAG, "checkBTPermissions: No need to check permissions.");
      }
    }
  }
  
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    mBluetoothAdapter.cancelDiscovery();
    Log.d(TAG, "onItemClick: You clicked any device.");
    String deviceName = mBTDevices.get(position).getName();
    String deviceAddress = mBTDevices.get(position).getAddress();
    Log.d(TAG, "onItemClick: deviceName = " + deviceName);
    Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);
    
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
    {
      Log.d(TAG, "Trying to pair with " + deviceName);
      mBTDevices.get(position).createBond();
      
      mBTDevice = mBTDevices.get(position);
      mBluetoothConnection = new BluetoothConnectionService(BluetoothActivity.this);
      
    }
    
  }
}
