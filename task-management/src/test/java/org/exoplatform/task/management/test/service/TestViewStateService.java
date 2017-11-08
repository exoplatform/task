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

package org.exoplatform.task.management.test.service;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.task.management.model.ViewState;
import org.exoplatform.task.management.model.ViewType;
import org.exoplatform.task.management.service.ViewStateService;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class TestViewStateService {
  private ViewStateService viewStateService;

  @Before
  public void setUp() throws Exception {
    viewStateService = new ViewStateService(new MockSettingService());
  }

  @Test
  public void testGetDefaultViewType() {
    ViewState viewState = viewStateService.getViewState("list@-1@1");
    ViewType vType = viewState.getViewType();
    assertEquals(vType, ViewType.LIST);
  }

  @Test
  public void testSaveAndGetViewType() {
    ViewState viewState = viewStateService.getViewState("list@-1@1");
    viewState.setViewType(ViewType.BOARD);
    viewStateService.saveViewState(viewState);

    viewState = viewStateService.getViewState("list@-1@1");
    assertEquals(ViewType.BOARD, viewState.getViewType());
  }

  private class MockSettingService implements SettingService {
    private Map<String, SettingValue<?>> values = new HashMap<>();
    @Override
    public void set(Context context, Scope scope, String key, SettingValue<?> value) {
      values.put(key, value);
    }

    @Override
    public void remove(Context context, Scope scope, String key) {
      values.remove(key);
    }

    @Override
    public void remove(Context context, Scope scope) {
      values.clear();
    }

    @Override
    public void remove(Context context) {
      values.clear();
    }

    @Override
    public SettingValue<?> get(Context context, Scope scope, String key) {
      return values.get(key);
    }

    @Override
    public Map<Scope, Map<String, SettingValue<String>>> getSettingsByContext(Context context) {
      return null;
    }

    @Override
    public List<Context> getContextsByTypeAndScopeAndSettingName(String contextType,
                                                                 String scopeType,
                                                                 String scopeName,
                                                                 String settingName,
                                                                 int offset,
                                                                 int limit) {
      return null;
    }

    @Override
    public Set<String> getEmptyContextsByScopeAndContextType(String contextType,
                                                             String scopeType,
                                                             String scopeName,
                                                             int offset,
                                                             int limit) {
      return null;
    }

    @Override
    public void save(Context context) {
    }
  }
}
