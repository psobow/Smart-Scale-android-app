package com.sobow.smartscale;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity
{
  private static final String TAG = "SignupActivity";
  private static final String DEFAULT_SPINNER_CHOICE = "Your choice...";
  private static final String FIRST_SPINNER_CHOICE = "Male";
  private static final String SECOND_SPINNER_CHOICE = "Female";
  
  @BindView(R.id.input_name)
  EditText _nameText;
  @BindView(R.id.input_age)
  EditText _addressText;
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
    spinnerValues.add(DEFAULT_SPINNER_CHOICE);
    spinnerValues.add(FIRST_SPINNER_CHOICE);
    spinnerValues.add(SECOND_SPINNER_CHOICE);
  
    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerValues);
    // Specify the layout to use when the list of choices appears
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    _sexSpinner.setAdapter(dataAdapter);
    
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
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
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
    String address = _addressText.getText().toString();
    String email = _emailText.getText().toString();
    String password = _passwordText.getText().toString();
    String reEnterPassword = _reEnterPasswordText.getText().toString();
    
    // TODO: Implement your own signup logic here.
    
    new android.os.Handler().postDelayed(
        new Runnable()
        {
          public void run()
          {
            // On complete call either onSignupSuccess or onSignupFailed
            // depending on success
            onSignupSuccess();
            // onSignupFailed();
            progressDialog.dismiss();
          }
        }, 3000);
  }
  
  
  public void onSignupSuccess()
  {
    _signupButton.setEnabled(true);
    setResult(RESULT_OK, null);
    finish();
  }
  
  public void onSignupFailed()
  {
    Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
    
    _signupButton.setEnabled(true);
  }
  
  public boolean validate()
  {
    boolean valid = true;
    
    String name = _nameText.getText().toString();
    String address = _addressText.getText().toString();
    String email = _emailText.getText().toString();
    String password = _passwordText.getText().toString();
    String reEnterPassword = _reEnterPasswordText.getText().toString();
    
    if (_sexSpinner.getSelectedItemPosition() == 0)
    {
      _sexText.setError("Choose your sex!");
    }
    
    if (name.isEmpty() || name.length() < 3)
    {
      _nameText.setError("at least 3 characters");
      valid = false;
    }
    else
    {
      _nameText.setError(null);
    }
    
    if (address.isEmpty())
    {
      _addressText.setError("Enter Valid Address");
      valid = false;
    }
    else
    {
      _addressText.setError(null);
    }
    
    
    if (email.isEmpty() || ! android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
    {
      _emailText.setError("enter a valid email address");
      valid = false;
    }
    else
    {
      _emailText.setError(null);
    }
    
    
    if (password.isEmpty() || password.length() < 4 || password.length() > 10)
    {
      _passwordText.setError("between 4 and 10 alphanumeric characters");
      valid = false;
    }
    else
    {
      _passwordText.setError(null);
    }
    
    if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || ! (reEnterPassword
        .equals(password)))
    {
      _reEnterPasswordText.setError("Password Do not match");
      valid = false;
    }
    else
    {
      _reEnterPasswordText.setError(null);
    }
    
    return valid;
  }
}