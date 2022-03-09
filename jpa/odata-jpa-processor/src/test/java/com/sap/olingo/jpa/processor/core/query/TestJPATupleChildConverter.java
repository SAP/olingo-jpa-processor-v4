package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.converter.JPAExpandResult.ROOT_RESULT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;
import com.sap.olingo.jpa.processor.core.util.ServiceMetadataDouble;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;
import com.sap.olingo.jpa.processor.core.util.TupleDouble;
import com.sap.olingo.jpa.processor.core.util.UriHelperDouble;

class TestJPATupleChildConverter extends TestBase {
  public static final int NO_POSTAL_ADDRESS_FIELDS = 8;
  public static final int NO_ADMIN_INFO_FIELDS = 2;
  private JPATupleChildConverter cut;
  private List<Tuple> jpaQueryResult;
  private UriHelperDouble uriHelper;
  private Map<String, String> keyPredicates;
  private final HashMap<String, List<Tuple>> queryResult = new HashMap<>(1);
  private JPAODataRequestContextAccess requestContext;
  private JPAODataRequestContext context;
  private JPAODataSessionContextAccess sessionContext;

  @BeforeEach
  void setup() throws ODataException {
    helper = new TestHelper(emf, PUNIT_NAME);
    jpaQueryResult = new ArrayList<>();

    queryResult.put(ROOT_RESULT_KEY, jpaQueryResult);
    uriHelper = new UriHelperDouble();
    keyPredicates = new HashMap<>();
    uriHelper.setKeyPredicates(keyPredicates, "ID");
    context = mock(JPAODataRequestContext.class);
    sessionContext = mock(JPAODataSessionContextAccess.class);
    requestContext = new JPAODataInternalRequestContext(context, sessionContext);
    cut = new JPATupleChildConverter(helper.sd, uriHelper, new ServiceMetadataDouble(nameBuilder, "Organization"),
        requestContext);
  }

