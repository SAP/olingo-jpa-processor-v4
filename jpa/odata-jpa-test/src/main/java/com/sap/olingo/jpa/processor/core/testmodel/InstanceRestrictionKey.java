package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class InstanceRestrictionKey {

  @Column(name = "\"UserName\"", length = 60)
  private String username;

  @Column(name = "\"SequenceNumber\"")
  private Integer sequenceNumber;

  public InstanceRestrictionKey() {
    // Needed
  }

  public InstanceRestrictionKey(String username, Integer sequenceNumber) {
    super();
    this.username = username;
    this.sequenceNumber = sequenceNumber;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((sequenceNumber == null) ? 0 : sequenceNumber.hashCode());
    result = prime * result + ((username == null) ? 0 : username.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    InstanceRestrictionKey other = (InstanceRestrictionKey) obj;
    if (sequenceNumber == null) {
      if (other.sequenceNumber != null) return false;
    } else if (!sequenceNumber.equals(other.sequenceNumber)) return false;
    if (username == null) {
      if (other.username != null) return false;
    } else if (!username.equals(other.username)) return false;
    return true;
  }
}
