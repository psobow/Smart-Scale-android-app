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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.sobow.smartscale.R;
import com.sobow.smartscale.activities.results.CustomActivityResultCodes;
import com.sobow.smartscale.config.WebConfig;
import com.sobow.smartscale.dto.MeasurementDto;
import com.sobow.smartscale.dto.UserDto;
import com.sobow.smartscale.mapper.CustomMapper;

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


// TODO: create mapper class implement Map user to json. map json to User object etc.


// TODO: implement functionality to remove given measurement from the list by pressing it. display confirmation dialog before removing.


// TODO: implement feature for sending request for validation constraints on each field. Constraints will be stored in one place then. less important
// TODO: find better way to handle requests in background thread. how to wait for the response? less important


public class MainActivity extends AppCompatActivity
{
  private static final String TAG = "MainActivity";
  
  // ACTIVITY CODES
  private static final int REQUEST_LOGIN = 0;
  private static final int REQUEST_BLUETOOTH = 1;
  private static final int REQUEST_USERDATA = 2;
  
  // dependencies
  private OkHttpClient client;
  private CustomMapper mapper;
  private WebConfig webConfig;
  
  // date format
  DateTimeFormatter dateTimeFormatter;
  
  // user information
  private UserDto user;
  private ArrayList<MeasurementDto> allMeasurements;
  
  // list view
  private ArrayList<String> stringListWithMeasurements;
  private ArrayAdapter arrayAdapter; // need for list view component
  
  // oldest and newest measurements date time. These fields are need for proper filtering workflow
  private LocalDateTime oldestMeasurementDateTime;
  private LocalDateTime newestMeasurementDateTime;
  // valid date filters
  private LocalDate previousValidStartDateFilter;
  private LocalDate previousValidEndDateFilter;
  
  
  // GUI components
  @BindView(R.id.sv_main)
  ScrollView sv_main;
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
  @BindView(R.id.btn_resetFilters)
  Button btn_resetFilters;
  @BindView(R.id.tv_yourMeasurements)
  TextView tv_yourMeasurements;
  @BindView(R.id.et_startDate)
  EditText et_startDate;
  @BindView(R.id.et_endDate)
  EditText et_endDate;
  
