package com.sobow.smartscale.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.threeten.bp.LocalDateTime;

import java.io.IOException;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime>
{
  private static final long serialVersionUID = 1L;
  
  protected LocalDateTimeDeserializer()
  {
    super(LocalDateTime.class);
  }
  
  
  @Override
  public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException
  {
    return LocalDateTime.parse(jp.readValueAs(String.class));
  }
  
}
