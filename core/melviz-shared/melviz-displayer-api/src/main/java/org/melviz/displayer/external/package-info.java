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
 * External component integration for React-based microfrontend visualizations.
 * <p>
 * This package provides the messaging protocol and data structures for integrating
 * custom React components as Melviz displayers. It enables the microfrontend architecture
 * where visualization components run independently and communicate with the GWT core
 * via a message bus.
 * </p>
 *
 * <h2>Architecture Overview</h2>
 * <p>
 * External components follow a message-based lifecycle:
 * </p>
 * <ol>
 * <li><b>Initialization</b>: Core sends INIT message with configuration parameters</li>
 * <li><b>Data Delivery</b>: Core sends DATA_SET message with transformed data</li>
 * <li><b>Filter Requests</b>: Component sends FILTER message to apply user interactions</li>
 * <li><b>Function Calls</b>: Component can invoke backend functions via FUNCTION_CALL messages</li>
 * </ol>
 *
 * <h2>Key Components</h2>
 * <ul>
 * <li>{@link org.melviz.displayer.external.ExternalComponentMessage} - Base message structure
 * for all communication between core and components</li>
 * <li>{@link org.melviz.displayer.external.ExternalComponentMessageType} - Message type
 * enumeration (INIT, DATA_SET, FILTER, FUNCTION_CALL, CONFIGURATION)</li>
 * <li>{@link org.melviz.displayer.external.ExternalComponentMessageHelper} - Utility for
 * creating and parsing messages</li>
 * <li>{@link org.melviz.displayer.external.ExternalDataSet} - Serializable dataset format
 * for component consumption</li>
 * <li>{@link org.melviz.displayer.external.ExternalFilterRequest} - Filter request from
 * component to core</li>
 * </ul>
 *
 * <h2>Message Types</h2>
 * <p>
 * Core to Component messages:
 * </p>
 * <ul>
 * <li><b>INIT</b>: Initialize component with configuration (parameters, settings)</li>
 * <li><b>DATA_SET</b>: Deliver filtered/grouped/sorted dataset for visualization</li>
 * <li><b>CONFIGURATION</b>: Update component configuration without full re-init</li>
 * </ul>
 * <p>
 * Component to Core messages:
 * </p>
 * <ul>
 * <li><b>FILTER</b>: Request core to apply filter based on user interaction (e.g., chart drill-down)</li>
 * <li><b>FUNCTION_CALL</b>: Invoke backend function (e.g., data export, external API call)</li>
 * </ul>
 *
 * <h2>Data Format</h2>
 * <p>
 * {@link org.melviz.displayer.external.ExternalDataSet} provides a simplified dataset structure
 * optimized for JSON serialization:
 * </p>
 * <ul>
 * <li><b>Columns</b>: Array of {@link org.melviz.displayer.external.ExternalColumn} with ID,
 * name, type, and settings</li>
 * <li><b>Data</b>: 2D array of values (rows x columns)</li>
 * <li><b>Metadata</b>: Dataset size, column count, and other metadata</li>
 * </ul>
 *
 * <h2>Component Implementation Pattern</h2>
 * <pre>{@code
 * // TypeScript component using @melviz/component-api
 * import { ComponentApi, ComponentController } from '@melviz/component-api';
 *
 * const api = new ComponentApi();
 * const controller = api.getComponentController();
 *
 * // Register data handler
 * controller.setOnDataSet((dataset, params) => {
 *   // Render visualization with dataset
 *   renderChart(dataset.data, dataset.columns);
 * });
 *
 * // Register init handler
 * controller.setOnInit((params) => {
 *   // Initialize with configuration
 *   initializeChart(params);
 * });
 *
 * // Signal ready to receive messages
 * controller.ready();
 *
 * // Send filter on user interaction
 * chart.on('click', (event) => {
 *   controller.filter({
 *     columnId: event.columnId,
 *     values: [event.value]
 *   });
 * });
 * }</pre>
 *
 * <h2>Filter Requests</h2>
 * <p>
 * {@link org.melviz.displayer.external.ExternalFilterRequest} enables components to send
 * filter requests back to core when users interact with the visualization (e.g., clicking
 * a bar in a bar chart to filter other components).
 * </p>
 *
 * <h2>Function Calls</h2>
 * <p>
 * {@link org.melviz.displayer.external.ExternalComponentFunction} allows components to
 * invoke backend functions for operations like:
 * </p>
 * <ul>
 * <li>Data export (CSV, Excel)</li>
 * <li>External API calls</li>
 * <li>Custom backend processing</li>
 * </ul>
 *
 * <h2>Type Safety</h2>
 * <p>
 * The TypeScript Component API ({@code @melviz/component-api} package) provides
 * type-safe interfaces matching these Java definitions, ensuring contract compliance
 * across the Java/GWT core and TypeScript components.
 * </p>
 *
 * @see org.melviz.displayer Package for displayer settings
 * @see org.melviz.dataset Package for dataset operations
 * @since 1.0
 */
package org.melviz.displayer.external;