  // need to set up hint properly in the source code
  @BindView(R.id.til_startDate)
  TextInputLayout til_startDate;
  @BindView(R.id.til_endDate)
  TextInputLayout til_endDate;
  
  
  // App initialization
  private void init()
  {
    clearFocusAndScrollViewToTheTop();
    
    // init dependencies
    client = new OkHttpClient();
    mapper = new CustomMapper();
    webConfig = new WebConfig();
    
    dateTimeFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_format));
    
    resetUserInformation();
    
    resetListView();
    
    resetOldestAndNewestMeasurement();
    
    resetFiltersTextAndHintAndError();
    
    resetPreviousValidFilterDates();
  }
  
  private void clearFocusAndScrollViewToTheTop()
  {
    // clear focus
    getWindow().getDecorView().clearFocus();
    
    // scroll to top
    sv_main.fullScroll(ScrollView.FOCUS_UP);
  
    lv_measurements.setSelectionAfterHeaderView();
  }
  
  private void resetPreviousValidFilterDates()
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
  
  private void resetUserInformation()
  {
    user = null;
    allMeasurements = new ArrayList<>();
  }
  
  // App restore state
  
  @Override
  public void onSaveInstanceState(Bundle bundle)
  {
    super.onSaveInstanceState(bundle);
    // Save UI state changes to the bundle.
    // This bundle will be passed to onCreate if the process is
    // killed and restarted.
    
    bundle.putSerializable("user", user);
    
    bundle.putSerializable("allMeasurements", allMeasurements);
    
    bundle.putSerializable("stringListWithMeasurements", stringListWithMeasurements);
    
    bundle.putSerializable("oldestMeasurementDateTime", oldestMeasurementDateTime);
    bundle.putSerializable("newestMeasurementDateTime", newestMeasurementDateTime);
    
    bundle.putSerializable("previousValidStartDateFilter", previousValidStartDateFilter);
    bundle.putSerializable("previousValidEndDateFilter", previousValidEndDateFilter);
  }
  
  @Override
  public void onRestoreInstanceState(Bundle bundle)
  {
    super.onRestoreInstanceState(bundle);
    // Restore UI state from the bundle.
    // This bundle has also been passed to onCreate.
    
    clearFocusAndScrollViewToTheTop();
    
    // init dependencies
    client = new OkHttpClient();
    mapper = new CustomMapper();
    webConfig = new WebConfig();
    
    dateTimeFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_format));
    
    user = (UserDto) bundle.getSerializable("user");
    tv_yourMeasurements.setText(getString(R.string.hello_user_name_your_measurements,
                                          (user == null ? "null_user" : user.getUserName())));
    
    allMeasurements = (ArrayList<MeasurementDto>) bundle.getSerializable("allMeasurements");
    
    // Restore list view
    stringListWithMeasurements = (ArrayList<String>) bundle.getSerializable("stringListWithMeasurements");
    arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, stringListWithMeasurements);
    lv_measurements.setAdapter(arrayAdapter);
    
    oldestMeasurementDateTime = (LocalDateTime) bundle.getSerializable("oldestMeasurementDateTime");
    newestMeasurementDateTime = (LocalDateTime) bundle.getSerializable("newestMeasurementDateTime");
    
    previousValidStartDateFilter = (LocalDate) bundle.getSerializable("previousValidStartDateFilter");
    previousValidEndDateFilter = (LocalDate) bundle.getSerializable("previousValidEndDateFilter");
    
    
    setUpPreviousValidDateInEditTextFilters();
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    AndroidThreeTen.init(this);
  
    if (savedInstanceState == null)
    {
      resetMainAndStartLogin();
    }
    // if savedInstanceState != null then onRestoreState method will restore previous app state
  
  
    // lv measurements on click behavior // TODO: implement removing measurement after click. Ask for confirmation before removing.
    lv_measurements.setOnItemClickListener(
        (parent, view, position, id) ->
        {
        
        });
    
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
  
    btn_printChart.setOnClickListener(
        v ->
        {
          Toast.makeText(getBaseContext(), "To be implemented...", Toast.LENGTH_LONG).show();
        });
    
    btn_logout.setOnClickListener(
        v ->
        {
          resetMainAndStartLogin();
        });
  
    btn_applyFilters.setOnClickListener(
        v ->
        {
          // TODO: move this logic to InputValidator
          // TODO: create methods isDateBefore isDateAfter
          // Validate input
          String startDate = et_startDate.getText().toString();
          String endDate = et_endDate.getText().toString();
  
          // reset error flags
          resetFilterErrors();
  
          boolean isInputValid = true;
  
          // Validate format and try to parse string to local date
          LocalDate startDateParsed = null;
          try
          {
            startDateParsed = LocalDate.parse(startDate, dateTimeFormatter);
          }
          catch (DateTimeParseException e)
          {
            isInputValid = false;
            et_startDate.setError(getString(R.string.invalid_start_date, getString(R.string.date_format)));
          }
  
          LocalDate endDateParsed = null;
          try
          {
            endDateParsed = LocalDate.parse(endDate, dateTimeFormatter);
          }
          catch (DateTimeParseException e)
          {
            isInputValid = false;
            et_endDate.setError(getString(R.string.invalid_end_date, getString(R.string.date_format)));
          }
  
  
          if (startDateParsed != null && endDateParsed != null
              && newestMeasurementDateTime != null && oldestMeasurementDateTime != null)
          {
            // START DATE
            // Forbid user to input start date before oldest measurement in database
            if (startDateParsed.isBefore(oldestMeasurementDateTime.toLocalDate()))
            {
              isInputValid = false;
              et_startDate.setError(getString(R.string.start_date_cant_be_before, oldestMeasurementDateTime
                  .format(dateTimeFormatter)));
            }

            // Forbid user to enter start date after the newest measurement in database
            else if (startDateParsed.isAfter(newestMeasurementDateTime.toLocalDate()))
            {
              isInputValid = false;
              et_startDate.setError(getString(R.string.start_date_cant_be_after, newestMeasurementDateTime
                  .format(dateTimeFormatter)));
            }
            else
            {
              et_startDate.setError(null);
            }
  
  
            // END DATE
            // forbid user to input end date before oldest measurement in database
            if (endDateParsed.isBefore(oldestMeasurementDateTime.toLocalDate()))
            {
              isInputValid = false;
              et_endDate.setError(getString(R.string.end_date_cant_be_before, oldestMeasurementDateTime
                  .format(dateTimeFormatter)));
            }
            // Forbid user to input end date after the newest measurement in database
            else if (endDateParsed.isAfter(newestMeasurementDateTime.toLocalDate()))
            {
              isInputValid = false;
              et_endDate.setError(getString(R.string.end_date_cant_be_after, newestMeasurementDateTime
                  .format(dateTimeFormatter)));
            }
            else
            {
              et_endDate.setError(null);
            }
  
  
            // in case if start date and end date are between oldest and newest
            // forbid user to input end date before start date and start date after end date
            if (isInputValid && endDateParsed.isBefore(startDateParsed))
            {
              isInputValid = false;
              et_endDate.setError(getString(R.string.end_date_before_start_date));
              et_startDate.setError(getString(R.string.start_date_after_end_date));
            }
  
          }
  
  
          if (isInputValid)
          {
            if (! allMeasurements.isEmpty())
            {
              Toast.makeText(getBaseContext(),
                             getString(R.string.filtered_from_to, startDate, endDate),
                             Toast.LENGTH_LONG)
                   .show();
  
              previousValidStartDateFilter = startDateParsed;
              previousValidEndDateFilter = endDateParsed;
  
              List<MeasurementDto> filteredMeasurements = getMeasurementsFromTimeSpan(startDateParsed, endDateParsed);
              updateListView(filteredMeasurements);
            }
            else
            {
              Toast.makeText(getBaseContext(),
                             getString(R.string.no_data_to_filter),
                             Toast.LENGTH_LONG)
                   .show();
            }
          }
          else
          {
            setUpPreviousValidDateInEditTextFilters();
            Toast.makeText(getBaseContext(), R.string.filters_were_not_applied, Toast.LENGTH_LONG).show();
          }
  
        });
  
    btn_resetFilters.setOnClickListener(
        v ->
        {
          if (! allMeasurements.isEmpty())
          {
            updateListView(allMeasurements);
            Toast.makeText(getBaseContext(),
                           getString(R.string.filtered_from_to,
                                     oldestMeasurementDateTime.format(dateTimeFormatter),
                                     newestMeasurementDateTime.format(dateTimeFormatter)),
                           Toast.LENGTH_LONG)
                 .show();
          }
          else
          {
            Toast.makeText(getBaseContext(),
                           getString(R.string.no_data_to_filter),
                           Toast.LENGTH_LONG)
                 .show();
          }
        });
  }
  
  private void resetMainAndStartLogin()
  {
    init();
    Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
    startActivityForResult(newIntent, REQUEST_LOGIN);
  }
  
  private List<MeasurementDto> getMeasurementsFromTimeSpan(LocalDate startDate, LocalDate endDate)
  {
    List<MeasurementDto> result = new ArrayList<>();
    
    for (MeasurementDto measurement : allMeasurements)
    {
      LocalDate measurementDate = measurement.getLocalDateTime().toLocalDate();
      if (measurementDate.equals(startDate)
          || measurementDate.equals(endDate)
          || measurementDate.isAfter(startDate) && measurementDate.isBefore(endDate))
      {
        result.add(measurement);
      }
    }
    
    return result;
  }
  
  private void setUpPreviousValidDateInEditTextFilters()
  {
    // set up previous valid dates
    if (previousValidStartDateFilter != null && previousValidEndDateFilter != null)
    {
      et_startDate.setText(previousValidStartDateFilter.format(dateTimeFormatter));
      et_endDate.setText(previousValidEndDateFilter.format(dateTimeFormatter));
    }
  }
  
  private void resetFiltersTextAndHintAndError()
  {
    resetFilterErrors();
  
    // reset edit texts
    et_startDate.setText("");
    et_endDate.setText("");
  
    // reset hints
    til_startDate.setHint(getString(R.string.hint_start_date_filter, getString(R.string.date_format)));
    til_endDate.setHint(getString(R.string.hint_end_date_filter, getString(R.string.date_format)));
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
    clearFocusAndScrollViewToTheTop();
  
    if (requestCode == REQUEST_LOGIN)
    {
      if (resultCode == Activity.RESULT_OK)
      {
        user = (UserDto) intent.getSerializableExtra("user");
        tv_yourMeasurements.setText(getString(R.string.hello_user_name_your_measurements,
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
    else if (requestCode == REQUEST_USERDATA)
    {
      if (resultCode == CustomActivityResultCodes.USER_DATA_UPDATED || resultCode == CustomActivityResultCodes.USER_MEASUREMENTS_DELETED)
      {
        user = (UserDto) intent.getSerializableExtra("user");
        tv_yourMeasurements.setText(getString(R.string.hello_user_name_your_measurements,
                                              (user == null ? "null_user" : user.getUserName())));
        sentPostForMeasurementsAndUpdateListView();
      }
      else if (resultCode == CustomActivityResultCodes.ACCOUNT_DELETED)
      {
        resetMainAndStartLogin();
      }
    }
  
  }
  
  
  void sentPostForMeasurementsAndUpdateListView()
  {
    // map user object to JSON string
    String userJsonString = mapper.mapObjectToJSONString(user);
    
    // create json request body
    RequestBody body = RequestBody.create(MediaType.parse(getString(R.string.json_media_type)), userJsonString);
    
    // concat URL
    String requestUrl = webConfig.getMeasurementControllerURL();
    
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
        onServerResponseFailure(e);
      }
      
      @Override
      public void onResponse(Call call, Response response) throws IOException
      {
        if (response.isSuccessful())
        {
          String jsonString = response.body().string();
          onPostSuccess(jsonString);
        }
        else
        {
          onPostFailure(response);
        }
      }
    });
  }
  
  private void onPostSuccess(String jsonString)
  {
    allMeasurements = new ArrayList<>(Arrays.asList(mapper.mapJSONStringToObject(jsonString, MeasurementDto[].class)));
    // update list view and filters Start Date, End Date
    MainActivity.this.runOnUiThread(() -> updateListView(allMeasurements));
  }
  
  private void onPostFailure(Response response)
  {
    MainActivity.this.runOnUiThread(() ->
                                        Toast.makeText(getBaseContext(),
                                                       getString(R.string.something_went_wrong, response.code()),
                                                       Toast.LENGTH_LONG)
                                             .show());
    Log.d(TAG, "response code = " + response.code());
  }
  
  private void onServerResponseFailure(IOException e)
  {
    MainActivity.this.runOnUiThread(
        () -> Toast.makeText(getBaseContext(), R.string.connection_with_server_failed, Toast.LENGTH_LONG)
                   .show());
    
    e.printStackTrace();
  }
  
  
  private void updateListView(List<MeasurementDto> measurements)
  {
    // clear list view content
    stringListWithMeasurements.clear();
    
    // sort by date time
    // date closest to present day will be shown at top place in list view
    Collections.sort(measurements, new CustomComparator());
  
    resetFiltersTextAndHintAndError();
    
    if (measurements.isEmpty())
    {
      stringListWithMeasurements.add(getString(R.string.no_measurements_from_server));
      
      resetOldestAndNewestMeasurement();
      
      resetPreviousValidFilterDates();
    }
    else
    {
      for (int i = 0; i < measurements.size(); i++)
      {
        stringListWithMeasurements.add(measurements.get(i).toString());
      }
      
      // update oldest and newest date measurement
      oldestMeasurementDateTime = measurements.get(measurements.size() - 1).getLocalDateTime();
      newestMeasurementDateTime = measurements.get(0).getLocalDateTime();
  
      // set up new previous valid dates
      previousValidStartDateFilter = oldestMeasurementDateTime.toLocalDate();
      previousValidEndDateFilter = newestMeasurementDateTime.toLocalDate();
      
      // update UI filters start date and end date
      et_startDate.setText(oldestMeasurementDateTime.format(dateTimeFormatter));
      et_endDate.setText(newestMeasurementDateTime.format(dateTimeFormatter));
    }
    
    // update list view
    arrayAdapter.notifyDataSetChanged();
    lv_measurements.invalidateViews();
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
