package com.sap.olingo.jpa.processor.core.testobjects;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataFunction;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.ChangeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionDeep;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionFirstLevelComplex;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionSecondLevelComplex;
import com.sap.olingo.jpa.processor.core.testmodel.CommunicationData;
import com.sap.olingo.jpa.processor.core.testmodel.InhouseAddress;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

public class TestFunctionReturnType implements ODataFunction {

  @EdmFunction(name = "PrimitiveValue", returnType = @ReturnType, hasFunctionImport = true)
  public Integer primitiveValue(@EdmParameter(name = "A") final short a) {
    if (a == 0)
      return null;
    return (int) a;
  }

  @EdmFunction(name = "PrimitiveValueNullable", returnType = @ReturnType, hasFunctionImport = false)
  public Integer primitiveValueNullable(@EdmParameter(name = "A") final Short a) {
    if (a == null)
      return 0;
    return (int) a;
  }

  @EdmFunction(name = "ListOfPrimitiveValues", returnType = @ReturnType(type = Integer.class), hasFunctionImport = true)
  public List<Integer> listOfPrimitiveValues(@EdmParameter(name = "A") final Integer a) {
    return Arrays.asList(a, a / 2);
  }

  @EdmFunction(name = "ComplexType", returnType = @ReturnType, hasFunctionImport = true)
  public CommunicationData complexType(@EdmParameter(name = "A") final int a) {
    if (a == 0)
      return null;
    final CommunicationData result = new CommunicationData();
    result.setLandlinePhoneNumber(Integer.valueOf(a).toString());
    return result;
  }

  @EdmFunction(name = "ListOfComplexType", returnType = @ReturnType(type = AdministrativeInformation.class),
      hasFunctionImport = true)
  public List<AdministrativeInformation> listOfComplexType(@EdmParameter(name = "A") final String user) {
    final Long milliPerDay = (long) (24 * 60 * 60 * 1000);
    final AdministrativeInformation admin1 = new AdministrativeInformation();
    admin1.setCreated(new ChangeInformation(user, new Date(LocalDate.now().toEpochDay() * milliPerDay)));
    final AdministrativeInformation admin2 = new AdministrativeInformation();
    admin2.setUpdated(new ChangeInformation(user, new Date(LocalDate.now().toEpochDay() * milliPerDay)));
    return Arrays.asList(admin1, admin2);
  }

  @EdmFunction(name = "EntityType", returnType = @ReturnType, hasFunctionImport = true)
  public AdministrativeDivision entityType(@EdmParameter(name = "A") final int a) {

    if (a == 0)
      return null;
    final AdministrativeDivision result = new AdministrativeDivision();
    result.setArea(a);
    result.setCodePublisher("1");
    result.setCodeID("2");
    result.setDivisionCode("3");
    return result;
  }

  @EdmFunction(name = "ListOfEntityType", returnType = @ReturnType(type = AdministrativeDivision.class),
      hasFunctionImport = true)
  public List<AdministrativeDivision> listOfEntityType(@EdmParameter(name = "A") final Integer a) {
    return Arrays.asList(entityType(a), entityType(a / 2));
  }

  @EdmFunction(name = "ConvertBirthday", returnType = @ReturnType, hasFunctionImport = true)
  public Person convertBirthday() {
    final Person p = new Person();
    p.setID("1");
    p.setETag(3L);
    p.setBirthDay(LocalDate.now());
    p.setInhouseAddress(new ArrayList<>());
    return p;
  }

  @EdmFunction(name = "ListOfEntityTypeWithCollection", returnType = @ReturnType(type = Person.class),
      hasFunctionImport = true)
  public List<Person> listOfEntityTypeWithCollection(@EdmParameter(name = "A") final Integer a) {
    final Person person = new Person();
    person.setID("1");
    person.addInhouseAddress(new InhouseAddress("DEV", "7"));
    person.addInhouseAddress(new InhouseAddress("ADMIN", "2"));
    return Arrays.asList(person);
  }

  @EdmFunction(name = "EntityTypeWithDeepCollection", returnType = @ReturnType(type = CollectionDeep.class),
      hasFunctionImport = true)
  public CollectionDeep entityTypeWithDeepCollection(@EdmParameter(name = "A") final Integer a) {
    final CollectionDeep deepCollection = new CollectionDeep();
    final CollectionFirstLevelComplex firstLevel = new CollectionFirstLevelComplex();
    final CollectionSecondLevelComplex secondLevel = new CollectionSecondLevelComplex();
    deepCollection.setFirstLevel(firstLevel);
    deepCollection.setID("1");
    firstLevel.setLevelID(10);
    firstLevel.setSecondLevel(secondLevel);
    secondLevel.setNumber(5L);

    secondLevel.addInhouseAddress(new InhouseAddress("DEV", "7"));
    secondLevel.addInhouseAddress(new InhouseAddress("ADMIN", "2"));
    secondLevel.setComment(Arrays.asList("One", "Two", "Three"));
    return deepCollection;
  }
}
