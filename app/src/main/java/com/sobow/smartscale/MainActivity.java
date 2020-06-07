package com.sobow.smartscale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {
  
  @BindView(R.id.listView)
  ListView listView;
  
  private static final int REQUEST_LOGIN = 0;
  
  private String userEmail = "";
  private String userPassword = "";
  
  private Bundle bundle = new Bundle();
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    Intent newIntent = new Intent(this, LoginActivity.class);
    newIntent.putExtras(bundle);
    startActivityForResult(newIntent, REQUEST_LOGIN);
  
    List<String> list = new ArrayList<>();
  
    list.add("2020-03-20 16:10:08    78.1 kg    BMI = 20.2");
    list.add("2020-03-21 10:25:34    79.0 kg    BMI = 20.3");
    list.add("2020-03-22 22:00:01    77.9 kg    BMI = 20.0");
    list.add("2020-03-23 13:47:54    78.5 kg    BMI = 20.2");
    list.add("2020-03-23 11:22:33    78.7 kg    BMI = 20.3");
  
    list.add("2020-03-20 16:10:08    78.1 kg    BMI = 20.2");
    list.add("2020-03-21 10:25:34    79.0 kg    BMI = 20.3");
    list.add("2020-03-22 22:00:01    77.9 kg    BMI = 20.0");
    list.add("2020-03-23 13:47:54    78.5 kg    BMI = 20.2");
    list.add("2020-03-23 11:22:33    78.7 kg    BMI = 20.3");
  
    list.add("2020-03-20 16:10:08    78.1 kg    BMI = 20.2");
    list.add("2020-03-21 10:25:34    79.0 kg    BMI = 20.3");
  
    list.add("2020-03-20 16:10:08    78.1 kg    BMI = 20.2");
    list.add("2020-03-21 10:25:34    79.0 kg    BMI = 20.3");
    list.add("2020-03-22 22:00:01    77.9 kg    BMI = 20.0");
    list.add("2020-03-23 13:47:54    78.5 kg    BMI = 20.2");
    list.add("2020-03-23 11:22:33    78.7 kg    BMI = 20.3");
  
    list.add("2020-03-20 16:10:08    78.1 kg    BMI = 20.2");
    list.add("2020-03-21 10:25:34    79.0 kg    BMI = 20.3");
    list.add("2020-03-22 22:00:01    77.9 kg    BMI = 20.0");
    list.add("2020-03-23 13:47:54    78.5 kg    BMI = 20.2");
    list.add("2020-03-23 11:22:33    78.7 kg    BMI = 20.3");
  
    list.add("2020-03-20 16:10:08    78.1 kg    BMI = 20.2");
    list.add("2020-03-21 10:25:34    79.0 kg    BMI = 20.3");
    
    ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
  
    listView.setAdapter(arrayAdapter);
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
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    
    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }
    
    return super.onOptionsItemSelected(item);
  }
}
