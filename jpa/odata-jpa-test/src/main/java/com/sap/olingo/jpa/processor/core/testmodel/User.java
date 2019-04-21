package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "\"OLINGO\"", name = "\"User\"")
public class User {

  @Id
  @Column(name = "\"UserName\"", length = 60)
  private String username;

  @Column(name = "\"Password\"", length = 60)
  private String password;

  @Column(name = "\"Enabled\"", length = 60)
  private Boolean enabled;

  public String getUsername() {
    return username;
  }

  public void setId(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }
}
