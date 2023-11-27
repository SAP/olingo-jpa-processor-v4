package com.sap.olingo.jpa.processor.core.testobjects;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;
import com.sap.olingo.jpa.processor.core.api.example.JPAExampleAuditable;

public class OrganizationWithAudit implements JPAExampleAuditable {

  @Id
  @Column(name = "\"ID\"")
  private Integer id;

  @Id
  @EdmProtectedBy(name = "name")
  @Column(name = "\"Name\"")
  private String name;

  @Column(name = "\"CreatedBy\"")
  private String createdBy;

  @Column(name = "\"CreatedAt\"", precision = 5)
  private LocalDateTime createdAt;

  @Column(name = "\"UpdatedBy\"")
  private String updatedBy;

  @Column(name = "\"UpdatedAt\"", precision = 5)
  private LocalDateTime updatedAt;

  @Override
  public void setCreatedBy(final String user) {
    createdBy = user;
  }

  @Override
  public void setCreatedAt(final LocalDateTime dateTime) {
    createdAt = dateTime;
  }

  @Override
  public void setUpdatedBy(final String user) {
    updatedBy = user;
  }

  @Override
  public void setUpdatedAt(final LocalDateTime dateTime) {
    updatedAt = dateTime;
  }

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }
}
