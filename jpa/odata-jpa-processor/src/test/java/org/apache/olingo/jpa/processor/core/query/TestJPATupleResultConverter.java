package org.apache.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.util.EdmEntitySetDouble;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.apache.olingo.jpa.processor.core.util.TestHelper;
import org.apache.olingo.jpa.processor.core.util.TupleDouble;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.Before;
import org.junit.Test;

public class TestJPATupleResultConverter extends TestBase {
  public static final int NO_POSTAL_ADDRESS_FIELDS = 8;
  public static final int NO_ADMIN_INFO_FIELDS = 2;
  private JPATupleResultConverter cut;
  private List<Tuple> jpaQueryResult;

  @Before
  public void setup() throws ODataJPAModelException, ODataApplicationException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    jpaQueryResult = new ArrayList<Tuple>();
    HashMap<String, List<Tuple>> result = new HashMap<String, List<Tuple>>(1);
    result.put("root", jpaQueryResult);

    cut = new JPATupleResultConverter(
        new EdmEntitySetDouble(nameBuilder, "Organizations"),
        helper.sd,
        new JPAExpandResult(result, Long.parseLong("0")));
  }

  @Test
  public void checkConvertsEmptyResult() throws ODataApplicationException {
    assertNotNull(cut.getResult());
  }

  @Test
  public void checkConvertsOneResultOneElement() throws ODataApplicationException {
    HashMap<String, Object> result = new HashMap<String, Object>();

    result.put("ID", new String("1"));
    jpaQueryResult.add(new TupleDouble(result));

    EntityCollection act = cut.getResult();
    assertEquals(1, act.getEntities().size());
    assertEquals("1", act.getEntities().get(0).getProperty("ID").getValue().toString());
  }

  @Test
  public void checkConvertsOneResultOneKey() throws ODataApplicationException {
    HashMap<String, Object> result = new HashMap<String, Object>();

    result.put("ID", new String("1"));
    jpaQueryResult.add(new TupleDouble(result));

    EntityCollection act = cut.getResult();
    assertEquals(1, act.getEntities().size());
    assertEquals("Organizations" + "('1')", act.getEntities().get(0).getId().getPath());
  }

  @Test
  public void checkConvertsTwoResultsOneElement() throws ODataApplicationException {
    HashMap<String, Object> result;

    result = new HashMap<String, Object>();
    result.put("ID", new String("1"));
    jpaQueryResult.add(new TupleDouble(result));

    result = new HashMap<String, Object>();
    result.put("ID", new String("5"));
    jpaQueryResult.add(new TupleDouble(result));

    EntityCollection act = cut.getResult();
    assertEquals(2, act.getEntities().size());
    assertEquals("1", act.getEntities().get(0).getProperty("ID").getValue().toString());
    assertEquals("5", act.getEntities().get(1).getProperty("ID").getValue().toString());
  }

  @Test
  public void checkConvertsOneResultsTwoElements() throws ODataApplicationException {
    HashMap<String, Object> result;

    result = new HashMap<String, Object>();
    result.put("ID", new String("1"));
    result.put("Name1", new String("Willi"));
    jpaQueryResult.add(new TupleDouble(result));

    EntityCollection act = cut.getResult();
    assertEquals(1, act.getEntities().size());
    assertEquals("1", act.getEntities().get(0).getProperty("ID").getValue().toString());
    assertEquals("Willi", act.getEntities().get(0).getProperty("Name1").getValue().toString());
  }

  @Test
  public void checkConvertsOneResultsOneComplexElement() throws ODataApplicationException {
    HashMap<String, Object> result;

    result = new HashMap<String, Object>();
    result.put("Address/CityName", "Test City");
    result.put("Address/Country", "GB");
    result.put("Address/PostalCode", "ZE1 3AA");
    result.put("Address/StreetName", "Test Road");
    result.put("Address/HouseNumber", "123");
    result.put("Address/POBox", "155");
    result.put("Address/Region", "GB-12");
    result.put("Address/CountryName", "Willi");

    jpaQueryResult.add(new TupleDouble(result));

    EntityCollection act = cut.getResult();
    assertEquals(1, act.getEntities().size());

    assertEquals(ValueType.COMPLEX, act.getEntities().get(0).getProperty("Address").getValueType());
    ComplexValue value = (ComplexValue) act.getEntities().get(0).getProperty("Address").getValue();
    assertEquals(NO_POSTAL_ADDRESS_FIELDS, value.getValue().size());
  }

  @Test
  public void checkConvertsOneResultsOneNestedComplexElement() throws ODataApplicationException {
    HashMap<String, Object> result;

//    AdministrativeInformation adminInfo = new AdministrativeInformation();
//    adminInfo.setCreated(new ChangeInformation("Joe Doe", Timestamp.valueOf("2016-01-22 12:25:23")));
//    adminInfo.setUpdated(new ChangeInformation("Joe Doe", Timestamp.valueOf("2016-01-24 14:29:45")));
    result = new HashMap<String, Object>();
    result.put("AdministrativeInformation/Created/By", "Joe Doe");
    result.put("AdministrativeInformation/Created/At", "2016-01-22 12:25:23");
    result.put("AdministrativeInformation/Updated/By", "Joe Doe");
    result.put("AdministrativeInformation/Updated/At", "2016-01-24 14:29:45");

    jpaQueryResult.add(new TupleDouble(result));

    EntityCollection act = cut.getResult();
    assertEquals(1, act.getEntities().size());
    // Check first level
    assertEquals(ValueType.COMPLEX, act.getEntities().get(0).getProperty("AdministrativeInformation").getValueType());
    ComplexValue value = (ComplexValue) act.getEntities().get(0).getProperty("AdministrativeInformation").getValue();
    assertEquals(NO_ADMIN_INFO_FIELDS, value.getValue().size());
    // Check second level
    assertEquals(ValueType.COMPLEX, value.getValue().get(0).getValueType());
  }

  @Test
  public void checkConvertsOneResultsOneElementOfComplexElement() throws ODataApplicationException {
    HashMap<String, Object> result;

    result = new HashMap<String, Object>();
    result.put("Address/Region", new String("CA"));
    jpaQueryResult.add(new TupleDouble(result));

    EntityCollection act = cut.getResult();
    assertEquals(1, act.getEntities().size());
    assertEquals("CA", ((ComplexValue) act.getEntities().get(0).getProperty("Address").getValue()).getValue().get(0)
        .getValue().toString());
  }
}
