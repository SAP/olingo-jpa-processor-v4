package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;

@Inheritance
@DiscriminatorColumn(name = "\"Type\"")
@Entity
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartnerProtected\"")
public class BusinessPartnerProtected {

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Version
  @EdmIgnore
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

  @Column(name = "\"Type\"", length = 1, insertable = false, updatable = false, nullable = false)
  protected String type;

  @Column(name = "\"NameLine1\"")
  private String name1;

  @Column(name = "\"NameLine2\"")
  private String name2;

  @Column(name = "\"Country\"", length = 4)
  private String country;

  @EdmProtectedBy(name = "UserId")
  @EdmIgnore
  @Column(name = "\"UserName\"", length = 60)
  private String userName;

  @Embedded
  private final AdministrativeInformation administrativeInformation = new AdministrativeInformation();

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "\"BusinessPartnerID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
  private Collection<BusinessPartnerRoleProtected> rolesProtected;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "\"BusinessPartnerID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
  private Collection<BusinessPartnerRole> roles;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinTable(name = "\"JoinPartnerRoleRelation\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"SourceID\"", referencedColumnName = "\"ID\""),
      inverseJoinColumns = {
          @JoinColumn(name = "\"SourceID\"", referencedColumnName = "\"BusinessPartnerID\""),
          @JoinColumn(name = "\"TargetID\"", referencedColumnName = "\"BusinessPartnerRole\"")
      })
  private List<BusinessPartnerRoleProtected> rolesJoinProtected;

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
    final BusinessPartnerProtected other = (BusinessPartnerProtected) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }

  public String getID() {
    return iD;
  }

  public long getETag() {
    return eTag;
  }

  public String getType() {
    return type;
  }

  public String getName1() {
    return name1;
  }

  public String getName2() {
    return name2;
  }

  public String getCountry() {
    return country;
  }

  public String getUserName() {
    return userName;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

}
