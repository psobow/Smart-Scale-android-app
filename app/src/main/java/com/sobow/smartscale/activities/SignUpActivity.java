package com.sobow.smartscale.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sobow.smartscale.R;
import com.sobow.smartscale.config.WebConfig;
import com.sobow.smartscale.dto.UserDto;
import com.sobow.smartscale.mapper.CustomMapper;
import com.sobow.smartscale.validation.InputValidator;

import org.threeten.bp.LocalDate;

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

// TODO: Zmodyfikować sposób wprowadzenia wieku. zmienic na date urodzenia. trzy pola jedno pod drugim rok, miesiac, dzien

public class SignUpActivity extends AppCompatActivity
{
  private static final String TAG = "SignUpActivity";
  
  private OkHttpClient client;
  private CustomMapper mapper;
  private WebConfig webConfig;
  private InputValidator inputValidator;
  
  // GUI components
  @BindView(R.id.et_userName)
  EditText et_userName;
  @BindView(R.id.et_height)
  EditText et_height;
  @BindView(R.id.tv_chooseYourBirthDate)
  TextView tv_chooseBirthDate;
  @BindView(R.id.datePicker1)
  DatePicker datePicker;
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
  
  
  private void init()
  {
    client = new OkHttpClient();
    mapper = new CustomMapper();
    webConfig = new WebConfig();
    inputValidator = new InputValidator();
    
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
  
    spinner_sex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
    {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
      {
        tv_chooseYourSex.setError(null);
      }
    
      @Override
      public void onNothingSelected(AdapterView<?> parentView)
      {
        // your code here
      }
    
    });
  
    datePicker.init(LocalDate.now().getYear(),
                    LocalDate.now().getMonthValue() - 1,
                    LocalDate.now().getDayOfMonth(),
                    new DatePicker.OnDateChangedListener()
                    {
                      @Override
                      public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                      {
                        tv_chooseBirthDate.setError(null);
                      }
                    });
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signup);
    ButterKnife.bind(this);
  
    init();
  
    // buttons on click behavior
    btn_signUp.setOnClickListener(v -> signUp());
  
    link_sign_in.setOnClickListener(
        v ->
        {
          finishAndPushRight();
        });
  }
  
  
  private void signUp()
  {
    Log.d(TAG, "SignUp");
  
    // read input
    String email = et_email.getText().toString();
    String password = et_password.getText().toString();
    String reEnteredPassword = et_reEnterPassword.getText().toString();
    String name = et_userName.getText().toString();
    String height = et_height.getText().toString();
    LocalDate birthDate = LocalDate.of(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
  
    int spinnerChoicePosition = spinner_sex.getSelectedItemPosition();
    String sex = spinner_sex.getItemAtPosition(spinnerChoicePosition).toString();
  
    // TODO: add age validation
    if (! validate(email, password, reEnteredPassword, name, birthDate, height, sex))
    {
      return;
    }
    
    btn_signUp.setEnabled(false);
  
    // display loading component
  
    ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                                                       R.style.AppTheme_Dark_Dialog);
    progressDialog.setIndeterminate(true);
    progressDialog.setTitle(getString(R.string.progress_creating_account));
    progressDialog.setMessage(getString(R.string.progress_please_wait));
    progressDialog.setCancelable(false);
    progressDialog.show();
  
  
    new android.os.Handler().postDelayed(
        () ->
        {
  
          // initialize UserDto object
          UserDto newUser = new UserDto();
          newUser.setUserName(name);
          newUser.setHeight(Integer.parseInt(height));
          newUser.setSex(sex);
          newUser.setEmail(email);
          newUser.setPassword(password);
          newUser.setMeasurementIds(new ArrayList<>());
          newUser.setBirthDate(birthDate);
  
          // map object to JSON string
          String userJsonString = mapper.mapObjectToJSONString(newUser);
  
          // json request body
          RequestBody body = RequestBody.create(MediaType.parse(getString(R.string.json_media_type)), userJsonString);
  
          String requestUrl = webConfig.getUserControllerURL();
  
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
              onServerResponseFailure(e);
            }
  
            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
              if (response.isSuccessful())
              {
                String jsonString = response.body().string();
                SignUpActivity.this.runOnUiThread(() -> onSignUpSuccess(jsonString));
              }
              else
              {
                onSignUpFailure(response);
              }
            }
          });
  
          btn_signUp.setEnabled(true);
          progressDialog.dismiss();
        }, 3000);
  }
  
  private void onServerResponseFailure(IOException e)
  {
    SignUpActivity.this.runOnUiThread(
        () -> Toast.makeText(getBaseContext(), R.string.connection_with_server_failed, Toast.LENGTH_LONG)
                   .show());
    
    e.printStackTrace();
  }
  
  
  private void onSignUpSuccess(String jsonString)
  {
    UserDto userFromServer = mapper.mapJSONStringToObject(jsonString, UserDto.class);
    
    Intent intent = getIntent();
    intent.putExtra("user", userFromServer);
    
    setResult(RESULT_OK, intent);
    
    finish();
    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
  }
  
  private void onSignUpFailure(Response response)
  {
    if (response.code() == 400)
    {
      SignUpActivity.this.runOnUiThread(
          () ->
          {
            Toast.makeText(getBaseContext(), R.string.sign_up_failed, Toast.LENGTH_LONG).show();
            et_email.setError(getString(R.string.email_already_exists));
          });
    }
    else
    {
      SignUpActivity.this.runOnUiThread(
          () -> Toast.makeText(getBaseContext(),
                               getString(R.string.something_went_wrong, response.code()),
                               Toast.LENGTH_LONG).show());
      Log.d(TAG, "response code = " + response.code());
    }
  }
  
  private boolean validate(String email, String password, String reEnteredPassword, String userName,
                           LocalDate birthDate,
                           String height, String userSex)
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
  
    if (! inputValidator.arePasswordsEquals(password, reEnteredPassword))
    {
      isValid = false;
      et_reEnterPassword.setError(getString(R.string.passwords_do_not_match));
    }
    else { et_reEnterPassword.setError(null); }
  
    // USER NAME
    if (! inputValidator.isUserNameValid(userName))
    {
      isValid = false;
      et_userName.setError(getString(R.string.invalid_user_name));
    }
    else { et_userName.setError(null); }
  
    // AGE
    if (! inputValidator.isBirthDateValid(birthDate))
    {
      isValid = false;
      tv_chooseBirthDate.setError(getString(R.string.invalid_age));
    }
    else { tv_chooseBirthDate.setError(null); }
  
    // HEIGHT
    if (! inputValidator.isHeightValid(height))
    {
      isValid = false;
      et_height.setError(getString(R.string.invalid_height));
    }
    else { et_height.setError(null); }
  
    // SEX
    if (! inputValidator.isSexValid(userSex))
    {
      isValid = false;
      tv_chooseYourSex.setError(getString(R.string.invalid_sex_choice));
    }
    else { tv_chooseYourSex.setError(null); }
  
  
    return isValid;
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