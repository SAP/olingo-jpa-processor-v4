package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class JoinRelationKey implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 5206755977104102088L;

  @Column(name = "\"SourceID\"")
  private Integer sourceID;

  @Column(name = "\"TargetID\"")
  private Integer targetID;

  public Integer getSourceID() {
    return sourceID;
  }

  public void setSourceID(final Integer sourceID) {
    this.sourceID = sourceID;
  }

  public Integer getTargetID() {
    return targetID;
  }

  public void setTargetID(final Integer targetID) {
    this.targetID = targetID;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((sourceID == null) ? 0 : sourceID.hashCode());
    result = prime * result + ((targetID == null) ? 0 : targetID.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final JoinRelationKey other = (JoinRelationKey) obj;
    if (sourceID == null) {
      if (other.sourceID != null) return false;
    } else if (!sourceID.equals(other.sourceID)) return false;
    if (targetID == null) {
      if (other.targetID != null) return false;
    } else if (!targetID.equals(other.targetID)) return false;
    return true;
  }
}
