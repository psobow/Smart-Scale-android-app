package com.sobow.smartscale.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobow.smartscale.R;
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

public class SignUpActivity extends AppCompatActivity
{
  private static final String TAG = "SignupActivity";
  
  private final String BASE_URL = "http://10.0.2.2:8080/v1";
  private final String USER_CONTROLLER = "/user";
  
  private OkHttpClient client = new OkHttpClient();
  private ObjectMapper mapper = new ObjectMapper();
  
  // GUI components
  @BindView(R.id.et_userName)
  EditText et_userName;
  @BindView(R.id.et_height)
  EditText et_height;
  @BindView(R.id.et_age)
  EditText et_age;
  @BindView(R.id.et_email)
  EditText et_email;
  @BindView(R.id.tv_chooseYourSex)
  TextView tv_chooseYourSex;
  @BindView(R.id.spinner_sex)
  Spinner spinner_sex;
  @BindView(R.id.et_password)
  EditText et_password;
  @BindView(R.id.et_reEnterPassword)
  EditText et_reEnterPassword;
  @BindView(R.id.btn_signUp)
  Button btn_signUp;
  @BindView(R.id.link_sign_in)
  TextView link_sign_in;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signup);
    ButterKnife.bind(this);
    
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
  
    // buttons on click behavior
    btn_signUp.setOnClickListener(v -> signUp());
  
    link_sign_in.setOnClickListener(
        v ->
        {
          // Finish the registration screen and return to the Login activity
          finish();
          overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        });
  }
  
  public void signUp()
  {
    Log.d(TAG, "SignUp");
    
    if (! validate())
    {
      onSignUpFailed();
      return;
    }
    
    btn_signUp.setEnabled(false);
  
    // display loading component
    final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                                                             R.style.AppTheme_Dark_Dialog);
    progressDialog.setIndeterminate(true);
    progressDialog.setMessage(getString(R.string.creating_account_progress));
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
          newUser.setMeasurementIds(new ArrayList<>());
        
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
        
          String requestUrl = BASE_URL + USER_CONTROLLER;
        
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
              SignUpActivity.this.runOnUiThread(
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
                UserDto userFromServer = mapper.readValue(jsonString, UserDto.class);
                SignUpActivity.this.runOnUiThread(() -> onSignUpSuccess(userFromServer));
              }
              else if (response.code() == 400)
              {
                SignUpActivity.this.runOnUiThread(
                    () ->
                    {
                      et_email.setError(getString(R.string.email_already_exists));
                      onSignUpFailed();
                    });
              }
              else
              {
                SignUpActivity.this.runOnUiThread(
                    () -> Toast.makeText(getBaseContext(),
                                         getString(R.string.something_went_wrong, response.code()),
                                         Toast.LENGTH_LONG).show());
                Log.i(TAG, "response code = " + response.code());
              }
            }
          });
  
          btn_signUp.setEnabled(true);
          progressDialog.dismiss();
        }, 3000);
  }
  
  
  public void onSignUpSuccess(UserDto userFromServer)
  {
    Intent intent = getIntent();
    intent.putExtra("user", userFromServer);
    
    setResult(RESULT_OK, intent);
    
    finish();
    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
  }
  
  public void onSignUpFailed()
  {
    Toast.makeText(getBaseContext(), R.string.sign_up_failed, Toast.LENGTH_LONG).show();
  }
  
  public boolean validate()
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
}