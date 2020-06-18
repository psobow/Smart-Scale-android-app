package com.sobow.smartscale.config;

public class WebConfig
{
  
  private static final String LOCAL_BASE_URL = "http://10.0.2.2:8080/v1";
  private static final String PRODUCTION_BASE_URL = "https://salty-shelf-82320.herokuapp.com/v1";
  
  private static final String USER_CONTROLLER = "/user";
  private static final String MEASUREMENT_CONTROLLER = "/measurement";
  
  private static final String MEASUREMENT_CREATE_ENDPOINT = "/create";
  
  private String currentBaseUrl;
  
  // Change configuration here!
  private Configuration currentConfiguration = Configuration.LOCAL;
  
  public WebConfig()
  {
    if (currentConfiguration.equals(Configuration.LOCAL))
    {
      currentBaseUrl = LOCAL_BASE_URL;
    }
    else if (currentConfiguration.equals(Configuration.PRODUCTION))
    {
      currentBaseUrl = PRODUCTION_BASE_URL;
    }
  }
  
  public String getUserControllerURL()
  {
    return currentBaseUrl + USER_CONTROLLER;
  }
  
  public String getMeasurementControllerURL()
  {
    return currentBaseUrl + MEASUREMENT_CONTROLLER;
  }
  
  public String getCreateMeasurementURL()
  {
    return currentBaseUrl + MEASUREMENT_CONTROLLER + MEASUREMENT_CREATE_ENDPOINT;
  }
  
  private enum Configuration
  {
    LOCAL, PRODUCTION
  }
}
