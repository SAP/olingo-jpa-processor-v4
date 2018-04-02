package com.sap.olingo.jpa.processor.core.testmodel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation;
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

  @EdmAnnotation(term = "Core.Description", constantExpression = @EdmAnnotation.ConstantExpression(
      type = ConstantExpressionType.String, value = "Address for inhouse Mail"))
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(schema = "\"OLINGO\"", name = "\"InhouseAddress\"",
      joinColumns = @JoinColumn(name = "\"ID\""))
  private List<InhouseAddress> inhouseAddress = new ArrayList<>();

  @ManyToMany
  @JoinTable(name = "\"SupportRelationship\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"PersonID\""),
      inverseJoinColumns = @JoinColumn(name = "\"OrganizationID\""))
  private List<Organization> supportedOrganizations;

  @ManyToMany
  @JoinTable(name = "\"Membership\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"PersonID\""),
      inverseJoinColumns = @JoinColumn(name = "\"TeamID\""))
  private List<Team> teams;

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

  public List<InhouseAddress> getInhouseAddress() {
    return inhouseAddress;
  }

  public void setInhouseAddress(final List<InhouseAddress> inhouseAddress) {
    this.inhouseAddress = inhouseAddress;
  }

  public void addInhouseAddress(final InhouseAddress address) {
    inhouseAddress.add(address);
  }
}
