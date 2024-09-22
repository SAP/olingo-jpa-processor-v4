package com.sap.olingo.jpa.processor.core.uri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGuid;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JPAUriParameterImplTest {

  private static final String ALIAS = "Alias";
  private static final String NAME = "Name";

  private JPAUriParameterImpl cut;
  private EdmKeyPropertyRef propertyReference;
  private EdmProperty property;

  @BeforeEach
  void setup() {
    propertyReference = mock(EdmKeyPropertyRef.class);
    property = mock(EdmProperty.class);
    when(propertyReference.getAlias()).thenReturn(ALIAS);
    when(propertyReference.getName()).thenReturn(NAME);
    when(propertyReference.getProperty()).thenReturn(property);
    when(property.getType()).thenReturn(EdmString.getInstance());
    cut = new JPAUriParameterImpl(propertyReference, "Test");
  }

  @Test
  void testGetReferencedPropertyIsNull() {
    assertNull(cut.getReferencedProperty());
  }

  @Test
  void testGetAlias() {
    assertEquals(ALIAS, cut.getAlias());
  }

  @Test
  void testGetName() {
    assertEquals(NAME, cut.getName());
  }

  @Test
  void testGetExpressionIsNull() {
    assertNull(cut.getExpression());
  }

  @Test
  void testGetTextString() {
    assertEquals("'Test'", cut.getText());
  }

  @Test
  void testGetTextUUID() {
    final var id = UUID.randomUUID();
    when(property.getType()).thenReturn(EdmGuid.getInstance());
    cut = new JPAUriParameterImpl(propertyReference, id.toString());
    assertEquals(id.toString(), cut.getText());
  }
}
