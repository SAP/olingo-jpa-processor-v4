package org.apache.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(CountryKey.class)
@Table(schema = "\"OLINGO\"", name = "\"org.apache.olingo.jpa::CountryDescription\"")
public class Country {
  @Id
  @Column(name = "\"ISOCode\"")
  private String code;
  @Id
  @Column(name = "\"LanguageISO\"")
  private String language;
  @Column(name = "\"Name\"", length = 100)
  private String name;
}
