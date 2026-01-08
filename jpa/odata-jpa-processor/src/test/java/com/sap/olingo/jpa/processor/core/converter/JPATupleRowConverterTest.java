package com.sap.olingo.jpa.processor.core.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Tuple;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

class JPATupleRowConverterTest {
  private static final String SET_NAME = "TestSet";
  private static final String ENTITY_NAME = "TestEntity";
  private static final FullQualifiedName ENTITY_FQN = new FullQualifiedName("Namespace", ENTITY_NAME);
  private JPATupleRowConverter cut;
  private EdmEntityType edmEntity;
  private JPAEntityType jpaEntity;
  private JPAEntitySet jpaEntitySet;
  private JPAServiceDocument sd;
  private UriHelper uriHelper;
  private Edm edm;
  private ServiceMetadata serviceMetadata;
  private JPAODataRequestContextAccess requestContext;

  @BeforeEach
  void setup() throws ODataApplicationException, ODataJPAModelException {
    jpaEntity = mock(JPAEntityType.class);
    jpaEntitySet = mock(JPAEntitySet.class);
    sd = mock(JPAServiceDocument.class);
    uriHelper = mock(UriHelper.class);
    edm = mock(Edm.class);
    edmEntity = mock(EdmEntityType.class);
    serviceMetadata = mock(ServiceMetadata.class);
    requestContext = mock(JPAODataRequestContextAccess.class);

    when(serviceMetadata.getEdm()).thenReturn(edm);
    when(sd.getEntitySet(jpaEntity)).thenReturn(jpaEntitySet);
    when(jpaEntitySet.getExternalName()).thenReturn(SET_NAME);
    when(jpaEntity.getExternalFQN()).thenReturn(ENTITY_FQN);

    when(edm.getEntityType(ENTITY_FQN)).thenReturn(edmEntity);

    cut = new JPATupleRowConverter(jpaEntity, sd, uriHelper, serviceMetadata, requestContext);
  }

  @Test
  void testGetResultFromJPAExpandResultReturnsNull() throws ODataApplicationException {
    assertNull(cut.getResult(null, List.of()));
  }

  @Test
  void testCreateIdFromEntity() throws SerializerException, URISyntaxException {
    final var exp = new URI("TestSet('ABC')");
    final var entity = mock(Entity.class);
    when(uriHelper.buildKeyPredicate(edmEntity, entity)).thenReturn("'ABC'");
    final var act = cut.createId(entity);

    assertEquals(exp, act);
  }

  @Test
  void testCreateIdFromEntityRethrowsSerializerException() throws SerializerException {
    final var entity = mock(Entity.class);
    when(uriHelper.buildKeyPredicate(edmEntity, entity)).thenThrow(SerializerException.class);

    assertThrows(ODataRuntimeException.class, () -> cut.createId(entity));
  }

  @Test
  void testCreateIdFromEntityIgnoresIllegalArgumentException() throws SerializerException {
    final var entity = mock(Entity.class);
    when(uriHelper.buildKeyPredicate(edmEntity, entity)).thenThrow(IllegalArgumentException.class);

    assertNull(cut.createId(entity));
  }

  @Test
  void testCreateIdFromEntityRethrowsURISyntaxException() throws SerializerException {
    final var entity = mock(Entity.class);
    when(uriHelper.buildKeyPredicate(edmEntity, entity)).thenReturn("'A:BC'");

    assertThrows(ODataRuntimeException.class, () -> cut.createId(entity));
  }

  @Test
  void testCreateIdFromRow() throws SerializerException, URISyntaxException, ODataApplicationException,
      ODataJPAModelException {
    final var exp = new URI("TestSet('ABC')");
    final Entity odataEntity = new Entity();
    final Tuple row = mock(Tuple.class);
    when(jpaEntity.getKey()).thenReturn(List.of());
    when(uriHelper.buildKeyPredicate(edmEntity, odataEntity)).thenReturn("'ABC'");
    cut.createId(jpaEntity, row, odataEntity, new ArrayList<>());

    assertEquals(exp, odataEntity.getId());
  }

  @Test
  void testCreateIdFromRowRethrowsException() throws SerializerException, ODataJPAModelException {
    final Entity odataEntity = new Entity();
    final Tuple row = mock(Tuple.class);
    when(jpaEntity.getKey()).thenThrow(ODataJPAModelException.class);
    when(uriHelper.buildKeyPredicate(edmEntity, odataEntity)).thenReturn("'ABC'");
    final var act = assertThrows(ODataApplicationException.class, () -> cut.createId(jpaEntity, row, odataEntity,
        new ArrayList<>()));

    assertTrue(act.getCause() instanceof ODataJPAModelException);
  }

  @Test
  void testDetermineSetName() throws ODataJPAQueryException {
    assertEquals(SET_NAME, cut.determineSetName(jpaEntity));
  }

  @Test
  void testDetermineSetNameReturnsEmptyStringIfEsNotFoud() throws ODataJPAQueryException, ODataJPAModelException {
    when(sd.getEntitySet(jpaEntity)).thenReturn(null);
    assertEquals("", cut.determineSetName(jpaEntity));
  }

  @Test
  void testDetermineSetNameRethrowsException() throws ODataJPAModelException {
    when(sd.getEntitySet(jpaEntity)).thenThrow(ODataJPAModelException.class);
    final var act = assertThrows(ODataJPAQueryException.class, () -> cut.determineSetName(jpaEntity));
    assertTrue(act.getCause() instanceof ODataJPAModelException);
  }

  @Test
  void testCreateEtagRethrowsException() throws ODataJPAModelException {
    when(jpaEntity.hasEtag()).thenThrow(ODataJPAModelException.class);
    final var act = assertThrows(ODataJPAQueryException.class, () -> cut.createEtag(jpaEntity, null, null));
    assertTrue(act.getCause() instanceof ODataJPAModelException);
  }
}