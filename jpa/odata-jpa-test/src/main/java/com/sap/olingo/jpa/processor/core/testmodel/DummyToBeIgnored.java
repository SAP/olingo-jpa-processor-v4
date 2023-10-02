package com.sap.olingo.jpa.processor.core.testmodel;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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
  @Column(name = "\"ID\"")
  private String iD;

  @Convert(converter = ByteConverter.class)
  private byte uuid;

  @Column(name = "\"GUID\"")
  // @Convert(converter = UUIDToStringConverter.class)
  private UUID guid;

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
