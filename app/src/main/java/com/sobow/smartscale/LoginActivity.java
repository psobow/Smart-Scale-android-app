package com.sobow.smartscale;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
  
  private final String BASE_URL = "http://10.0.2.2:8080/v1";
  private final String USER_CONTROLLER = "/user";
  
  private OkHttpClient client = new OkHttpClient();
  
  @BindView(R.id.input_email)
  EditText _emailText;
  @BindView(R.id.input_password)
  EditText _passwordText;
  @BindView(R.id.btn_login)
  Button _loginButton;
  @BindView(R.id.link_signup)
  TextView _signupLink;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    ButterKnife.bind(this);
    
    _loginButton.setOnClickListener(new View.OnClickListener()
    {
      
      @Override
      public void onClick(View v)
      {
        login();
      }
    });
    
    _signupLink.setOnClickListener(new View.OnClickListener()
    {
      
      @Override
      public void onClick(View v)
      {
        // Start the Signup activity
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivityForResult(intent, REQUEST_SIGNUP);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
      }
    });
  }
  
  public void login()
  {
    Log.d(TAG, "Login");
    
    
    if (! validate())
    {
      onLoginFailed();
      return;
    }
    
    
    _loginButton.setEnabled(false);
    
    final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                                                             R.style.AppTheme_Dark_Dialog);
    progressDialog.setIndeterminate(true);
    progressDialog.setMessage("Authenticating...");
    progressDialog.show();
  
  
    new android.os.Handler().postDelayed(
        new Runnable()
        {
          public void run()
          {
            String emailInput = _emailText.getText().toString();
            String passwordInput = _passwordText.getText().toString();
            
            String requestUrl = BASE_URL + USER_CONTROLLER + "/" + emailInput + "/" + passwordInput;
            Request request = new Request.Builder().url(requestUrl).build();
            
            // Execute HTTP requests in background thread
            client.newCall(request).enqueue(new Callback()
            {
              @Override
              public void onFailure(Call call, IOException e)
              {
                LoginActivity.this.runOnUiThread(new Runnable()
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
                if(response.isSuccessful())
                {
                  Log.i(TAG, response.body().string());
                  
                  LoginActivity.this.runOnUiThread(new Runnable()
                  {
                    @Override
                    public void run()
                    {
                      
                      onLoginSuccess();
                      //progressDialog.dismiss();
                    }
                  });
                }
                else if (response.code() == 404)
                {
                  LoginActivity.this.runOnUiThread(new Runnable()
                  {
                    @Override
                    public void run()
                    {
                      _emailText.setError("Email or password incorrect");
                      _passwordText.setError("Email or password incorrect");
                      
                      onLoginFailed();
                      //progressDialog.dismiss();
                    }
                  });
                }
  
  
              }
              
            });
            //for internet connection:  sent get for user with email and password if server didn't return user display dialog
            // - if wrong email or password display: wrong email or password
          
            //no internet connection:  find user in database based on email and password
            // - if wrong email or password display: wrong email or password
  
  
            progressDialog.dismiss();
          }
        }, 3000);
    
  }
  
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_SIGNUP)
    {
      if (resultCode == RESULT_OK)
      {
  
        // TODO: Implement successful signup logic here
        // By default we just finish the Activity and log them in automatically
        this.finish();
      }
    }
  }
  
  @Override
  public void onBackPressed()
  {
    // Disable going back to the MainActivity
    moveTaskToBack(true);
  }
  
  public void onLoginSuccess()
  {
    _loginButton.setEnabled(true);
    finish();
  }
  
  public void onLoginFailed()
  {
    Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
    
    _loginButton.setEnabled(true);
  }
  
  public boolean validate()
  {
    boolean valid = true;
    
    String email = _emailText.getText().toString();
    String password = _passwordText.getText().toString();
    
    if ( ! email.matches("^((\"[\\w-\\s]+\")|([\\w-]+(?:\\.[\\w-]+)*)|(\"[\\w-\\s]+\")([\\w-]+(?:\\.[\\w-]+)*))(@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)$)|(@\\[?((25[0-5]\\.|2[0-4][0-9]\\.|1[0-9]{2}\\.|[0-9]{1,2}\\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\]?$)"))
    {
      _emailText.setError("enter a valid email address");
      valid = false;
    }
    else
    {
      _emailText.setError(null);
    }
    
    if ( ! password.matches("[^\\s]{3,20}"))
    {
      _passwordText.setError("between 3 and 20 alphanumeric characters without spaces");
      valid = false;
    }
    else
    {
      _passwordText.setError(null);
    }
    
    return valid;
  }
}
