package org.apache.olingo.jpa.processor.core.modify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import org.apache.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescriptionKey;
import org.apache.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import org.apache.olingo.jpa.processor.core.testmodel.Organization;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.apache.olingo.jpa.processor.core.util.TestHelper;
import org.apache.org.jpa.processor.core.converter.JPAExpandResult;
import org.junit.Before;
import org.junit.Test;

public class TestJPAEntityResult extends TestBase {
  private JPAEntityResult cut;
  private JPAEntityType et;
  private Map<String, List<String>> headers;
  private Object jpaEntity;

  @Before
  public void setUp() throws Exception {
    headers = new HashMap<String, List<String>>();
    jpaEntity = new Organization();
    helper = new TestHelper(emf, PUNIT_NAME);
    et = helper.getJPAEntityType("Organizations");
  }

  @Test
  public void testGetChildrenProvidesEmptyMap() throws ODataJPAProcessException, ODataJPAModelException {
    cut = new JPAEntityResult(et, jpaEntity, headers);

    Map<JPAAssociationPath, JPAExpandResult> act = cut.getChildren();

    assertNotNull(act);
    assertEquals(0, act.size());
  }

  @Test
  public void testGetResultSimpleEntity() throws ODataJPAProcessorException, ODataJPAModelException {
    et = helper.getJPAEntityType("BusinessPartnerRoles");

    jpaEntity = new BusinessPartnerRole();
    ((BusinessPartnerRole) jpaEntity).setBusinessPartnerID("34");
    ((BusinessPartnerRole) jpaEntity).setRoleCategory("A");
    cut = new JPAEntityResult(et, jpaEntity, headers);

    List<Tuple> act = cut.getResult("root");

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("34", act.get(0).get("BusinessPartnerID"));
  }

  @Test
  public void testGetResultWithOneLevelEmbedded() throws ODataJPAProcessorException, ODataJPAModelException {
    et = helper.getJPAEntityType("AdministrativeDivisionDescriptions");

    AdministrativeDivisionDescriptionKey key = new AdministrativeDivisionDescriptionKey();
    key.setCodeID("A");
    key.setLanguage("en");
    jpaEntity = new AdministrativeDivisionDescription();
    ((AdministrativeDivisionDescription) jpaEntity).setName("Hugo");
    ((AdministrativeDivisionDescription) jpaEntity).setKey(key);

    cut = new JPAEntityResult(et, jpaEntity, headers);
    List<Tuple> act = cut.getResult("root");

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("A", act.get(0).get("CodeID"));
    assertEquals("Hugo", act.get(0).get("Name"));
  }

  @Test
  public void testGetResultWithTwoLevelEmbedded() throws ODataJPAProcessorException, ODataJPAModelException {

    jpaEntity = new Organization();
    ((Organization) jpaEntity).onCreate();
    ((Organization) jpaEntity).setID("01");
    ((Organization) jpaEntity).setCustomString1("Dummy");

    cut = new JPAEntityResult(et, jpaEntity, headers);
    List<Tuple> act = cut.getResult("root");

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("01", act.get(0).get("ID"));
    assertEquals("99", act.get(0).get("AdministrativeInformation/Created/By"));
  }
}
