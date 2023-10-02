package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class JoinPartnerRoleRelationKey implements Serializable{

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
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof JoinPartnerRoleRelationKey)) return false;
    final JoinPartnerRoleRelationKey other = (JoinPartnerRoleRelationKey) obj;
    return Objects.equals(sourceID, other.sourceID) && Objects.equals(targetID, other.targetID);
  }

}
