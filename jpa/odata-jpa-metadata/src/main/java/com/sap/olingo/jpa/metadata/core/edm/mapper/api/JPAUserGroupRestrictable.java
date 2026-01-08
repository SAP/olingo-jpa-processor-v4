package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAUserGroupRestrictable {
  /**
   * @return The list of user groups a artifact is assigned to. In case the list is empty, the artifact is not assigned
   * to a special group and therefore can be accessed by everyone
   * @throws ODataJPAModelException
   */
  @Nonnull
  public List<String> getUserGroups() throws ODataJPAModelException;

  /**
   *
   * @param assignedUserGroups
   * @return
   * @throws ODataJPAModelException
   */
  public default boolean isAccessibleFor(@Nonnull List<String> assignedUserGroups) throws ODataJPAModelException {
    if (getUserGroups().isEmpty())
      return true;
    for (var userGroup : getUserGroups()) {
      if (assignedUserGroups.contains(userGroup))
        return true;
    }
    return false;

  }
}
