package com.sobow.smartscale.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobow.smartscale.R;
import com.sobow.smartscale.dto.UserDto;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity
{
  private static final String TAG = "LoginActivity";
  private static final int REQUEST_SIGNUP = 0;
  
  // TODO: move it to config class
  private static final String BASE_URL = "http://10.0.2.2:8080/v1";
  private static final String USER_CONTROLLER = "/user";
  
  private OkHttpClient client = new OkHttpClient();
  private ObjectMapper mapper = new ObjectMapper();
  
  // GUI components
  @BindView(R.id.et_email)
  EditText et_email;
  @BindView(R.id.et_password)
  EditText et_password;
  @BindView(R.id.btn_login)
  Button btn_login;
  @BindView(R.id.link_signup)
  TextView btn_signup;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    ButterKnife.bind(this);
  
    // TODO: implement "Forgot password?" functionality
  
    btn_login.setOnClickListener(v -> login());
  
    btn_signup.setOnClickListener(
        v ->
        {
          // Start the Signup activity
          Intent newIntent = new Intent(getApplicationContext(), SignupActivity.class);
          startActivityForResult(newIntent, REQUEST_SIGNUP);
          overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });
  }
  
  private void login()
  {
    Log.d(TAG, "Login");
    
    
    if (! validate())
    {
      onLoginFailed();
      return;
    }
  
  
    btn_login.setEnabled(false);
  
    final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
    progressDialog.setIndeterminate(true);
    progressDialog.setMessage(getString(R.string.authenticating_progress));
    progressDialog.show();
  
  
    new android.os.Handler().postDelayed(
        () ->
        {
          String emailInput = et_email.getText().toString();
          String passwordInput = et_password.getText().toString();
  
          String requestUrl = BASE_URL + USER_CONTROLLER + "/" + emailInput + "/" + passwordInput;
          Request request = new Request.Builder().url(requestUrl).build();
  
          // Execute HTTP requests in background thread
          client.newCall(request).enqueue(new Callback()
          {
            @Override
            public void onFailure(Call call, IOException e)
            {
              LoginActivity.this.runOnUiThread(
                  () -> Toast.makeText(getBaseContext(), R.string.connection_with_server_failed, Toast.LENGTH_LONG)
                             .show());
            }
  
            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
              if (response.isSuccessful())
              {
                String jsonString = response.body().string();
                UserDto user = mapper.readValue(jsonString, UserDto.class);
                LoginActivity.this.runOnUiThread(() -> onLoginSuccess(user));
              }
              else if (response.code() == 404)
              {
                LoginActivity.this.runOnUiThread(
                    () ->
                    {
                      et_email.setError(getString(R.string.email_or_password_incorrect));
                      et_password.setError(getString(R.string.email_or_password_incorrect));
                      
                      onLoginFailed();
                    });
              }
              else
              {
                LoginActivity.this.runOnUiThread(
                    () -> Toast.makeText(getBaseContext(),
                                         getString(R.string.something_went_wrong, response.code()),
                                         Toast.LENGTH_LONG).show());
              }
            }
          });
  
  
          btn_login.setEnabled(true);
          progressDialog.dismiss();
        }, 3000);
    
  }
  
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent)
  {
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == REQUEST_SIGNUP)
    {
      if (resultCode == RESULT_OK)
      {
        // Pass object to main activity
        UserDto user = (UserDto) intent.getSerializableExtra("user");
        getIntent().putExtra("user", user);
        
        setResult(RESULT_OK, getIntent());
        finish();
      }
      else
      {
        et_email.setError(null);
        et_password.setError(null);
      }
    }
  }
  
  @Override
  public void onBackPressed()
  {
    // Disable going back to the MainActivity
    moveTaskToBack(true);
  }
  
  public void onLoginSuccess(UserDto user)
  {
    Intent intent = getIntent();
    intent.putExtra("user", user);
  
    setResult(RESULT_OK, intent);
    finish();
  }
  
  private void onLoginFailed()
  {
    Toast.makeText(getBaseContext(), R.string.login_failed, Toast.LENGTH_LONG).show();
  }
  
  private boolean validate()
  {
    boolean valid = true;
  
    String email = et_email.getText().toString();
    String password = et_password.getText().toString();
  
    // email
    if (! email.matches(
        "^((\"[\\w-\\s]+\")|([\\w-]+(?:\\.[\\w-]+)*)|(\"[\\w-\\s]+\")([\\w-]+(?:\\.[\\w-]+)*))(@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)$)|(@\\[?((25[0-5]\\.|2[0-4][0-9]\\.|1[0-9]{2}\\.|[0-9]{1,2}\\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\]?$)"))
    {
      et_email.setError(getString(R.string.email_invalid));
      valid = false;
    }
    else
    {
      et_email.setError(null);
    }
  
    // password
    if (! password.matches("[^\\s]{3,20}"))
    {
      et_password.setError(getString(R.string.password_invalid));
      valid = false;
    }
    else
    {
      et_password.setError(null);
    }
    
    return valid;
  }
}
