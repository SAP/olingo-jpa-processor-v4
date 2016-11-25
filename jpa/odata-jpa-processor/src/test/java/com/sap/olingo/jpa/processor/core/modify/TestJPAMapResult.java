package com.sap.olingo.jpa.processor.core.modify;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

public class TestJPAMapResult extends TestJPACreateResult {
  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    headers = new HashMap<String, List<String>>();
    jpaEntity = new HashMap<String, Object>();
    helper = new TestHelper(emf, PUNIT_NAME);
    et = helper.getJPAEntityType("Organizations");
    cut = new JPAMapResult(et, (Map<String, Object>) jpaEntity, headers);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void createCutGetResultSimpleEntity() throws ODataJPAModelException, ODataJPAProcessorException {

    ((Map<String, Object>) jpaEntity).put("businessPartnerID", "34");
    ((Map<String, Object>) jpaEntity).put("roleCategory", "A");
    cut = new JPAMapResult(et, (Map<String, Object>) jpaEntity, headers);

  }

  @SuppressWarnings("unchecked")
  @Override
  protected void createCutGetResultWithOneLevelEmbedded() throws ODataJPAModelException, ODataJPAProcessorException {

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("codeID", "A");
    key.put("language", "en");

    ((Map<String, Object>) jpaEntity).put("name", "Hugo");
    ((Map<String, Object>) jpaEntity).put("key", key);

    cut = new JPAMapResult(et, (Map<String, Object>) jpaEntity, headers);

  }

  @SuppressWarnings("unchecked")
  @Override
  protected void createCutGetResultWithTwoLevelEmbedded() throws ODataJPAModelException,
      ODataJPAProcessorException {

    long time = new Date().getTime();
    Map<String, Object> created = new HashMap<String, Object>();
    created.put("by", "99");
    created.put("at", new Timestamp(time));

    Map<String, Object> admin = new HashMap<String, Object>();
    admin.put("created", created);
    admin.put("updated", created);

    ((Map<String, Object>) jpaEntity).put("iD", "01");
    ((Map<String, Object>) jpaEntity).put("customString1", "Dummy");
    ((Map<String, Object>) jpaEntity).put("administrativeInformation", admin);

    cut = new JPAMapResult(et, (Map<String, Object>) jpaEntity, headers);
  }
}
