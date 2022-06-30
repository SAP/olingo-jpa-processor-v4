package com.sap.olingo.jpa.processor.core.testmodel;

import java.math.BigDecimal;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;

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
        parameter = { @EdmParameter(name = "Number", type = BigDecimal.class, precision = 32, scale = 0) }),

})
@Table(schema = "\"OLINGO\"", name = "\"DummyToBeIgnored\"")
@EdmIgnore
public class DummyToBeIgnored {

  @Id
  private String iD;

  @Convert(converter = ByteConverter.class)
  private byte uuid;

  @EdmIgnore
  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "iD", insertable = false, updatable = false)
  private BusinessPartner businessPartner;

  public DummyToBeIgnored() {
    super();
  }

  public String getID() {
    return this.iD;
  }

  public void setID(final String ID) {
    this.iD = ID;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((iD == null) ? 0 : iD.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final DummyToBeIgnored other = (DummyToBeIgnored) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }

}
