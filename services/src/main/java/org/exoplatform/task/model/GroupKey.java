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
public class GroupKey<T> implements Comparable<GroupKey> {
  private final String name;
  private final T value;
  private final int rank;

  public GroupKey(String name, T value, int rank) {
    this.name = name;
    this.value = value;
    this.rank = rank;
  }

  public String getName() {
    return name;
  }

  public T getValue() {
    return value;
  }

  public int getRank() {
    return rank;
  }

  @Override
  public int compareTo(GroupKey o) {
    int compare = Integer.compare(this.rank, o.rank);
    if (compare == 0) {
      if (value != null && value instanceof Comparable) {
        if (o.value == null) {
          compare = 1;
        } else if (o.value.getClass() == value.getClass()){
          compare = ((Comparable)value).compareTo(o.value);
        }
      } else if (value == null && o.value != null) {
        compare = -1;
      }
    }
    return compare;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GroupKey groupKey = (GroupKey) o;

    if (name != null ? !name.equals(groupKey.name) : groupKey.name != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }
}
