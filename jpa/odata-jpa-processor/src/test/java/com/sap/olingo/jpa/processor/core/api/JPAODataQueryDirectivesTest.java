package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Test;

class JPAODataQueryDirectivesTest {

  @Test
  void testBuildWithoutSettingMaxZero() throws ODataException {

    final JPAODataSessionContextAccess act = JPAODataServiceContext.with()
        .useQueryDirectives().build().build();
    assertEquals(0, act.getQueryDirectives().getMaxValuesInInClause());
  }

  @Test
  void testBuildProvideSetValue() throws ODataException {

    final JPAODataSessionContextAccess act = JPAODataServiceContext.with()
        .useQueryDirectives().maxValuesInInClause(300).build().build();
    assertEquals(300, act.getQueryDirectives().getMaxValuesInInClause());
  }

  @Test
  void testContextReturnDirectivesWithZero() throws ODataException {

    final JPAODataSessionContextAccess act = JPAODataServiceContext.with().build();
    assertEquals(0, act.getQueryDirectives().getMaxValuesInInClause());
  }
}
