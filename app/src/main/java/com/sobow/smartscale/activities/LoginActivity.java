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
import com.sobow.smartscale.validation.InputValidator;

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
  private InputValidator inputValidator;
  
  // GUI components
  @BindView(R.id.et_email)
  EditText et_email;
  @BindView(R.id.et_password)
  EditText et_password;
  @BindView(R.id.btn_sign_in)
  Button btn_signIn;
  @BindView(R.id.link_sign_up)
  TextView btn_signUp;
  @BindView(R.id.link_forgot_password)
  TextView btn_forgotPassword;
  
  
  private void init()
  {
    client = new OkHttpClient();
    mapper = new ObjectMapper();
    webConfig = new WebConfig();
    inputValidator = new InputValidator();
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
  
    btn_forgotPassword.setOnClickListener(
        v ->
        {
          Toast.makeText(getBaseContext(), "To be implemented...", Toast.LENGTH_LONG).show();
        });
  }
  
  private void signIn()
  {
    Log.d(TAG, "Sign in");
  
    String email = et_email.getText().toString();
    String password = et_password.getText().toString();
  
    if (! validate(email, password))
    {
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
  
          String requestUrl = webConfig.getUserControllerURL() + "/" + email + "/" + password;
          Request request = new Request.Builder().url(requestUrl).build();
  
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
                UserDto user = mapper.readValue(jsonString, UserDto.class);
                LoginActivity.this.runOnUiThread(() -> onSignInSuccess(user));
              }
              else
              {
                onSignInFailed(response);
              }
            }
          });
  
  
          btn_signIn.setEnabled(true);
          progressDialog.dismiss();
        }, 3000);
    
  }
  
  private void onServerResponseFailure(IOException e)
  {
    LoginActivity.this.runOnUiThread(
        () -> Toast.makeText(getBaseContext(), R.string.connection_with_server_failed, Toast.LENGTH_LONG)
                   .show());
    
    e.printStackTrace();
  }
  
  
  private void onSignInSuccess(UserDto user)
  {
    Intent intent = getIntent();
    intent.putExtra("user", user);
  
    setResult(RESULT_OK, intent);
    finish();
  }
  
  private void onSignInFailed(Response response)
  {
    if (response.code() == 404)
    {
      LoginActivity.this.runOnUiThread(
          () ->
          {
            et_email.setError(getString(R.string.email_or_password_incorrect));
            et_password.setError(getString(R.string.email_or_password_incorrect));
            Toast.makeText(getBaseContext(), R.string.login_failed, Toast.LENGTH_LONG).show();
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
  
  private boolean validate(String email, String password)
  {
    boolean isValid = true;
    
    // EMAIL
    if (! inputValidator.isEmailValid(email))
    {
      isValid = false;
      et_email.setError(getString(R.string.invalid_email));
    }
    else { et_email.setError(null); }
    
    // PASSWORD
    if (! inputValidator.isPasswordValid(password))
    {
      isValid = false;
      et_password.setError(getString(R.string.invalid_password));
    }
    else { et_password.setError(null); }
    
    return isValid;
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
