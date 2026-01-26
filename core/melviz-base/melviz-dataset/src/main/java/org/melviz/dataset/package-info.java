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
 * Core dataset API for representing, manipulating, and querying tabular data.
 * <p>
 * This package provides the foundational data structures and interfaces for working
 * with datasets in Melviz. Key components include:
 * </p>
 * <ul>
 * <li>{@link org.melviz.dataset.DataSet} - The primary interface representing a tabular dataset
 * with rows and columns</li>
 * <li>{@link org.melviz.dataset.DataColumn} - Individual column within a dataset</li>
 * <li>{@link org.melviz.dataset.DataSetLookup} - Query specification for filtering, grouping,
 * sorting, and selecting data</li>
 * <li>{@link org.melviz.dataset.DataSetBuilder} - Fluent API for constructing datasets
 * programmatically</li>
 * <li>{@link org.melviz.dataset.DataSetManager} - Main entry point for dataset operations and
 * lifecycle management</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Create a dataset programmatically
 * DataSet dataset = DataSetFactory.newDataSetBuilder()
 *     .label("sales-data")
 *     .column("product", ColumnType.LABEL)
 *     .column("revenue", ColumnType.NUMBER)
 *     .row("Widget A", 1500.0)
 *     .row("Widget B", 2300.0)
 *     .buildDataSet();
 *
 * // Query with filtering and grouping
 * DataSetLookup lookup = DataSetLookupFactory.newDataSetLookupBuilder()
 *     .dataset("sales-data")
 *     .filter("product", FilterFactory.notNull())
 *     .group("product").sum("revenue")
 *     .buildLookup();
 * }</pre>
 *
 * <h2>Data Types</h2>
 * <p>
 * Supported column types are defined in {@link org.melviz.dataset.ColumnType}:
 * </p>
 * <ul>
 * <li>LABEL - Text/string values</li>
 * <li>TEXT - Long-form text content</li>
 * <li>NUMBER - Numeric values (integers, decimals)</li>
 * <li>DATE - Temporal values</li>
 * </ul>
 *
 * @see org.melviz.dataset.def Package for dataset definitions (metadata)
 * @see org.melviz.dataset.engine Package for dataset operation engine
 * @see org.melviz.dataset.filter Package for data filtering
 * @see org.melviz.dataset.group Package for data grouping and aggregation
 * @since 1.0
 */
package org.melviz.dataset;
