package com.sap.olingo.jpa.processor.core.testmodel;

import static jakarta.persistence.EnumType.STRING;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

  @Column(name = "\"UserType\"")
  @Enumerated(STRING)
  private UserType userType;

  public String getUsername() {
    return username;
  }

  public void setId(final String username) {
    this.username = username;
  }

  public String getId() {
    return this.username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(final Boolean enabled) {
    this.enabled = enabled;
  }

  public void setUsername(final String username) {
    setId(username);
  }

  public UserType getUserType() {
    return userType;
  }

  public void setUserType(final UserType userType) {
    this.userType = userType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(username);
  }

  @Override
  public boolean equals(final Object object) {

    if (object instanceof final User other)
      return Objects.equals(username, other.username);
    return false;
  }
}
