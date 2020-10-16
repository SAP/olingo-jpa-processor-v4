package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AdministrativeDivisionDescriptionKey implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 4843041820527005995L;

  @Column(name = "\"CodePublisher\"", length = 10, nullable = false)
  private String codePublisher;

  @Column(name = "\"CodeID\"", length = 10, nullable = false)
  private String codeID;

  @Column(name = "\"DivisionCode\"", length = 10, nullable = false)
  private String divisionCode;

  @Column(name = "\"LanguageISO\"")
  private String language;

  public AdministrativeDivisionDescriptionKey() {
    // Needed for JPA
  }

  public AdministrativeDivisionDescriptionKey(final String codePublisher, final String codeID,
      final String divisionCode, final String language) {
    super();
    this.codePublisher = codePublisher;
    this.codeID = codeID;
    this.divisionCode = divisionCode;
    this.language = language;
  }

  public String getLanguage() {
    return language;
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

  public void setLanguage(String language) {
    this.language = language;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((codeID == null) ? 0 : codeID.hashCode());
    result = prime * result + ((codePublisher == null) ? 0 : codePublisher.hashCode());
    result = prime * result + ((divisionCode == null) ? 0 : divisionCode.hashCode());
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) { // NOSONAR
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AdministrativeDivisionDescriptionKey other = (AdministrativeDivisionDescriptionKey) obj;
    if (codeID == null) {
      if (other.codeID != null) return false;
    } else if (!codeID.equals(other.codeID)) return false;
    if (codePublisher == null) {
      if (other.codePublisher != null) return false;
    } else if (!codePublisher.equals(other.codePublisher)) return false;
    if (divisionCode == null) {
      if (other.divisionCode != null) return false;
    } else if (!divisionCode.equals(other.divisionCode)) return false;
    if (language == null) {
      if (other.language != null) return false;
    } else if (!language.equals(other.language)) return false;
    return true;
  }

}
