package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;
import java.sql.Clob;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@EdmIgnore
@Entity
@Table(schema = "\"OLINGO\"", name = "\"Comment\"")
public class Comment implements Serializable {
  private static final long serialVersionUID = 1L;

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

  public void setOrder(String order) {
    this.order = order;
  }

  public Clob getText() {
    return text;
  }

  public void setText(Clob text) {
    this.text = text;
  }

  public void setBusinessPartnerID(String businessPartnerID) {
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
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Comment other = (Comment) obj;
    if (businessPartnerID == null) {
      if (other.businessPartnerID != null) return false;
    } else if (!businessPartnerID.equals(other.businessPartnerID)) return false;
    return true;
  }

}
