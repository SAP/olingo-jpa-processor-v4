package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmGeospatial;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperation;

class IntermediateOperationResultParameterTest {

  private static final String WGS84 = "4326";
  private static final String NAMESPACE = "Test";
  private static final String FUNCTION_NAME = "Function";
  private static final int MAX_LENGTH = 10;
  private static final int PRECISION = 3;
  private static final int SCALE = 15;
  private IntermediateOperationResultParameter cut;
  private JPAOperation jpaOperation;
  private ReturnType jpaReturnType;
  private EdmGeospatial geospatial;
  private CsdlReturnType csdlReturnType;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @BeforeEach
  void setup() {
    jpaOperation = mock(JPAOperation.class);
    jpaReturnType = mock(ReturnType.class);
    csdlReturnType = mock(CsdlReturnType.class);
    geospatial = mock(EdmGeospatial.class);
    when(jpaOperation.getReturnType()).thenReturn(csdlReturnType);
    when(jpaReturnType.maxLength()).thenReturn(MAX_LENGTH);
    when(jpaReturnType.precision()).thenReturn(PRECISION);
    when(jpaReturnType.scale()).thenReturn(SCALE);
    when(jpaReturnType.isCollection()).thenReturn(Boolean.TRUE);
    when(jpaReturnType.srid()).thenReturn(geospatial);
    when(jpaReturnType.type()).thenReturn((Class) Integer.class);
    when(csdlReturnType.getTypeFQN()).thenReturn(new FullQualifiedName(NAMESPACE, FUNCTION_NAME));
    cut = new IntermediateOperationResultParameter(jpaOperation, jpaReturnType, Integer.class);
  }

  @Test
  void checkTypeIsReturned() {
    assertEquals(Integer.class, cut.getType());
  }

  @Test
  void checkMaxLength() {
    assertEquals(MAX_LENGTH, cut.getMaxLength());
  }

  @Test
  void checkPrecision() {
    assertEquals(PRECISION, cut.getPrecision());
  }

  @Test
  void checkScale() {
    assertEquals(SCALE, cut.getScale());
  }

  @Test
  void checkIsCollection() {
    assertEquals(Boolean.TRUE, cut.isCollection());
  }

  @Test
  void checkFullQualifiedName() {
    final FullQualifiedName act = cut.getTypeFQN();
    assertEquals(FUNCTION_NAME, act.getName());
    assertEquals(NAMESPACE, act.getNamespace());
  }

  @Test
  void checkSrid() {
    when(geospatial.srid()).thenReturn(WGS84);
    when(geospatial.dimension()).thenReturn(Dimension.GEOMETRY);
    final SRID act = cut.getSrid();
    final SRID exp = SRID.valueOf(WGS84);
    exp.setDimension(Dimension.GEOMETRY);
    assertEquals(exp, act);
  }

  @Test
  void checkSridNullIfNotProvided() {
    when(geospatial.srid()).thenReturn("");
    assertNull(cut.getSrid());
  }

  @Test
  void checkConstructorForJavaFunctionCollection() {
    cut = new IntermediateOperationResultParameter(jpaOperation, jpaReturnType, Collections.class, true);
    assertEquals(Integer.class, cut.getType());
  }

  @Test
  void checkConstructorForJavaFunctionSingle() {
    cut = new IntermediateOperationResultParameter(jpaOperation, jpaReturnType, String.class, false);
    assertEquals(String.class, cut.getType());
  }
}
