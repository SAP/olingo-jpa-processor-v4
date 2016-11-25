package com.sap.olingo.jpa.processor.core.modify;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescriptionKey;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

public class TestJPAEntityResult extends TestJPACreateResult {
  @Before
  public void setUp() throws Exception {
    headers = new HashMap<String, List<String>>();
    jpaEntity = new Organization();
    helper = new TestHelper(emf, PUNIT_NAME);
    et = helper.getJPAEntityType("Organizations");
    cut = new JPAEntityResult(et, jpaEntity, headers);
  }

  @Override
  protected void createCutGetResultSimpleEntity() throws ODataJPAModelException, ODataJPAProcessorException {
    jpaEntity = new BusinessPartnerRole();
    ((BusinessPartnerRole) jpaEntity).setBusinessPartnerID("34");
    ((BusinessPartnerRole) jpaEntity).setRoleCategory("A");
    cut = new JPAEntityResult(et, jpaEntity, headers);
  }

  @Override
  protected void createCutGetResultWithOneLevelEmbedded() throws ODataJPAModelException, ODataJPAProcessorException {
    AdministrativeDivisionDescriptionKey key = new AdministrativeDivisionDescriptionKey();
    key.setCodeID("A");
    key.setLanguage("en");
    jpaEntity = new AdministrativeDivisionDescription();
    ((AdministrativeDivisionDescription) jpaEntity).setName("Hugo");
    ((AdministrativeDivisionDescription) jpaEntity).setKey(key);

    cut = new JPAEntityResult(et, jpaEntity, headers);
  }

  @Override
  protected void createCutGetResultWithTwoLevelEmbedded() throws ODataJPAModelException,
      ODataJPAProcessorException {

    jpaEntity = new Organization();
    ((Organization) jpaEntity).onCreate();
    ((Organization) jpaEntity).setID("01");
    ((Organization) jpaEntity).setCustomString1("Dummy");

    cut = new JPAEntityResult(et, jpaEntity, headers);
  }
}
