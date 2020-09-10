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
import org.exoplatform.task.dto.CommentDto;
import org.exoplatform.task.model.User;

import java.util.List;

@Data
public class CommentEntity {
  private final CommentDto comment;
  private final User author;
  private final String formattedComment;
  private List<CommentEntity> subComments;


  public CommentEntity(CommentDto cmt, User author, String formattedComment) {
    this.comment = cmt;
    this.author = author;
    this.formattedComment = formattedComment;
  }
}
