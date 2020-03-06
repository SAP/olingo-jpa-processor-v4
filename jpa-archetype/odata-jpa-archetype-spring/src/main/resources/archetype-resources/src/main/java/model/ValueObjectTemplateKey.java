package ${package}.model;

import java.io.Serializable;

import javax.persistence.Id;

public class ValueObjectTemplateKey implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  private String id;

  @Id
  private String entityId;

  String getEmploymentId() {
    return entityId;
  }

  void setEmploymentId(String employmentId) {
    this.entityId = employmentId;
  }

  String getId() {
    return id;
  }

  void setId(String id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ValueObjectTemplateKey other = (ValueObjectTemplateKey) obj;
    if (entityId == null) {
      if (other.entityId != null) return false;
    } else if (!entityId.equals(other.entityId)) return false;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    return true;
  }
}