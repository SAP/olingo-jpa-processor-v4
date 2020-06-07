package com.sap.olingo.jpa.processor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionKey;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRoleKey;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.testmodel.InstanceRestrictionKey;
import com.sap.olingo.jpa.processor.core.testmodel.MembershipKey;

/**
 * @author Oliver Grande
 * Created: 11.11.2019
 *
 */
public class TestEqualHashCodeMethodsTestModel extends TestEqualHashCodeMethods {
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private static final String ENTITY_MANAGER_DATA_SOURCE = "javax.persistence.nonJtaDataSource";

  @BeforeAll
  public static void setupClass() {
    final Map<String, Object> properties = new HashMap<>();
    properties.put(ENTITY_MANAGER_DATA_SOURCE, DataSourceHelper.createDataSource(
        DataSourceHelper.DB_HSQLDB));
    final EntityManagerFactory emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
    model = emf.getMetamodel();
  }

  @Test
  public void testBusinessPartnerRoleKeyEqual() {
    final BusinessPartnerRoleKey cut = new BusinessPartnerRoleKey("12", "A");
    assertFalse(cut.equals(null));
    assertTrue(cut.equals(cut));
    assertTrue(cut.equals(new BusinessPartnerRoleKey("12", "A")));
    assertFalse(cut.equals(new BusinessPartnerRoleKey()));
    assertFalse(cut.equals(new BusinessPartnerRoleKey("12", "B")));
    assertFalse(cut.equals(new BusinessPartnerRoleKey("11", "A")));
    assertFalse(new BusinessPartnerRoleKey("12", null).equals(cut));
    assertFalse(new BusinessPartnerRoleKey(null, "A").equals(cut));
  }

  @Test
  public void testInstanceRestrictionKeyEqual() {
    final InstanceRestrictionKey cut = new InstanceRestrictionKey("12", 1);
    assertFalse(cut.equals(null));
    assertTrue(cut.equals(cut));
    assertTrue(cut.equals(new InstanceRestrictionKey("12", 1)));
    assertFalse(cut.equals(new InstanceRestrictionKey()));
    assertFalse(cut.equals(new InstanceRestrictionKey("12", 2)));
    assertFalse(cut.equals(new InstanceRestrictionKey("11", 1)));
    assertFalse(new InstanceRestrictionKey(null, 1).equals(cut));
    assertFalse(new InstanceRestrictionKey("12", null).equals(cut));
  }

  @Test
  public void testMembershipKeyEqual() {
    final MembershipKey cut = new MembershipKey("12", "A");
    assertFalse(cut.equals(null));
    assertTrue(cut.equals(cut));
    assertTrue(cut.equals(new MembershipKey("12", "A")));
    assertFalse(cut.equals(new MembershipKey()));
    assertFalse(cut.equals(new MembershipKey("12", "B")));
    assertFalse(cut.equals(new MembershipKey("11", "A")));
    assertFalse(new MembershipKey(null, "A").equals(cut));
    assertFalse(new MembershipKey("12", null).equals(cut));
  }

  @Test
  public void testAdministrativeDivisionKeyEqual() {
    final AdministrativeDivisionKey cut = new AdministrativeDivisionKey("A", "B", "C");
    assertFalse(cut.equals(null));
    assertTrue(cut.equals(cut));
    assertTrue(cut.equals(new AdministrativeDivisionKey("A", "B", "C")));
    assertFalse(cut.equals(new AdministrativeDivisionKey()));
    assertFalse(new AdministrativeDivisionKey("B", "B", "C").equals(cut));
    assertFalse(new AdministrativeDivisionKey("A", "C", "C").equals(cut));
    assertFalse(new AdministrativeDivisionKey("A", "B", "D").equals(cut));
    assertFalse(new AdministrativeDivisionKey(null, "B", "C").equals(cut));
    assertFalse(new AdministrativeDivisionKey("A", null, "C").equals(cut));
    assertFalse(new AdministrativeDivisionKey("A", "B", null).equals(cut));
  }

  @Test
  public void testAdministrativeDivisionKeyCompareTo() {
    final AdministrativeDivisionKey cut = new AdministrativeDivisionKey("B", "B", "B");

    assertEquals(0, cut.compareTo(cut));
    assertEquals(1, cut.compareTo(new AdministrativeDivisionKey("B", "B", "A")));
    assertEquals(-1, cut.compareTo(new AdministrativeDivisionKey("B", "B", "C")));
    assertEquals(1, cut.compareTo(new AdministrativeDivisionKey("B", "A", "B")));
    assertEquals(-1, cut.compareTo(new AdministrativeDivisionKey("B", "C", "B")));
    assertEquals(1, cut.compareTo(new AdministrativeDivisionKey("A", "B", "B")));
    assertEquals(-1, cut.compareTo(new AdministrativeDivisionKey("C", "B", "B")));
  }
}