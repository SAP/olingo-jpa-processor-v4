package com.sap.olingo.jpa.processor.core.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory.JPAODataTransaction;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;
import com.sap.olingo.jpa.processor.core.query.EdmBindingTargetInfo;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionKey;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.util.TestBase;

abstract class TestJPAModifyProcessor {
  protected static final String LOCATION_HEADER = "Organization('35')";
  protected static final String PREFERENCE_APPLIED = "return=minimal";
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  protected static EntityManagerFactory emf;
  protected static JPAEdmProvider jpaEdm;
  protected static DataSource ds;

  @BeforeAll
  public static void setupClass() throws ODataException {
    final JPAEdmMetadataPostProcessor pP = mock(JPAEdmMetadataPostProcessor.class);

    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
    jpaEdm = new JPAEdmProvider(PUNIT_NAME, emf.getMetamodel(), pP, TestBase.enumPackages);

  }

  protected JPACUDRequestProcessor processor;
  protected OData odata;
  protected ServiceMetadata serviceMetadata;
  protected JPAODataRequestContextAccess requestContext;
  protected UriInfo uriInfo;
  protected UriResourceEntitySet uriEts;
  protected EntityManager em;
  protected JPAODataTransaction transaction;
  protected JPASerializer serializer;
  protected EdmEntitySet ets;
  protected EdmBindingTargetInfo etsInfo;
  protected List<UriParameter> keyPredicates;
  protected JPAConversionHelper convHelper;
  protected List<UriResource> pathParts = new ArrayList<>();
  protected SerializerResult serializerResult;
  protected List<String> header = new ArrayList<>();
  protected JPAServiceDebugger debugger;
  protected JPAODataTransactionFactory factory;

  @BeforeEach
  public void setup() throws Exception {
    odata = OData.newInstance();
    requestContext = mock(JPAODataRequestContextAccess.class);
    serviceMetadata = mock(ServiceMetadata.class);
    uriInfo = mock(UriInfo.class);
    keyPredicates = new ArrayList<>();
    ets = mock(EdmEntitySet.class);
    etsInfo = mock(EdmBindingTargetInfo.class);
    serializer = mock(JPASerializer.class);
    uriEts = mock(UriResourceEntitySet.class);
    pathParts.add(uriEts);
    convHelper = mock(JPAConversionHelper.class);
    em = mock(EntityManager.class);
    transaction = mock(JPAODataTransaction.class);
    serializerResult = mock(SerializerResult.class);
    debugger = mock(JPAServiceDebugger.class);
    factory = mock(JPAODataTransactionFactory.class);

    when(requestContext.getEdmProvider()).thenReturn(jpaEdm);
    when(requestContext.getDebugger()).thenReturn(debugger);
    when(requestContext.getEntityManager()).thenReturn(em);
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(requestContext.getSerializer()).thenReturn(serializer);
    when(requestContext.getTransactionFactory()).thenReturn(factory);
    when(uriInfo.getUriResourceParts()).thenReturn(pathParts);
    when(uriEts.getKeyPredicates()).thenReturn(keyPredicates);
    when(uriEts.getEntitySet()).thenReturn(ets);
    when(uriEts.getKind()).thenReturn(UriResourceKind.entitySet);
    when(ets.getName()).thenReturn("Organizations");
    when(factory.createTransaction()).thenReturn(transaction);
    when(etsInfo.getEdmBindingTarget()).thenReturn(ets);
    processor = new JPACUDRequestProcessor(odata, serviceMetadata, requestContext, convHelper);

  }

