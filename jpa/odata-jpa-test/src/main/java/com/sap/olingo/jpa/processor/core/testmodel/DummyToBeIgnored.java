package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

/**
 * Entity implementation class for Entity: DummyToBeIgnored
 *
 */
@Entity
@EdmFunctions({
    @EdmFunction(
        name = "IsOdd",
        functionName = "IS_ODD",
        returnType = @EdmFunction.ReturnType(isCollection = true),
        parameter = { @EdmFunctionParameter(name = "Number", type = BigDecimal.class, precision = 32, scale = 0) }),

})
@Table(schema = "\"OLINGO\"", name = "\"DummyToBeIgnored\"")
@EdmIgnore
public class DummyToBeIgnored implements Serializable {

  @Id
  private String ID;
  private static final long serialVersionUID = 1L;

  @Convert(converter = ByteConverter.class)
  private byte uuid;

  @EdmIgnore
  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "\"ID\"", insertable = false, updatable = false)
  private BusinessPartner businessPartner;

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
