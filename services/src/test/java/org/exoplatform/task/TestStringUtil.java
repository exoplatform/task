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

package org.exoplatform.task;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.task.util.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class TestStringUtil {
  private static String[] elems = new String[] {"b", "i", "font", "s", "u", "o", "sup", "sub", "ins", "del", "strong",
    "strike", "tt", "code", "big", "small", "span", "em", "p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "cite",
    "blockquote"};
  private static String[] emptyElems = new String[] {"br"};
  private static List<String> tables = Arrays.asList("table", "tr", "td", "th",
      "colgroup", "thead", "tbody", "tfoot");
  private static String[] disallowElem = new String[] {"script", "iframe", "link", "test"};
  
  //There are more, I just try to test some basic
  private static String[] allowAttrs = new String[] {"style=\"color:red\""};
  private static String[] disallowAttrs = new String[] {"onmouseover=\"alert('click')\"", "onmouseout=\"alert('click')\""};
  private static List<String> allAttrs = new LinkedList<String>();
  static {
    allAttrs.addAll(Arrays.asList(allowAttrs));
    allAttrs.addAll(Arrays.asList(disallowAttrs));    
  }
  
  @Test
  public void testCommon() {
    StringBuilder input = new StringBuilder();
    buildTag(input, elems, allAttrs, true);
    buildTag(input, emptyElems, allAttrs, false);
    
    StringBuilder output = new StringBuilder();
    buildTag(output, elems, Arrays.asList(allowAttrs), true);
    buildTag(output, emptyElems, Arrays.asList(allowAttrs), false);
    
    String encoded = StringUtil.encodeInjectedHtmlTag(input.toString());
    Assert.assertEquals(output.toString(), encoded);
  }
  
  @Test
  public void testLinks() {
    StringBuilder input = new StringBuilder();    
    buildTag(input, new String[] {"a"}, Arrays.asList("href=\"abcd\""), true);
    StringBuilder output = new StringBuilder();
    buildTag(output, new String[] {"a"}, Arrays.asList("href=\"abcd\"", "rel=\"nofollow\""), true);
    //
    String encoded = StringUtil.encodeInjectedHtmlTag(input.toString());
    Assert.assertEquals(output.toString(), encoded);
    
    input = new StringBuilder();    
    buildTag(input, new String[] {"a"}, Arrays.asList("style=\"color:red\"", "href=\"javascript:test\""), true);
    output = new StringBuilder();
    buildTag(output, new String[] {"a"}, Arrays.asList("style=\"color:red\""), true);
    //
    encoded = StringUtil.encodeInjectedHtmlTag(input.toString());
    Assert.assertEquals(output.toString(), encoded);
  }
  
  @Test
  public void testImages() {
    StringBuilder input = new StringBuilder();    
    buildTag(input, new String[] {"img"}, Arrays.asList("src=\"abcd\"", "width=\"1\"", "onerror=\"test\""), false);
    StringBuilder output = new StringBuilder();
    buildTag(output, new String[] {"img"}, Arrays.asList("src=\"abcd\"", "width=\"1\""), false);
    //
    String encoded = StringUtil.encodeInjectedHtmlTag(input.toString());
    Assert.assertEquals(output.toString(), encoded);
  }
  
  @Test
  public void testParseAttribute() {
    StringBuilder input = new StringBuilder();    
    buildTag(input, new String[] {"img"}, Arrays.asList("src=\"jav  ascript:alert('XSS');\"", "width=\"1\"", "onerror=\"test\""), false);
    StringBuilder output = new StringBuilder();
    buildTag(output, new String[] {"img"}, Arrays.asList("width=\"1\""), false);
    //
    String encoded = StringUtil.encodeInjectedHtmlTag(input.toString());
    Assert.assertEquals(output.toString(), encoded);
  }
  
  @Test
  public void testEncodedAttribute() {
    StringBuilder input = new StringBuilder();    
    //javascript:alert encoded
    buildTag(input, new String[] {"img"}, Arrays.asList("SRC=&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65&#x72&#x74&#x28&#x27&#x58&#x53&#x53&#x27&#x29", 
                                                        "width=\"1\""), false);
    StringBuilder output = new StringBuilder();
    buildTag(output, new String[] {"img"}, Arrays.asList("width=\"1\""), false);
    //
    String encoded = StringUtil.encodeInjectedHtmlTag(input.toString());
    Assert.assertEquals(output.toString(), encoded);
  }
  
  @Test
  public void testTables() {
    StringBuilder input = new StringBuilder();    
    buildTag(input, tables.toArray(new String[tables.size()]), Arrays.asList("align=\"abcd\"", "onmouseover=\"1\""), true);
    StringBuilder output = new StringBuilder();
    buildTag(output, tables.toArray(new String[tables.size()]), Arrays.asList("align=\"abcd\""), true);
    //
    String encoded = StringUtil.encodeInjectedHtmlTag(input.toString());
    Assert.assertEquals(output.toString(), encoded);
  }
  
  @Test
  public void testDisallowElem() {
    StringBuilder input = new StringBuilder();    
    buildTag(input, disallowElem, Collections.<String>emptyList(), true);
    //
    String encoded = StringUtil.encodeInjectedHtmlTag(input.toString());
    Assert.assertEquals("abcdabcd", encoded);
  }
  
  private void buildTag(StringBuilder builder, String[] elems, List<String> attrs, boolean hasCloseTag) {
    for (String elem : elems) {
      builder.append("<").append(elem).append(" ");
      for (String att : attrs) {
        builder.append(att).append(" ");
      }
      if (attrs.size() > 0) {
        builder.deleteCharAt(builder.length() - 1);        
      }
      if (hasCloseTag) {
        builder.append(">");
        if (!tables.contains(elem)) {
          builder.append("abcd");          
        }
        builder.append("</").append(elem).append(">");
      } else {
        builder.append(" />");
      }
    }    
  }
}