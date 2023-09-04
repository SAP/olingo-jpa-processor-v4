package com.sap.olingo.jpa.processor.core.testmodel;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;

@Entity(name = "PersonProtected")
@Table(schema = "\"OLINGO\"", name = "\"PersonProtected\"")
public class PersonDeepProtected {// #NOSONAR use equal method from BusinessPartner
//  CREATE VIEW
//  AS
//SELECT b."ID",b."ETag",b."NameLine1",b."NameLine2",b."CreatedBy",b."CreatedAt",b."UpdatedBy",b."UpdatedAt"

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

  @Column(name = "\"NameLine1\"")
  private String firstName;

  @Column(name = "\"NameLine2\"")
  private String lastName;

  @Column(name = "\"CreatedAt\"", precision = 3, insertable = false, updatable = false)
  private Timestamp creationDateTime;

  @Embedded
  private AddressDeepProtected inhouseAddress;

  @Embedded
  @EdmProtectedBy(name = "Creator", path = "created/by")
  @EdmProtectedBy(name = "Updator", path = "updated/by")
  private AdministrativeInformation protectedAdminInfo = new AdministrativeInformation();

  // To test that claims are respected also when retrieving collection!
  @EdmAnnotation(term = "Core.Description", qualifier = "Address",
      constantExpression = @EdmAnnotation.ConstantExpression(type = ConstantExpressionType.String,
          value = "Address for inhouse Mail"))
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(schema = "\"OLINGO\"", name = "\"InhouseAddress\"",
      joinColumns = @JoinColumn(name = "\"ParentID\""))
  private final List<InhouseAddress> inhouseAddresses = new ArrayList<>();

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  public AddressDeepProtected getInhouseAddress() {
    return inhouseAddress;
  }

  public void setInhouseAddress(final AddressDeepProtected inhouseAddress) {
    this.inhouseAddress = inhouseAddress;
  }

  public AdministrativeInformation getProtectedAdminInfo() {
    return protectedAdminInfo;
  }

  public void setProtectedAdminInfo(final AdministrativeInformation protectedAdminInfo) {
    this.protectedAdminInfo = protectedAdminInfo;
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
    final PersonDeepProtected other = (PersonDeepProtected) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }

}
