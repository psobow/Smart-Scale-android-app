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
  
  
  @BindView(R.id.input_name)
  EditText _nameText;
  @BindView(R.id.input_height)
  EditText _heightText;
  @BindView(R.id.input_age)
  EditText _ageText;
  @BindView(R.id.input_email)
  EditText _emailText;
  @BindView(R.id.tvSex)
  TextView _sexText;
  @BindView(R.id.input_sex)
  Spinner _sexSpinner;
  @BindView(R.id.input_password)
  EditText _passwordText;
  @BindView(R.id.input_reEnterPassword)
  EditText _reEnterPasswordText;
  @BindView(R.id.btn_signup)
  Button _signupButton;
  @BindView(R.id.link_login)
  TextView _loginLink;
  
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
    _sexSpinner.setAdapter(dataAdapter);
  
    // buttons on click behavior
    _signupButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        signup();
      }
    });
    
    _loginLink.setOnClickListener(new View.OnClickListener()
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
    
    _signupButton.setEnabled(false);
    
    final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                                                             R.style.AppTheme_Dark_Dialog);
    progressDialog.setIndeterminate(true);
    progressDialog.setMessage("Creating Account...");
    progressDialog.show();
    
    String name = _nameText.getText().toString();
    String height = _heightText.getText().toString();
    String age = _ageText.getText().toString();
    String email = _emailText.getText().toString();
    String password = _passwordText.getText().toString();
    
    int spinnerChoicePosition = _sexSpinner.getSelectedItemPosition();
    String sex = _sexSpinner.getItemAtPosition(spinnerChoicePosition).toString();
    
    final UserDto newUser = new UserDto();
    newUser.setUserName(name);
    newUser.setHeight(Integer.parseInt(height));
    newUser.setAge(Integer.parseInt(age));
    newUser.setSex(sex);
    newUser.setEmail(email);
    newUser.setPassword(password);
    
    
    new android.os.Handler().postDelayed(
        new Runnable()
        {
          public void run()
          {
            // Send post to server
            // - if email adress alredy exsists set up error flag for field email
  
  
            // map object to JSON string
            String userJsonString = "";
            try
            {
              userJsonString = mapper.writeValueAsString(newUser);
              Log.i(TAG, "Mapped User Json String = " + userJsonString);
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
                  SignupActivity.this.runOnUiThread(new Runnable()
                  {
                    @Override
                    public void run()
                    {
                      onSignupSuccess(newUser);
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
                      _emailText.setError("Email already exists in database");
            
                      onSignupFailed();
                    }
                  });
                }
      
      
              }
    
            });
            
            
            progressDialog.dismiss();
          }
        }, 3000);
  }
  
  
  public void onSignupSuccess(UserDto userDto)
  {
    _signupButton.setEnabled(true);
  
    Intent intent = getIntent();
    Bundle bundle = intent.getExtras();
  
    bundle.putString("email", userDto.getEmail());
    bundle.putString("password", userDto.getPassword());
  
    intent.putExtras(bundle);
    setResult(RESULT_OK, intent);
    
    finish();
    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
  }
  
  public void onSignupFailed()
  {
    Toast.makeText(getBaseContext(), "Sign up failed", Toast.LENGTH_LONG).show();
    
    _signupButton.setEnabled(true);
  }
  
  public boolean validate()
  {
    boolean valid = true;
    
    String name = _nameText.getText().toString();
    String height = _heightText.getText().toString();
    String age = _ageText.getText().toString();
    String email = _emailText.getText().toString();
    String password = _passwordText.getText().toString();
    String reEnterPassword = _reEnterPasswordText.getText().toString();
    
    // Sex spinner
    if (_sexSpinner.getSelectedItemPosition() == 0)
    {
      _sexText.setError("Choose your sex");
  
      valid = false;
    }
    else
    {
      _sexText.setError(null);
    }
    
    
    // name
    if (! name.matches("[A-Za-z0-9]{3,20}"))
    {
      _nameText.setError("Enter only letters and numbers between 3 to 20 length");
      valid = false;
    }
    else
    {
      _nameText.setError(null);
    }
  
    // Height
    if (! height.matches("^(?:[1-9]\\d?|[12]\\d{2})$"))
    {
      _heightText.setError("Enter height between 0 to 300 centimetres");
      valid = false;
    }
    else
    {
      _heightText.setError(null);
    }
    
    // age
    if (! age.matches("^[1-9][0-9]?$|^100$"))
    {
      _ageText.setError("Enter age between 0 and 100");
      valid = false;
    }
    else
    {
      _ageText.setError(null);
    }
    
    // email
    if (! email.matches(
        "^((\"[\\w-\\s]+\")|([\\w-]+(?:\\.[\\w-]+)*)|(\"[\\w-\\s]+\")([\\w-]+(?:\\.[\\w-]+)*))(@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)$)|(@\\[?((25[0-5]\\.|2[0-4][0-9]\\.|1[0-9]{2}\\.|[0-9]{1,2}\\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\]?$)"))
    {
      _emailText.setError("Enter a valid email address");
      valid = false;
    }
    else
    {
      _emailText.setError(null);
    }
    
    // password
    if (! password.matches("[^\\s]{3,20}"))
    {
      _passwordText.setError("Enter password between 3 and 20 characters without spaces");
      valid = false;
    }
    else
    {
      _passwordText.setError(null);
    }
    
    if (! (reEnterPassword.equals(password)))
    {
      _reEnterPasswordText.setError("Passwords do not match");
      valid = false;
    }
    else
    {
      _reEnterPasswordText.setError(null);
    }
    
    return valid;
  }
}