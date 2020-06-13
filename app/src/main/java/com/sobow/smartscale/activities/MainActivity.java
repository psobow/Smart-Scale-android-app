package com.sobow.smartscale.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
  
  // ACTIVITY CODES
  private static final int REQUEST_LOGIN = 0;
  private static final int REQUEST_BLUETOOTH = 1;
  private static final int REQUEST_USERDATA = 2;
  
  // API END POINT TODO: move this to some config class
  private static final String BASE_URL = "http://10.0.2.2:8080/v1";
  private static final String MEASUREMENT_CONTROLLER = "/measurement";
  
  // dependencies
  private OkHttpClient client;
  private ObjectMapper mapper;
  private ArrayAdapter arrayAdapter; // need for list view component
  
  // user information
  private UserDto user;
  private List<String> stringListWithMeasurements;
  private boolean isUserLogged;
  
  // measurements information
  private LocalDateTime oldestMeasurementDateTime;
  private LocalDateTime newestMeasurementDateTime;
  
  // valid date filters
  private LocalDate previousValidStartDateFilter;
  private LocalDate previousValidEndDateFilter;
  
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
  
    init();
    
    // Start login activity for result if user is not logged in
    if (! isUserLogged)
    {
      Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
      startActivityForResult(newIntent, REQUEST_LOGIN);
    }
    
    // buttons on click behavior
    btn_newMeasurement.setOnClickListener(
        v ->
        {
          Intent newIntent = new Intent(getApplicationContext(), BluetoothActivity.class);
          newIntent.putExtra("user", user);
          startActivityForResult(newIntent, REQUEST_BLUETOOTH);
          overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });
  
    btn_userData.setOnClickListener(
        v ->
        {
          Intent newIntent = new Intent(getApplicationContext(), UserDataActivity.class);
          newIntent.putExtra("user", user);
          startActivityForResult(newIntent, REQUEST_USERDATA);
          overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });
  
    btn_logout.setOnClickListener(
        v ->
        {
          init();
          Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
          startActivityForResult(newIntent, REQUEST_LOGIN);
        });
  
    btn_applyFilters.setOnClickListener(
        v ->
        {
          // Validate input
          String startDate = et_startDate.getText().toString();
          String endDate = et_endDate.getText().toString();
        
          // reset error messages
          resetFilterErrors();
        
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
              et_startDate.setError("Start date can't be before: " + oldestMeasurementDateTime
                  .format(DateTimeFormatter.ofPattern(getString(R.string.date_format))));
            }
          
            // Forbid user to enter start date after the newest measurement in database
            else if (startDateParsed.isAfter(newestMeasurementDateTime.toLocalDate()))
            {
              isInputValid = false;
              et_startDate.setError("Start date can't be after: " + newestMeasurementDateTime
                  .format(DateTimeFormatter.ofPattern(getString(R.string.date_format))));
            }
          
          
            // END DATE
            // Forbid user to input end date after the newest measurement in database
            if (endDateParsed.isAfter(newestMeasurementDateTime.toLocalDate()))
            {
              isInputValid = false;
              et_endDate.setError("End date can't be after: " + newestMeasurementDateTime
                  .format(DateTimeFormatter.ofPattern(getString(R.string.date_format))));
            }
            // forbid user to input end date before oldest measurement in database
            else if (endDateParsed.isBefore(oldestMeasurementDateTime.toLocalDate()))
            {
              isInputValid = false;
              et_endDate.setError("End date can't be before: " + oldestMeasurementDateTime
                  .format(DateTimeFormatter.ofPattern(getString(R.string.date_format))));
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
            Toast.makeText(getBaseContext(), "Filtering from: " + startDate + " to: " + endDate, Toast.LENGTH_LONG)
                 .show();
          
            previousValidStartDateFilter = startDateParsed;
            previousValidEndDateFilter = endDateParsed;
          }
          else
          {
            setUpPreviousValidDateFilters();
            Toast.makeText(getBaseContext(), "Filter was not applied", Toast.LENGTH_LONG).show();
          }
        });
    
    // TODO: implement remaining buttons on click behavior
  }
  
  private void init()
  {
    // clear focus
    getWindow().getDecorView().clearFocus();
    
    // init dependencies
    client = new OkHttpClient();
    mapper = new ObjectMapper();
    
    resetUser();
    
    resetListView();
    
    resetOldestAndNewestMeasurement();
    
    resetPreviousValidDateFilters();
  }
  
  private void resetPreviousValidDateFilters()
  {
    previousValidStartDateFilter = null;
    previousValidEndDateFilter = null;
  }
  
  private void resetOldestAndNewestMeasurement()
  {
    oldestMeasurementDateTime = null;
    newestMeasurementDateTime = null;
  }
  
  private void resetListView()
  {
    stringListWithMeasurements = new ArrayList<>();
    arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, stringListWithMeasurements);
    lv_measurements.setAdapter(arrayAdapter);
  }
  
  private void resetUser()
  {
    user = null;
    isUserLogged = false;
  }
  
  private void setUpPreviousValidDateFilters()
  {
    // set up previous valid dates
    if (previousValidStartDateFilter != null && previousValidEndDateFilter != null)
    {
      et_startDate.setText(previousValidStartDateFilter.format(DateTimeFormatter.ofPattern(getString(R.string.date_format))));
      et_endDate.setText(previousValidEndDateFilter.format(DateTimeFormatter.ofPattern(getString(R.string.date_format))));
    }
  }
  
  private void resetFilterErrors()
  {
    et_startDate.setError(null);
    et_endDate.setError(null);
  }
  
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent)
  {
    super.onActivityResult(requestCode, resultCode, intent);
  
    // clear focus
    getWindow().getDecorView().clearFocus();
    
    if (requestCode == REQUEST_LOGIN)
    {
      if (resultCode == Activity.RESULT_OK)
      {
        isUserLogged = true;
        user = (UserDto) intent.getSerializableExtra("user");
        tv_yourMeasurements.setText(getString(R.string.welcome_username_your_measurements,
                                              (user == null ? "null_user" : user.getUserName())));
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
        MainActivity.this.runOnUiThread(
            () -> Toast.makeText(getBaseContext(), "Connection with server failed", Toast.LENGTH_LONG).show());
        
        e.printStackTrace();
      }
      
      @Override
      public void onResponse(Call call, Response response) throws IOException
      {
        if (response.isSuccessful())
        {
          String jsonString = response.body().string();
  
          List<MeasurementDto> measurements = Arrays.asList(mapper.readValue(jsonString, MeasurementDto[].class));
          stringListWithMeasurements.clear();
  
          // sort by date time
          // date closest to present day will be shown at top place in list view
          Collections.sort(measurements, new CustomComparator());
  
          // update list view and Start Date / End Date
          MainActivity.this.runOnUiThread(
              () ->
              {
                resetFilterErrors();
        
                if (measurements.isEmpty())
                {
                  stringListWithMeasurements.add("You haven't added any measurements yet.");
          
                  resetOldestAndNewestMeasurement();
          
                  resetPreviousValidDateFilters();
          
                  // update UI filters start date and end date
                  et_startDate.setText("");
                  et_endDate.setText("");
                }
                else
                {
                  for (int i = 0; i < measurements.size(); i++)
                  {
                    stringListWithMeasurements.add(measurements.get(i).toString());
                  }
          
                  // update list view
                  arrayAdapter.notifyDataSetChanged();
                  lv_measurements.invalidateViews();
          
                  // update oldest and newest date measurement
                  oldestMeasurementDateTime = measurements.get(measurements.size() - 1).getLocalDateTime();
                  newestMeasurementDateTime = measurements.get(0).getLocalDateTime();
          
                  // set up previous valid dates
                  previousValidStartDateFilter = oldestMeasurementDateTime.toLocalDate();
                  previousValidEndDateFilter = newestMeasurementDateTime.toLocalDate();
          
                  // update UI filters start date and end date
                  et_startDate.setText(oldestMeasurementDateTime.format(DateTimeFormatter.ofPattern(getString(R.string.date_format))));
                  et_endDate.setText(newestMeasurementDateTime.format(DateTimeFormatter.ofPattern(getString(R.string.date_format))));
                }
              });
        }
        else
        {
          Toast.makeText(getBaseContext(), "Something went wrong response code: " + response.code(), Toast.LENGTH_LONG)
               .show();
          Log.d(TAG, "response code = " + response.code());
        }
      }
    });
  }
  
  
  // comparator for sorting measurements by date time descending
  private class CustomComparator implements Comparator<MeasurementDto>
  {
    @Override
    public int compare(MeasurementDto o1, MeasurementDto o2)
    {
      return o2.getLocalDateTime().compareTo(o1.getLocalDateTime());
    }
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
