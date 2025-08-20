package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmGeospatial;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataFunction;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.AccessRights;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.ChangeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.PostalAddressData;

public class ExampleJavaFunctions implements ODataFunction {

  public ExampleJavaFunctions(final JPARequestParameterMap parameterMap, final JPAHttpHeaderMap header) {
    super();
  }

  @EdmFunction(name = "Add", isBound = true, hasFunctionImport = true, returnType = @ReturnType,
      visibleFor = @EdmVisibleFor({ "Person", "Company" }))
  public Integer sum(
      @EdmParameter(name = "A") final short a, @EdmParameter(name = "B") final int b) {
    return a + b;
  }

  @EdmFunction(name = "", returnType = @ReturnType,
      parameter = {
          @EdmParameter(name = "Dummy", parameterName = "A",
              type = String.class, maxLength = 10) })
  public Integer div(
      @EdmParameter(name = "A") final short a, @EdmParameter(name = "B") final int b) {
    return a / b;
  }

  @EdmFunction(name = "", returnType = @ReturnType(type = Double.class))
  public Integer errorReturnType(
      @EdmParameter(name = "A") final short a, @EdmParameter(name = "B") final int b) {
    return a + b;
  }

  @EdmFunction(name = "", returnType = @ReturnType(isNullable = false, precision = 9, scale = 3))
  public Timestamp now() {
    return Timestamp.valueOf(LocalDateTime.now(ZoneId.of("UTC")));
  }

  @EdmFunction(name = "", returnType = @ReturnType(maxLength = 60, srid = @EdmGeospatial(
      dimension = Dimension.GEOGRAPHY, srid = "4326")))
  public String determineLocation() {
    return "";
  }

  @EdmFunction(name = "", returnType = @ReturnType(type = String.class))
  public List<String> returnCollection() {
    return new ArrayList<>();
  }

  @EdmFunction(name = "", returnType = @ReturnType())
  public List<String> returnCollectionWithoutReturnType() {
    return new ArrayList<>();
  }

  @EdmFunction(name = "", returnType = @ReturnType())
  public ChangeInformation returnEmbeddable() {
    return new ChangeInformation();
  }

  @EdmFunction(name = "", returnType = @ReturnType(type = ChangeInformation.class))
  public List<ChangeInformation> returnEmbeddableCollection() {
    return Arrays.asList(new ChangeInformation());
  }

  @EdmFunction(name = "", returnType = @ReturnType())
  public Person returnEntity() {
    return new Person();
  }

  @EdmFunction(name = "", returnType = @ReturnType())
  public ExampleJavaOneFunction wrongReturnType() {
    return new ExampleJavaOneFunction();
  }

  @EdmFunction(name = "", returnType = @ReturnType())
  public Integer errorNonPrimitiveParameter(
      @EdmParameter(name = "A") final PostalAddressData a) {
    return 1;
  }

  @EdmFunction(name = "", returnType = @ReturnType())
  public ABCClassification returnEnumerationType(@EdmParameter(name = "Rights") final AccessRights rights) {
    return ABCClassification.A;
  }

  @EdmFunction(name = "", returnType = @ReturnType(type = ABCClassification.class))
  public List<ABCClassification> returnEnumerationCollection() {
    return new ArrayList<>();
  }

  @EdmFunction(name = "", isBound = true, entitySetPath = "Person/Roles", returnType = @ReturnType())
  public BusinessPartnerRole boundWithEntitySetPath(
      @EdmParameter(name = "Person") final Person person) {
    return null;
  }

  @EdmFunction(name = "", returnType = @ReturnType())
  public Integer nameEmpty(@EdmParameter(name = "") final AccessRights rights) {
    return 0;
  }

  @EdmFunction(name = "", returnType = @ReturnType(type = Void.class))
  public void returnsNothing(@EdmParameter(name = "rights") final AccessRights rights) {
    // Do nothing
  }
}
