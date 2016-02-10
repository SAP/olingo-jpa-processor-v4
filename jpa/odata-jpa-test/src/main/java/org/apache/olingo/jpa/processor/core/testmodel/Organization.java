package org.apache.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;

@EdmFunction(
    name = "AllCustomersByABC",
    functionName = "CUSTOMER_BY_ABC",
    returnType = @ReturnType(type = Organization.class, isCollection = true) ,
    parameter = { @EdmFunctionParameter(name = "Class", type = Character.class) })

@Entity(name = "Organization")
@DiscriminatorValue(value = "2")
@Table(schema = "\"OLINGO\"", name = "\"org.apache.olingo.jpa::BusinessPartner\"")
public class Organization extends BusinessPartner {

  @Column(name = "\"NameLine1\"")
  private String name1;

  @Column(name = "\"NameLine2\"")
  private String name2;

  public String getName1() {
    return name1;
  }

  public void setName1(String name1) {
    this.name1 = name1;
  }

  public String getName2() {
    return name2;
  }

  public void setName2(String name2) {
    this.name2 = name2;
  }
}
