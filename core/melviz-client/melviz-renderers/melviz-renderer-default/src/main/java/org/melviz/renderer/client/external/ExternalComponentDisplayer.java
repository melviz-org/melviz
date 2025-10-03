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
package org.melviz.renderer.client.external;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import elemental2.dom.DomGlobal;
import org.melviz.common.client.StringUtils;
import org.melviz.dataset.DataColumn;
import org.melviz.dataset.DataSet;
import org.melviz.dataset.DataSetLookupConstraints;
import org.melviz.displayer.ColumnSettings;
import org.melviz.displayer.DisplayerAttributeDef;
import org.melviz.displayer.DisplayerAttributeGroupDef;
import org.melviz.displayer.DisplayerConstraints;
import org.melviz.displayer.client.AbstractDisplayer;
import org.melviz.displayer.client.widgets.ExternalComponentPresenter;
import org.melviz.displayer.external.ExternalColumn;
import org.melviz.displayer.external.ExternalColumnSettings;
import org.melviz.displayer.external.ExternalComponentMessage;
import org.melviz.displayer.external.ExternalComponentMessageHelper;
import org.melviz.displayer.external.ExternalDataSet;
import org.melviz.displayer.external.ExternalFilterRequest;

@Dependent
public class ExternalComponentDisplayer extends AbstractDisplayer<ExternalComponentDisplayer.View> {

    private static final String DEFAULT_WIDTH = "100%";

    public interface View extends AbstractDisplayer.View<ExternalComponentDisplayer> {

        void setSize(int chartWidth, int chartHeight);

        void setMargin(int chartMarginTop, int chartMarginRight, int chartMarginBottom, int chartMarginLeft);

        void setHeight(String height);

        void setWidth(String width);

    }

    @Inject
    View view;

    @Inject
    ExternalComponentPresenter externalComponentPresenter;

    @Inject
    ExternalComponentMessageHelper messageHelper;

    private String componentId;

    @Override
    public View getView() {
        return view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        externalComponentPresenter.setFilterConsumer(this::receiveFilterRequest);
    }

    @Override
    public DisplayerConstraints createDisplayerConstraints() {
        DataSetLookupConstraints lookupConstraints = new DataSetLookupConstraints().setGroupAllowed(true)
                .setGroupRequired(false)
                .setExtraColumnsAllowed(true)
                .setGroupsTitle("Groups")
                .setColumnsTitle("Columns");

        return new DisplayerConstraints(lookupConstraints).supportsAttribute(DisplayerAttributeDef.TYPE)
                .supportsAttribute(DisplayerAttributeDef.EXTERNAL_COMPONENT_ID_DEPRECATED)
                .supportsAttribute(DisplayerAttributeGroupDef.COLUMNS_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.FILTER_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.REFRESH_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.EXTERNAL_COMPONENT_GROUP)
                .supportsAttribute(DisplayerAttributeGroupDef.EXTERNAL_COMPONENT_WIDTH)
                .supportsAttribute(DisplayerAttributeGroupDef.EXTERNAL_COMPONENT_HEIGHT)
                .supportsAttribute(DisplayerAttributeGroupDef.CHART_MARGIN_GROUP);
    }

    @Override
    protected void createVisualization() {
        ExternalComponentMessage init = initMessage();
        externalComponentPresenter.sendMessage(init);
        updateVisualization();
    }

    @Override
    protected void updateVisualization() {
        var currentComponentId = displayerSettings.getComponentId();
        if (currentComponentId != null && !currentComponentId.equals(componentId)) {
            componentId = currentComponentId;
            var partitionId = displayerSettings.getComponentPartition();
            var baseUrl = displayerSettings.getComponentBaseUrl();
            externalComponentPresenter.withComponentBaseUrlIdAndPartition(baseUrl, componentId, partitionId);
        }

        var message = dataSetMessage();
        externalComponentPresenter.sendMessage(message);

        // legacy
        view.setSize(displayerSettings.getChartWidth(), displayerSettings.getChartHeight());

        view.setMargin(displayerSettings.getChartMarginTop(),
                displayerSettings.getChartMarginRight(),
                displayerSettings.getChartMarginBottom(),
                displayerSettings.getChartMarginLeft());

        var width = displayerSettings.getComponentWidth();
        var finalWidth = width != null && !width.trim().isEmpty() ? width : DEFAULT_WIDTH;
        view.setWidth(finalWidth);
        var height = displayerSettings.getComponentHeight();
        if (height != null && !height.trim().isEmpty()) {
            view.setHeight(height);
        }
    }

