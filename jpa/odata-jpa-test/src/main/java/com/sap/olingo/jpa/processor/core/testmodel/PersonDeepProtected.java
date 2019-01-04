package com.sap.olingo.jpa.processor.core.testmodel;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtections;

@EdmIgnore // Only
@Entity(name = "PersonDeepProtected")
@DiscriminatorValue(value = "1")
public class PersonDeepProtected extends BusinessPartnerProtected {// #NOSONAR use equal method from BusinessPartner

  @Convert(converter = DateConverter.class)
  @Column(name = "\"BirthDay\"")
  private LocalDate birthDay;

  @Convert(converter = AccessRightsConverter.class)
  @Column(name = "\"AccessRights\"")
  private AccessRights[] accessRights;

  @Embedded
  private AddressDeepProtected inhouseAddress;

  @Embedded
  @EdmProtections({
      @EdmProtectedBy(name = "Creator", path = "created/by"),
      @EdmProtectedBy(name = "Updator", path = "updated/by")
  })
  private AdministrativeInformation protectedAdminInfo = new AdministrativeInformation();

  public PersonDeepProtected() {
    type = "1";
  }

  public LocalDate getBirthDay() {
    return birthDay;
  }

  public void setBirthDay(LocalDate birthDay) {
    this.birthDay = birthDay;
  }

  public AccessRights[] getAccessRights() {
    return accessRights;
  }

}
