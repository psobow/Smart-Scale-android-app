package com.sobow.smartscale.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class MeasurementDto
{
  @JsonProperty("id")
  private long idFromServer;
  
  private LocalDateTime localDateTime;
  
  private double weight;
  
  private double BMI;
  
  private long userId;
  
  public MeasurementDto()
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
  
  public LocalDateTime getLocalDateTime()
  {
    return localDateTime;
  }
  
  public void setLocalDateTime(LocalDateTime localDateTime)
  {
    this.localDateTime = localDateTime;
  }
  
  public double getWeight()
  {
    return weight;
  }
  
  public void setWeight(double weight)
  {
    this.weight = weight;
  }
  
  public double getBMI()
  {
    return BMI;
  }
  
  public void setBMI(double BMI)
  {
    this.BMI = BMI;
  }
  
  public long getUserId()
  {
    return userId;
  }
  
  public void setUserId(long userId)
  {
    this.userId = userId;
  }
}

