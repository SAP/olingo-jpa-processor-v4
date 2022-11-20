package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmGeospatial;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataAction;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.AccessRights;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.ChangeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.PostalAddressData;

public class ExampleJavaActions implements ODataAction {

  @EdmAction(name = "")
  public Integer unboundWithImport(
      @EdmParameter(name = "A") final short a, @EdmParameter(name = "B") final int b) {
    return a + b;
  }

  @EdmAction(name = "BoundNoImport", isBound = true)
  public void boundNoImport(
      @EdmParameter(name = "Person") final Person person,
      @EdmParameter(name = "A", precision = 34, scale = 10) final BigDecimal a) {
    // Do nothing
  }

  @EdmAction(name = "", returnType = @ReturnType(isNullable = false, precision = 20, scale = 5))
  public BigDecimal unboundReturnFacet(
      @EdmParameter(name = "A") final short a, @EdmParameter(name = "B") final int b) {
    return new BigDecimal(a).add(new BigDecimal(b));
  }

  @EdmAction(name = "", isBound = true, entitySetPath = "Person/Roles")
  public BusinessPartnerRole boundWithEntitySetPath(
      @EdmParameter(name = "Person") final Person person) {
    return null;
  }

  @EdmAction(name = "")
  public ChangeInformation returnEmbeddable() {
    return new ChangeInformation();
  }

  @EdmAction(name = "")
  public Person returnEntity() {
    return new Person();
  }

  @EdmAction
  public ABCClassification returnEnumeration(@EdmParameter(name = "Rights") final AccessRights rights) {
    return ABCClassification.B;
  }

  @EdmAction(name = "", returnType = @ReturnType(type = String.class))
  public List<String> returnCollection() {
    return new ArrayList<>();
  }

  @EdmAction(name = "", returnType = @ReturnType(type = ChangeInformation.class))
  public List<ChangeInformation> returnEmbeddableCollection() {
    return Arrays.asList(new ChangeInformation());
  }

  @EdmAction(name = "")
  public List<String> returnCollectionWithoutReturnType() {
    return new ArrayList<>();
  }

  @EdmAction(name = "",
      returnType = @ReturnType(maxLength = 60,
          srid = @EdmGeospatial(dimension = Dimension.GEOGRAPHY, srid = "4326")))
  public String calculateLocation(
      @EdmParameter(name = "String", maxLength = 100,
          srid = @EdmGeospatial(dimension = Dimension.GEOGRAPHY, srid = "4326")) final String a) {
    return "";
  }

  @EdmAction(name = "")
  public Integer errorNonPrimitiveParameter(
      @EdmParameter(name = "A") final PostalAddressData a) {
    return 1;
  }

  @EdmAction(name = "", isBound = true)
  public void boundWithOutBindingParameter(
      @EdmParameter(name = "A", precision = 34, scale = 10) final BigDecimal a) {
    // Do nothing
  }

  @EdmAction(name = "", isBound = true)
  public void boundWithOutParameter() {
    // Do nothing
  }

  @EdmAction(name = "", isBound = true)
  public void boundBindingParameterSecondParameter(
      @EdmParameter(name = "A", precision = 34, scale = 10) final BigDecimal a,
      @EdmParameter(name = "Person") final Person person) {
    // Do nothing
  }

  @EdmAction(name = "", isBound = false, entitySetPath = "Person/Address")
  public PostalAddressData errorUnboundWithEntitySetPath(
      @EdmParameter(name = "Person") final Person person) {
    return null;
  }

  @EdmAction(name = "", isBound = true, entitySetPath = "Person/Address")
  public Integer errorPrimitiveTypeWithEntitySetPath(
      @EdmParameter(name = "Person") final Person person) {
    return null;
  }

  @EdmAction(name = "", returnType = @ReturnType())
  public void nameEmpty(@EdmParameter(name = "") final AccessRights rights) {
    // Do nothing
  }
}
