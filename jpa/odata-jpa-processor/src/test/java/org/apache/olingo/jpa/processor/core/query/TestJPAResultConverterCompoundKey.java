package org.apache.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.testmodel.RegionKey;
import org.apache.olingo.jpa.processor.core.util.EdmEntitySetDouble;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.apache.olingo.jpa.processor.core.util.TestHelper;
import org.apache.olingo.jpa.processor.core.util.TupleDouble;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestJPAResultConverterCompoundKey extends TestBase {
  public static final int NO_POSTAL_ADDRESS_FIELDS = 8;
  public static final int NO_ADMIN_INFO_FIELDS = 2;
  private JPATupleResultConverter cut;
  private List<Tuple> jpaQueryResult;

  @Before
  public void setup() throws ODataJPAModelException, ODataApplicationException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    jpaQueryResult = new ArrayList<Tuple>();

  }

  @Test
  public void checkConvertsOneResultsTwoKeys() throws ODataApplicationException, ODataJPAModelException {
    // .../BusinessPartnerRoles(BusinessPartnerID='3',RoleCategory='C')
    cut = new JPATupleResultConverter(new EdmEntitySetDouble(nameBuilder, "BusinessPartnerRoles"), helper.sd,
        jpaQueryResult);
    HashMap<String, Object> result;

    result = new HashMap<String, Object>();
    result.put("BusinessPartnerID", new String("3"));
    result.put("RoleCategory", new String("C"));
    jpaQueryResult.add(new TupleDouble(result));

    EntityCollection act = ((JPATupleResultConverter) cut).getResult();
    assertEquals(1, act.getEntities().size());
    assertEquals("3", act.getEntities().get(0).getProperty("BusinessPartnerID").getValue().toString());
    assertEquals("C", act.getEntities().get(0).getProperty("RoleCategory").getValue().toString());

    assertEquals("BusinessPartnerRoles(BusinessPartnerID='3',RoleCategory='C')",
        act.getEntities().get(0).getId().getPath());
  }

  @Ignore // Looks like OData or Olingo do not support Complex Types
  @Test
  public void checkConvertsOneResultsEmbeddedKey() throws ODataApplicationException, ODataJPAModelException {
    // .../Regions(RegionKey/CountryCode='DE', RegionKey/RegionCode='DE-HB', RegionKey/Language = 'en')
    cut = new JPATupleResultConverter(new EdmEntitySetDouble(nameBuilder, "Regions"), helper.sd, jpaQueryResult);

    RegionKey region = new RegionKey();
    region.setCountryCode("DE");
    region.setRegionCode("DE-HB");
    region.setLanguage("en");

    HashMap<String, Object> result;

    result = new HashMap<String, Object>();
    result.put("RegionKey", region);
    jpaQueryResult.add(new TupleDouble(result));

    EntityCollection act = ((JPATupleResultConverter) cut).getResult();
    assertEquals(1, act.getEntities().size());
    assertEquals(ValueType.COMPLEX, act.getEntities().get(0).getProperty("RegionKey").getValueType());

    assertEquals("Regions(RegionKey/RegionCode='DE-HB',RegionKey/CountryCode='DE',RegionKey/Language='en')",
        act.getEntities().get(0).getId().getPath());
  }

}
