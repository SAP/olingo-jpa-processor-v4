package org.apache.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

/**
 * Entity implementation class for Entity: DummyToBeIgnored
 *
 */
@Entity
@EdmFunctions({
    @EdmFunction(
        name = "IsOdd",
        functionName = "IS_ODD",
        returnType = @EdmFunction.ReturnType(isCollection = true) ,
        parameter = { @EdmFunctionParameter(name = "Number", type = BigDecimal.class, precision = 32, scale = 0) }),

})
@Table(schema = "\"OLINGO\"", name = "\"org.apache.olingo.jpa::DummyToBeIgnored\"")
@EdmIgnore
public class DummyToBeIgnored implements Serializable {

  @Id
  private String ID;
  private static final long serialVersionUID = 1L;

  public DummyToBeIgnored() {
    super();
  }

  public String getID() {
    return this.ID;
  }

  public void setID(String ID) {
    this.ID = ID;
  }

}
