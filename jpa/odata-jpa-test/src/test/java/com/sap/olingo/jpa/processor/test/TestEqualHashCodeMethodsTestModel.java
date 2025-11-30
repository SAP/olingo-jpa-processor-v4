package com.sap.olingo.jpa.processor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionKey;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRoleKey;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.testmodel.InstanceRestrictionKey;
import com.sap.olingo.jpa.processor.core.testmodel.MembershipKey;
import com.sap.olingo.jpa.processor.core.testmodel.TemporalWithValidityPeriodKey;

/**
 * @author Oliver Grande
 * Created: 11.11.2019
 *
 */
class TestEqualHashCodeMethodsTestModel extends TestEqualHashCodeMethods {
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";

  @BeforeAll
  static void setupClass() {
    final Map<String, Object> properties = new HashMap<>();
    properties.put(ENTITY_MANAGER_DATA_SOURCE, DataSourceHelper.createDataSource(
        DataSourceHelper.DB_HSQLDB));
    final EntityManagerFactory emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
    model = emf.getMetamodel();
  }

  @Test
  void testBusinessPartnerRoleKeyEqual() {
    final BusinessPartnerRoleKey cut = new BusinessPartnerRoleKey("12", "A");
    assertFalse(cut.equals(null)); // NOSONAR
    assertTrue(cut.equals(cut));// NOSONAR
    assertTrue(cut.equals(new BusinessPartnerRoleKey("12", "A")));// NOSONAR
    assertFalse(cut.equals(new BusinessPartnerRoleKey()));// NOSONAR
    assertFalse(cut.equals(new BusinessPartnerRoleKey("12", "B")));// NOSONAR
    assertFalse(cut.equals(new BusinessPartnerRoleKey("11", "A")));// NOSONAR
    assertFalse(new BusinessPartnerRoleKey("12", null).equals(cut));// NOSONAR
    assertFalse(new BusinessPartnerRoleKey(null, "A").equals(cut));// NOSONAR
  }

  @Test
  void testInstanceRestrictionKeyEqual() {
    final InstanceRestrictionKey cut = new InstanceRestrictionKey("12", 1);
    assertFalse(cut.equals(null));// NOSONAR
    assertTrue(cut.equals(cut));// NOSONAR
    assertTrue(cut.equals(new InstanceRestrictionKey("12", 1)));// NOSONAR
    assertFalse(cut.equals(new InstanceRestrictionKey()));// NOSONAR
    assertFalse(cut.equals(new InstanceRestrictionKey("12", 2)));// NOSONAR
    assertFalse(cut.equals(new InstanceRestrictionKey("11", 1)));// NOSONAR
    assertFalse(new InstanceRestrictionKey(null, 1).equals(cut));// NOSONAR
    assertFalse(new InstanceRestrictionKey("12", null).equals(cut));// NOSONAR
  }

  @Test
  void testMembershipKeyEqual() {
    final MembershipKey cut = new MembershipKey("12", "A");
    assertFalse(cut.equals(null));// NOSONAR
    assertTrue(cut.equals(cut));// NOSONAR
    assertTrue(cut.equals(new MembershipKey("12", "A")));// NOSONAR
    assertFalse(cut.equals(new MembershipKey()));// NOSONAR
    assertFalse(cut.equals(new MembershipKey("12", "B")));// NOSONAR
    assertFalse(cut.equals(new MembershipKey("11", "A")));// NOSONAR
    assertFalse(new MembershipKey(null, "A").equals(cut));// NOSONAR
    assertFalse(new MembershipKey("12", null).equals(cut));// NOSONAR
  }

  @Test
  void testAdministrativeDivisionKeyEqual() {
    final AdministrativeDivisionKey cut = new AdministrativeDivisionKey("A", "B", "C");
    assertFalse(cut.equals(null));// NOSONAR
    assertTrue(cut.equals(cut));// NOSONAR
    assertTrue(cut.equals(new AdministrativeDivisionKey("A", "B", "C")));// NOSONAR
    assertFalse(cut.equals(new AdministrativeDivisionKey()));// NOSONAR
    assertFalse(new AdministrativeDivisionKey("B", "B", "C").equals(cut));// NOSONAR
    assertFalse(new AdministrativeDivisionKey("A", "C", "C").equals(cut));// NOSONAR
    assertFalse(new AdministrativeDivisionKey("A", "B", "D").equals(cut));// NOSONAR
    assertFalse(new AdministrativeDivisionKey(null, "B", "C").equals(cut));// NOSONAR
    assertFalse(new AdministrativeDivisionKey("A", null, "C").equals(cut));// NOSONAR
    assertFalse(new AdministrativeDivisionKey("A", "B", null).equals(cut));// NOSONAR
  }

  @Test
  void testAdministrativeDivisionKeyCompareTo() {
    final AdministrativeDivisionKey cut = new AdministrativeDivisionKey("B", "B", "B");

    assertEquals(0, cut.compareTo(cut));
    assertEquals(1, cut.compareTo(new AdministrativeDivisionKey("B", "B", "A")));
    assertEquals(-1, cut.compareTo(new AdministrativeDivisionKey("B", "B", "C")));
    assertEquals(1, cut.compareTo(new AdministrativeDivisionKey("B", "A", "B")));
    assertEquals(-1, cut.compareTo(new AdministrativeDivisionKey("B", "C", "B")));
    assertEquals(1, cut.compareTo(new AdministrativeDivisionKey("A", "B", "B")));
    assertEquals(-1, cut.compareTo(new AdministrativeDivisionKey("C", "B", "B")));
  }

  @Test
  void testTemporalWithValidityPeriodKeyEqual() {
    final LocalDate now = LocalDate.now();
    final TemporalWithValidityPeriodKey cut = new TemporalWithValidityPeriodKey("12", now);
    assertFalse(cut.equals(null));// NOSONAR
    assertTrue(cut.equals(cut));// NOSONAR
    assertTrue(cut.equals(new TemporalWithValidityPeriodKey("12", now)));// NOSONAR
    assertFalse(cut.equals(new TemporalWithValidityPeriodKey()));// NOSONAR
    assertFalse(cut.equals(new TemporalWithValidityPeriodKey("12", now.minusDays(1L))));// NOSONAR
    assertFalse(cut.equals(new TemporalWithValidityPeriodKey("11", now)));// NOSONAR
    assertFalse(new TemporalWithValidityPeriodKey(null, now).equals(cut));// NOSONAR
    assertFalse(new TemporalWithValidityPeriodKey("12", null).equals(cut));// NOSONAR
  }
}