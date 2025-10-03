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
package org.melviz.dataset.def;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.melviz.dataprovider.DataSetProviderType;
import org.melviz.dataset.ColumnType;
import org.melviz.dataset.date.TimeAmount;
import org.melviz.dataset.filter.DataSetFilter;

import static org.melviz.dataset.json.DataSetDefJSONMarshaller.isBlank;

/**
 * This class is used to define the origin, structure and runtime behaviour of a data set instance.
 */
public class DataSetDef {

    protected String UUID;

    protected String name;

    protected DataSetProviderType provider;

    // Cannot @Valid due to this GWT issue https://github.com/gwtproject/gwt/issues/8816.
    // Columns validation must be performed explicitly when validating a datasetdef or any of its sub-classes.
    protected List<DataColumnDef> columns = new ArrayList<DataColumnDef>();

    protected DataSetFilter dataSetFilter = null;
    protected boolean isPublic = true;
    protected boolean pushEnabled = false;
    protected Integer pushMaxSize = 1024;
    protected boolean cacheEnabled = false;
    protected Integer cacheMaxRows = 1000;
    protected String refreshTime = null;
    protected boolean refreshAlways = false;
    protected boolean allColumnsEnabled = true;

    protected Map<String, String> patternMap = new HashMap<>();
    protected Map<String, String> propertyMap = new HashMap<>();

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataSetFilter getDataSetFilter() {
        return dataSetFilter;
    }

    public void setDataSetFilter(DataSetFilter dataSetFilter) {
        this.dataSetFilter = dataSetFilter;
        if (dataSetFilter != null)
            this.dataSetFilter.setDataSetUUID(UUID);
    }

    public DataSetProviderType getProvider() {
        return provider;
    }

    public void setProvider(DataSetProviderType provider) {
        this.provider = provider;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public Integer getPushMaxSize() {
        return pushMaxSize;
    }

    public void setPushMaxSize(Integer pushMaxSize) {
        this.pushMaxSize = pushMaxSize;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public Integer getCacheMaxRows() {
        return cacheMaxRows;
    }

    public void setCacheMaxRows(Integer cacheMaxRows) {
        this.cacheMaxRows = cacheMaxRows;
    }

    public String getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(String refreshTime) {
        this.refreshTime = refreshTime;
    }

    public TimeAmount getRefreshTimeAmount() {
        if (refreshTime != null && refreshTime.trim().length() > 0) {
            return TimeAmount.parse(refreshTime);
        }
        return null;
    }

    public boolean isRefreshAlways() {
        return refreshAlways;
    }

    public void setRefreshAlways(boolean refreshAlways) {
        this.refreshAlways = refreshAlways;
    }

    public String getPattern(String columnId) {
        return patternMap.get(columnId);
    }

    public void setPattern(String columnId, String pattern) {
        patternMap.put(columnId, pattern);
    }

    public boolean isAllColumnsEnabled() {
        return allColumnsEnabled;
    }

    public void setAllColumnsEnabled(boolean allColumnsEnabled) {
        this.allColumnsEnabled = allColumnsEnabled;
    }

    public List<DataColumnDef> getColumns() {
        return columns;
    }

    public void setColumns(List<DataColumnDef> columns) {
        this.columns = columns;
    }

    public DataColumnDef getColumnById(final String id) {
        if (id != null && columns != null && !columns.isEmpty()) {
            for (final DataColumnDef columnDef : columns) {
                if (columnDef.getId().equalsIgnoreCase(id)) {
                    return columnDef;
                }
            }
        }
        return null;
    }

    public boolean addColumn(final String id, final ColumnType type) {
        if (columns == null) {
            columns = new LinkedList<DataColumnDef>();
        }
        return columns.add(new DataColumnDef(id, type));
    }

    public Set<String> getPropertyNames() {
        return propertyMap.keySet();
    }

    public String getProperty(String key) {
        return propertyMap.get(key);
    }

    public void setProperty(String key, String value) {
        propertyMap.put(key, value);
    }

    public void validate() {
        if (isBlank(UUID)) {
            throw new IllegalArgumentException("UUID is a required field in data sets");
        }
    }

    public DataSetDef clone() {
        DataSetDef def = new DataSetDef();
        clone(def);
        return def;
    }

    protected void clone(final DataSetDef def) {
        def.setUUID(getUUID());
        def.setName(getName());
        def.setProvider(getProvider());
        def.setPublic(isPublic());
        final DataSetFilter currentFilter = getDataSetFilter();
        if (currentFilter != null) {
            final DataSetFilter nFilter = currentFilter.cloneInstance();
            nFilter.setDataSetUUID(getUUID());
            def.setDataSetFilter(nFilter);
        }
        def.setDataSetFilter(getDataSetFilter());
        def.setCacheEnabled(isCacheEnabled());
        def.setCacheMaxRows(getCacheMaxRows());
        def.setPushEnabled(isPushEnabled());
        def.setPushMaxSize(getPushMaxSize());
        def.setRefreshAlways(isRefreshAlways());
        def.setRefreshTime(getRefreshTime());
        def.setAllColumnsEnabled(isAllColumnsEnabled());

        final List<DataColumnDef> columns = getColumns();
        if (columns != null && !columns.isEmpty()) {
            final List<DataColumnDef> c = new LinkedList<DataColumnDef>();
            for (final DataColumnDef columnDef : columns) {
                final DataColumnDef _c = columnDef.clone();
                c.add(_c);
            }
            def.setColumns(c);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getUUID() == null) {
            return false;
        }
        try {
            DataSetDef other = (DataSetDef) obj;
            if (UUID != null && !UUID.equals(other.UUID)) {
                return false;
            }
            if (provider != null && !provider.equals(other.provider)) {
                return false;
            }
            if (name != null && !name.equals(other.name)) {
                return false;
            }
            if (isPublic != other.isPublic) {
                return false;
            }
            if (allColumnsEnabled != other.allColumnsEnabled) {
                return false;
            }
            if (pushEnabled != other.pushEnabled) {
                return false;
            }
            if (pushMaxSize != null && !pushMaxSize.equals(other.pushMaxSize)) {
                return false;
            }
            if (cacheEnabled != other.cacheEnabled) {
                return false;
            }
            if (cacheMaxRows != null && !cacheMaxRows.equals(other.cacheMaxRows)) {
                return false;
            }
            if (columns.size() != other.columns.size()) {
                return false;
            }
            if (refreshTime != null && !refreshTime.equals(other.refreshTime)) {
                return false;
            }
            if (refreshAlways != other.refreshAlways) {
                return false;
            }
            if (dataSetFilter != null && !dataSetFilter.equals(other.dataSetFilter)) {
                return false;
            }
            for (int i = 0; i < columns.size(); i++) {
                DataColumnDef el = columns.get(i);
                DataColumnDef otherEl = other.columns.get(i);
                if (!el.equals(otherEl)) {
                    return false;
                }
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(UUID,
                name,
                provider,
                columns,
                dataSetFilter,
                isPublic,
                pushEnabled,
                pushMaxSize,
                cacheEnabled,
                cacheMaxRows,
                refreshTime,
                refreshAlways,
                allColumnsEnabled,
                patternMap,
                propertyMap);
    }
}