  protected ODataRequest prepareRepresentationRequest(final JPAAbstractCUDRequestHandler spy)
      throws ODataJPAProcessorException, SerializerException, ODataException {

    final ODataRequest request = prepareSimpleRequest("return=representation");

    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    final Organization org = new Organization();
    when(em.find(Organization.class, "35")).thenReturn(org);
    org.setID("35");
    final Edm edm = mock(Edm.class);
    when(serviceMetadata.getEdm()).thenReturn(edm);
    final EdmEntityType edmET = mock(EdmEntityType.class);
    final FullQualifiedName fqn = new FullQualifiedName("com.sap.olingo.jpa.Organization");
    when(edm.getEntityType(fqn)).thenReturn(edmET);
    final List<String> keyNames = new ArrayList<>();
    keyNames.add("ID");
    when(edmET.getKeyPredicateNames()).thenReturn(keyNames);
    final EdmKeyPropertyRef refType = mock(EdmKeyPropertyRef.class);
    when(edmET.getKeyPropertyRef("ID")).thenReturn(refType);
    when(edmET.getFullQualifiedName()).thenReturn(fqn);
    final EdmProperty edmProperty = mock(EdmProperty.class);
    when(refType.getProperty()).thenReturn(edmProperty);
    when(refType.getName()).thenReturn("ID");
    final EdmPrimitiveType type = mock(EdmPrimitiveType.class);
    when(edmProperty.getType()).thenReturn(type);
    when(type.toUriLiteral(ArgumentMatchers.any())).thenReturn("35");

    when(serializer.serialize(ArgumentMatchers.eq(request), ArgumentMatchers.any(EntityCollection.class))).thenReturn(
        serializerResult);
    when(serializerResult.getContent()).thenReturn(new ByteArrayInputStream("{\"ID\":\"35\"}".getBytes()));

    return request;
  }

  protected ODataRequest prepareLinkRequest(final JPAAbstractCUDRequestHandler spy)
      throws ODataJPAProcessorException, SerializerException, ODataException {

    // .../AdministrativeDivisions(DivisionCode='DE60',CodeID='NUTS2',CodePublisher='Eurostat')
    final ODataRequest request = prepareSimpleRequest("return=representation");
    final Edm edm = mock(Edm.class);
    final EdmEntityType edmET = mock(EdmEntityType.class);

    final FullQualifiedName fqn = new FullQualifiedName("com.sap.olingo.jpa.AdministrativeDivision");
    final List<String> keyNames = new ArrayList<>();

    final AdministrativeDivisionKey key = new AdministrativeDivisionKey("Eurostat", "NUTS2", "DE60");
    final AdministrativeDivision div = new AdministrativeDivision(key);

    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    when(em.find(AdministrativeDivision.class, key)).thenReturn(div);
    when(serviceMetadata.getEdm()).thenReturn(edm);
    when(edm.getEntityType(fqn)).thenReturn(edmET);
    when(ets.getName()).thenReturn("AdministrativeDivisions");
    when(uriEts.getKeyPredicates()).thenReturn(keyPredicates);
    keyNames.add("DivisionCode");
    keyNames.add("CodeID");
    keyNames.add("CodePublisher");
    when(edmET.getKeyPredicateNames()).thenReturn(keyNames);
    when(edmET.getFullQualifiedName()).thenReturn(fqn);

    final EdmPrimitiveType type = EdmString.getInstance();
    EdmKeyPropertyRef refType = mock(EdmKeyPropertyRef.class);
    EdmProperty edmProperty = mock(EdmProperty.class);
    when(edmET.getKeyPropertyRef("DivisionCode")).thenReturn(refType);
    when(refType.getProperty()).thenReturn(edmProperty);
    when(refType.getName()).thenReturn("DivisionCode");
    when(edmProperty.getType()).thenReturn(type);
    when(edmProperty.getMaxLength()).thenReturn(50);

    refType = mock(EdmKeyPropertyRef.class);
    edmProperty = mock(EdmProperty.class);
    when(edmET.getKeyPropertyRef("CodeID")).thenReturn(refType);
    when(refType.getProperty()).thenReturn(edmProperty);
    when(refType.getName()).thenReturn("CodeID");
    when(edmProperty.getType()).thenReturn(type);
    when(edmProperty.getMaxLength()).thenReturn(50);

    refType = mock(EdmKeyPropertyRef.class);
    edmProperty = mock(EdmProperty.class);
    when(edmET.getKeyPropertyRef("CodePublisher")).thenReturn(refType);
    when(refType.getProperty()).thenReturn(edmProperty);
    when(refType.getName()).thenReturn("CodePublisher");
    when(edmProperty.getType()).thenReturn(type);
    when(edmProperty.getMaxLength()).thenReturn(50);

    when(serializer.serialize(ArgumentMatchers.eq(request), ArgumentMatchers.any(EntityCollection.class))).thenReturn(
        serializerResult);
    when(serializerResult.getContent()).thenReturn(new ByteArrayInputStream("{\"ParentCodeID\":\"NUTS1\"}".getBytes()));

    return request;

  }

