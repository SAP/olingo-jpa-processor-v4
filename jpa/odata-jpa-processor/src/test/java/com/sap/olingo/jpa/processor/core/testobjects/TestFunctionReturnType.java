package com.sap.olingo.jpa.processor.core.testobjects;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataFunction;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.ChangeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.CommunicationData;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

public class TestFunctionReturnType implements ODataFunction {
  public static int constructorCalls = 0;

  public TestFunctionReturnType() {
    super();
    constructorCalls++;
  }

  @EdmFunction(name = "PrimitiveValue", returnType = @ReturnType)
  public Integer primitiveValue(@EdmParameter(name = "A") short a) {
    if (a == 0)
      return null;
    return Integer.valueOf(a);
  }

  @EdmFunction(name = "ListOfPrimitiveValues", returnType = @ReturnType(type = Integer.class))
  public List<Integer> listOfPrimitiveValues(@EdmParameter(name = "A") Integer a) {
    return Arrays.asList(new Integer[] { a, a / 2 });
  }

  @EdmFunction(name = "ComplexType", returnType = @ReturnType)
  public CommunicationData complexType(@EdmParameter(name = "A") int a) {
    if (a == 0)
      return null;
    CommunicationData result = new CommunicationData();
    result.setLandlinePhoneNumber(Integer.valueOf(a).toString());
    return result;
  }

  @EdmFunction(name = "ListOfComplexType", returnType = @ReturnType(type = AdministrativeInformation.class))
  public List<AdministrativeInformation> listOfComplexType(@EdmParameter(name = "A") String user) {
    AdministrativeInformation admin1 = new AdministrativeInformation();
    admin1.setCreated(new ChangeInformation(user, Date.valueOf(LocalDate.now())));
    AdministrativeInformation admin2 = new AdministrativeInformation();
    admin2.setUpdated(new ChangeInformation(user, Date.valueOf(LocalDate.now())));
    return Arrays.asList(new AdministrativeInformation[] { admin1, admin2 });
  }

  @EdmFunction(name = "EntityType", returnType = @ReturnType)
  public AdministrativeDivision entityType(@EdmParameter(name = "A") int a) {

    if (a == 0)
      return null;
    AdministrativeDivision result = new AdministrativeDivision();
    result.setArea(a);
    result.setCodePublisher("1");
    result.setCodeID("2");
    result.setDivisionCode("3");
    return result;
  }

  @EdmFunction(name = "ListOfEntityType", returnType = @ReturnType(type = AdministrativeDivision.class))
  public List<AdministrativeDivision> listOfEntityType(@EdmParameter(name = "A") Integer a) {
    return Arrays.asList(new AdministrativeDivision[] { entityType(a), entityType(a / 2) });
  }

  @EdmFunction(name = "ConvertBirthday", returnType = @ReturnType)
  public Person convertBirthday() {
    Person p = new Person();
    p.setID("1");
    p.setBirthDay(LocalDate.now());
    return p;
  }
}
