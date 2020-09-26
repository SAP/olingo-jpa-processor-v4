package com.sap.olingo.jpa.processor.core.serializer;

import static org.mockito.Mockito.when;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

public abstract class TestJPAOperationSerializer extends TestJPASerializer {

  @Test
  public void testAnnotatableContextUrlFilledForAbsoluteRequested() throws SerializerException,
      ODataJPASerializerException {
    when(context.useAbsoluteContextURL()).thenReturn(true);
    when(request.getRawBaseUri()).thenReturn("localhost:8080/v1/");
    ((JPAOperationSerializer) cut).serialize(annotatable, getType(), request);
    verifySerializerCall(serializer, "localhost:8080/v1/");
  }

  @Test
  public void testAnnotatableContextUrlFilledForAbsoluteRequestedWithOutSlash() throws SerializerException,
      ODataJPASerializerException {
    when(context.useAbsoluteContextURL()).thenReturn(true);
    when(request.getRawBaseUri()).thenReturn("localhost:8080/v1");
    ((JPAOperationSerializer) cut).serialize(annotatable, getType(), request);
    verifySerializerCall(serializer, "localhost:8080/v1/");
  }

  @Test
  public void testAnnotatableContextUrlNullForRelativeRequested() throws SerializerException,
      ODataJPASerializerException {
    when(context.useAbsoluteContextURL()).thenReturn(false);
    ((JPAOperationSerializer) cut).serialize(annotatable, getType(), request);
    verifySerializerCall(serializer, null);
  }

  protected abstract EdmType getType();
}