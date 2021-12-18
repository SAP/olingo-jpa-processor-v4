package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.List;
import java.util.Objects;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Version;

import com.sap.olingo.jpa.processor.core.testmodel.AddressDeepProtected;

@Entity(name = "PersonDeepCollectionProtected")
public class PersonDeepCollectionProtected {// #NOSONAR use equal method from

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag; // BusinessPartner

  @Column(name = "\"Type\"", length = 1, insertable = false, updatable = false, nullable = false)
  protected String type;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(schema = "\"OLINGO\"", name = "\"InhouseAddress\"",
      joinColumns = @JoinColumn(name = "\"ID\""))
  private List<AddressDeepProtected> inhouseAddress;

  public PersonDeepCollectionProtected() {
    type = "1";
  }

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public long geteTag() {
    return eTag;
  }

  public void seteTag(final long eTag) {
    this.eTag = eTag;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public List<AddressDeepProtected> getInhouseAddress() {
    return inhouseAddress;
  }

  public void setInhouseAddress(final List<AddressDeepProtected> inhouseAddress) {
    this.inhouseAddress = inhouseAddress;
  }

  @Override
  public int hashCode() {
    return Objects.hash(iD);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final PersonDeepCollectionProtected other = (PersonDeepCollectionProtected) obj;
    return Objects.equals(iD, other.iD);
  }

}
