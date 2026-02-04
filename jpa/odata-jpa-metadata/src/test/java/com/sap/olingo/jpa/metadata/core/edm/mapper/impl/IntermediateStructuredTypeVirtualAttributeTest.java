package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static jakarta.persistence.metamodel.Attribute.PersistentAttributeType.BASIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import jakarta.persistence.metamodel.ManagedType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntermediateStructuredTypeVirtualAttributeTest {

  private static final String COLUMN_NAME = "\"Test\"";
  private IntermediateStructuredType.VirtualAttribute<?, ?> cut;
  private ManagedType<?> managedType;

  @BeforeEach
  void setup() {
    managedType = mock(ManagedType.class);
    cut = new IntermediateStructuredType.VirtualAttribute<>(managedType, COLUMN_NAME);
  }

  @Test
  void checkGetJavaTypeReturnsNull() {
    assertNull(cut.getJavaType());
  }

  @Test
  void checkIsAssociationReturnsFalse() {
    assertFalse(cut.isAssociation());
  }

  @Test
  void checkIsCollectionReturnsFalse() {
    assertFalse(cut.isCollection());
  }

  @Test
  void checkPersistentAttributeTypeIsBasic() {
    assertEquals(BASIC, cut.getPersistentAttributeType());
  }

  @Test
  void checkGetNamerReturnsLowerCase() {
    assertEquals("test", cut.getName());
  }

  @Test
  void checkGetJavaMemberReturnsNull() {
    assertNull(cut.getJavaMember());
  }

  @Test
  void checkGetDeclaringTypeReturnsParent() {
    assertEquals(managedType, cut.getDeclaringType());
  }
}
