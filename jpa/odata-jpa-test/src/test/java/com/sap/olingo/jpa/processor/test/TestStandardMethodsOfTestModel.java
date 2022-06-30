/**
 *
 */
package com.sap.olingo.jpa.processor.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.hsqldb.jdbc.JDBCClob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescriptionKey;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionKey;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRoleKey;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRoleProtected;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRoleWithGroup;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerWithGroups;
import com.sap.olingo.jpa.processor.core.testmodel.ChangeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.Collection;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionDeep;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionFirstLevelComplex;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionInnerComplex;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionNestedComplex;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionPartOfComplex;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionSecondLevelComplex;
import com.sap.olingo.jpa.processor.core.testmodel.Comment;
import com.sap.olingo.jpa.processor.core.testmodel.CommunicationData;
import com.sap.olingo.jpa.processor.core.testmodel.CountryKey;
import com.sap.olingo.jpa.processor.core.testmodel.CountryRestriction;
import com.sap.olingo.jpa.processor.core.testmodel.DummyToBeIgnored;
import com.sap.olingo.jpa.processor.core.testmodel.InhouseAddress;
import com.sap.olingo.jpa.processor.core.testmodel.InhouseAddressTable;
import com.sap.olingo.jpa.processor.core.testmodel.InhouseAddressWithGroup;
import com.sap.olingo.jpa.processor.core.testmodel.InhouseAddressWithProtection;
import com.sap.olingo.jpa.processor.core.testmodel.InhouseAddressWithThreeProtections;
import com.sap.olingo.jpa.processor.core.testmodel.InstanceRestrictionKey;
import com.sap.olingo.jpa.processor.core.testmodel.JoinRelationKey;
import com.sap.olingo.jpa.processor.core.testmodel.MembershipKey;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.OrganizationImage;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.PersonDeepProtected;
import com.sap.olingo.jpa.processor.core.testmodel.PersonDeepProtectedHidden;
import com.sap.olingo.jpa.processor.core.testmodel.PersonImage;
import com.sap.olingo.jpa.processor.core.testmodel.PostalAddressData;
import com.sap.olingo.jpa.processor.core.testmodel.PostalAddressDataWithGroup;
import com.sap.olingo.jpa.processor.core.testmodel.User;

/**
 * The following set of test methods checks a number of standard methods of model pojos.
 * @author Oliver Grande
 * Created: 05.10.2019
 *
 */
class TestStandardMethodsOfTestModel {

  private final String expString = "TestString";
  private final Integer expInteger = 20;
  private final int expInt = 10;
  private final Boolean expBoolean = Boolean.TRUE;
  private final BigInteger expBigInt = new BigInteger("10");
  private final BigDecimal expDecimal = new BigDecimal(1.10);
  private final LocalDate expLocalDate = LocalDate.now();
  private final long expLong = 15l;
  private final byte[] expByteArray = new byte[] { 1, 1, 1, 1 };
  private final Date expDate = Date.valueOf(expLocalDate);
  @SuppressWarnings("deprecation")
  private final java.util.Date expUtilDate = new java.util.Date(119, 10, 01);
  private final Timestamp expTimestamp = Timestamp.valueOf(LocalDateTime.now());
  private final Short expShort = Short.valueOf("10");
  private Clob expClob;

  static Stream<Arguments> testModelEntities() {
    return Stream.of(
        arguments(AdministrativeDivisionDescription.class),
        arguments(AdministrativeDivisionDescriptionKey.class),
        arguments(AdministrativeDivisionKey.class),
        arguments(AdministrativeDivision.class),
        arguments(AdministrativeInformation.class),
        arguments(BusinessPartnerProtected.class),
        arguments(BusinessPartnerWithGroups.class),
        arguments(BusinessPartnerRole.class),
        arguments(BusinessPartnerRoleWithGroup.class),
        arguments(BusinessPartnerRoleProtected.class),
        arguments(BusinessPartnerRoleKey.class),
        arguments(ChangeInformation.class),
        arguments(CommunicationData.class),
        arguments(Collection.class),
        arguments(CollectionInnerComplex.class),
        arguments(CollectionPartOfComplex.class),
        arguments(CollectionNestedComplex.class),
        arguments(CollectionDeep.class),
        arguments(CollectionFirstLevelComplex.class),
        arguments(CollectionSecondLevelComplex.class),
        arguments(Comment.class),
        arguments(CountryKey.class),
        arguments(CountryRestriction.class),
        arguments(InhouseAddress.class),
        arguments(InhouseAddressTable.class),
        arguments(InhouseAddressWithGroup.class),
        arguments(InhouseAddressWithProtection.class),
        arguments(InhouseAddressWithThreeProtections.class),
        arguments(InstanceRestrictionKey.class),
        arguments(JoinRelationKey.class),
        arguments(MembershipKey.class),
        arguments(Organization.class),
        arguments(OrganizationImage.class),
        arguments(Person.class),
        arguments(PersonImage.class),
        arguments(PersonDeepProtected.class),
        arguments(PersonDeepProtectedHidden.class),
        arguments(PostalAddressData.class),
        arguments(PostalAddressDataWithGroup.class),
        arguments(User.class),
        arguments(DummyToBeIgnored.class));
  }

