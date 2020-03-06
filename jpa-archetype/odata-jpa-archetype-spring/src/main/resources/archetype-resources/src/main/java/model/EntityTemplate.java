package ${package}.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity(name = "${entity-table}")
@Table(schema = "\"${schema}\"", name = "\"${entity-table}\"")
public class EntityTemplate {
  @Id
  @Column(name = "\"ID\"", length = 32)
  private String id;

  @OneToMany(mappedBy = "entity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Collection<ValueObjectTemplate> valueObjects = new ArrayList<>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Collection<ValueObjectTemplate> getValueObjects() {
    return valueObjects;
  }

  public void setValueObjects(Collection<ValueObjectTemplate> valueObjects) {
    this.valueObjects = valueObjects;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    EntityTemplate other = (EntityTemplate) obj;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    return true;
  }
}