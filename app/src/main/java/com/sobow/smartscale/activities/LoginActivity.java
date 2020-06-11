package com.sobow.smartscale.activities;

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

import com.sobow.smartscale.R;

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
  
    btn_login.setOnClickListener(new View.OnClickListener()
    {
      
      @Override
      public void onClick(View v)
      {
        login();
      }
    });
  
    btn_signup.setOnClickListener(new View.OnClickListener()
    {
      
      @Override
      public void onClick(View v)
      {
        // Start the Signup activity
        Intent newIntent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivityForResult(newIntent, REQUEST_SIGNUP);
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
  
  
    btn_login.setEnabled(false);
    
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
                  Log.i(TAG, "Response body = " + response.body().string());
                  
                  LoginActivity.this.runOnUiThread(new Runnable()
                  {
                    @Override
                    public void run()
                    {
                      onLoginSuccess(emailInput, passwordInput);
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
                      et_email.setError("Email or password incorrect");
                      et_password.setError("Email or password incorrect");
                      
                      onLoginFailed();
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
        // By default we just finish the Activity and log them in automatically
        Bundle bundle = data.getExtras();
        getIntent().putExtras(bundle);
  
        setResult(RESULT_OK, getIntent());
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
  
  public void onLoginSuccess(String email, String password)
  {
    btn_login.setEnabled(true);
  
    Intent intent = getIntent();
    Bundle bundle = new Bundle();
  
    bundle.putString("email", email);
    bundle.putString("password", password);
  
    intent.putExtras(bundle);
    setResult(RESULT_OK, intent);
    
    finish();
  }
  
  public void onLoginFailed()
  {
    Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
  
    btn_login.setEnabled(true);
  }
  
  public boolean validate()
  {
    boolean valid = true;
  
    String email = et_email.getText().toString();
    String password = et_password.getText().toString();
    
    if ( ! email.matches("^((\"[\\w-\\s]+\")|([\\w-]+(?:\\.[\\w-]+)*)|(\"[\\w-\\s]+\")([\\w-]+(?:\\.[\\w-]+)*))(@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)$)|(@\\[?((25[0-5]\\.|2[0-4][0-9]\\.|1[0-9]{2}\\.|[0-9]{1,2}\\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\]?$)"))
    {
      et_email.setError("enter a valid email address");
      valid = false;
    }
    else
    {
      et_email.setError(null);
    }
    
    if ( ! password.matches("[^\\s]{3,20}"))
    {
      et_password.setError("between 3 and 20 alphanumeric characters without spaces");
      valid = false;
    }
    else
    {
      et_password.setError(null);
    }
    
    return valid;
  }
}
