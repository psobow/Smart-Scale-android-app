package com.sobow.smartscale.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class CustomMapper
{
  private ObjectMapper mapper;
  
  public CustomMapper()
  {
    this.mapper = new ObjectMapper();
  }
  
  public String mapObjectToJSONString(Object value)
  {
    // map object to JSON string
    String objectJsonString = "";
    try
    {
      objectJsonString = mapper.writeValueAsString(value);
    }
    catch (JsonProcessingException e)
    {
      e.printStackTrace();
    }
    
    return objectJsonString;
  }
  
  public <T> T mapJSONStringToObject(String JSONString, Class<T> valueType)
  {
    T object = null;
    try
    {
      object = mapper.readValue(JSONString, valueType);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return object;
  }
}
