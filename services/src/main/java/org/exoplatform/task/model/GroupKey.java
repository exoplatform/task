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


/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class GroupKey implements Comparable<GroupKey> {
  private final String name;
  private final Object value;
  private final int rank;

  public GroupKey(String name, Object value, int rank) {
    this.name = name;
    this.value = value;
    this.rank = rank;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  public int getRank() {
    return rank;
  }

  @Override
  public int compareTo(GroupKey o) {
    return Integer.compare(this.rank, o.rank);
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o != null && o instanceof GroupKey) {
      GroupKey groupKey = (GroupKey) o;
      return (value == groupKey) || (value != null && value.equals(groupKey.value));
    }

    return false;
  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }
}
