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
import com.sobow.smartscale.config.WebConfig;
import com.sobow.smartscale.dto.UserDto;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// TODO: implement "Forgot password?" functionality

public class LoginActivity extends AppCompatActivity
{
  private static final String TAG = "LoginActivity";
  private static final int REQUEST_SIGN_UP = 0;
  
  // dependencies
  private OkHttpClient client;
  private ObjectMapper mapper;
  private WebConfig webConfig;
  
  // GUI components
  @BindView(R.id.et_email)
  EditText et_email;
  @BindView(R.id.et_password)
  EditText et_password;
  @BindView(R.id.btn_sign_in)
  Button btn_signIn;
  @BindView(R.id.link_sign_up)
  TextView btn_signUp;
  
  
  private void init()
  {
    client = new OkHttpClient();
    mapper = new ObjectMapper();
    webConfig = new WebConfig();
  }
  
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    ButterKnife.bind(this);
  
    init();
  
    btn_signIn.setOnClickListener(v -> signIn());
  
    btn_signUp.setOnClickListener(
        v ->
        {
          // Start the SignUp activity
          Intent newIntent = new Intent(getApplicationContext(), SignUpActivity.class);
          startActivityForResult(newIntent, REQUEST_SIGN_UP);
          overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        });
  }
  
  private void signIn()
  {
    Log.d(TAG, "Sign in");
    
    
    if (! validate())
    {
      onSignInFailed();
      return;
    }
    
    
    btn_signIn.setEnabled(false);
  
    final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
    progressDialog.setIndeterminate(true);
    progressDialog.setTitle(getString(R.string.progress_authenticating));
    progressDialog.setMessage(getString(R.string.progress_please_wait));
    progressDialog.setCancelable(false);
    progressDialog.show();
  
    new android.os.Handler().postDelayed(
        () ->
        {
          String email = et_email.getText().toString();
          String password = et_password.getText().toString();
  
          String requestUrl = webConfig.getUserControllerURL() + "/" + email + "/" + password;
          Request request = new Request.Builder().url(requestUrl).build();
  
          // Execute HTTP requests in background thread
          client.newCall(request).enqueue(new Callback()
          {
            @Override
            public void onFailure(Call call, IOException e)
            {
              LoginActivity.this.runOnUiThread(
                  () ->
                  {
                    Toast.makeText(getBaseContext(), R.string.connection_with_server_failed, Toast.LENGTH_LONG)
                         .show();
                  });
              
            }
  
            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
              if (response.isSuccessful())
              {
                String jsonString = response.body().string();
                UserDto user = mapper.readValue(jsonString, UserDto.class);
                LoginActivity.this.runOnUiThread(() -> onSignInSuccess(user));
              }
              else if (response.code() == 404)
              {
                LoginActivity.this.runOnUiThread(
                    () ->
                    {
                      et_email.setError(getString(R.string.email_or_password_incorrect));
                      et_password.setError(getString(R.string.email_or_password_incorrect));
  
                      onSignInFailed();
                    });
              }
              else
              {
                LoginActivity.this.runOnUiThread(
                    () -> Toast.makeText(getBaseContext(),
                                         getString(R.string.something_went_wrong, response.code()),
                                         Toast.LENGTH_LONG).show());
                Log.d(TAG, "response code = " + response.code());
              }
            }
          });
  
  
          btn_signIn.setEnabled(true);
          progressDialog.dismiss();
        }, 3000);
    
  }
  
  private void onSignInSuccess(UserDto user)
  {
    Intent intent = getIntent();
    intent.putExtra("user", user);
  
    setResult(RESULT_OK, intent);
    finish();
  }
  
  private void onSignInFailed()
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
    
    return valid;
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent)
  {
    super.onActivityResult(requestCode, resultCode, intent);
    
    // clear focus
    getWindow().getDecorView().clearFocus();
    
    if (requestCode == REQUEST_SIGN_UP)
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
}
