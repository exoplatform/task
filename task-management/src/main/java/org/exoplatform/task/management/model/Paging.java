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

package org.exoplatform.task.management.model;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class Paging {
  private final int numberItemPerPage;
  private final int currentPage;
  private long total;

  public Paging(int currentPage) {
    this.currentPage = currentPage;
    this.numberItemPerPage = 50;
    this.total = 0;
  }

  public int getNumberItemPerPage() {
    return numberItemPerPage;
  }

  public int getCurrentPage() {
    return currentPage;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public int getStart() {
    return numberItemPerPage * (currentPage - 1);
  }

  public int getNoPages() {
    int no = (int)(total / numberItemPerPage);
    if (total % numberItemPerPage != 0) {
      no++;
    }
    return no;
  }
}
