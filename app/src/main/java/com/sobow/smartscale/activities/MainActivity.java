package com.sobow.smartscale.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobow.smartscale.R;
import com.sobow.smartscale.dto.MeasurementDto;
import com.sobow.smartscale.dto.UserDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity
{
  private static final String TAG = "MainActivity";
  
  private static final int REQUEST_LOGIN = 0;
  private static final int REQUEST_BLUETOOTH = 1;
  private static final int REQUEST_USERDATA = 2;
  
  private static final String BASE_URL = "http://10.0.2.2:8080/v1";
  private static final String MEASUREMENT_CONTROLLER = "/measurement";
  
  
  private UserDto user;
  
  private List<MeasurementDto> measurements = new ArrayList<>();
  
  List<String> listView = new ArrayList<>();
  
  private OkHttpClient client = new OkHttpClient();
  private ObjectMapper mapper = new ObjectMapper();
  
  private ArrayAdapter arrayAdapter;
  
  // GUI components
  @BindView(R.id.lv_measurements)
  ListView lv_measurements;
  
  @BindView(R.id.btn_newMeasurement)
  Button btn_newMeasurement;
  
  @BindView(R.id.btn_userData)
  Button btn_userData;
  
  @BindView(R.id.btn_printChart)
  Button btn_printChart;
  
  @BindView(R.id.btn_logout)
  Button btn_logout;
  
  @BindView(R.id.tv_yourMeasurements)
  TextView tv_yourMeasurements;
  
  
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
  
    // Start login activity for result
    Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
    startActivityForResult(newIntent, REQUEST_LOGIN);
  
  
    // TODO: Implement sending http request for all user measurements. sort them by date and print out in list View
    // TODO: sent http request for userDto. extract from userDto measurementIds. send http request for all measurements.
  
  
    // TODO: filter data. po kliknięciu w przycik SHOW FILTERS w widoku głównym pojawią się nowe pola na filtry.
    //  np. pola na date początkową i datę końcową z jakiego okresu czasu mają być pokazywane dane. oraz przycisk APPLY FILTERS.
  
    // list view
  
  
    listView.add("2020-03-20 16:10:08    78.1 kg    BMI = 20.2");
    listView.add("2020-03-21 10:25:34    79.0 kg    BMI = 20.3");
    listView.add("2020-03-22 22:00:01    77.9 kg    BMI = 20.0");
    listView.add("2020-03-23 13:47:54    78.5 kg    BMI = 20.2");
    listView.add("2020-03-23 11:22:33    78.7 kg    BMI = 20.3");
  
  
    arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listView);
  
    lv_measurements.setAdapter(arrayAdapter);
  
  
    // buttons on click behavior
    btn_newMeasurement.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Intent newIntent = new Intent(getApplicationContext(), BluetoothActivity.class);
        newIntent.putExtra("user", user);
        startActivityForResult(newIntent, REQUEST_BLUETOOTH);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
      }
    });
  
    btn_userData.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Intent newIntent = new Intent(getApplicationContext(), UserDataActivity.class);
        newIntent.putExtra("user", user);
        startActivityForResult(newIntent, REQUEST_USERDATA);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
      }
    });
  
    btn_logout.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        // TODO: clear list view
        Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(newIntent, REQUEST_LOGIN);
      }
    });
  
    // TODO: impelement remaining buttons on click behavior
  }
  
  
  
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent)
  {
    super.onActivityResult(requestCode, resultCode, intent);
    
    if (requestCode == REQUEST_LOGIN)
    {
      if (resultCode == Activity.RESULT_OK)
      {
        user = (UserDto) intent.getSerializableExtra("user");
        tv_yourMeasurements.setText("Welcome " + user.getUserName() + "!\nYour measurements:");
        // map object to JSON string
        String userJsonString = "";
        try
        {
          userJsonString = mapper.writeValueAsString(user);
          Log.d(TAG, "Mapped User Json String = " + userJsonString);
        }
        catch (JsonProcessingException e)
        {
          e.printStackTrace();
        }
        // json request body
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), userJsonString);
  
  
        // sent http for measurements
  
        String requestUrl = BASE_URL + MEASUREMENT_CONTROLLER;
  
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
            MainActivity.this.runOnUiThread(new Runnable()
            {
              @Override
              public void run()
              {
                Toast.makeText(getBaseContext(), "Connection with server failed", Toast.LENGTH_LONG).show();
              }
            });
      
            e.printStackTrace();
          }
    
          @Override
          public void onResponse(Call call, Response response) throws IOException
          {
            if (response.isSuccessful())
            {
              String jsonString = response.body().string();
        
              measurements = Arrays.asList(mapper.readValue(jsonString, MeasurementDto[].class));
        
              MainActivity.this.runOnUiThread(new Runnable()
              {
                @Override
                public void run()
                {
                  listView.clear();
                  for (int i = 0; i < measurements.size(); i++)
                  {
                    listView.add(measurements.get(i).toString());
                  }
                  arrayAdapter.notifyDataSetChanged();
                  lv_measurements.invalidateViews();
                }
              });
            }
            else if (response.code() == 404)
            {
              MainActivity.this.runOnUiThread(new Runnable()
              {
                @Override
                public void run()
                {
            
                }
              });
            }
      
      
          }
    
        });
      }
      
    }
    else if (requestCode == REQUEST_BLUETOOTH)
    {
      if (resultCode == Activity.RESULT_OK)
      {
    
        // TODO: read measurement and append to list view
    
      }
    }
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
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
    if (id == R.id.action_settings)
    {
      return true;
    }
    
    return super.onOptionsItemSelected(item);
  }
}
