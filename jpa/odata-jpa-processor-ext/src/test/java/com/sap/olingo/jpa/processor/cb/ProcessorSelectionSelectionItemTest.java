package com.sap.olingo.jpa.processor.cb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.cb.ProcessorSelection.SelectionItem;

class ProcessorSelectionSelectionItemTest {

  private SelectionItem cut;
  private JPAPath path;
  
  @BeforeEach
  void setup() {
    path = mock(JPAPath.class);
    cut = new SelectionItem("Test", path);
  }
  
  
  @Test
  void testGetKey() {
   assertEquals("Test", cut.getKey());
  }

  @Test
  void testGetValue() {
    assertEquals(path, cut.getValue());
  }

  @Test
  void testSetValueThrowsException() {
    assertThrows(IllegalAccessError.class, () -> cut.setValue(path));
  }
}
