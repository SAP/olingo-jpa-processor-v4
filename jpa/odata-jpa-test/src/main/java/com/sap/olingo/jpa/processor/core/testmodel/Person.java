package com.sap.olingo.jpa.processor.core.testmodel;

import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;

@Entity(name = "Person")
@EdmFunctions({
    @EdmFunction(
        name = "CheckRights",
        functionName = "CHECK_RIGHTS",
        hasFunctionImport = true,
        isBound = false,
        returnType = @EdmFunction.ReturnType(type = Boolean.class, isCollection = false),
        parameter = {
            @EdmParameter(name = "R", parameterName = "Right", type = AccessRights.class),
            @EdmParameter(name = "U", parameterName = "UserRights", type = Integer.class) }),

    @EdmFunction(
        name = "ReturnRights",
        functionName = "RETURN_RIGHTS",
        hasFunctionImport = true,
        isBound = false,
        returnType = @EdmFunction.ReturnType(type = AccessRights.class, isCollection = false),
        parameter = {
            @EdmParameter(name = "U", parameterName = "UserRights", type = Integer.class) })
})

@DiscriminatorValue(value = "1")
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartner\"")
public class Person extends BusinessPartner {

  @Column(name = "\"NameLine1\"")
  private String firstName;

  @Column(name = "\"NameLine2\"")
  private String lastName;

  @Convert(converter = DateConverter.class)
  @Column(name = "\"BirthDay\"")
  private LocalDate birthDay;

  @Convert(converter = AccessRightsConverter.class)
  @Column(name = "\"AccessRights\"")
  private AccessRights[] accessRights;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "\"ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false, nullable = true)
  private PersonImage image;

  public Person() {
    type = "1";
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public LocalDate getBirthDay() {
    return birthDay;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setBirthDay(LocalDate birthDay) {
    this.birthDay = birthDay;
  }

  public AccessRights[] getAccessRights() {
    return accessRights;
  }
}
