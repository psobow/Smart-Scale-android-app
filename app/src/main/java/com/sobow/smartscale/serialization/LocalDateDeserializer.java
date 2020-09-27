package com.sobow.smartscale.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.threeten.bp.LocalDate;

import java.io.IOException;

public class LocalDateDeserializer extends StdDeserializer<LocalDate>
{
  private static final long serialVersionUID = 1L;
  
  protected LocalDateDeserializer()
  {
    super(LocalDate.class);
  }
  
  
  @Override
  public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException
  {
    return LocalDate.parse(jp.readValueAs(String.class));
  }
  
}
