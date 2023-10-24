package com.sap.olingo.jpa.processor.core.testobjects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmGeospatial;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataAction;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.ChangeInformation;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

public class TestJavaActions implements ODataAction {

  public static int constructorCalls = 0;
  @SuppressWarnings("unused")
  private final EntityManager em;

  public TestJavaActions(final EntityManager em) {
    super();
    this.em = em;
    assert em != null;
    constructorCalls++;
  }

  @EdmAction()
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

  @EdmAction(returnType = @ReturnType(isNullable = false, precision = 20, scale = 5))
  public BigDecimal unboundReturnFacetNoParameter() {
    return new BigDecimal(7);
  }

  @EdmAction(returnType = @ReturnType(isNullable = false, precision = 20, scale = 5))
  public BigDecimal unboundReturnFacet(
      @EdmParameter(name = "A") final short a, @EdmParameter(name = "B") final int b) {
    return new BigDecimal(a).add(new BigDecimal(b));
  }

  @EdmAction(isBound = true, entitySetPath = "Person/Roles")
  public BusinessPartnerRole boundWithEntitySetPath(
      @EdmParameter(name = "Person") final Person person) {
    return null;
  }

  @EdmAction()
  public ChangeInformation returnEmbeddable() {
    return new ChangeInformation();
  }

  @EdmAction()
  public Person returnEntity() {
    return new Person();
  }

  @EdmAction(returnType = @ReturnType(type = String.class))
  public List<String> returnCollection() {
    return new ArrayList<>();
  }

  @EdmAction(returnType = @ReturnType(type = ChangeInformation.class))
  public List<ChangeInformation> returnEmbeddableCollection() {
    return Arrays.asList(new ChangeInformation());
  }

  @EdmAction(returnType = @ReturnType(maxLength = 60,
      srid = @EdmGeospatial(dimension = Dimension.GEOGRAPHY, srid = "4326")))
  public String calculateLocation(
      @EdmParameter(name = "String", maxLength = 100,
          srid = @EdmGeospatial(dimension = Dimension.GEOGRAPHY, srid = "4326")) final String a) {
    return "";
  }

  @EdmAction()
  public void unboundWithOutParameter() {
    // Do nothing
  }

}
