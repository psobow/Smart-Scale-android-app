package com.sobow.smartscale.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class SignupActivity extends AppCompatActivity
{
  private static final String TAG = "SignupActivity";
  private static final String SPINNER_CHOICE_DEFAULT = "Your choice...";
  private static final String SPINNER_CHOICE_MALE = "Male";
  private static final String SPINNER_CHOICE_FEMALE = "Female";
  
  private final String BASE_URL = "http://10.0.2.2:8080/v1";
  private final String USER_CONTROLLER = "/user";
  
  private OkHttpClient client = new OkHttpClient();
  private ObjectMapper mapper = new ObjectMapper();
  
  private UserDto userFromServer = new UserDto();
  
  
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
  @BindView(R.id.btn_updateData)
  Button btn_signup;
  @BindView(R.id.link_login)
  TextView link_login;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signup);
    ButterKnife.bind(this);
    
    // Spinner values
    List<String> spinnerValues = new ArrayList<>();
    spinnerValues.add(SPINNER_CHOICE_DEFAULT);
    spinnerValues.add(SPINNER_CHOICE_MALE);
    spinnerValues.add(SPINNER_CHOICE_FEMALE);
    
    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerValues);
    // Specify the layout to use when the list of choices appears
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinner_sex.setAdapter(dataAdapter);
  
    // buttons on click behavior
    btn_signup.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        signup();
      }
    });
  
    link_login.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        // Finish the registration screen and return to the Login activity
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
      }
    });
  }
  
  public void signup()
  {
    Log.d(TAG, "Signup");
    
    if (! validate())
    {
      onSignupFailed();
      return;
    }
  
    btn_signup.setEnabled(false);
  
    // display loading component
    final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                                                             R.style.AppTheme_Dark_Dialog);
    progressDialog.setIndeterminate(true);
    progressDialog.setMessage("Creating Account...");
    progressDialog.show();
  
  
    new android.os.Handler().postDelayed(
        new Runnable()
        {
          public void run()
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
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), userJsonString);
  
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
                SignupActivity.this.runOnUiThread(new Runnable()
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
  
                  userFromServer = mapper.readValue(jsonString, UserDto.class);
                  SignupActivity.this.runOnUiThread(new Runnable()
                  {
                    @Override
                    public void run()
                    {
                      onSignupSuccess();
                    }
                  });
                }
                else if (response.code() == 400)
                {
                  SignupActivity.this.runOnUiThread(new Runnable()
                  {
                    @Override
                    public void run()
                    {
                      et_email.setError("Email already exists in database");
            
                      onSignupFailed();
                    }
                  });
                }
                else
                {
                  Log.i(TAG, "response code = " + response.code());
                }
      
      
              }
    
            });
  
            btn_signup.setEnabled(true);
            progressDialog.dismiss();
          }
        }, 3000);
  }
  
  
  public void onSignupSuccess()
  {
    Intent intent = getIntent();
    intent.putExtra("user", userFromServer);
    
    setResult(RESULT_OK, intent);
    
    finish();
    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
  }
  
  public void onSignupFailed()
  {
    Toast.makeText(getBaseContext(), "Sign up failed", Toast.LENGTH_LONG).show();
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
      tv_chooseYourSex.setError("Choose your sex");
  
      valid = false;
    }
    else
    {
      tv_chooseYourSex.setError(null);
    }
    
    
    // name
    if (! name.matches("[A-Za-z0-9]{3,20}"))
    {
      et_userName.setError("Enter only letters and numbers between 3 to 20 length");
      valid = false;
    }
    else
    {
      et_userName.setError(null);
    }
  
    // Height
    if (! height.matches("^(?:[1-9]\\d?|[12]\\d{2})$"))
    {
      et_height.setError("Enter height between 0 to 300 centimetres");
      valid = false;
    }
    else
    {
      et_height.setError(null);
    }
    
    // age
    if (! age.matches("^[1-9][0-9]?$|^100$"))
    {
      et_age.setError("Enter age between 0 and 100");
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
      et_email.setError("Enter a valid email address");
      valid = false;
    }
    else
    {
      et_email.setError(null);
    }
    
    // password
    if (! password.matches("[^\\s]{3,20}"))
    {
      et_password.setError("Enter password between 3 and 20 characters without spaces");
      valid = false;
    }
    else
    {
      et_password.setError(null);
    }
  
    if (reEnterPassword.isEmpty())
    {
      et_reEnterPassword.setError("Password can not be empty");
      valid = false;
    }
    else if (! (reEnterPassword.equals(password)))
    {
      et_reEnterPassword.setError("Passwords do not match");
      valid = false;
    }
    else
    {
      et_reEnterPassword.setError(null);
    }
    
    return valid;
  }
}