    private ExternalComponentMessage dataSetMessage() {
        Map<String, Object> componentProperties = new HashMap<>(displayerSettings.getComponentProperties());
        var ds = ExternalDataSet.of(buildColumns(),
                buildData(dataSet));
        return messageHelper.newDataSetMessage(ds, componentProperties);
    }

    private ExternalComponentMessage initMessage() {
        Map<String, Object> componentProperties = new HashMap<>(displayerSettings.getComponentProperties());
        var message = messageHelper.newInitMessage(componentProperties);
        var mode = displayerSettings.getMode();
        message.setProperty(DisplayerAttributeDef.MODE.getId(), mode.name());
        return message;
    }

    private ExternalColumn[] buildColumns() {
        return dataSet.getColumns()
                .stream()
                .map(this::buildExternalColumn)
                .toArray(ExternalColumn[]::new);
    }

    public ExternalComponentPresenter getExternalComponentPresenter() {
        return externalComponentPresenter;
    }

    public String[][] buildData(DataSet ds) {
        var columns = ds.getColumns();
        var result = new String[0][0];
        int cols = columns.size();
        if (cols > 0) {
            int rows = columns.get(0).getValues().size();
            result = new String[rows][];
            for (int i = 0; i < rows; i++) {
                var line = new String[cols];
                for (int j = 0; j < cols; j++) {
                    line[j] = getEvaluatedValue(ds, i, j);
                }
                result[i] = line;
            }
        }
        return result;
    }

    @Override
    protected void afterClose() {
        super.afterClose();
        externalComponentPresenter.destroy();
    }

    protected String columnValueToString(Object mightBeNull) {
        return mightBeNull == null ? "" : mightBeNull.toString();
    }

    protected ExternalColumn buildExternalColumn(DataColumn cl) {
        ColumnSettings clSettings = displayerSettings.getColumnSettings(cl);
        ExternalColumnSettings settings = ExternalColumnSettings.of(clSettings.getColumnId(),
                clSettings.getColumnName(),
                clSettings.getValueExpression(),
                clSettings.getEmptyTemplate(),
                clSettings.getValuePattern());
        return ExternalColumn.of(cl.getId(),
                cl.getColumnType().name(),
                settings);

    }

    protected void onFilterLabelRemoved(String columnId, int row) {
        super.filterUpdate(columnId, row);
        if (!displayerSettings.isFilterSelfApplyEnabled()) {
            updateVisualization();
        }
    }

    protected void onFilterClearAll() {
        super.filterReset();
        if (!displayerSettings.isFilterSelfApplyEnabled()) {
            updateVisualization();
        }
    }

    private void receiveFilterRequest(ExternalFilterRequest filterRequest) {
        if (displayerSettings.isFilterEnabled()) {
            if (filterRequest.isReset()) {
                super.filterReset();
            } else {
                DataColumn column = dataSet.getColumnByIndex(filterRequest.getColumn());
                super.filterUpdate(column.getId(), filterRequest.getRow());
            }
        }
    }

    private String getEvaluatedValue(DataSet ds, int i, int j) {
        String value = columnValueToString(ds.getValueAt(i, j));
        try {
            String columnId = ds.getColumnByIndex(j).getId();
            ColumnSettings settings = displayerSettings.getColumnSettings(columnId);
            if (settings != null) {
                String expression = settings.getValueExpression();
                if (!StringUtils.isBlank(expression)) {
                    return getEvaluator().evalExpression(value, expression);
                }
            }
        } catch (Exception e) {
            DomGlobal.console.debug("Error evaluating value at " + i + "," + j);
            DomGlobal.console.debug(e);
        }
        return value;
    }

}
