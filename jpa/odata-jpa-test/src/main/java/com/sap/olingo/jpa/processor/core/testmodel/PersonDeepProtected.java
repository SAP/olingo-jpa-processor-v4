package com.sap.olingo.jpa.processor.core.testmodel;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtections;

@Entity(name = "PersonProtected")
@Table(schema = "\"OLINGO\"", name = "PersonProtected")
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
  @EdmProtections({
      @EdmProtectedBy(name = "Creator", path = "created/by"),
      @EdmProtectedBy(name = "Updator", path = "updated/by")
  })
  private AdministrativeInformation protectedAdminInfo = new AdministrativeInformation();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((iD == null) ? 0 : iD.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    PersonDeepProtected other = (PersonDeepProtected) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }

}
