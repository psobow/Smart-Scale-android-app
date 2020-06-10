package com.sobow.smartscale.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.sobow.smartscale.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity
{
  private static final int REQUEST_LOGIN = 0;
  private static final int REQUEST_BLUETOOTH = 1;
  
  private String userEmail = "";
  private String userPassword = "";
  
  private Bundle bundle = new Bundle();
  
  
  // GUI components
  @BindView(R.id.listView)
  ListView listView;
  
  @BindView(R.id.btn_newMeasurement)
  Button btn_newMeasurement;
  
  @BindView(R.id.btn_logout)
  Button btn_logout;
  
  
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
  
    // Start login activity
    Intent newIntent = new Intent(getApplicationContext(), LoginActivity.class);
    newIntent.putExtras(bundle);
    startActivityForResult(newIntent, REQUEST_LOGIN);
  
  
    // TODO: Implement sending http request for all user measurements. sort them by date and print out in list View
  
    // TODO: filter data. po kliknięciu w przycik SHOW FILTERS w widoku głównym pojawią się nowe pola na filtry.
    //  np. pola na date początkową i datę końcową z jakiego okresu czasu mają być pokazywane dane. oraz przycisk APPLY FILTERS.
  
    // list view
    List<String> list = new ArrayList<>();
    
    list.add("2020-03-20 16:10:08    78.1 kg    BMI = 20.2");
    list.add("2020-03-21 10:25:34    79.0 kg    BMI = 20.3");
    list.add("2020-03-22 22:00:01    77.9 kg    BMI = 20.0");
    list.add("2020-03-23 13:47:54    78.5 kg    BMI = 20.2");
    list.add("2020-03-23 11:22:33    78.7 kg    BMI = 20.3");

    
    ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
  
    listView.setAdapter(arrayAdapter);
  
  
    // buttons on click behavior
    btn_newMeasurement.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Intent newIntent = new Intent(getApplicationContext(), BluetoothActivity.class);
        newIntent.putExtras(bundle);
        startActivityForResult(newIntent, REQUEST_BLUETOOTH);
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
        newIntent.putExtras(bundle);
        startActivityForResult(newIntent, REQUEST_LOGIN);
      }
    });
  
  }
  
  
  
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);
    
    if (requestCode == REQUEST_LOGIN)
    {
      if (resultCode == Activity.RESULT_OK)
      {
        Bundle bundle = data.getExtras();
        
        userEmail = bundle.getString("email");
        userPassword = bundle.getString("password");
        
      }
      
    }
    else if (requestCode == REQUEST_BLUETOOTH)
    {
      if (resultCode == Activity.RESULT_OK)
      {
        Bundle bundle = data.getExtras();
    
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
