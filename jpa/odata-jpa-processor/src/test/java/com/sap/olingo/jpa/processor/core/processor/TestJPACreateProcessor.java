package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializerFactory;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionKey;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.FullNameCalculator;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

class TestJPACreateProcessor extends TestJPAModifyProcessor {

  @Test
  void testHookIsCalled() throws ODataJPAModelException, ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertTrue(spy.called);
  }

  @Test
  void testEntityTypeProvided() throws ODataJPAProcessorException, SerializerException, ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals("Organization", spy.et.getExternalName());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testAttributesProvided() throws ODataJPAProcessorException, SerializerException, ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();
    final Map<String, Object> attributes = new HashMap<>(1);

    attributes.put("ID", "35");

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    when(convHelper.convertProperties(ArgumentMatchers.any(OData.class), ArgumentMatchers.any(JPAStructuredType.class),
        ArgumentMatchers.any(
            List.class))).thenReturn(attributes);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertNotNull(spy.jpaAttributes);
    assertEquals(1, spy.jpaAttributes.size());
    assertEquals("35", spy.jpaAttributes.get("ID"));
  }

  @Test
  void testHeadersProvided() throws ODataJPAProcessorException, SerializerException, ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();
    final Map<String, List<String>> headers = new HashMap<>();

    when(request.getAllHeaders()).thenReturn(headers);
    headers.put("If-Match", Arrays.asList("2"));

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertNotNull(spy.headers);
    assertEquals(1, spy.headers.size());
    assertNotNull(spy.headers.get("If-Match"));
    assertEquals("2", spy.headers.get("If-Match").get(0));
  }

  @Test
  void testClaimsProvided() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    final JPAODataClaimProvider provider = new JPAODataClaimsProvider();
    final Optional<JPAODataClaimProvider> claims = Optional.of(provider);
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(requestContext.getClaimsProvider()).thenReturn(claims);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertNotNull(spy.claims);
    assertTrue(spy.claims.isPresent());
    assertEquals(provider, spy.claims.get());
  }

  @Test
  void testGroupsProvided() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    final JPAODataGroupsProvider provider = new JPAODataGroupsProvider();
    provider.addGroup("Person");
    // final List<String> groups = new ArrayList<>(Arrays.asList("Person"));
    final Optional<JPAODataGroupProvider> groups = Optional.of(provider);
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(requestContext.getGroupsProvider()).thenReturn(groups);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertNotNull(spy.groups);
    assertFalse(spy.groups.isEmpty());
    assertEquals("Person", spy.groups.get(0));
  }

  @Test
  void testThrowExpectedExceptionInCaseOfError() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).createEntity(any(JPARequestEntity.class), any(EntityManager.class));

    try {
      processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    } catch (final ODataApplicationException e) {
      assertEquals(HttpStatusCode.BAD_REQUEST.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  void testThrowUnexpectedExceptionInCaseOfError() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(NullPointerException.class).when(handler).createEntity(any(JPARequestEntity.class), any(
        EntityManager.class));

    try {
      processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    } catch (final ODataApplicationException e) {
      assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  void testMinimalResponseLocationHeader() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(LOCATION_HEADER, response.getHeader(HttpHeader.LOCATION));
  }

  @Test
  void testMinimalResponseODataEntityIdHeader() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(LOCATION_HEADER, response.getHeader(HttpHeader.ODATA_ENTITY_ID));
  }

  @Test
  void testMinimalResponseStatusCode() throws ODataJPAProcessorException, SerializerException, ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(HttpStatusCode.NO_CONTENT.getStatusCode(), response.getStatusCode());
  }

  @Test
  void testMinimalResponsePreferApplied() throws ODataJPAProcessorException, SerializerException,
      ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals("return=minimal", response.getHeader(HttpHeader.PREFERENCE_APPLIED));
  }

  @Test
  void testRepresentationResponseStatusCode() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareRepresentationRequest(new RequestHandleSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(HttpStatusCode.CREATED.getStatusCode(), response.getStatusCode());
  }

  @Test
  void testRepresentationResponseStatusCodeMapResult() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareRepresentationRequest(new RequestHandleMapResultSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(HttpStatusCode.CREATED.getStatusCode(), response.getStatusCode());
  }

  @Test
  void testRepresentationResponseContent() throws ODataJPAProcessorException, SerializerException,
      ODataException, IOException {

    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareRepresentationRequest(new RequestHandleSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    final byte[] act = new byte[100];
    response.getContent().read(act);
    final String s = new String(act).trim();
    assertEquals("{\"ID\":\"35\"}", s);
  }

  @Test
  void testPersonResponseContent() throws ODataJPAProcessorException, SerializerException,
      ODataException, IOException {
    final JPAODataSessionContextAccess context = mock(JPAODataSessionContextAccess.class);
    final EdmType propertyType = mock(EdmPrimitiveType.class);
    final EdmProperty propertyFN = createProperty("FullName", EdmString.getInstance());
    final EdmProperty propertyID = createProperty("ID", EdmString.getInstance());
    final EdmEntityType entityType = mock(EdmEntityType.class);
    final FullQualifiedName fqn = new FullQualifiedName("com.sap.olingo.jpa.Person");
    when(ets.getEntityType()).thenReturn(entityType);
    when(entityType.getFullQualifiedName()).thenReturn(fqn);
    when(entityType.getPropertyNames()).thenReturn(Arrays.asList("FullName", "ID"));
    when(entityType.getStructuralProperty("FullName")).thenReturn(propertyFN);
    when(entityType.getStructuralProperty("ID")).thenReturn(propertyID);

    when(propertyType.getKind()).thenReturn(EdmTypeKind.PRIMITIVE);
    final JPASerializer jpaSerializer = new JPASerializerFactory(odata, serviceMetadata, context)
        .createCUDSerializer(ContentType.APPLICATION_JSON, uriInfo, Optional.empty());

    when(requestContext.getSerializer()).thenReturn(jpaSerializer);
    when(requestContext.getCalculator(any())).thenReturn(Optional.of(new FullNameCalculator()));
    when(ets.getName()).thenReturn("Persons");
    processor = new JPACUDRequestProcessor(odata, serviceMetadata, requestContext, convHelper);
    final Person person = new Person();
    person.setID("35");
    person.setFirstName("Willi");
    person.setLastName("Wunder");
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = preparePersonRequest(new RequestHandleSpy(person));

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    final byte[] act = new byte[1000];
    response.getContent().read(act);
    final String s = new String(act).trim();
    assertTrue(s.contains("\"FullName\":\"Wunder, Willi\""));
  }

  @Test
  void testRepresentationResponseContentMapResult() throws ODataJPAProcessorException, SerializerException,
      ODataException, IOException {

    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareRepresentationRequest(new RequestHandleMapResultSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    final byte[] act = new byte[100];
    response.getContent().read(act);
    final String s = new String(act).trim();
    assertEquals("{\"ID\":\"35\"}", s);
  }

  @Test
  void testRepresentationLocationHeader() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareRepresentationRequest(new RequestHandleSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(LOCATION_HEADER, response.getHeader(HttpHeader.LOCATION));
  }

  @Test
  void testRepresentationLocationHeaderMapResult() throws ODataJPAProcessorException, SerializerException,
      ODataException {

    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareRepresentationRequest(new RequestHandleMapResultSpy());

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertEquals(LOCATION_HEADER, response.getHeader(HttpHeader.LOCATION));
  }

  @Test
  void testCallsValidateChangesOnSuccessfullProcessing() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    assertEquals(1, spy.noValidateCalls);
  }

  @Test
  void testDoesNotCallsValidateChangesOnForginTransaction() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(factory.hasActiveTransaction()).thenReturn(Boolean.TRUE);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    assertEquals(0, spy.noValidateCalls);
  }

  @Test
  void testDoesNotCallsValidateChangesOnError() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).createEntity(any(JPARequestEntity.class), any(EntityManager.class));

    assertThrows(ODataApplicationException.class,
        () -> processor.createEntity(request, response, ContentType.JSON, ContentType.JSON));
    verify(handler, never()).validateChanges(em);
  }

  @Test
  void testDoesRollbackIfValidateRaisesError() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.BAD_REQUEST)).when(handler).validateChanges(em);

    assertThrows(ODataApplicationException.class,
        () -> processor.createEntity(request, response, ContentType.JSON, ContentType.JSON));
    verify(transaction, never()).commit();
    verify(transaction, times(1)).rollback();
  }

  @Test
  void testDoesRollbackIfCreateRaisesArbitraryError() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);

    doThrow(new RuntimeException("Test")).when(handler).createEntity(any(), any());

    assertThrows(ODataApplicationException.class,
        () -> processor.createEntity(request, response, ContentType.JSON, ContentType.JSON));
    verify(transaction, never()).commit();
    verify(transaction, times(1)).rollback();
  }

  @Test
  void testDoesRollbackOnWrongResponse() throws ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();
    final String result = "";
    final JPACUDRequestHandler handler = mock(JPACUDRequestHandler.class);
    when(requestContext.getCUDRequestHandler()).thenReturn(handler);
    when(handler.createEntity(any(), any())).thenReturn(result);

    assertThrows(ODataException.class,
        () -> processor.createEntity(request, response, ContentType.JSON, ContentType.JSON));
    verify(transaction, never()).commit();
    verify(transaction, times(1)).rollback();
  }

  @Test
  void testOwnTransactionCommitted() throws ODataJPAModelException, ODataException {
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareSimpleRequest();

    final RequestHandleSpy spy = new RequestHandleSpy();
    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);
    verify(transaction, times(1)).commit();
  }

  @Test
  void testResponseCreateChildSameTypeContent() throws ODataJPAProcessorException, SerializerException,
      ODataException, IOException {

    when(ets.getName()).thenReturn("AdministrativeDivisions");
    final AdministrativeDivision div = new AdministrativeDivision(new AdministrativeDivisionKey("Eurostat", "NUTS1",
        "DE6"));
    final AdministrativeDivision child = new AdministrativeDivision(new AdministrativeDivisionKey("Eurostat", "NUTS2",
        "DE60"));
    div.getChildren().add(child);
    final RequestHandleSpy spy = new RequestHandleSpy(div);
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareRequestToCreateChild(spy);

    final UriResourceNavigation uriChild = mock(UriResourceNavigation.class);
    final List<UriParameter> uriKeys = new ArrayList<>();
    final EdmNavigationProperty naviProperty = mock(EdmNavigationProperty.class);

    createKeyPredicate(uriKeys, "DivisionCode", "DE6");
    createKeyPredicate(uriKeys, "CodeID", "NUTS1");
    createKeyPredicate(uriKeys, "CodePublisher", "Eurostat");
    when(uriChild.getKind()).thenReturn(UriResourceKind.navigationProperty);
    when(uriChild.getProperty()).thenReturn(naviProperty);
    when(naviProperty.getName()).thenReturn("Children");
    when(uriEts.getKeyPredicates()).thenReturn(uriKeys);
    when(convHelper.convertUriKeys(any(), any(), any())).thenCallRealMethod();
    when(convHelper.buildGetterMap(div)).thenReturn(new JPAConversionHelper().determineGetter(div));
    when(convHelper.buildGetterMap(child)).thenReturn(new JPAConversionHelper().determineGetter(child));
    pathParts.add(uriChild);

    processor = new JPACUDRequestProcessor(odata, serviceMetadata, requestContext, convHelper);
    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertNotNull(spy.requestEntity.getKeys());
    assertEquals("DE6", spy.requestEntity.getKeys().get("divisionCode"));
    assertNotNull(spy.requestEntity.getRelatedEntities());
    for (final Entry<JPAAssociationPath, List<JPARequestEntity>> c : spy.requestEntity.getRelatedEntities().entrySet())
      assertEquals("Children", c.getKey().getAlias());
  }

  @Test
  void testResponseCreateChildDifferentTypeContent() throws ODataJPAProcessorException, SerializerException,
      ODataException, IOException {

    final Organization org = new Organization("Test");
    final BusinessPartnerRole role = new BusinessPartnerRole();
    role.setBusinessPartner(org);
    role.setRoleCategory("A");
    org.getRoles().add(role);

    final RequestHandleSpy spy = new RequestHandleSpy(org);
    final ODataResponse response = new ODataResponse();
    final ODataRequest request = prepareRequestToCreateChild(spy);

    final UriResourceNavigation uriChild = mock(UriResourceNavigation.class);
    final List<UriParameter> uriKeys = new ArrayList<>();
    final EdmNavigationProperty naviProperty = mock(EdmNavigationProperty.class);
    final EdmNavigationPropertyBinding naviBinding = mock(EdmNavigationPropertyBinding.class);
    final EdmEntityContainer container = mock(EdmEntityContainer.class);
    final List<EdmNavigationPropertyBinding> naviBindings = new ArrayList<>(0);
    final EdmEntitySet targetEts = mock(EdmEntitySet.class);
    naviBindings.add(naviBinding);

    createKeyPredicate(uriKeys, "ID", "Test");
    when(uriChild.getKind()).thenReturn(UriResourceKind.navigationProperty);
    when(uriChild.getProperty()).thenReturn(naviProperty);
    when(naviProperty.getName()).thenReturn("Roles");
    when(uriEts.getKeyPredicates()).thenReturn(uriKeys);
    when(convHelper.convertUriKeys(any(), any(), any())).thenCallRealMethod();
    when(convHelper.buildGetterMap(org)).thenReturn(new JPAConversionHelper().determineGetter(org));
    when(convHelper.buildGetterMap(role)).thenReturn(new JPAConversionHelper().determineGetter(role));
    when(ets.getNavigationPropertyBindings()).thenReturn(naviBindings);
    when(naviBinding.getPath()).thenReturn("Roles");
    when(naviBinding.getTarget()).thenReturn("BusinessPartnerRoles");
    when(ets.getEntityContainer()).thenReturn(container);
    when(container.getEntitySet("BusinessPartnerRoles")).thenReturn(targetEts);

    final FullQualifiedName fqn = new FullQualifiedName("com.sap.olingo.jpa.BusinessPartnerRole");
    final List<String> keyNames = Arrays.asList("BusinessPartnerID", "RoleCategory");
    final Edm edm = mock(Edm.class);
    final EdmEntityType edmET = mock(EdmEntityType.class);

    when(serviceMetadata.getEdm()).thenReturn(edm);
    when(edm.getEntityType(fqn)).thenReturn(edmET);
    when(edmET.getKeyPredicateNames()).thenReturn(keyNames);
    createKeyProperty(fqn, edmET, "BusinessPartnerID", "Test");
    createKeyProperty(fqn, edmET, "RoleCategory", "A");
    // edmType.getFullQualifiedName().getFullQualifiedNameAsString()

    pathParts.add(uriChild);
    // return serviceMetadata.getEdm().getEntityType(es.getODataEntityType().getExternalFQN());
    // com.sap.olingo.jpa.BusinessPartnerRole
    processor = new JPACUDRequestProcessor(odata, serviceMetadata, requestContext, convHelper);
    processor.createEntity(request, response, ContentType.JSON, ContentType.JSON);

    assertNotNull(spy.requestEntity.getKeys());
    assertEquals("Test", spy.requestEntity.getKeys().get("iD"));
    assertNotNull(spy.requestEntity.getRelatedEntities());
    for (final Entry<JPAAssociationPath, List<JPARequestEntity>> c : spy.requestEntity.getRelatedEntities().entrySet())
      assertEquals("Roles", c.getKey().getAlias());
  }

  protected ODataRequest prepareRequestToCreateChild(final JPAAbstractCUDRequestHandler spy)
      throws ODataJPAProcessorException, SerializerException, ODataException {
    // .../AdministrativeDivisions(DivisionCode='DE6',CodeID='NUTS1',CodePublisher='Eurostat')/Children
    final ODataRequest request = prepareSimpleRequest("return=representation");

    final FullQualifiedName fqn = new FullQualifiedName("com.sap.olingo.jpa.AdministrativeDivision");
    final List<String> keyNames = Arrays.asList("DivisionCode", "CodeID", "CodePublisher");
    final Edm edm = mock(Edm.class);
    final EdmEntityType edmET = mock(EdmEntityType.class);

    when(requestContext.getCUDRequestHandler()).thenReturn(spy);

    when(serviceMetadata.getEdm()).thenReturn(edm);
    when(edm.getEntityType(fqn)).thenReturn(edmET);
    when(edmET.getKeyPredicateNames()).thenReturn(keyNames);

    createKeyProperty(fqn, edmET, "DivisionCode", "DE6");
    createKeyProperty(fqn, edmET, "CodeID", "NUTS1");
    createKeyProperty(fqn, edmET, "CodePublisher", "Eurostat");

    createKeyProperty(fqn, edmET, "DivisionCode", "DE60");
    createKeyProperty(fqn, edmET, "CodeID", "NUTS2");
    createKeyProperty(fqn, edmET, "CodePublisher", "Eurostat");

    when(serializer.serialize(ArgumentMatchers.eq(request), ArgumentMatchers.any(EntityCollection.class))).thenReturn(
        serializerResult);
    when(serializerResult.getContent()).thenReturn(new ByteArrayInputStream("{\"ID\":\"35\"}".getBytes()));

    return request;
  }

  private void createKeyPredicate(final List<UriParameter> uriKeys, final String name, final String value) {
    final UriParameter key = mock(UriParameter.class);
    uriKeys.add(key);
    when(key.getName()).thenReturn(name);
    when(key.getText()).thenReturn("'" + value + "'");
  }

  private void createKeyProperty(final FullQualifiedName fqn, final EdmEntityType edmET, final String name,
      final String value)
      throws EdmPrimitiveTypeException {
    final EdmKeyPropertyRef refType = mock(EdmKeyPropertyRef.class);
    when(edmET.getKeyPropertyRef(name)).thenReturn(refType);
    when(edmET.getFullQualifiedName()).thenReturn(fqn);
    final EdmProperty edmProperty = mock(EdmProperty.class);
    when(refType.getProperty()).thenReturn(edmProperty);
    when(refType.getName()).thenReturn(name);
    final EdmPrimitiveType type = mock(EdmPrimitiveType.class);
    when(edmProperty.getType()).thenReturn(type);
    when(type.valueToString(ArgumentMatchers.eq(value), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers
        .any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(value);
    when(type.toUriLiteral(ArgumentMatchers.anyString())).thenReturn(value);
  }

  private EdmProperty createProperty(final String name, final EdmType propertyType) {
    final EdmProperty property = mock(EdmProperty.class);
    when(property.isPrimitive()).thenReturn(true);
    when(property.getType()).thenReturn(propertyType);
    when(property.getName()).thenReturn(name);
    when(property.isNullable()).thenReturn(true);
    when(property.getMaxLength()).thenReturn(255);
    return property;
  }

  class RequestHandleSpy extends JPAAbstractCUDRequestHandler {
    public int noValidateCalls;
    public JPAEntityType et;
    public Map<String, Object> jpaAttributes;
    public EntityManager em;
    public boolean called = false;
    public Map<String, List<String>> headers;
    public JPARequestEntity requestEntity;
    private final Object result;
    public Optional<JPAODataClaimProvider> claims;
    public List<String> groups;

    RequestHandleSpy(final Object result) {
      this.result = result;
    }

    RequestHandleSpy() {
      this.result = new Organization();
      ((Organization) result).setID("35");
    }

    @Override
    public Object createEntity(final JPARequestEntity requestEntity, final EntityManager em)
        throws ODataJPAProcessException {

      this.et = requestEntity.getEntityType();
      this.jpaAttributes = requestEntity.getData();
      this.em = em;
      this.headers = requestEntity.getAllHeader();
      this.called = true;
      this.requestEntity = requestEntity;
      this.claims = requestEntity.getClaims();
      this.groups = requestEntity.getGroups();
      return result;
    }

    @Override
    public void validateChanges(final EntityManager em) throws ODataJPAProcessException {
      this.noValidateCalls++;
    }

  }

  class RequestHandleMapResultSpy extends JPAAbstractCUDRequestHandler {
    public JPAEntityType et;
    public Map<String, Object> jpaAttributes;
    public EntityManager em;
    public boolean called = false;
    public JPARequestEntity requestEntity;

    @Override
    public Object createEntity(final JPARequestEntity requestEntity, final EntityManager em)
        throws ODataJPAProcessException {
      final Map<String, Object> result = new HashMap<>();
      result.put("iD", "35");
      this.et = requestEntity.getEntityType();
      this.jpaAttributes = requestEntity.getData();
      this.em = em;
      this.called = true;
      this.requestEntity = requestEntity;
      return result;
    }
  }
}
