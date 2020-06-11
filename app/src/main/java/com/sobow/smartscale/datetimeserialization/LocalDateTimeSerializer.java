package com.sobow.smartscale.datetimeserialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;

public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime>
{
  private static final long serialVersionUID = 1L;
  
  public LocalDateTimeSerializer()
  {
    super(LocalDateTime.class);
  }
  
  @Override
  public void serialize(LocalDateTime value, JsonGenerator gen,
                        SerializerProvider sp) throws IOException, JsonProcessingException
  {
    gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
  }
}
