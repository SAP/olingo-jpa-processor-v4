package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;

class JPACollectionQueryResultTest {
  private JPACollectionQueryResult cut;
  private JPAAssociationPath associationPath;
  private JPAEntityType entityType;

  @BeforeEach
  void setup() {
    associationPath = mock(JPAAssociationPath.class);
    entityType = mock(JPAEntityType.class);
    cut = new JPACollectionQueryResult(entityType, associationPath, Collections.emptyList());
  }

  @Test
  void testGetSkipTokenReturnsNull() {
    assertNull(cut.getSkipToken(Collections.singletonList(new JPAODataPageExpandInfo("Test", "17"))));
  }
}
