package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Id;

public class AdministrativeDivisionKey implements Serializable, Comparable<AdministrativeDivisionKey> {

  private static final long serialVersionUID = 5482165952249228988L;
  @Id
  @Column(name = "\"CodePublisher\"", length = 10)
  private String codePublisher;
  @Id
  @Column(name = "\"CodeID\"", length = 10)
  private String codeID;
  @Id
  @Column(name = "\"DivisionCode\"", length = 10)
  private String divisionCode;

  public AdministrativeDivisionKey() {
    // Needed to be used as IdClass
  }

  public AdministrativeDivisionKey(String codePublisher, String codeID, String divisionCode) {
    this.codePublisher = codePublisher;
    this.codeID = codeID;
    this.divisionCode = divisionCode;
  }

  public String getCodePublisher() {
    return codePublisher;
  }

  public void setCodePublisher(String codePublisher) {
    this.codePublisher = codePublisher;
  }

  public String getCodeID() {
    return codeID;
  }

  public void setCodeID(String codeID) {
    this.codeID = codeID;
  }

  public String getDivisionCode() {
    return divisionCode;
  }

  public void setDivisionCode(String divisionCode) {
    this.divisionCode = divisionCode;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((codeID == null) ? 0 : codeID.hashCode());
    result = prime * result + ((codePublisher == null) ? 0 : codePublisher.hashCode());
    result = prime * result + ((divisionCode == null) ? 0 : divisionCode.hashCode());
    return result;
  }

  @Override
  public int compareTo(final AdministrativeDivisionKey o) {
    Objects.requireNonNull(o);
    int result = codePublisher.compareTo(o.codePublisher);
    if (result == 0) {
      result = codeID.compareTo(o.codeID);
      if (result == 0)
        return divisionCode.compareTo(o.divisionCode);
      else
        return result;
    } else {
      return result;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AdministrativeDivisionKey other = (AdministrativeDivisionKey) obj;
    if (codeID == null) {
      if (other.codeID != null) return false;
    } else if (!codeID.equals(other.codeID)) return false;
    if (codePublisher == null) {
      if (other.codePublisher != null) return false;
    } else if (!codePublisher.equals(other.codePublisher)) return false;
    if (divisionCode == null) {
      if (other.divisionCode != null) return false;
    } else if (!divisionCode.equals(other.divisionCode)) return false;
    return true;
  }
}
