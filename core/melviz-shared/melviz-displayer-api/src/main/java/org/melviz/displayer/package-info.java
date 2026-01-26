/*
 *
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

/**
 * Displayer API for configuring and rendering data visualizations.
 * <p>
 * This package provides the core API for defining visualization settings across multiple
 * chart types and rendering libraries. It uses the Builder pattern extensively for fluent,
 * type-safe configuration of complex visualization options.
 * </p>
 *
 * <h2>Core Components</h2>
 * <ul>
 * <li>{@link org.melviz.displayer.DisplayerSettings} - Base configuration for all displayers,
 * including dataset lookup, title, and common settings</li>
 * <li>{@link org.melviz.displayer.DisplayerSettingsBuilder} - Fluent API for building displayer
 * configurations</li>
 * <li>{@link org.melviz.displayer.DisplayerSettingsFactory} - Factory for creating displayer
 * settings instances</li>
 * <li>{@link org.melviz.displayer.DisplayerType} - Enumeration of supported visualization types</li>
 * <li>{@link org.melviz.displayer.DisplayerSubType} - Specific chart variants within each type</li>
 * </ul>
 *
 * <h2>Supported Visualizations</h2>
 * <p>
 * Built-in displayer types via {@link org.melviz.displayer.DisplayerType}:
 * </p>
 * <ul>
 * <li><b>Charts</b>: BAR, LINE, AREA, PIE, BUBBLE, METER</li>
 * <li><b>Maps</b>: MAP (geographic visualizations)</li>
 * <li><b>Tables</b>: TABLE (data grids with sorting/pagination)</li>
 * <li><b>Metrics</b>: METRIC (single KPI values)</li>
 * <li><b>Selectors</b>: SELECTOR (dropdowns, sliders for filtering)</li>
 * <li><b>External</b>: EXTERNAL_COMPONENT (custom React/microfrontend components)</li>
 * </ul>
 *
 * <h2>Builder Pattern Usage</h2>
 * <pre>{@code
 * // Bar chart with custom colors and legend
 * DisplayerSettings barChart = DisplayerSettingsFactory.newBarChartSettings()
 *     .title("Sales by Region")
 *     .titleVisible(true)
 *     .dataset("sales-data")
 *     .group("region")
 *     .column("region", "Region")
 *     .column("revenue", AggregateFunctionType.SUM, "Total Revenue")
 *     .width(800).height(400)
 *     .margins(10, 50, 50, 100)
 *     .legendOn(Position.RIGHT)
 *     .filterOn(true, true, true)
 *     .buildSettings();
 *
 * // External component (React microfrontend)
 * DisplayerSettings externalComponent = DisplayerSettingsFactory.newExternalDisplayerSettings()
 *     .component("echarts-line")
 *     .dataset("time-series")
 *     .buildSettings();
 * }</pre>
 *
 * <h2>Specialized Builders</h2>
 * <p>
 * Type-specific builders for advanced configuration:
 * </p>
 * <ul>
 * <li>{@link org.melviz.displayer.BarChartSettingsBuilder} - Bar/column charts with orientation,
 * stacking, and grouping options</li>
 * <li>{@link org.melviz.displayer.LineChartSettingsBuilder} - Line charts with curve styles and
 * data points</li>
 * <li>{@link org.melviz.displayer.PieChartSettingsBuilder} - Pie/donut charts with slice
 * configuration</li>
 * <li>{@link org.melviz.displayer.MapChartSettingsBuilder} - Geographic maps with color schemes</li>
 * <li>{@link org.melviz.displayer.TableDisplayerSettingsBuilder} - Data tables with sorting and
 * pagination</li>
 * <li>{@link org.melviz.displayer.ExternalDisplayerSettingsBuilder} - External React components</li>
 * </ul>
 *
 * <h2>Column Settings</h2>
 * <p>
 * {@link org.melviz.displayer.ColumnSettings} provides per-column configuration:
 * </p>
 * <ul>
 * <li>Display name and formatting patterns</li>
 * <li>Value expressions (JSONata transformations)</li>
 * <li>Empty value handling</li>
 * <li>Column-specific display options</li>
 * </ul>
 *
 * <h2>External Component Integration</h2>
 * <p>
 * The {@link org.melviz.displayer.external} sub-package provides the bridge to React-based
 * microfrontend components, enabling custom visualizations beyond built-in types.
 * </p>
 *
 * @see org.melviz.displayer.external Package for external component messaging
 * @see org.melviz.dataset Package for dataset operations
 * @since 1.0
 */
package org.melviz.displayer;
