package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmGeospatial;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataFunction;
import com.sap.olingo.jpa.processor.core.testmodel.ChangeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.PostalAddressData;

public class ExampleJavaFunctions implements ODataFunction {

  @EdmFunction(name = "Add", isBound = true, hasFunctionImport = false, returnType = @ReturnType)
  public Integer sum(
      @EdmFunctionParameter(name = "A") short a, @EdmFunctionParameter(name = "B") int b) {
    return a + b;
  }

  @EdmFunction(name = "", returnType = @ReturnType,
      parameter = {
          @EdmFunctionParameter(name = "Dmmy", parameterName = "A",
              type = String.class, maxLength = 10) })
  public Integer div(
      @EdmFunctionParameter(name = "A") short a, @EdmFunctionParameter(name = "B") int b) {
    return a / b;
  }

  @EdmFunction(name = "", returnType = @ReturnType(type = Double.class))
  public Integer errorReturnType(
      @EdmFunctionParameter(name = "A") short a, @EdmFunctionParameter(name = "B") int b) {
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
    return new ArrayList<String>();
  }

  @EdmFunction(name = "", returnType = @ReturnType())
  public List<String> returnCollectionWithoutReturnType() {
    return new ArrayList<String>();
  }

  @EdmFunction(name = "", returnType = @ReturnType())
  public ChangeInformation returnEmbeddable() {
    return new ChangeInformation();
  }

  @EdmFunction(name = "", returnType = @ReturnType(type = ChangeInformation.class))
  public List<ChangeInformation> returnEmbeddableCollection() {
    return Arrays.asList(new ChangeInformation[] { new ChangeInformation() });
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
      @EdmFunctionParameter(name = "A") PostalAddressData a) {
    return 1;
  }
}
