package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CountryKey implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 229175464207091262L;

  @Column(name = "\"ISOCode\"")
  private String code;

  @Column(name = "\"LanguageISO\"")
  private String language;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CountryKey other = (CountryKey) obj;
    if (code == null) {
      if (other.code != null) return false;
    } else if (!code.equals(other.code)) return false;
    if (language == null) {
      if (other.language != null) return false;
    } else if (!language.equals(other.language)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((code == null) ? 0 : code.hashCode());
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    return result;
  }
}
