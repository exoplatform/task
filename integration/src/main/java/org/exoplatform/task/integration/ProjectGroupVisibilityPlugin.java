package org.exoplatform.task.integration;

import org.exoplatform.portal.config.GroupVisibilityPlugin;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

import java.util.Collection;

/**
 * The user can see a group only when:
 * * the given user is the super user
 * * the given user is a platform administrator
 * * the given user is a manager of the group
 */
public class ProjectGroupVisibilityPlugin extends GroupVisibilityPlugin {

  private UserACL userACL;

  public ProjectGroupVisibilityPlugin(UserACL userACL) {
    this.userACL = userACL;
  }

  public boolean hasPermission(Identity userIdentity, Group group) {
    Collection<MembershipEntry> userMemberships = userIdentity.getMemberships();
    return userACL.getSuperUser().equals(userIdentity.getUserId())
        || userMemberships.stream()
                          .anyMatch(userMembership -> userMembership.getGroup().equals(userACL.getAdminGroups())
                              || ((userMembership.getGroup().equals(group.getId())
                                  || userMembership.getGroup().startsWith(group.getId() + "/"))
                                  && (userMembership.getMembershipType().equals("*")
                                      || userMembership.getMembershipType().equals("manager"))));
  }
}
