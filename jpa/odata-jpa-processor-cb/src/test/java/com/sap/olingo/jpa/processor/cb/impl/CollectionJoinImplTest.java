package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

class CollectionJoinImplTest {

  private ProcessorCriteriaBuilder cb;
  private AliasBuilder ab;
  private FromImpl<?, ?> parent;
  private JPAPath path;
  private JPACollectionAttribute attribute;
  private JPAAssociationPath associationPath;
  private JPAJoinTable joinTable;
  private JPAJoinColumn joinColumn;
  private JPAEntityType targetType;
  private CollectionJoinImpl<?, ?> cut;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    ab = new AliasBuilder();
    parent = mock(FromImpl.class);
    path = mock(JPAPath.class);
    attribute = mock(JPACollectionAttribute.class);
    associationPath = mock(JPAAssociationPath.class);
    joinTable = mock(JPAJoinTable.class);
    joinColumn = mock(JPAJoinColumn.class);
    targetType = mock(JPAEntityType.class);
    when(path.getLeaf()).thenReturn(attribute);
    when(attribute.asAssociation()).thenReturn(associationPath);
    when(attribute.getInternalName()).thenReturn("Test");
    when(associationPath.getJoinTable()).thenReturn(joinTable);
    when(joinTable.getRawInverseJoinInformation()).thenReturn(Arrays.asList(joinColumn));
    when(joinTable.getEntityType()).thenReturn(targetType);
    when(targetType.getTypeClass()).thenAnswer(new ClassAnswer(Person.class));
    when(targetType.getInternalName()).thenReturn("Dummy");
    cut = new CollectionJoinImpl<>(path, parent, ab, cb, null);
  }

  @Test
  void testAsSql() throws ODataJPAModelException {
    final StringBuilder stmt = new StringBuilder();
    when(attribute.asAssociation()).thenThrow(ODataJPAModelException.class);
    assertThrows(IllegalStateException.class, () -> cut.asSQL(stmt));
  }

  @Test
  void testResolvePathElementsRethrowsException() throws ODataJPAModelException {
    when(attribute.getStructuredType()).thenThrow(ODataJPAModelException.class);
    when(attribute.isComplex()).thenReturn(true);
    assertThrows(IllegalStateException.class, () -> cut.resolvePathElements());
  }

  @Test
  void testGetPathListRethrowsException() throws ODataJPAModelException {
    when(attribute.getStructuredType()).thenThrow(ODataJPAModelException.class);
    when(attribute.isComplex()).thenReturn(true);
    assertThrows(IllegalStateException.class, () -> cut.getPathList());
  }

  @Test
  void testGetAttributeNotImplemented() {
    assertThrows(NotImplementedException.class, () -> cut.getAttribute());
  }
}
