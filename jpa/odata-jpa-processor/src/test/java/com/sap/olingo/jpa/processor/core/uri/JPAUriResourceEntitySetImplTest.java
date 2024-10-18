package com.sap.olingo.jpa.processor.core.uri;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;

class JPAUriResourceEntitySetImplTest {

  private static final String TO_STRING = "ToString";
  private static final String SEGMENT_VALUE = "SegmentValue";

  private JPAUriResourceEntitySetImpl cut;
  private UriResourceEntitySet es;
  private JPAODataPageExpandInfo expandInfo;
  private EdmType type;
  private EdmEntityType entityType;
  private EdmEntitySet entitySet;
  private EdmKeyPropertyRef keyReference;
  private EdmType filterType;

  @BeforeEach
  void setup() {
    type = mock(EdmType.class);
    entityType = mock(EdmEntityType.class);
    entitySet = mock(EdmEntitySet.class);
    es = mock(UriResourceEntitySet.class);
    keyReference = mock(EdmKeyPropertyRef.class);
    expandInfo = new JPAODataPageExpandInfo("Test", UUID.randomUUID().toString());
    when(es.getType()).thenReturn(type);
    when(es.getEntityType()).thenReturn(entityType);
    when(es.getEntitySet()).thenReturn(entitySet);
    when(es.toString(true)).thenReturn(TO_STRING);
    when(es.getSegmentValue(true)).thenReturn(SEGMENT_VALUE);
    when(es.getSegmentValue()).thenReturn(SEGMENT_VALUE);
    when(es.getKind()).thenReturn(UriResourceKind.entitySet);
    when(es.getTypeFilterOnCollection()).thenReturn(filterType);
    when(es.getTypeFilterOnEntry()).thenReturn(filterType);
    when(entityType.getKeyPropertyRefs()).thenReturn(singletonList(keyReference));
    cut = new JPAUriResourceEntitySetImpl(es, expandInfo);
  }

  @Test
  void testGetType() {
    assertEquals(type, cut.getType());
  }

  @Test
  void testIsCollectionReturnsFalse() {
    assertFalse(cut.isCollection());
  }

  @Test
  void testToStringTrue() {
    assertEquals(TO_STRING, cut.toString(true));
  }

  @Test
  void testGetSegmentValueTrue() {
    assertEquals(SEGMENT_VALUE, cut.getSegmentValue(true));
  }

  @Test
  void testGetSegmentValue() {
    assertEquals(SEGMENT_VALUE, cut.getSegmentValue());
  }

  @Test
  void testGetKind() {
    assertEquals(UriResourceKind.entitySet, cut.getKind());
  }

  @Test
  void testGetEntityType() {
    assertEquals(entityType, cut.getEntityType());
  }

  @Test
  void testGetEntitySet() {
    assertEquals(entitySet, cut.getEntitySet());
  }

  @Test
  void testGetKeyPredicates() {
    assertFalse(cut.getKeyPredicates().isEmpty());
  }

  @Test
  void testGetTypeFilterOnCollection() {
    assertEquals(filterType, cut.getTypeFilterOnCollection());
  }

  @Test
  void testGetTypeFilterOnEntry() {
    assertEquals(filterType, cut.getTypeFilterOnEntry());
  }
}
