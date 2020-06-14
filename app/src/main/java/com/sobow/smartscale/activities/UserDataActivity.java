package com.sobow.smartscale.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobow.smartscale.R;
import com.sobow.smartscale.config.WebConfig;
import com.sobow.smartscale.dto.UserDto;

import java.io.IOException;
import java.util.ArrayList;
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

public class UserDataActivity extends AppCompatActivity
{
  private static final String TAG = "UserDataActivity";
  
  private UserDto user;
  
  // dependencies
  private OkHttpClient client;
  private ObjectMapper mapper;
  private WebConfig webConfig;
  
  // GUI components
  @BindView(R.id.tv_currentEmail)
  TextView tv_currentEmail;
  @BindView(R.id.et_email)
  EditText et_email;
  @BindView(R.id.et_password)
  EditText et_password;
  @BindView(R.id.et_reEnterPassword)
  EditText et_reEnterPassword;
  @BindView(R.id.tv_currentUserName)
  TextView tv_currentUserName;
  @BindView(R.id.et_userName)
  EditText et_userName;
  @BindView(R.id.tv_currentAge)
  TextView tv_currentAge;
  @BindView(R.id.et_age)
  EditText et_age;
  @BindView(R.id.tv_currentHeight)
  TextView tv_currentHeight;
  @BindView(R.id.et_height)
  EditText et_height;
  @BindView(R.id.tv_currentSex)
  TextView tv_currentSex;
  @BindView(R.id.tv_chooseYourSex)
  TextView tv_chooseYourSex;
  @BindView(R.id.spinner_sex)
  Spinner spinner_sex;
  @BindView(R.id.btn_updateData)
  Button btn_updateData;
  @BindView(R.id.btn_goBackToMainMenu)
  Button btn_goBackToMainMenu;
  @BindView(R.id.btn_deleteAllYourMeasurements)
  Button btn_deleteAllYourMeasurements;
  @BindView(R.id.btn_deleteYourAccount)
  Button btn_deleteYourAccount;
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_userdata);
    ButterKnife.bind(this);
  
    init();
  
    // buttons on click behavior
  
    btn_updateData.setOnClickListener(
        v ->
        {
          updateData();
        });
  
    btn_goBackToMainMenu.setOnClickListener(
        v ->
        {
          finishAndPushRight();
        });
  
    btn_deleteAllYourMeasurements.setOnClickListener(
        v ->
        {
          new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_Dark_Dialog))
              .setTitle("Warning!")
              .setMessage("Do you really want to delete measurements?")
              .setIcon(android.R.drawable.ic_dialog_alert)
              .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                  Toast.makeText(UserDataActivity.this, "To be implemented...", Toast.LENGTH_SHORT).show();
                }
              })
              .setNegativeButton(android.R.string.no, null).show();
        });
  
    btn_deleteYourAccount.setOnClickListener(
        v ->
        {
          new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_Dark_Dialog))
              .setTitle("Warning!")
              .setMessage("Do you really want to delete account?")
              .setIcon(android.R.drawable.ic_dialog_alert)
              .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                  Toast.makeText(UserDataActivity.this, "To be implemented...", Toast.LENGTH_SHORT).show();
                }
              })
              .setNegativeButton(android.R.string.no, null).show();
        });
  }
  
  private void init()
  {
    // init dependencies
    client = new OkHttpClient();
    mapper = new ObjectMapper();
    webConfig = new WebConfig();
    
    user = (UserDto) getIntent().getSerializableExtra("user");
    
    updateUserDataUI();
    
    // Spinner values
    List<String> spinnerValues = new ArrayList<>();
    spinnerValues.add(getString(R.string.spinner_default_choice));
    spinnerValues.add(getString(R.string.spinner_male_choice));
    spinnerValues.add(getString(R.string.spinner_female_choice));
    
    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerValues);
    // Specify the layout to use when the list of choices appears
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinner_sex.setAdapter(dataAdapter);
  }
  
  private void updateUserDataUI()
  {
    tv_currentEmail.setText(getString(R.string.current_email, user.getEmail()));
    tv_currentUserName.setText(getString(R.string.current_user_name, user.getUserName()));
    tv_currentAge.setText(getString(R.string.current_age, user.getAge()));
    tv_currentHeight.setText(getString(R.string.current_height, user.getHeight()));
    tv_currentSex.setText(getString(R.string.current_sex, user.getSex()));
    
    et_email.setText("");
    et_password.setText("");
    et_reEnterPassword.setText("");
    et_userName.setText("");
    et_age.setText("");
    et_height.setText("");
    spinner_sex.setSelection(0);
  }
  
  private void updateData()
  {
    Log.d(TAG, "update data");
    
    if (! validate())
    {
      onUpdateDataFailed();
      return;
    }
    
    btn_updateData.setEnabled(false);
    
    // Display loading component
    ProgressDialog progressDialog = new ProgressDialog(UserDataActivity.this,
                                                       R.style.AppTheme_Dark_Dialog);
    progressDialog.setIndeterminate(true);
    progressDialog.setTitle(getString(R.string.progress_updating_data));
    progressDialog.setMessage(getString(R.string.progress_please_wait));
    progressDialog.setCancelable(false);
    progressDialog.show();
    
    new android.os.Handler().postDelayed(
        () ->
        {
          // read input
          String name = et_userName.getText().toString();
          String height = et_height.getText().toString();
          String age = et_age.getText().toString();
          String email = et_email.getText().toString();
          String password = et_password.getText().toString();
          int spinnerChoicePosition = spinner_sex.getSelectedItemPosition();
          String sex = spinner_sex.getItemAtPosition(spinnerChoicePosition).toString();
          
          // initialize UserDto object
          UserDto newUser = new UserDto();
          newUser.setUserName(name);
          newUser.setHeight(Integer.parseInt(height));
          newUser.setAge(Integer.parseInt(age));
          newUser.setSex(sex);
          newUser.setEmail(email);
          newUser.setPassword(password);
          newUser.setMeasurementIds(user.getMeasurementIds());
          
          // map object to JSON string
          String userJsonString = "";
          try
          {
            userJsonString = mapper.writeValueAsString(newUser);
            Log.d(TAG, "Mapped User Json String = " + userJsonString);
          }
          catch (JsonProcessingException e)
          {
            e.printStackTrace();
          }
          
          // json request body
          RequestBody body = RequestBody.create(MediaType.parse(getString(R.string.json_media_type)), userJsonString);
          
          String oldEmailAddress = user.getEmail();
          String oldPassword = user.getPassword();
          
          String requestUrl = webConfig.getUserControllerURL() + "/" + oldEmailAddress + "/" + oldPassword;
          
          Request request = new Request.Builder()
              .url(requestUrl)
              .put(body)
              .build();
          
          
          // Execute HTTP requests in background thread
          client.newCall(request).enqueue(new Callback()
          {
            @Override
            public void onFailure(Call call, IOException e)
            {
              UserDataActivity.this.runOnUiThread(
                  () -> Toast.makeText(getBaseContext(), R.string.connection_with_server_failed, Toast.LENGTH_LONG)
                             .show());
              
              e.printStackTrace();
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
              if (response.isSuccessful())
              {
                String jsonString = response.body().string();
                user = mapper.readValue(jsonString, UserDto.class);
                UserDataActivity.this.runOnUiThread(() -> onUpdateDataSuccess());
              }
              else if (response.code() == 400)
              {
                UserDataActivity.this.runOnUiThread(
                    () ->
                    {
                      et_email.setError(getString(R.string.email_already_exists));
                      onUpdateDataFailed();
                    });
              }
              else
              {
                UserDataActivity.this.runOnUiThread(
                    () -> Toast.makeText(getBaseContext(),
                                         getString(R.string.something_went_wrong, response.code()),
                                         Toast.LENGTH_LONG).show());
                Log.d(TAG, "response code = " + response.code());
              }
            }
          });
          
          btn_updateData.setEnabled(true);
          progressDialog.dismiss();
        }, 3000);
  }
  
  private void onUpdateDataSuccess()
  {
    setResult(RESULT_OK);
    Toast.makeText(getBaseContext(), R.string.data_updated, Toast.LENGTH_LONG).show();
    getIntent().putExtra("user", user);
    updateUserDataUI();
    
    // clear focus
    getWindow().getDecorView().clearFocus();
  }
  
  private void onUpdateDataFailed()
  {
    Toast.makeText(getBaseContext(), R.string.update_data_failed, Toast.LENGTH_LONG).show();
  }
  
  
  // TODO: create class validator move validate logic there
  private boolean validate()
  {
    boolean valid = true;
    
    String name = et_userName.getText().toString();
    String height = et_height.getText().toString();
    String age = et_age.getText().toString();
    String email = et_email.getText().toString();
    String password = et_password.getText().toString();
    String reEnterPassword = et_reEnterPassword.getText().toString();
    
    // Sex spinner
    if (spinner_sex.getSelectedItemPosition() == 0)
    {
      tv_chooseYourSex.setError(getString(R.string.invalid_sex_choice));
      
      valid = false;
    }
    else
    {
      tv_chooseYourSex.setError(null);
    }
    
    // user name
    if (! name.matches("[A-Za-z0-9]{3,20}"))
    {
      et_userName.setError(getString(R.string.invalid_user_name));
      valid = false;
    }
    else
    {
      et_userName.setError(null);
    }
    
    // Height
    if (! height.matches("^(?:[1-9]\\d?|[12]\\d{2})$"))
    {
      et_height.setError(getString(R.string.invalid_height));
      valid = false;
    }
    else
    {
      et_height.setError(null);
    }
    
    // age
    if (! age.matches("^[1-9][0-9]?$|^100$"))
    {
      et_age.setError(getString(R.string.invalid_age));
      valid = false;
    }
    else
    {
      et_age.setError(null);
    }
    
    // email
    if (! email.matches(
        "^((\"[\\w-\\s]+\")|([\\w-]+(?:\\.[\\w-]+)*)|(\"[\\w-\\s]+\")([\\w-]+(?:\\.[\\w-]+)*))(@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)$)|(@\\[?((25[0-5]\\.|2[0-4][0-9]\\.|1[0-9]{2}\\.|[0-9]{1,2}\\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\]?$)"))
    {
      et_email.setError(getString(R.string.invalid_email));
      valid = false;
    }
    else
    {
      et_email.setError(null);
    }
    
    // password
    if (! password.matches("[^\\s]{3,20}"))
    {
      et_password.setError(getString(R.string.invalid_password));
      valid = false;
    }
    else
    {
      et_password.setError(null);
    }
    
    if (reEnterPassword.isEmpty())
    {
      et_reEnterPassword.setError(getString(R.string.invalid_password));
      valid = false;
    }
    else if (! (reEnterPassword.equals(password)))
    {
      et_reEnterPassword.setError(getString(R.string.passwords_do_not_match));
      valid = false;
    }
    else
    {
      et_reEnterPassword.setError(null);
    }
    
    return valid;
  }
  
  private void finishAndPushRight()
  {
    finish();
    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
  }
  
  @Override
  public void onBackPressed()
  {
    finishAndPushRight();
  }
}