  static Stream<Arguments> testErrorEntities() {
    return Stream.of(
        arguments(com.sap.olingo.jpa.processor.core.errormodel.Team.class),
        arguments(com.sap.olingo.jpa.processor.core.errormodel.AdministrativeInformation.class),
        arguments(com.sap.olingo.jpa.processor.core.errormodel.ChangeInformation.class),
        arguments(com.sap.olingo.jpa.processor.core.errormodel.CollectionAttributeProtected.class),
        arguments(com.sap.olingo.jpa.processor.core.errormodel.ComplexProtectedNoPath.class),
        arguments(com.sap.olingo.jpa.processor.core.errormodel.ComplexProtectedWrongPath.class),
        arguments(com.sap.olingo.jpa.processor.core.errormodel.EmbeddedKeyPartOfGroup.class),
        arguments(com.sap.olingo.jpa.processor.core.errormodel.NavigationAttributeProtected.class),
        arguments(com.sap.olingo.jpa.processor.core.errormodel.NavigationPropertyPartOfGroup.class),
        arguments(com.sap.olingo.jpa.processor.core.errormodel.KeyPartOfGroup.class),
        arguments(com.sap.olingo.jpa.processor.core.errormodel.MandatoryPartOfGroup.class),
        arguments(com.sap.olingo.jpa.processor.core.errormodel.PersonDeepCollectionProtected.class));
  }

  @BeforeEach
  void setup() throws SQLException {
    expClob = new JDBCClob("Test");
  }

  @ParameterizedTest
  @MethodSource({ "testModelEntities", "testErrorEntities" })
  void testGetterReturnsSetPrimitiveValue(final Class<?> clazz) throws NoSuchMethodException, SecurityException,
      InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    final Method[] methods = clazz.getMethods();
    final Constructor<?> constructor = clazz.getConstructor();
    assertNotNull(constructor);
    final Object instance = constructor.newInstance();

    for (final Method setter : methods) {
      if ("set".equals(setter.getName().substring(0, 3))
          && setter.getParameterCount() == 1) {

        final String getterName = "g" + setter.getName().substring(1);
        assertNotNull(clazz.getMethod(getterName));
        final Method getter = clazz.getMethod(getterName);
        final Class<?> paramType = setter.getParameterTypes()[0];
        final Object exp = getExpected(paramType);
        if (exp != null) {
          setter.invoke(instance, exp);
          if (exp.getClass().isArray())
            if ("byte[]".equals(exp.getClass().getTypeName()))
              assertArrayEquals((byte[]) exp, (byte[]) getter.invoke(instance));
            else
              assertArrayEquals((Object[]) exp, (Object[]) getter.invoke(instance));
          else
            assertEquals(exp, getter.invoke(instance));
        }
      }
    }
  }

  @ParameterizedTest
  @MethodSource({ "testModelEntities", "testErrorEntities" })
  void testToStringReturnsValue(final Class<?> clazz) throws NoSuchMethodException, SecurityException,
      InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    final Constructor<?> constructor = clazz.getConstructor();
    assertNotNull(constructor);
    final Object instance = constructor.newInstance();
    assertFalse(instance.toString().isEmpty());
  }

  @ParameterizedTest
  @MethodSource({ "testModelEntities", "testErrorEntities" })
  void testHasValueReturnsValue(final Class<?> clazz) throws NoSuchMethodException, SecurityException,
      InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    final Method[] methods = clazz.getMethods();
    final Constructor<?> constructor = clazz.getConstructor();
    assertNotNull(constructor);
    final Object instance = constructor.newInstance();

    for (final Method hashcode : methods) {
      if ("hashCode".equals(hashcode.getName())
          && hashcode.getParameterCount() == 0) {
        assertNotEquals(0, hashcode.invoke(instance));
      }
    }
  }

  private Object getExpected(final Class<?> paramType) {

    if (paramType == String.class)
      return expString;
    else if (paramType == Integer.class)
      return expInteger;
    else if (paramType == int.class)
      return expInt;
    else if (paramType == Boolean.class)
      return expBoolean;
    else if (paramType == BigInteger.class)
      return expBigInt;
    else if (paramType == BigDecimal.class)
      return expDecimal;
    else if (paramType == LocalDate.class)
      return expLocalDate;
    else if (paramType == Long.class)
      return expLong;
    else if (paramType == long.class)
      return expLong;
    else if (paramType == Clob.class)
      return expClob;
    else if (paramType == expByteArray.getClass())
      return expByteArray;
    else if (paramType == Date.class)
      return expDate;
    else if (paramType == expUtilDate.getClass())
      return expUtilDate;
    else if (paramType == Timestamp.class)
      return expTimestamp;
    else if (paramType == Short.class)
      return expShort;
    else if (paramType == short.class)
      return expShort;
    else
      return null;
  }

}
