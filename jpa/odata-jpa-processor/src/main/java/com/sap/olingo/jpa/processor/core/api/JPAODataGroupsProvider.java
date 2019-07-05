package com.sap.olingo.jpa.processor.core.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JPAODataGroupsProvider implements JPAODataGroupProvider {

  private final List<String> groups = new ArrayList<>(1);

  @Override
  public List<String> getGroups() {
    return groups;
  }

  /**
   * Adds a single group. Null values are ignored.
   * @param group
   */
  public void addGroup(final String group) {
    if (group != null)
      groups.add(group);
  }

  /**
   * Adds an array of groups
   * @param groups
   */
  public void addGroups(final String... groups) {
    for (final String group : groups)
      addGroup(group);
  }

  public void addGroups(final Collection<String> groups) {
    for (final String group : groups)
      addGroup(group);
  }

  @Override
  public String toString() {
    return "JPAODataGroupsProvider [groups=" + groups + "]";
  }
}
