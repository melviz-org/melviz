/*
 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.melviz.displayer.impl;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.melviz.dataset.sort.SortOrder;
import org.melviz.displayer.DisplayerSettings;
import org.melviz.displayer.DisplayerType;
import org.melviz.displayer.TableDisplayerSettingsBuilder;

@Portable
public class TableDisplayerSettingsBuilderImpl extends
                                               AbstractDisplayerSettingsBuilder<TableDisplayerSettingsBuilderImpl>
                                               implements
                                               TableDisplayerSettingsBuilder<TableDisplayerSettingsBuilderImpl> {

    public DisplayerSettings createDisplayerSettings() {
        return new DisplayerSettings(DisplayerType.TABLE);
    }

    public TableDisplayerSettingsBuilderImpl tablePageSize(int pageSize) {
        displayerSettings.setTablePageSize(pageSize);
        return this;
    }

    public TableDisplayerSettingsBuilderImpl tableOrderEnabled(boolean enabled) {
        displayerSettings.setTableSortEnabled(enabled);
        return this;
    }

    public TableDisplayerSettingsBuilderImpl tableOrderDefault(String columnId, SortOrder order) {
        displayerSettings.setTableDefaultSortColumnId(columnId);
        displayerSettings.setTableDefaultSortOrder(order);
        return this;
    }

    public TableDisplayerSettingsBuilderImpl tableOrderDefault(String columnId, String order) {
        return tableOrderDefault(columnId, SortOrder.getByName(order));
    }

    public TableDisplayerSettingsBuilderImpl tableWidth(int tableWidth) {
        displayerSettings.setTableWidth(tableWidth);
        return this;
    }

    public TableDisplayerSettingsBuilderImpl tableColumnPickerEnabled(boolean enabled) {
        displayerSettings.setTableColumnPickerEnabled(enabled);
        return this;
    }

    @Override
    public TableDisplayerSettingsBuilderImpl resizable(boolean resizable) {
        displayerSettings.setResizable(resizable);
        return this;
    }
}
