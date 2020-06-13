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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.sobow.smartscale.R;
import com.sobow.smartscale.dto.MeasurementDto;
import com.sobow.smartscale.dto.UserDto;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
  private boolean isUserLogged = false;
  
  private List<MeasurementDto> measurements = new ArrayList<>();
  
  List<String> listView = new ArrayList<>();
  
  private OkHttpClient client = new OkHttpClient();
  private ObjectMapper mapper = new ObjectMapper();
  
  private ArrayAdapter arrayAdapter;
  
  private LocalDateTime oldestMeasurementDateTime;
  private LocalDateTime newestMeasurementDateTime;
  
  private LocalDate previousValidStartDate;
  private LocalDate previousValidEndDate;
  
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
  
  @BindView(R.id.btn_applyFilters)
  Button btn_applyFilters;
  
  @BindView(R.id.tv_yourMeasurements)
  TextView tv_yourMeasurements;
  
  @BindView(R.id.et_startDate)
  EditText et_startDate;
  
  @BindView(R.id.et_endDate)
  EditText et_endDate;
  
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    AndroidThreeTen.init(this);
  
    // Start login activity for result if user is not logged in
    if (! isUserLogged)
    {
      Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
      startActivityForResult(newIntent, REQUEST_LOGIN);
    }
    
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
        measurements.clear();
        listView.clear();
  
        oldestMeasurementDateTime = null;
        newestMeasurementDateTime = null;
  
        previousValidStartDate = null;
        previousValidEndDate = null;
  
        user = null;
  
        isUserLogged = false;
        
        Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(newIntent, REQUEST_LOGIN);
      }
    });
  
    btn_applyFilters.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        // Validate input
        String startDate = et_startDate.getText().toString();
        String endDate = et_endDate.getText().toString();
  
        // reset error messages
        et_startDate.setError(null);
        et_endDate.setError(null);
        
        boolean isInputValid = true;
  
        // Validate format and try to parse string to local date
        LocalDate startDateParsed = null;
        try
        {
          startDateParsed = LocalDate.parse(startDate, DateTimeFormatter.ofPattern(getString(R.string.date_format)));
        }
        catch (DateTimeParseException e)
        {
          isInputValid = false;
          et_startDate.setError("Enter valid start date! (" + getString(R.string.date_format) + ")");
        }
  
  
        LocalDate endDateParsed = null;
        try
        {
          endDateParsed = LocalDate.parse(endDate, DateTimeFormatter.ofPattern(getString(R.string.date_format)));
        }
        catch (DateTimeParseException e)
        {
          isInputValid = false;
          et_endDate.setError("Enter valid end date! (" + getString(R.string.date_format) + ")");
        }
  
  
        if (startDateParsed != null && endDateParsed != null
            && newestMeasurementDateTime != null && oldestMeasurementDateTime != null)
        {
          // START DATE
          // Forbid user to input start date before oldest measurement in database
          if (startDateParsed.isBefore(oldestMeasurementDateTime.toLocalDate()))
          {
            isInputValid = false;
            et_startDate.setError("Start date can't be before: " + oldestMeasurementDateTime.format(
                DateTimeFormatter.ofPattern(getString(R.string.date_format))));
          }
    
          // Forbid user to enter start date after the newest measurement in database
          else if (startDateParsed.isAfter(newestMeasurementDateTime.toLocalDate()))
          {
            isInputValid = false;
            et_startDate.setError("Start date can't be after: " + newestMeasurementDateTime.format(
                DateTimeFormatter.ofPattern(getString(R.string.date_format))));
          }
    
    
          // END DATE
          // Forbid user to input end date after the newest measurement in database
          if (endDateParsed.isAfter(newestMeasurementDateTime.toLocalDate()))
          {
            isInputValid = false;
            et_endDate.setError("End date can't be after: " + newestMeasurementDateTime.format(
                DateTimeFormatter.ofPattern(getString(R.string.date_format))));
          }
          // forbid user to input end date before oldest measurement in database
          else if (endDateParsed.isBefore(oldestMeasurementDateTime.toLocalDate()))
          {
            isInputValid = false;
            et_endDate.setError("End date can't be before: " + oldestMeasurementDateTime.format(
                DateTimeFormatter.ofPattern(getString(R.string.date_format))));
          }
    
          // in case if start date and end date are between oldest and newest
          // forbid user to input end date before start date and start date after end date
          if (isInputValid && endDateParsed.isBefore(startDateParsed))
          {
            isInputValid = false;
            et_endDate.setError("End date can't be before start date!");
            et_startDate.setError("Start date can't be after end date!");
          }
        }
        
        
        if (isInputValid)
        {
          Toast.makeText(getBaseContext(), "Filtering from: " + startDate + " to: " + endDate,
                         Toast.LENGTH_LONG).show();
  
          previousValidStartDate = startDateParsed;
          previousValidEndDate = endDateParsed;
        }
        else
        {
          setUpPreviousValidDateFilters();
          Toast.makeText(getBaseContext(), "Filter was not applied",
                         Toast.LENGTH_LONG).show();
        }
      }
    });
  
  
    // TODO: impelement remaining buttons on click behavior
  }
  
  private void setUpPreviousValidDateFilters()
  {
    // set up previous valid dates
    if (previousValidStartDate != null && previousValidEndDate != null)
    {
      et_startDate.setText(previousValidStartDate.format(
          DateTimeFormatter.ofPattern(getString(R.string.date_format))));
      et_endDate.setText(previousValidEndDate.format(
          DateTimeFormatter.ofPattern(getString(R.string.date_format))));
    }
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent)
  {
    super.onActivityResult(requestCode, resultCode, intent);
    
    if (requestCode == REQUEST_LOGIN)
    {
      if (resultCode == Activity.RESULT_OK)
      {
        isUserLogged = true;
        user = (UserDto) intent.getSerializableExtra("user");
        tv_yourMeasurements.setText(getString(R.string.welcome_username_your_measurements, user.getUserName()));
        sentPostForMeasurementsAndUpdateListView();
      }
      
    }
    else if (requestCode == REQUEST_BLUETOOTH)
    {
      if (resultCode == Activity.RESULT_OK)
      {
        sentPostForMeasurementsAndUpdateListView();
      }
    }
  }
  
  void sentPostForMeasurementsAndUpdateListView()
  {
    // map user object to JSON string
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
    
    // create json request body
    RequestBody body = RequestBody.create(MediaType.parse(getString(R.string.json_media_type)), userJsonString);
    
    
    // concat URL
    String requestUrl = BASE_URL + MEASUREMENT_CONTROLLER;
    
    // build post request with body
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
          
          // sort by date time. date closest to present day will be shown at top
          Collections.sort(measurements, new CustomComparator());
  
          // update list view and Start Date / End Date
          MainActivity.this.runOnUiThread(new Runnable()
          {
            @Override
            public void run()
            {
              listView.clear();
  
              // disable errors
              et_startDate.setError(null);
              et_endDate.setError(null);
  
              if (measurements.isEmpty())
              {
                listView.add("You haven't added any measurements yet.");
                oldestMeasurementDateTime = null;
                newestMeasurementDateTime = null;
                previousValidEndDate = null;
                previousValidStartDate = null;
  
                // update UI filters start date and end date
                et_startDate.setText("");
                et_endDate.setText("");
              }
              else
              {
                for (int i = 0; i < measurements.size(); i++)
                {
                  listView.add(measurements.get(i).toString());
                }
    
                // update list view
                arrayAdapter.notifyDataSetChanged();
                lv_measurements.invalidateViews();
    
                // update oldest and newest date measurement
                oldestMeasurementDateTime = measurements.get(measurements.size() - 1).getLocalDateTime();
                newestMeasurementDateTime = measurements.get(0).getLocalDateTime();
    
                // set up previous valid dates
                previousValidStartDate = oldestMeasurementDateTime.toLocalDate();
                previousValidEndDate = newestMeasurementDateTime.toLocalDate();
    
                // update UI filters start date and end date
                et_startDate.setText(oldestMeasurementDateTime.format(
                    DateTimeFormatter.ofPattern(getString(R.string.date_format))));
    
                et_endDate.setText(newestMeasurementDateTime.format(
                    DateTimeFormatter.ofPattern(getString(R.string.date_format))));
              }
            }
          });
        }
      }
    });
  }
  
  // comparator for sorting measurements by date time
  private class CustomComparator implements Comparator<MeasurementDto>
  {
    @Override
    public int compare(MeasurementDto o1, MeasurementDto o2)
    {
      return o2.getLocalDateTime().compareTo(o1.getLocalDateTime());
    }
  }
  
  @Override
  public void onSaveInstanceState(Bundle savedInstanceState)
  {
    super.onSaveInstanceState(savedInstanceState);
    // Save UI state changes to the savedInstanceState.
    // This bundle will be passed to onCreate if the process is
    // killed and restarted.
    
    
  }
  
  // options menu
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
