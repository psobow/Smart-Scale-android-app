package com.sobow.smartscale.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sobow.smartscale.serialization.LocalDateDeserializer;
import com.sobow.smartscale.serialization.LocalDateSerializer;

import org.threeten.bp.LocalDate;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class UserDto implements Serializable
{
  @JsonProperty("id")
  private long idFromServer;
  
  private String userName;
  
  private int age;
  
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @JsonSerialize(using = LocalDateSerializer.class)
  private LocalDate birthDate;
  
  private int height;
  
  private String email;
  
  private String sex;
  
  private String password;
  
  private List<Long> measurementIds;
  
  public UserDto()
  {
  }
  
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
  
  public LocalDate getBirthDate()
  {
    return birthDate;
  }
  
  public void setBirthDate(LocalDate birthDate)
  {
    this.birthDate = birthDate;
  }
}
