package com.sap.olingo.jpa.processor.core.testmodel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

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
