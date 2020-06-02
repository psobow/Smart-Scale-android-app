package com.sobow.smartscale.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDto
{

  @JsonProperty("id")
  private long idFromServer;
  
  private String userName;
  
  private int age;
  
  private int height;
  
  private String email;
  
  private String sex;
  
  private String password;
  
  private List<Long> measurementIds;
  
  public long getIdFromServer()
  {
    return idFromServer;
  }
  
  public void setIdFromServer(long idFromServer)
  {
    this.idFromServer = idFromServer;
  }
  
  public String getUserName()
  {
    return userName;
  }
  
  public void setUserName(String userName)
  {
    this.userName = userName;
  }
  
  public int getAge()
  {
    return age;
  }
  
  public void setAge(int age)
  {
    this.age = age;
  }
  
  public int getHeight()
  {
    return height;
  }
  
  public void setHeight(int height)
  {
    this.height = height;
  }
  
  public String getEmail()
  {
    return email;
  }
  
  public void setEmail(String email)
  {
    this.email = email;
  }
  
  public String getSex()
  {
    return sex;
  }
  
  public void setSex(String sex)
  {
    this.sex = sex;
  }
  
  public String getPassword()
  {
    return password;
  }
  
  public void setPassword(String password)
  {
    this.password = password;
  }
  
  public List<Long> getMeasurementIds()
  {
    return measurementIds;
  }
  
  public void setMeasurementIds(List<Long> measurementIds)
  {
    this.measurementIds = measurementIds;
  }
}
