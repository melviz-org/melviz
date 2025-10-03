/*
 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.melviz.renderer.echarts.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.melviz.displayer.DisplayerSettings;
import org.melviz.displayer.DisplayerSubType;
import org.melviz.displayer.DisplayerType;
import org.melviz.displayer.client.AbstractRendererLibrary;
import org.melviz.displayer.client.Displayer;
import org.melviz.renderer.echarts.client.exports.ResourcesInjector;

import static org.melviz.displayer.DisplayerSubType.AREA;
import static org.melviz.displayer.DisplayerSubType.AREA_STACKED;
import static org.melviz.displayer.DisplayerSubType.BAR;
import static org.melviz.displayer.DisplayerSubType.BAR_STACKED;
import static org.melviz.displayer.DisplayerSubType.COLUMN;
import static org.melviz.displayer.DisplayerSubType.COLUMN_STACKED;
import static org.melviz.displayer.DisplayerSubType.DONUT;
import static org.melviz.displayer.DisplayerSubType.LINE;
import static org.melviz.displayer.DisplayerSubType.PIE;
import static org.melviz.displayer.DisplayerSubType.SMOOTH;
import static org.melviz.displayer.DisplayerType.AREACHART;
import static org.melviz.displayer.DisplayerType.BARCHART;
import static org.melviz.displayer.DisplayerType.BUBBLECHART;
import static org.melviz.displayer.DisplayerType.LINECHART;
import static org.melviz.displayer.DisplayerType.METERCHART;
import static org.melviz.displayer.DisplayerType.PIECHART;
import static org.melviz.displayer.DisplayerType.SCATTERCHART;
import static org.melviz.displayer.DisplayerType.TIMESERIES;

@ApplicationScoped
public class EChartsRenderer extends AbstractRendererLibrary {

    private static final DisplayerType DEFAULT_CHART = BARCHART;

    public static final String UUID = "echarts";

    private static final List<DisplayerType> SUPPORTED_TYPES = Arrays.asList(LINECHART,
            BARCHART,
            PIECHART,
            AREACHART,
            BUBBLECHART,
            METERCHART,
            SCATTERCHART,
            TIMESERIES);

    @PostConstruct
    public void prepare() {
        ResourcesInjector.ensureEChartsInjected();
    }

    @Override
    public String getUUID() {
        return UUID;
    }

    @Override
    public String getName() {
        return "ECharts";
    }

    @Override
    public List<DisplayerSubType> getSupportedSubtypes(DisplayerType type) {
        var displayerType = type == null ? DEFAULT_CHART : type;
        switch (displayerType) {
            case LINECHART:
                return Arrays.asList(LINE, SMOOTH);
            case BARCHART:
                return Arrays.asList(BAR, BAR_STACKED, COLUMN, COLUMN_STACKED);
            case PIECHART:
                return Arrays.asList(PIE, DONUT);
            case AREACHART:
                return Arrays.asList(AREA, AREA_STACKED);
            default:
                return Collections.emptyList();
        }
    }

    public Displayer lookupDisplayer(DisplayerSettings displayerSettings) {
        var displayerType = displayerSettings.getType() == null ? DEFAULT_CHART : displayerSettings.getType();
        switch (displayerType) {
            case LINECHART:
            case BARCHART:
            case AREACHART:
            case SCATTERCHART:
                return buildAndManageInstance(EChartsXYChartDisplayer.class);
            case TIMESERIES:
                return buildAndManageInstance(EChartsTimeseriesDisplayer.class);
            case BUBBLECHART:
                return buildAndManageInstance(EChartsBubbleChartDisplayer.class);
            case PIECHART:
                return buildAndManageInstance(EChartsPieChartDisplayer.class);
            case METERCHART:
                return buildAndManageInstance(EChartsMeterChartDisplayer.class);
            case MAP:
            default:
                throw new IllegalArgumentException("Type not supported by ECharts, use C3 renderer instead");
        }
    }

    @Override
    public List<DisplayerType> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public boolean isDefault(DisplayerType type) {
        return SUPPORTED_TYPES.contains(type);
    }

}
