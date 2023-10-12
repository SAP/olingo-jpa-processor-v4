package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Version;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;
import com.sap.olingo.jpa.processor.core.testmodel.InhouseAddress;

@Entity(name = "CollectionAttributeProtected")
@DiscriminatorValue(value = "1")
public class CollectionAttributeProtected {

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(schema = "\"OLINGO\"", name = "\"InhouseAddress\"",
      joinColumns = @JoinColumn(name = "\"ParentID\""))
  @EdmProtectedBy(name = "WrongAnnotation")
  private List<InhouseAddress> inhouseAddress = new ArrayList<>();

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
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
    final CollectionAttributeProtected other = (CollectionAttributeProtected) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }
}
