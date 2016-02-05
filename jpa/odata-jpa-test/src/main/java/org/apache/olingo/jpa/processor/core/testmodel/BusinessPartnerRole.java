package org.apache.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ReadOnly;

@IdClass(BusinessPartnerRoleKey.class)
@ReadOnly
@Entity(name = "BusinessPartnerRole")
@Table(schema = "\"OLINGO\"", name = "\"org.apache.olingo.jpa::BusinessPartnerRole\"")
public class BusinessPartnerRole {
  @Id
  @Column(name = "\"BusinessPartnerID\"")
  private String businessPartnerID;
  @Id
  @Column(name = "\"BusinessPartnerRoleCategoryCode\"")
  private String roleCategory;

  @ManyToOne(optional = false)
  @JoinColumn(referencedColumnName = "\"ID\"", name = "\"BusinessPartnerID\"", insertable = false,
      updatable = false)
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

}
