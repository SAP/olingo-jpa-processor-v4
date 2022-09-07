package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute.CollectionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateStructuredTypeTransientPluralAttributeTest extends TestMappingRoot {

  private IntermediateStructuredType.TransientPluralAttribute<?, ?, ?> cut;
  private ManagedType<?> managedType;
  private Field attribute;
  private TestHelper helper;
  @SuppressWarnings("unused")
  private Map<String, String> mapAttribute;
  @SuppressWarnings("unused")
  private Set<String> setAttribute;
  @SuppressWarnings("unused")
  private List<String> listAttribute;
  @SuppressWarnings("unused")
  private Collection<String> collAttribute;

  @BeforeEach
  void setup() throws ODataJPAModelException, NoSuchFieldException, SecurityException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    managedType = mock(ManagedType.class);
  }

  @TestFactory
  Collection<DynamicTest> checkGetCollectionType() {
    return Arrays.asList(
        dynamicTest("Map", () -> executeGetCollectionTypeCheck(CollectionType.MAP, "mapAttribute")),
        dynamicTest("Set", () -> executeGetCollectionTypeCheck(CollectionType.SET, "setAttribute")),
        dynamicTest("List", () -> executeGetCollectionTypeCheck(CollectionType.LIST, "listAttribute")),
        dynamicTest("Default", () -> executeGetCollectionTypeCheck(CollectionType.COLLECTION, "collAttribute")));
  }

  void executeGetCollectionTypeCheck(final CollectionType type, final String attributeName) {
    try {
      attribute = this.getClass().getDeclaredField(attributeName);
      cut = new IntermediateStructuredType.TransientPluralAttribute<>(managedType, attribute, helper.schema);
      assertEquals(type, cut.getCollectionType());
    } catch (NoSuchFieldException | SecurityException e) {
      fail();
    }
  }
}
