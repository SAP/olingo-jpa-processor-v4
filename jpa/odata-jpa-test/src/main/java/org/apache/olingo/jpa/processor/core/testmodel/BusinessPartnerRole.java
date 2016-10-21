package org.apache.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@IdClass(BusinessPartnerRoleKey.class)
//@ReadOnly
@Entity(name = "BusinessPartnerRole")
@Table(schema = "\"OLINGO\"", name = "\"org.apache.olingo.jpa::BusinessPartnerRole\"")
public class BusinessPartnerRole {
  @Id
  @Column(name = "\"BusinessPartnerID\"")
  private String businessPartnerID;
  @Id
  @Column(name = "\"BusinessPartnerRole\"")
  private String roleCategory;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "\"BusinessPartnerID\"", insertable = false, updatable = false)
  private BusinessPartner businessPartner;

  public String getBusinessPartnerID() {
    return businessPartnerID;
  }

  public String getRoleCategory() {
    return roleCategory;
  }

  public BusinessPartner getBusinessPartner() {
    return businessPartner;
  }

  public void setBusinessPartnerID(String businessPartnerID) {
    this.businessPartnerID = businessPartnerID;
  }

  public void setRoleCategory(String roleCategory) {
    this.roleCategory = roleCategory;
  }

}
