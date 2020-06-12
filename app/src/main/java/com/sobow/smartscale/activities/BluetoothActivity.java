package com.sobow.smartscale.activities;

import android.Manifest;
import android.app.ProgressDialog;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobow.smartscale.R;
import com.sobow.smartscale.activities.adapter.DeviceListAdapter;
import com.sobow.smartscale.dto.MeasurementDto;
import com.sobow.smartscale.dto.UserDto;
import com.sobow.smartscale.services.BluetoothConnectionService;

import org.threeten.bp.LocalDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BluetoothActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
  private static final String TAG = "BluetoothActivity";
  
  private static final String BASE_URL = "http://10.0.2.2:8080/v1";
  private static final String MEASUREMENT_CONTROLLER = "/measurement";
  
  private BluetoothAdapter bluetoothAdapter;
  private BluetoothConnectionService bluetoothConnectionService;
  private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  private BluetoothDevice bluetoothDevice;
  private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
  private DeviceListAdapter deviceListAdapter;
  
  private UserDto user;
  private String userWeightFromScale = "";
  private List<MeasurementDto> newMeasurements = new ArrayList<>();
  
  private ObjectMapper mapper = new ObjectMapper();
  private OkHttpClient client = new OkHttpClient();
  
  // GUI components
  @BindView(R.id.lv_devices)
  ListView lv_devices;
  
  @BindView(R.id.btn_startConnection)
  Button btn_startConnection;
  
  @BindView(R.id.btn_bluetoothOnOff)
  Button btn_bluetoothOnOff;
  
  @BindView(R.id.btn_enableVisibility)
  Button btn_enableVisibility;
  
  @BindView(R.id.btn_discoverDevices)
  Button btn_discoverDevices;
  
  @BindView(R.id.tv_dataFromTheDevice)
  TextView tv_dataFromTheDevice;
  
  @BindView(R.id.btn_saveData)
  Button btn_saveData;
  
  @BindView(R.id.btn_backToMainActivity)
  Button btn_backToMainActivity;
  
  @BindView(R.id.btn_simulateScale)
  Button btn_simulateScale;
  
  
  // BroadCast Receiver
  
  // Enabling bluetooth
  private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      String action = intent.getAction();
      if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED))
      {
        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);
        
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
      if (action.equals(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
      {
        int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, bluetoothAdapter.ERROR);
        
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
        bluetoothDevices.add(device);
        Log.d(TAG, "mBroadcastReceiver3: " + device.getName() + ": " + device.getAddress());
        deviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, bluetoothDevices);
        lv_devices.setAdapter(deviceListAdapter);
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
          bluetoothDevice = mDevice;
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
      userWeightFromScale = intent.getStringExtra("theMessage");
      tv_dataFromTheDevice.setText("Data from the device: " + userWeightFromScale + " kg.");
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
  
    bluetoothDevices = new ArrayList<>();
    
    LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
    
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    
    registerReceiver(mBroadcastReceiver4, filter);
  
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  
    user = (UserDto) getIntent().getSerializableExtra("user");
    
    // OnClickListeners
    btn_bluetoothOnOff.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Log.d(TAG, "onClick: enabling/disabling bluetooth.");
        // TODO: simplify button behavior to simply enable bluetooth if disabled
        enableDisableBT();
      }
    });
  
    btn_enableVisibility.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Log.d(TAG, "onClick: making device discoverable for 300 seconds.");
        
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
  
        IntentFilter IntentFilter = new IntentFilter(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
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
        bluetoothDevices.clear();
        if (bluetoothAdapter.isDiscovering())
        {
          bluetoothAdapter.cancelDiscovery();
          Log.d(TAG, "onClick: canceling discovery.");
  
          bluetoothAdapter.startDiscovery();
          IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
          registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        else
        {
          checkBTPermissions();
  
          bluetoothAdapter.startDiscovery();
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
        startBTConnection(bluetoothDevice, MY_UUID_INSECURE);
      }
    });
    
    btn_saveData.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        // TODO: implement functionality. remember to sent post request to /create endpoint!
        // TODO: create const instead of Data from the device:
        if (! tv_dataFromTheDevice.getText().toString().equals("Data from the device:"))
        {
  
          btn_saveData.setEnabled(false);
  
          final ProgressDialog progressDialog = new ProgressDialog(BluetoothActivity.this,
                                                                   R.style.AppTheme_Dark_Dialog);
          progressDialog.setIndeterminate(true);
          progressDialog.setMessage("Sending data to server...");
          progressDialog.show();
  
          new android.os.Handler().postDelayed(
              new Runnable()
              {
                public void run()
                {
                  MeasurementDto newMeasurement = new MeasurementDto();
                  newMeasurement.setLocalDateTime(LocalDateTime.now());
                  double weight = Double.parseDouble(userWeightFromScale);
                  newMeasurement.setWeight(weight);
                  double height = user.getHeight() / 100.0;
                  newMeasurement.setBMI(weight / (height * height));
                  newMeasurement.setUserId(user.getIdFromServer());
          
                  // map object to JSON string
                  String measurementJsonString = "";
                  try
                  {
                    measurementJsonString = mapper.writeValueAsString(newMeasurement);
                    Log.d(TAG, "Mapped Json String = " + measurementJsonString);
                  }
                  catch (JsonProcessingException e)
                  {
                    e.printStackTrace();
                  }
          
                  // json request body
                  RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                                                        measurementJsonString);
          
                  // sent post for measurements
          
                  String requestUrl = BASE_URL + MEASUREMENT_CONTROLLER + "/create";
          
                  Request request = new Request.Builder()
                      .url(requestUrl)
                      .post(body)
                      .build();
          
                  // Execute HTTP requests in background thread
                  client.newCall(request).enqueue(new Callback()
                  {
                    @Override
                    public void onFailure(Call call, IOException e)
                    {
                      BluetoothActivity.this.runOnUiThread(new Runnable()
                      {
                        @Override
                        public void run()
                        {
                          Toast.makeText(getBaseContext(), "Connection with server failed", Toast.LENGTH_LONG).show();
                          btn_saveData.setEnabled(true);
                        }
                      });
              
                      e.printStackTrace();
                    }
            
                    @Override
                    public void onResponse(Call call, Response response) throws IOException
                    {
                      if (response.isSuccessful())
                      {
                
                
                        BluetoothActivity.this.runOnUiThread(new Runnable()
                        {
                          @Override
                          public void run()
                          {
                            btn_saveData.setEnabled(true);
                            Toast.makeText(getBaseContext(), "Data successfully saved!", Toast.LENGTH_LONG).show();
                          }
                        });
                      }
              
              
                    }
            
                  });
                  progressDialog.dismiss();
                }
              }, 3000);
        }
        else
        {
          Toast.makeText(getBaseContext(), "Wait for data from scale", Toast.LENGTH_LONG).show();
        }
  
      }
  
  
    });
  
    btn_simulateScale.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        userWeightFromScale = "75.0";
        tv_dataFromTheDevice.setText("Data from the device: " + userWeightFromScale + " kg.");
      }
    });
    
    
    btn_backToMainActivity.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        setResult(RESULT_OK);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
      }
    });
    
  }
  
  
  public void startBTConnection(BluetoothDevice device, UUID uuid)
  {
    Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
  
    bluetoothConnectionService.startClient(device, uuid);
    
  }
  
  public void enableDisableBT()
  {
    if (bluetoothAdapter == null)
    {
      Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
      Toast.makeText(getBaseContext(), "Device does not have Bluetooth capabilities", Toast.LENGTH_LONG).show();
      
    }
    else if (! bluetoothAdapter.isEnabled())
    {
      Log.d(TAG, "enableDisableBT: enabling BT.");
      Toast.makeText(getBaseContext(), "Enabling Bluetooth", Toast.LENGTH_LONG).show();
      Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivity(enableBTIntent);
      
      IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
      registerReceiver(mBroadcastReceiver1, BTIntent);
    }
    else if (bluetoothAdapter.isEnabled())
    {
      Log.d(TAG, "enableDisableBT: disabling BT.");
      Toast.makeText(getBaseContext(), "Disabling Bluetooth", Toast.LENGTH_LONG).show();
      bluetoothAdapter.disable();
      
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
    bluetoothAdapter.cancelDiscovery();
    Log.d(TAG, "onItemClick: You clicked any device.");
    String deviceName = bluetoothDevices.get(position).getName();
    String deviceAddress = bluetoothDevices.get(position).getAddress();
    Log.d(TAG, "onItemClick: deviceName = " + deviceName);
    Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);
    
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
    {
      Log.d(TAG, "Trying to pair with " + deviceName);
      bluetoothDevices.get(position).createBond();
  
      bluetoothDevice = bluetoothDevices.get(position);
      bluetoothConnectionService = new BluetoothConnectionService(BluetoothActivity.this);
      
    }
    
  }
}