  @Test
  void checkConvertsEmptyResult() throws ODataApplicationException, ODataJPAModelException {

    assertNotNull(cut.getResult(new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("Organizations"),
        Collections.emptyList()), Collections.emptyList()));
  }

  @Test
  void checkConvertsOneResultOneElement() throws ODataApplicationException, ODataJPAModelException {
    final HashMap<String, Object> result = new HashMap<>();

    result.put("ID", new String("1"));
    jpaQueryResult.add(new TupleDouble(result));

    keyPredicates.put("1", "Organizations('1')");

    final EntityCollection act = cut.getResult(new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType(
        "Organizations"), Collections.emptyList()), Collections.emptyList()).get(ROOT_RESULT_KEY);
    assertEquals(1, act.getEntities().size());
    assertEquals("1", act.getEntities().get(0).getProperty("ID").getValue().toString());
  }

  @Test
  void checkConvertsOneResultOneKey() throws ODataApplicationException, ODataJPAModelException {
    final HashMap<String, Object> result = new HashMap<>();
    keyPredicates.put("1", "'1'");

    result.put("ID", new String("1"));
    jpaQueryResult.add(new TupleDouble(result));

    final EntityCollection act = cut.getResult(new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType(
        "Organizations"), Collections.emptyList()), Collections.emptyList()).get(ROOT_RESULT_KEY);
    assertEquals(1, act.getEntities().size());
    assertEquals("Organizations" + "('1')", act.getEntities().get(0).getId().getPath());
  }

  @Test
  void checkConvertsTwoResultsOneElement() throws ODataApplicationException, ODataJPAModelException {
    HashMap<String, Object> result;

    result = new HashMap<>();
    result.put("ID", new String("1"));
    jpaQueryResult.add(new TupleDouble(result));

    result = new HashMap<>();
    result.put("ID", new String("5"));
    jpaQueryResult.add(new TupleDouble(result));

    keyPredicates.put("1", "Organizations('1')");
    keyPredicates.put("5", "Organizations('5')");

    final EntityCollection act = cut.getResult(new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType(
        "Organizations"), Collections.emptyList()), Collections.emptyList()).get(ROOT_RESULT_KEY);
    assertEquals(2, act.getEntities().size());
    assertEquals("1", act.getEntities().get(0).getProperty("ID").getValue().toString());
    assertEquals("5", act.getEntities().get(1).getProperty("ID").getValue().toString());
  }

  @Test
  void checkConvertsOneResultsTwoElements() throws ODataApplicationException, ODataJPAModelException {
    HashMap<String, Object> result;

    result = new HashMap<>();
    result.put("ID", new String("1"));
    result.put("Name1", new String("Willi"));
    jpaQueryResult.add(new TupleDouble(result));

    keyPredicates.put("1", "Organizations('1')");

    final EntityCollection act = cut.getResult(new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType(
        "Organizations"), Collections.emptyList()), Collections.emptyList()).get(ROOT_RESULT_KEY);
    assertEquals(1, act.getEntities().size());
    assertEquals("1", act.getEntities().get(0).getProperty("ID").getValue().toString());
    assertEquals("Willi", act.getEntities().get(0).getProperty("Name1").getValue().toString());
  }

  @Test
  void checkConvertsOneResultsTwoElementsSelectionWithEtag() throws ODataApplicationException,
      ODataJPAModelException {

    cut = new JPATupleChildConverter(helper.sd, uriHelper, new ServiceMetadataDouble(nameBuilder,
        "BusinessPartnerProtected"), requestContext);
    HashMap<String, Object> result;
    result = new HashMap<>();
    result.put("ID", new String("1"));
    result.put("ETag", Integer.valueOf(2));
    jpaQueryResult.add(new TupleDouble(result));

    final EntityCollection act = cut.getResult(new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType(
        "BusinessPartnerProtecteds"), Collections.emptyList()), Collections.emptyList()).get(ROOT_RESULT_KEY);
    assertEquals(1, act.getEntities().size());
    assertEquals(1, act.getEntities().get(0).getProperties().size());
    assertEquals("1", act.getEntities().get(0).getProperties().get(0).getValue());
    assertEquals("ID", act.getEntities().get(0).getProperties().get(0).getName());
    assertEquals("2", act.getEntities().get(0).getETag());
  }

  @Test
  void checkConvertsOneResultsOneComplexElement() throws ODataApplicationException, ODataJPAModelException {
    HashMap<String, Object> result;

    result = new HashMap<>();
    result.put("ID", "1");
    result.put("Address/CityName", "Test City");
    result.put("Address/Country", "GB");
    result.put("Address/PostalCode", "ZE1 3AA");
    result.put("Address/StreetName", "Test Road");
    result.put("Address/HouseNumber", "123");
    result.put("Address/POBox", "155");
    result.put("Address/Region", "GB-12");
    result.put("Address/CountryName", "Willi");
    jpaQueryResult.add(new TupleDouble(result));

    keyPredicates.put("1", "Organizations('1')");

    final EntityCollection act = cut.getResult(new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType(
        "Organizations"), Collections.emptyList()), Collections.emptyList()).get(ROOT_RESULT_KEY);
    assertEquals(1, act.getEntities().size());

    assertEquals(ValueType.COMPLEX, act.getEntities().get(0).getProperty("Address").getValueType());
    final ComplexValue value = (ComplexValue) act.getEntities().get(0).getProperty("Address").getValue();
    assertEquals(NO_POSTAL_ADDRESS_FIELDS, value.getValue().size());
  }

  @Test
  void checkConvertsOneResultsOneNestedComplexElement() throws ODataApplicationException,
      ODataJPAModelException {
    HashMap<String, Object> result;

    result = new HashMap<>();
    result.put("ID", "1");
    result.put("AdministrativeInformation/Created/By", "Joe Doe");
    result.put("AdministrativeInformation/Created/At", "2016-01-22 12:25:23");
    result.put("AdministrativeInformation/Updated/By", "Joe Doe");
    result.put("AdministrativeInformation/Updated/At", "2016-01-24 14:29:45");
    jpaQueryResult.add(new TupleDouble(result));

    keyPredicates.put("1", "Organizations('1')");

    final EntityCollection act = cut.getResult(new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType(
        "Organizations"), Collections.emptyList()), Collections.emptyList()).get(ROOT_RESULT_KEY);
    assertEquals(1, act.getEntities().size());
    // Check first level
    assertEquals(ValueType.COMPLEX, act.getEntities().get(0).getProperty("AdministrativeInformation").getValueType());
    final ComplexValue value = (ComplexValue) act.getEntities().get(0).getProperty("AdministrativeInformation")
        .getValue();
    assertEquals(NO_ADMIN_INFO_FIELDS, value.getValue().size());
    // Check second level
    assertEquals(ValueType.COMPLEX, value.getValue().get(0).getValueType());
  }

  @Test
  void checkConvertsOneResultsOneElementOfComplexElement() throws ODataApplicationException,
      ODataJPAModelException {
    HashMap<String, Object> result;

    result = new HashMap<>();
    result.put("ID", "1");
    result.put("Address/Region", new String("CA"));
    jpaQueryResult.add(new TupleDouble(result));

    keyPredicates.put("1", "Organizations('1')");

    final EntityCollection act = cut.getResult(new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType(
        "Organizations"), Collections.emptyList()), Collections.emptyList()).get(ROOT_RESULT_KEY);
    assertEquals(1, act.getEntities().size());
    assertEquals("CA", ((ComplexValue) act.getEntities().get(0).getProperty("Address").getValue()).getValue().get(0)
        .getValue().toString());
  }

  @Test
  void checkConvertsOneResultPrimitiveIncludingTransient() throws ODataApplicationException,
      ODataJPAModelException {
    cut = new JPATupleChildConverter(helper.sd, uriHelper, new ServiceMetadataDouble(nameBuilder, "Person"),
        requestContext);
    final Map<String, Object> result;
    final Set<JPAPath> selection = new HashSet<>();
    final JPAEntityType et = helper.getJPAEntityType("Persons");
    result = new HashMap<>();
    result.put("ID", "1");
    result.put("FirstName", new String("Willi"));
    result.put("LastName", new String("Wichtig"));
    jpaQueryResult.add(new TupleDouble(result));
    selection.add(et.getPath("ID"));
    selection.add(et.getPath("FullName"));

    keyPredicates.put("1", "Persons('99')");

    final EntityCollection act = cut.getResult(new JPAExpandQueryResult(queryResult, null, et, Collections.emptyList()),
        selection).get(ROOT_RESULT_KEY);
    assertEquals(1, act.getEntities().size());
    assertEquals(2, act.getEntities().get(0).getProperties().size());
    assertEquals("Wichtig, Willi", act.getEntities().get(0).getProperty("FullName").getValue().toString());
  }

  @Test
  void checkConvertsOneResultComplexIncludingTransient() throws ODataApplicationException,
      ODataJPAModelException {
    final Map<String, Object> result;
    final Set<JPAPath> selection = new HashSet<>();
    final JPAEntityType et = helper.getJPAEntityType("Organizations");
    result = new HashMap<>();
    result.put("ID", "1");
    result.put("Address/Region", new String("CA"));
    result.put("Address/StreetName", new String("Test Road"));
    result.put("Address/HouseNumber", new String("1230"));
    jpaQueryResult.add(new TupleDouble(result));
    selection.add(et.getPath("ID"));
    selection.add(et.getPath("Address/Street"));

    keyPredicates.put("1", "Organizations('1')");

    final EntityCollection act = cut.getResult(new JPAExpandQueryResult(queryResult, null, et, Collections.emptyList()),
        selection).get(ROOT_RESULT_KEY);
    assertEquals(1, act.getEntities().size());
    assertEquals(2, act.getEntities().get(0).getProperties().size());
    assertEquals("Test Road 1230", ((ComplexValue) act.getEntities().get(0).getProperty("Address").getValue())
        .getValue().get(0)
        .getValue().toString());
  }

  @Test
  void checkConvertMediaStreamStaticMime() throws ODataJPAModelException, NumberFormatException,
      ODataApplicationException {

    final HashMap<String, List<Tuple>> result = new HashMap<>(1);
    result.put("root", jpaQueryResult);

    cut = new JPATupleChildConverter(helper.sd, uriHelper, new ServiceMetadataDouble(nameBuilder, "PersonImage"),
        requestContext);

    HashMap<String, Object> entityResult;
    final byte[] image = { -119, 10 };
    entityResult = new HashMap<>();
    entityResult.put("ID", "1");
    entityResult.put("Image", image);
    jpaQueryResult.add(new TupleDouble(entityResult));

    final EntityCollection act = cut.getResult(new JPAExpandQueryResult(result, null, helper.getJPAEntityType(
        "PersonImages"), Collections.emptyList()), Collections.emptyList()).get(ROOT_RESULT_KEY);

    assertEquals("image/png", act.getEntities().get(0).getMediaContentType());
  }

  @Test
  void checkConvertMediaStreamDynamicMime() throws ODataJPAModelException, NumberFormatException,
      ODataApplicationException {

    final HashMap<String, List<Tuple>> result = new HashMap<>(1);
    result.put("root", jpaQueryResult);

    cut = new JPATupleChildConverter(helper.sd, uriHelper, new ServiceMetadataDouble(nameBuilder,
        "OrganizationImage"), requestContext);

    HashMap<String, Object> entityResult;
    final byte[] image = { -119, 10 };
    entityResult = new HashMap<>();
    entityResult.put("ID", "9");
    entityResult.put("Image", image);
    entityResult.put("MimeType", "image/svg+xml");
    jpaQueryResult.add(new TupleDouble(entityResult));

    final EntityCollection act = cut.getResult(new JPAExpandQueryResult(result, null, helper.getJPAEntityType(
        "OrganizationImages"), Collections.emptyList()), Collections.emptyList()).get(ROOT_RESULT_KEY);
    assertEquals("image/svg+xml", act.getEntities().get(0).getMediaContentType());
    assertEquals(2, act.getEntities().get(0).getProperties().size());
  }
}
