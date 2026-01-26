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
 * Dataset operation engine for executing filter, group, sort, and aggregation operations.
 * <p>
 * This package contains the core engine that processes {@link org.melviz.dataset.DataSetLookup}
 * requests and applies operations to datasets. The engine is optimized for performance through
 * indexing, caching, and incremental computation strategies.
 * </p>
 *
 * <h2>Architecture</h2>
 * <p>
 * The engine follows a pipeline architecture where operations are applied sequentially:
 * </p>
 * <ol>
 * <li><b>Filter</b> - Reduce rows based on column predicates (see {@link org.melviz.dataset.engine.filter})</li>
 * <li><b>Group</b> - Partition data into groups with aggregation functions (see {@link org.melviz.dataset.engine.group})</li>
 * <li><b>Sort</b> - Order rows by column values (see {@link org.melviz.dataset.engine.sort})</li>
 * <li><b>Select</b> - Choose specific columns to include in result</li>
 * </ol>
 *
 * <h2>Key Components</h2>
 * <ul>
 * <li>{@link org.melviz.dataset.engine.SharedDataSetOpEngine} - Main engine implementation
 * applying operations to datasets</li>
 * <li>{@link org.melviz.dataset.engine.DataSetHandler} - Manages dataset lifecycle, caching,
 * and operation coordination</li>
 * <li>{@link org.melviz.dataset.DataSetOpEngine} - Interface for dataset operation engines</li>
 * <li>{@link org.melviz.dataset.engine.Chronometer} - Performance timing utility for
 * monitoring operation execution</li>
 * </ul>
 *
 * <h2>Performance Optimization</h2>
 * <p>
 * The engine employs several strategies for optimal performance:
 * </p>
 * <ul>
 * <li><b>Indexing</b> - Pre-computed indices for filtering and grouping (see {@link org.melviz.dataset.engine.index})</li>
 * <li><b>Incremental Updates</b> - Reuse previous computation results when possible</li>
 * <li><b>Lazy Evaluation</b> - Operations are only executed when results are requested</li>
 * <li><b>Caching</b> - Result caching at multiple levels (index, handler, operation)</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * The engine is designed to be thread-safe for concurrent read operations. Write operations
 * (dataset updates) should be synchronized externally.
 * </p>
 *
 * @see org.melviz.dataset.engine.filter Filter algorithm implementations
 * @see org.melviz.dataset.engine.group Grouping and interval builders
 * @see org.melviz.dataset.engine.sort Sorting algorithms
 * @see org.melviz.dataset.engine.index Indexing strategies for performance
 * @see org.melviz.dataset.engine.function Aggregate function implementations
 * @since 1.0
 */
package org.melviz.dataset.engine;
