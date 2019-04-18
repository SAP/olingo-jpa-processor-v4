package ${package}.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "${table}")
@Table(schema = "\"${schema}\"", name = "\"${table}\"")
public class Template {
  @Id
  @Column(name = "\"ID\"", length = 32)
  private String id;
}
