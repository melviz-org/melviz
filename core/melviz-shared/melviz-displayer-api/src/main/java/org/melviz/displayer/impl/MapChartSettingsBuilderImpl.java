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

import org.melviz.displayer.DisplayerSettings;
import org.melviz.displayer.DisplayerSubType;
import org.melviz.displayer.DisplayerType;
import org.melviz.displayer.MapChartSettingsBuilder;
import org.melviz.displayer.MapColorScheme;

public class MapChartSettingsBuilderImpl extends AbstractXAxisChartSettingsBuilder<MapChartSettingsBuilderImpl>
                                         implements MapChartSettingsBuilder<MapChartSettingsBuilderImpl> {

    protected DisplayerSettings createDisplayerSettings() {
        return new DisplayerSettings(DisplayerType.MAP, DisplayerSubType.MAP_REGIONS);
    }

    @Override
    public MapChartSettingsBuilderImpl subType_Region_Map() {
        displayerSettings.setSubtype(DisplayerSubType.MAP_REGIONS);
        return this;
    }

    @Override
    public MapChartSettingsBuilderImpl subType_Marker_Map() {
        displayerSettings.setSubtype(DisplayerSubType.MAP_MARKERS);
        return this;
    }

    @Override
    public MapChartSettingsBuilderImpl colorScheme(MapColorScheme colorScheme) {
        displayerSettings.setMapColorScheme(colorScheme);
        return this;
    }
}
