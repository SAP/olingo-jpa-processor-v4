package org.apache.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmSearchable;

@Entity
//@IdClass(AdministrativeDivisionDescriptionKey.class)
@Table(schema = "\"OLINGO\"", name = "\"org.apache.olingo.jpa::AdministrativeDivisionDescription\"")
public class AdministrativeDivisionDescription {

//  @Id
//  @Column(name = "\"CodePublisher\"", length = 10, nullable = false)
//  private String codePublisher;
//  @Id
//  @Column(name = "\"CodeID\"", length = 10, nullable = false)
//  private String codeID;
//  @Id
//  @Column(name = "\"DivisionCode\"", length = 10, nullable = false)
//  private String divisionCode;
//  @Id
//  @Column(name = "\"LanguageISO\"")
//  private String language;

  @EmbeddedId
  private AdministrativeDivisionDescriptionKey key;
  @EdmSearchable
  @Column(name = "\"Name\"", length = 100)
  private String name;

//  public String getCodePublisher() {
//    return codePublisher;
//  }
//
//  public void setCodePublisher(String codePublisher) {
//    this.codePublisher = codePublisher;
//  }
//
//  public String getCodeID() {
//    return codeID;
//  }
//
//  public void setCodeID(String codeID) {
//    this.codeID = codeID;
//  }
//
//  public String getDivisionCode() {
//    return divisionCode;
//  }
//
//  public void setDivisionCode(String divisionCode) {
//    this.divisionCode = divisionCode;
//  }
//
//  public String getLanguage() {
//    return language;
//  }
//
//  public void setLanguage(String language) {
//    this.language = language;
//  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
