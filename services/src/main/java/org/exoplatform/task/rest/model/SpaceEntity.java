/**
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
  
package org.exoplatform.task.rest.model;

import lombok.Data;

@Data
public class SpaceEntity {
  private String id;
  private String displayName;
  private String groupId;
  private String url;
  private String prettyName;
  private String avatarUrl;
/*  private String editor;
  private String[] managers;*/

  public SpaceEntity() {
  }

  public SpaceEntity(String id, String displayName, String groupId, String url, String prettyName, String avatarUrl) {
    this.id = id;
    this.displayName = displayName;
    this.groupId = groupId;
    this.url = url;
    this.prettyName = prettyName;
    this.avatarUrl = avatarUrl;
  }

}