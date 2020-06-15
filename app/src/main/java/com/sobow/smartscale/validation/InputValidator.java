package com.sobow.smartscale.validation;


// TODO: Implement validation logic here!
// TODO: Implement singleton
public class InputValidator
{
  // Remember to update strings.xml messages after changing constraints!!
  
  private static final String TAG = "UserDataActivity";
  
  private static final String EMAIL_REGEX = "^((\"[\\w-\\s]+\")|([\\w-]+(?:\\.[\\w-]+)*)|(\"[\\w-\\s]+\")([\\w-]+(?:\\.[\\w-]+)*))(@((?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.([a-z]{2,6}(?:\\.[a-z]{2})?)$)|(@\\[?((25[0-5]\\.|2[0-4][0-9]\\.|1[0-9]{2}\\.|[0-9]{1,2}\\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\]?$)";
  
  private static final String PASSWORD_REGEX = "[^\\s]{3,20}";
  
  private static final String USER_NAME_REGEX = "[A-Za-z0-9]{3,20}";
  
  private static final String SEX_REGEX = "Male|Female";
  
  private static final int AGE_LOWER_LIMIT = 1;
  
  private static final int AGE_UPPER_LIMIT = 150;
  
  private static final int HEIGHT_LOWER_LIMIT = 1;
  
  private static final int HEIGHT_UPPER_LIMIT = 300;
  
  public InputValidator()
  {
  }
  
  public boolean isEmailValid(String email)
  {
    return email.matches(EMAIL_REGEX);
  }
  
  public boolean isPasswordValid(String password)
  {
    return password.matches(PASSWORD_REGEX);
  }
  
  public boolean arePasswordsEquals(String password, String reEnteredPassword)
  {
    return password.equals(reEnteredPassword);
  }
  
  public boolean isUserNameValid(String userName)
  {
    return userName.matches(USER_NAME_REGEX);
  }
  
  public boolean isAgeValid(String age)
  {
    boolean isValid = true;
    int ageParsed = 0;
    try
    {
      ageParsed = Integer.parseInt(age);
    }
    catch (NumberFormatException e)
    {
      isValid = false;
    }
    
    return isValid ? (ageParsed >= AGE_LOWER_LIMIT && ageParsed <= AGE_UPPER_LIMIT) : false;
  }
  
  public boolean isHeightValid(String height)
  {
    boolean isValid = true;
    int heightParsed = 0;
    try
    {
      heightParsed = Integer.parseInt(height);
    }
    catch (NumberFormatException e)
    {
      isValid = false;
    }
    
    return isValid ? (heightParsed >= HEIGHT_LOWER_LIMIT && heightParsed <= HEIGHT_UPPER_LIMIT) : false;
  }
  
  public boolean isSexValid(String userSex)
  {
    return userSex.matches(SEX_REGEX);
  }
}
