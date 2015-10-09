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

package org.exoplatform.task.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.ProjectQuery;
import org.exoplatform.task.dao.Query;
import org.exoplatform.task.dao.condition.AggregateCondition;
import org.exoplatform.task.dao.condition.Condition;
import org.exoplatform.task.dao.condition.Conditions;
import org.exoplatform.task.dao.condition.SingleCondition;
import org.exoplatform.task.domain.Project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class CommonJPADAO<E, K extends Serializable> extends GenericDAOJPAImpl<E, K> {
  protected <E> List<E> cloneEntities(List<E> list) {
    if (list == null || list.isEmpty()) return list;

    List<E> result = new ArrayList<E>(list.size());
    for (E e : list) {
      E cloned = cloneEntity(e);
      result.add(cloned);
    }
    return result;
  }

  protected <E> E cloneEntity(E e) {
    return DAOHandlerJPAImpl.clone(e);
  }

  @Override
  public E find(K id) {
    return cloneEntity(super.find(id));
  }

  @Override
  public E create(E entity) {
    return cloneEntity(super.create(entity));
  }

  protected ListAccess<E> findEntities(Query query, Class<E> clazz) {
    EntityManager em = getEntityManager();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery q = cb.createQuery();
    q.distinct(true);
    Root<E> root = q.from(clazz);

    Predicate predicate = buildQuery(query.getCondition(), root, cb, q);
    if (predicate != null) {
      q.where(predicate);
    }

    //
    q.select(cb.count(root));
    final TypedQuery<Long> countQuery = em.createQuery(q);

    //
    q.select(root);

    List<OrderBy> orderby = query.getOrderBy();
    if(orderby != null && !orderby.isEmpty()) {
      Order[] orders = new Order[orderby.size()];
      for(int i = 0; i < orders.length; i++) {
        OrderBy orderBy = orderby.get(i);
        Path p = root.get(orderBy.getFieldName());
        orders[i] = orderBy.isAscending() ? cb.asc(p) : cb.desc(p);
      }
      q.orderBy(orders);
    }

    TypedQuery<E> selectQuery = em.createQuery(q);
    return new JPAQueryListAccess<E>(clazz, countQuery, selectQuery);
  }
  
  protected Predicate buildQuery(Condition condition, Root<E> root, CriteriaBuilder cb, CriteriaQuery query) {
    if (condition == null) {
      return null;
    }
    if (condition instanceof SingleCondition) {
      return buildSingleCondition((SingleCondition)condition, root, cb, query);
    } else if (condition instanceof AggregateCondition) {
      AggregateCondition agg = (AggregateCondition)condition;
      String type = agg.getType();
      List<Condition> cds = agg.getConditions();
      Predicate[] ps = new Predicate[cds.size()];
      for (int i = 0; i < ps.length; i++) {
        ps[i] = buildQuery(cds.get(i), root, cb, query);
      }

      if (ps.length == 1) {
        return ps[0];
      }

      if (AggregateCondition.AND.equals(type)) {
        return cb.and(ps);
      } else if (AggregateCondition.OR.equals(type)) {
        return cb.or(ps);
      }
    }
    return null;
  }

  protected <T> Predicate buildSingleCondition(SingleCondition<T> condition, Root<E> root, CriteriaBuilder cb, CriteriaQuery query) {
    String type = condition.getType();
    T value = condition.getValue();

    Path path = buildPath(condition, root);

    if (SingleCondition.EQ.equals(condition.getType())) {
      return cb.equal(path, value);
    } else if (SingleCondition.LT.equals(condition.getType())) {
      return cb.lessThan((Path<Comparable>) path, (Comparable) value);
    } else if (SingleCondition.GT.equals(condition.getType())) {
      return cb.greaterThan((Path<Comparable>) path, (Comparable) value);
    } else if (SingleCondition.LTE.equals(condition.getType())) {
      return cb.lessThanOrEqualTo((Path<Comparable>)path, (Comparable)value);
    } else if (SingleCondition.GTE.equals(condition.getType())) {
      return cb.greaterThanOrEqualTo((Path<Comparable>)path, (Comparable) value);
    } else if (SingleCondition.IS_NULL.equals(type)) {
      return path.isNull();
    } else if (SingleCondition.NOT_NULL.equals(type)) {
      return path.isNotNull();
    } else if (SingleCondition.IS_EMPTY.equals(type)) {
        return cb.isEmpty(path);
    } else if (SingleCondition.LIKE.equals(type)) {
      return cb.like(cb.lower(path), String.valueOf(value));
    } else if (SingleCondition.IN.equals(type)) {
      return path.in((Collection) value);
    } else if (SingleCondition.IS_TRUE.equals(type)) {
      return cb.isTrue(path);
    } else if (SingleCondition.IS_FALSE.equals(type)) {
      return cb.isFalse(path);
    }

    throw new RuntimeException("Condition type " + type + " is not supported");
  }

  protected Path buildPath(SingleCondition condition, Root<E> root) {
    String field = condition.getField();
    
    Join join = null;
    if (field.indexOf('.') > 0) {
      String[] arr = field.split("\\.");
      for (int i = 0; i < arr.length - 1; i++) {
        String s = arr[i];
        if (join == null) {
          join = root.join(s, JoinType.INNER);
        } else {
          join = join.join(s, JoinType.INNER);
        }
      }
      field = arr[arr.length - 1];
    }    
    
    return join == null ? root.get(field) : join.get(field);
  }
}
