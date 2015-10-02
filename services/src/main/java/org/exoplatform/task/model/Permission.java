/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.task.model;

import org.exoplatform.services.organization.*;
import org.exoplatform.task.util.UserUtil;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class Permission {
  public static final int USER = 1;
  public static final int GROUP = 2;

  private int type;
  private String id;
  private String displayName;

  private String membershipType;
  private String groupId;
  private String groupName;

  public Permission(int type, String id, String displayName) {
    this.id = id;
    this.displayName = displayName;
    this.type = type;
  }

  public static Permission parse(String permission, OrganizationService orgService) {
    if (permission == null || permission.isEmpty()) {
      return null;
    }
    int index = permission.indexOf(':');
    if (index == -1) {
      String displayName = permission;
      try {
        org.exoplatform.services.organization.User user = orgService.getUserHandler().findUserByName(permission);
        displayName = UserUtil.getDisplayName(user);
      } catch (Exception ex) {//NOSONAR

      }
      return new Permission(Permission.USER, permission, displayName);

    } else {
      Permission perm = new Permission(Permission.GROUP, permission, permission);
      String membershipType = permission.substring(0, index);
      String groupId = permission.substring(index + 1);

      perm.setMembershipType(membershipType);
      perm.setGroupId(groupId);

      try {
        Group g = orgService.getGroupHandler().findGroupById(groupId);
        perm.setGroupName(g.getLabel());

        String displayName = new StringBuilder("*".equals(membershipType) ? "Any" : membershipType)
                .append(" in ").append(g.getLabel()).toString();
        perm.setDisplayName(displayName);
      } catch (Exception ex) {

      }

      return perm;
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getMembershipType() {
    return membershipType;
  }

  public void setMembershipType(String membershipType) {
    this.membershipType = membershipType;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }
}
