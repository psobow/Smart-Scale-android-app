package com.sobow.smartscale.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sobow.smartscale.serialization.LocalDateTimeDeserializer;
import com.sobow.smartscale.serialization.LocalDateTimeSerializer;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.Serializable;

public class MeasurementDto implements Serializable
{
  private static final String DATE_FORMATTER = "yyyy-MM-dd HH:mm";
  
  @JsonProperty("id")
  private long idFromServer;
  
  
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
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
  
  @Override
  public String toString()
  {
    return "Date Time: " + localDateTime.format(DateTimeFormatter.ofPattern(DATE_FORMATTER)) +
        ", weight: " + String.format("%.1f", weight) + " kg" +
        ", BMI: " + String.format("%.1f", BMI);
  }
}

