/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.exoplatform.task.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public final class UserUtils {

  private UserUtils() {
  }

  public static String getDisplayName(User user) {
    if(user == null) {
      return "";
    }
    String displayName = user.getDisplayName();
    if(displayName == null) {
      if (user.getFirstName() != null) {
        displayName = user.getFirstName();
      }
      if (user.getLastName() != null) {
        if (displayName == null) {
          displayName = user.getLastName();
        } else {
          displayName = new StringBuffer(displayName).append(" "+user.getLastName()).toString();
        }
      }
      if(displayName == null) {
        displayName = user.getUserName();
      }
    }
    return displayName;
  }

  public static List<String> getMemberships(Identity identity) {
    Map<String, List<MembershipEntry>> gms = new HashMap<String, List<MembershipEntry>>();
    for (MembershipEntry m : identity.getMemberships()) {
      List<MembershipEntry> ms = gms.get(m.getGroup());
      if (ms == null) {
        ms = new LinkedList<MembershipEntry>();
        gms.put(m.getGroup(), ms);
        //
        ms.add(new MembershipEntry(m.getGroup(), MembershipEntry.ANY_TYPE));
      }
      if (!m.getMembershipType().equals(MembershipEntry.ANY_TYPE)) {
        ms.add(m);
      }
    }

    List<String> memberships = new ArrayList<String>();
    String userName = identity.getUserId();
    memberships.add(userName);
    for (List<MembershipEntry> ms : gms.values()) {
      for (MembershipEntry m : ms) {
        memberships.add(m.toString());
      }
    }
    return memberships;
  }
}
