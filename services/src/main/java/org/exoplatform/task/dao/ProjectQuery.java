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

package org.exoplatform.task.dao;

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.task.dao.condition.Condition;
import org.exoplatform.task.dao.condition.Conditions;

public class ProjectQuery extends Query {
  public ProjectQuery setMembership(List<String> memberships) {
    if (memberships != null) {
      this.add(Conditions.or(Conditions.in(Conditions.MANAGER, memberships),
        Conditions.in(Conditions.PARTICIPATOR, memberships)));
    }
    return this;
  }

  public ProjectQuery setKeyword(String keyword) {
    if (keyword != null && !keyword.trim().isEmpty()) {
      List<Condition> keys = new LinkedList<Condition>(); 
      for (String k : keyword.split(" ")) {
        if (!(k = k.trim()).isEmpty()) {
          k = "%" + k.toLowerCase() + "%";
          keys.add(Conditions.like(Conditions.NAME, k));
        }
      }
      this.add(Conditions.or(keys.toArray(new Condition[keys.size()])));
    }
    return this;
  }

  public ProjectQuery setParent(Long parentId) {
    if (parentId != null && parentId > 0) {
      this.add(Conditions.eq(Conditions.PARENT, parentId));
    } else {
      this.add(Conditions.or(Conditions.eq(Conditions.PARENT, 0L),
        Conditions.isNull(Conditions.PARENT)));
    }
    return this;
  }
}
