package com.sap.olingo.jpa.processor.core.testmodel;

import java.sql.Clob;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@EdmIgnore
@Entity
@Table(schema = "\"OLINGO\"", name = "\"Comment\"")
public class Comment {

  @Id
  @Column(name = "\"BusinessPartnerID\"")
  private String businessPartnerID;

  @Column(name = "\"Order\"")
  private String order;

  @Lob
  @Column(name = "\"Text\"")
  @Basic(fetch = FetchType.LAZY)
  private Clob text;

  public Comment() {
    super();
  }

  public String getBusinessPartnerID() {
    return this.businessPartnerID;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(final String order) {
    this.order = order;
  }

  public Clob getText() {
    return text;
  }

  public void setText(final Clob text) {
    this.text = text;
  }

  public void setBusinessPartnerID(final String businessPartnerID) {
    this.businessPartnerID = businessPartnerID;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((businessPartnerID == null) ? 0 : businessPartnerID.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final Comment other = (Comment) obj;
    if (businessPartnerID == null) {
      if (other.businessPartnerID != null) return false;
    } else if (!businessPartnerID.equals(other.businessPartnerID)) return false;
    return true;
  }

}
