package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;

public class MembershipKey implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -2197928070426048826L;

  @Id
  @Column(name = "\"PersonID\"", length = 32)
  private String personID;
  @Id
  @Column(name = "\"TeamID\"", length = 32)
  private String teamID;

  public MembershipKey() {
    // Needed to be used as IdClass
  }

  public MembershipKey(final String personID, final String teamID) {
    super();
    this.personID = personID;
    this.teamID = teamID;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((personID == null) ? 0 : personID.hashCode());
    result = prime * result + ((teamID == null) ? 0 : teamID.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MembershipKey other = (MembershipKey) obj;
    if (personID == null) {
      if (other.personID != null) return false;
    } else if (!personID.equals(other.personID)) return false;
    if (teamID == null) {
      if (other.teamID != null) return false;
    } else if (!teamID.equals(other.teamID)) return false;
    return true;
  }

}
