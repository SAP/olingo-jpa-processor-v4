package com.sap.olingo.jpa.processor.core.modify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.junit.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.org.jpa.processor.core.converter.JPAExpandResult;

public abstract class TestJPACreateResult extends TestBase {

  protected JPAExpandResult cut;
  protected JPAEntityType et;
  protected Map<String, List<String>> headers;
  protected Object jpaEntity;

  public TestJPACreateResult() {
    super();
  }

  @Test
  public void testGetChildrenProvidesEmptyMap() throws ODataJPAProcessException, ODataJPAModelException {

    Map<JPAAssociationPath, JPAExpandResult> act = cut.getChildren();

    assertNotNull(act);
    assertEquals(0, act.size());
  }

  @Test
  public void testGetResultSimpleEntity() throws ODataJPAProcessorException, ODataJPAModelException {
    et = helper.getJPAEntityType("BusinessPartnerRoles");

    createCutGetResultSimpleEntity();

    List<Tuple> act = cut.getResult("root");

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("34", act.get(0).get("BusinessPartnerID"));
  }

  @Test
  public void testGetResultWithOneLevelEmbedded() throws ODataJPAProcessorException, ODataJPAModelException {
    et = helper.getJPAEntityType("AdministrativeDivisionDescriptions");

    createCutGetResultWithOneLevelEmbedded();

    List<Tuple> act = cut.getResult("root");

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("A", act.get(0).get("CodeID"));
    assertEquals("Hugo", act.get(0).get("Name"));
  }

  @Test
  public void testGetResultWithTwoLevelEmbedded() throws ODataJPAProcessorException, ODataJPAModelException {

    createCutGetResultWithTwoLevelEmbedded();

    List<Tuple> act = cut.getResult("root");
    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("01", act.get(0).get("ID"));
    assertEquals("99", act.get(0).get("AdministrativeInformation/Created/By"));
  }

  @Test
  public void testGetResultWithOneLinked() throws ODataJPAProcessorException, ODataJPAModelException {
    createCutGetResultWithWithOneLinked();
    Map<JPAAssociationPath, JPAExpandResult> act = cut.getChildren();
    assertNotNull(act);
    assertEquals(1, act.size());
    for (JPAAssociationPath actPath : act.keySet()) {
      assertEquals("Children", actPath.getAlias());
      List<Tuple> subResult = act.get(actPath).getResult("Eurostat/NUTS1/BE2");
      assertEquals(1, subResult.size());
    }

    // Eurostat/NUTS1/BE2
  }

  @Test
  public void testGetResultWithTwoLinked() throws ODataJPAProcessorException, ODataJPAModelException {
    createCutGetResultWithWithTwoLinked();
    Map<JPAAssociationPath, JPAExpandResult> act = cut.getChildren();
    assertNotNull(act);
    assertEquals(1, act.size());
    for (JPAAssociationPath actPath : act.keySet()) {
      assertEquals("Children", actPath.getAlias());
      List<Tuple> subResult = act.get(actPath).getResult("Eurostat/NUTS1/BE2");
      assertEquals(2, subResult.size());
    }
  }

  protected abstract void createCutGetResultWithWithTwoLinked() throws ODataJPAProcessorException,
      ODataJPAModelException;

  protected abstract void createCutGetResultWithWithOneLinked() throws ODataJPAProcessorException,
      ODataJPAModelException;

  protected abstract void createCutGetResultSimpleEntity() throws ODataJPAModelException, ODataJPAProcessorException;

  protected abstract void createCutGetResultWithOneLevelEmbedded() throws ODataJPAModelException,
      ODataJPAProcessorException;

  protected abstract void createCutGetResultWithTwoLevelEmbedded() throws ODataJPAModelException,
      ODataJPAProcessorException;

}