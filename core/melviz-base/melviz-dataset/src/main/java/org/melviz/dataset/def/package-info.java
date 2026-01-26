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
 * Dataset definition metadata and configuration.
 * <p>
 * This package contains classes for defining dataset metadata, including data sources,
 * column definitions, caching policies, and refresh strategies. Dataset definitions
 * serve as blueprints for creating and managing datasets.
 * </p>
 *
 * <h2>Core Concepts</h2>
 * <ul>
 * <li>{@link org.melviz.dataset.def.DataSetDef} - Complete dataset definition including UUID,
 * name, columns, filters, and configuration</li>
 * <li>{@link org.melviz.dataset.def.DataColumnDef} - Column metadata with type, ID, and
 * optional pattern/expression</li>
 * <li>{@link org.melviz.dataset.def.DataSetDefBuilder} - Fluent API for constructing dataset
 * definitions</li>
 * <li>{@link org.melviz.dataset.def.DataSetDefRegistry} - Central registry for storing and
 * retrieving dataset definitions</li>
 * </ul>
 *
 * <h2>External Data Sources</h2>
 * <p>
 * The package supports external data sources through:
 * </p>
 * <ul>
 * <li>{@link org.melviz.dataset.def.ExternalDataSetDef} - Definition for external HTTP/REST
 * data sources</li>
 * <li>{@link org.melviz.dataset.def.ExternalDataSetDefBuilder} - Builder for external dataset
 * definitions</li>
 * <li>{@link org.melviz.dataset.def.ExternalServiceType} - Supported external service types
 * (JSON, CSV, Metrics)</li>
 * <li>{@link org.melviz.dataset.def.HttpMethod} - HTTP methods for data retrieval</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Define an external JSON dataset
 * DataSetDef def = DataSetDefBuilder.get()
 *     .uuid("sales-api")
 *     .name("Sales Data")
 *     .external()
 *         .url("https://api.example.com/sales")
 *         .expression("$.data[*]")  // JSONata expression
 *         .accumulate(true)
 *         .poll(60, TimeUnit.SECONDS)
 *     .buildDef();
 *
 * // Register the definition
 * registry.registerDataSetDef(def);
 * }</pre>
 *
 * <h2>Caching and Refresh Policies</h2>
 * <p>
 * Dataset definitions support sophisticated caching strategies:
 * </p>
 * <ul>
 * <li><b>Cache Enabled</b> - Whether to cache dataset results</li>
 * <li><b>Push Enabled</b> - Whether to support real-time push updates</li>
 * <li><b>Refresh</b> - Automatic refresh interval and policies</li>
 * <li><b>Accumulate</b> - Append new data versus replace on refresh</li>
 * </ul>
 *
 * <h2>Validation</h2>
 * <p>
 * All definitions support validation through the {@code validate()} method, which
 * returns a list of {@link org.melviz.dataset.ValidationError} instances for any
 * configuration issues.
 * </p>
 *
 * @see org.melviz.dataset Package for runtime dataset instances
 * @see org.melviz.dataset.engine Package for dataset operation engine
 * @since 1.0
 */
package org.melviz.dataset.def;
