package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class JoinPartnerRoleRelationKey implements Serializable {

  private static final long serialVersionUID = 336967410352659606L;

  @Column(name = "\"SourceID\"")
  private String sourceID;

  @Column(name = "\"TargetID\"")
  private String targetID;

  public String getSourceID() {
    return sourceID;
  }

  public void setSourceID(final String sourceID) {
    this.sourceID = sourceID;
  }

  public String getTargetID() {
    return targetID;
  }

  public void setTargetID(final String targetID) {
    this.targetID = targetID;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceID, targetID);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof final JoinPartnerRoleRelationKey other)
      return Objects.equals(sourceID, other.sourceID) && Objects.equals(targetID, other.targetID);
    return false;
  }

}
