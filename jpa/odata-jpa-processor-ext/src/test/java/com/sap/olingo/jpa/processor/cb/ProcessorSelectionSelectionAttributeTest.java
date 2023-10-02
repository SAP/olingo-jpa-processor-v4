package com.sap.olingo.jpa.processor.cb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.processor.cb.ProcessorSelection.SelectionAttribute;

class ProcessorSelectionSelectionAttributeTest {

  private SelectionAttribute cut;
  private JPAAttribute attribute;
  
  @BeforeEach
  void setup() {
    attribute = mock(JPAAttribute.class);
    cut = new SelectionAttribute("Test", attribute);
  }
  
  
  @Test
  void testGetKey() {
   assertEquals("Test", cut.getKey());
  }

  @Test
  void testGetValue() {
    assertEquals(attribute, cut.getValue());
  }

  @Test
  void testSetValueThrowsException() {
    assertThrows(IllegalAccessError.class, () -> cut.setValue(attribute));
  }
}