  protected ODataRequest prepareSimpleRequest() throws ODataException, ODataJPAProcessorException, SerializerException {

    return prepareSimpleRequest("return=minimal");
  }

  @SuppressWarnings("unchecked")
  protected ODataRequest prepareSimpleRequest(final String content) throws ODataException, ODataJPAProcessorException,
      SerializerException {

    final EntityTransaction transaction = mock(EntityTransaction.class);
    when(em.getTransaction()).thenReturn(transaction);

    final ODataRequest request = mock(ODataRequest.class);
    when(request.getHeaders(HttpHeader.PREFER)).thenReturn(header);
    when(requestContext.getEdmProvider()).thenReturn(jpaEdm);
    when(etsInfo.getEdmBindingTarget()).thenReturn(ets);
    header.add(content);

    final Entity odataEntity = mock(Entity.class);
    when(convHelper.convertInputStream(same(odata), same(request), same(ContentType.JSON), any(List.class)))
        .thenReturn(odataEntity);
    when(convHelper.convertKeyToLocal(ArgumentMatchers.eq(odata), ArgumentMatchers.eq(request), ArgumentMatchers.eq(
        ets), ArgumentMatchers.any(JPAEntityType.class), ArgumentMatchers.any())).thenReturn(LOCATION_HEADER);
    return request;
  }

  protected ODataRequest preparePersonRequest(final JPAAbstractCUDRequestHandler spy)
      throws ODataJPAProcessorException, SerializerException, ODataException {

    final ODataRequest request = prepareSimpleRequest("return=representation");

    when(requestContext.getCUDRequestHandler()).thenReturn(spy);
    final Person person = new Person();
    when(em.find(Person.class, "35")).thenReturn(person);
    person.setID("35");
    final Edm edm = mock(Edm.class);
    when(serviceMetadata.getEdm()).thenReturn(edm);
    final EdmEntityType edmET = mock(EdmEntityType.class);
    final FullQualifiedName fqn = new FullQualifiedName("com.sap.olingo.jpa.Person");
    when(edm.getEntityType(fqn)).thenReturn(edmET);
    final List<String> keyNames = new ArrayList<>();
    keyNames.add("ID");
    when(edmET.getKeyPredicateNames()).thenReturn(keyNames);
    final EdmKeyPropertyRef refType = mock(EdmKeyPropertyRef.class);
    when(edmET.getKeyPropertyRef("ID")).thenReturn(refType);
    when(edmET.getFullQualifiedName()).thenReturn(fqn);
    final EdmProperty edmProperty = mock(EdmProperty.class);
    when(refType.getProperty()).thenReturn(edmProperty);
    when(refType.getName()).thenReturn("ID");
    final EdmPrimitiveType type = mock(EdmPrimitiveType.class);
    when(edmProperty.getType()).thenReturn(type);
    when(type.toUriLiteral(ArgumentMatchers.any())).thenReturn("35");

    when(serializer.serialize(ArgumentMatchers.eq(request), ArgumentMatchers.any(EntityCollection.class))).thenReturn(
        serializerResult);
    when(serializerResult.getContent()).thenReturn(new ByteArrayInputStream("{\"ID\":\"35\"}".getBytes()));

    return request;
  }
}
