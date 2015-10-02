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

package org.exoplatform.task.dao.condition;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class SingleCondition<T> extends Condition implements Cloneable {

  public static final String EQ = "eq";
  public static final String LT = "lt";
  public static final String GT = "gt";
  public static final String LTE = "lte";
  public static final String GTE = "gte";
  public static final String IS_NULL = "null";
  public static final String NOT_NULL = "not_null";
  public static final String LIKE = "like";
  public static final String IN = "in";
  public static final String IS_TRUE = "is_true";
  public static final String IS_FALSE = "is_false";

  final String type;
  final String field;
  final T value;

  public SingleCondition(String type, String field, T value) {
    this.type = type;
    this.field = field;
    this.value = value;
  }

  public String getType() {
    return type;
  }

  public String getField() {
    return field;
  }

  public T getValue() {
    return value;
  }

  @Override
  public Condition clone() {
    return new SingleCondition<T>(type, field, value);
  }
}
