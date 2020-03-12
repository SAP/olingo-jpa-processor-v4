package com.sap.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;

import javax.persistence.Id;

public class BusinessPartnerRoleKey implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -2350388598203342905L;

  @Id
  private String businessPartnerID;
  @Id
  private String roleCategory;

  public BusinessPartnerRoleKey() {
    // Needed for JPA
  }

  public BusinessPartnerRoleKey(final String businessPartnerID, final String roleCategory) {
    super();
    this.businessPartnerID = businessPartnerID;
    this.roleCategory = roleCategory;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    BusinessPartnerRoleKey other = (BusinessPartnerRoleKey) obj;
    if (businessPartnerID == null) {
      if (other.businessPartnerID != null) return false;
    } else if (!businessPartnerID.equals(other.businessPartnerID)) return false;
    if (roleCategory == null) {
      if (other.roleCategory != null) return false;
    } else if (!roleCategory.equals(other.roleCategory)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((businessPartnerID == null) ? 0 : businessPartnerID.hashCode());
    result = prime * result + ((roleCategory == null) ? 0 : roleCategory.hashCode());
    return result;
  }

  public String getBusinessPartnerID() {
    return businessPartnerID;
  }

  public void setBusinessPartnerID(String businessPartnerID) {
    this.businessPartnerID = businessPartnerID;
  }

  public String getRoleCategory() {
    return roleCategory;
  }

  public void setRoleCategory(String roleCategory) {
    this.roleCategory = roleCategory;
  }
}
