package com.sap.olingo.jpa.processor.core.testmodel;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
public class PersonDeepProtectedHidden extends BusinessPartnerProtected {// #NOSONAR use equal method from
                                                                         // BusinessPartner

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
  private final AdministrativeInformation protectedAdminInfo = new AdministrativeInformation();

  public PersonDeepProtectedHidden() {
    type = "1";
  }

  public LocalDate getBirthDay() {
    return birthDay;
  }

  public void setBirthDay(final LocalDate birthDay) {
    this.birthDay = birthDay;
  }

  public Short getAccessRights() {
    return new AccessRightsConverter().convertToDatabaseColumn(accessRights);
  }

  public List<AccessRights> getAccessRightsAsList() {
    return accessRights == null ? Collections.emptyList() : Arrays.asList(accessRights);
  }
}
