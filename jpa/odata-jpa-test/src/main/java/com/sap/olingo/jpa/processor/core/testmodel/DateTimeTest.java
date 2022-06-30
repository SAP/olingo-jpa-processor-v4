package com.sap.olingo.jpa.processor.core.testmodel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Test for time conversion
 * @author Oliver Grande
 * @since 1.0.1
 * Created 2020-10-31
 */
@Entity(name = "DateTimeTest")
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartner\"")
public class DateTimeTest {

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Column(name = "\"CreatedAt\"", precision = 3, insertable = false, updatable = false)
  @Convert(converter = DateTimeConverter.class)
  private LocalDateTime creationDateTime;

  @Column(name = "\"CreatedAt\"", precision = 9)
  @Temporal(TemporalType.TIMESTAMP)
  private Date at;

  @Column(name = "\"BirthDay\"")
  private LocalDate birthDay;
}
