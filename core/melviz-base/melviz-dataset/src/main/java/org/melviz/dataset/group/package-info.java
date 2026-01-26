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
 * Dataset grouping and aggregation API for data summarization.
 * <p>
 * This package provides functionality for grouping rows by column values and applying
 * aggregation functions to compute summary statistics. It supports both simple grouping
 * and complex date interval grouping.
 * </p>
 *
 * <h2>Core Concepts</h2>
 * <ul>
 * <li>{@link org.melviz.dataset.group.DataSetGroup} - Represents a group operation with
 * column grouping and aggregate functions</li>
 * <li>{@link org.melviz.dataset.group.ColumnGroup} - Individual column to group by, with
 * optional interval settings for date columns</li>
 * <li>{@link org.melviz.dataset.group.AggregateFunction} - Function to apply to grouped
 * values (sum, average, count, etc.)</li>
 * <li>{@link org.melviz.dataset.group.AggregateFunctionManager} - Registry and factory for
 * aggregate functions</li>
 * </ul>
 *
 * <h2>Aggregate Functions</h2>
 * <p>
 * Supported aggregate functions via {@link org.melviz.dataset.group.AggregateFunctionType}:
 * </p>
 * <ul>
 * <li><b>Numeric</b>: SUM, AVERAGE, MIN, MAX, MEDIAN</li>
 * <li><b>Count</b>: COUNT, DISTINCT (unique count)</li>
 * <li><b>String</b>: JOIN (concatenate), JOIN_COMMA, JOIN_HYPHEN</li>
 * </ul>
 *
 * <h2>Date Interval Grouping</h2>
 * <p>
 * Date columns can be grouped by time intervals using {@link org.melviz.dataset.group.DateIntervalType}:
 * </p>
 * <ul>
 * <li><b>Time Units</b>: SECOND, MINUTE, HOUR, DAY, MONTH, QUARTER, YEAR, DECADE, CENTURY, MILLENIUM</li>
 * <li><b>Derived</b>: DAY_OF_WEEK (Monday-Sunday), MONTH (January-December), QUARTER (Q1-Q4)</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Simple grouping: Group by category and sum revenue
 * DataSetLookup lookup = DataSetLookupFactory.newDataSetLookupBuilder()
 *     .dataset("sales")
 *     .group("category")
 *     .column("category")
 *     .column("revenue", AggregateFunctionType.SUM)
 *     .buildLookup();
 *
 * // Date interval grouping: Monthly sales totals
 * DataSetLookup monthlyLookup = DataSetLookupFactory.newDataSetLookupBuilder()
 *     .dataset("sales")
 *     .group("date", DateIntervalType.MONTH)
 *     .column("date")
 *     .column("revenue", AggregateFunctionType.SUM)
 *     .column("order_id", AggregateFunctionType.COUNT)
 *     .buildLookup();
 *
 * // Multiple grouping columns: Category and region breakdown
 * DataSetLookup multiGroup = DataSetLookupFactory.newDataSetLookupBuilder()
 *     .dataset("sales")
 *     .group("category")
 *     .group("region")
 *     .column("category")
 *     .column("region")
 *     .column("revenue", AggregateFunctionType.AVERAGE)
 *     .column("customer_id", AggregateFunctionType.DISTINCT)
 *     .buildLookup();
 * }</pre>
 *
 * <h2>Fixed vs Dynamic Intervals</h2>
 * <p>
 * Date interval grouping supports two modes:
 * </p>
 * <ul>
 * <li><b>Fixed Intervals</b>: Predefined buckets (e.g., all Mondays, all Januaries)</li>
 * <li><b>Dynamic Intervals</b>: Intervals derived from actual data range</li>
 * </ul>
 *
 * <h2>Group Strategy</h2>
 * <p>
 * {@link org.melviz.dataset.group.GroupStrategy} controls grouping behavior:
 * </p>
 * <ul>
 * <li><b>GROUP</b>: Standard SQL-style grouping with aggregation</li>
 * <li><b>DYNAMIC</b>: Dynamic interval calculation based on data range</li>
 * <li><b>FIXED</b>: Fixed, predefined intervals regardless of data</li>
 * </ul>
 *
 * <h2>Performance Tips</h2>
 * <ul>
 * <li>Index columns used for grouping for faster performance</li>
 * <li>Group by low-cardinality columns first in multi-column groups</li>
 * <li>Use DISTINCT sparingly as it requires maintaining unique sets</li>
 * <li>Consider pre-aggregating frequently queried combinations</li>
 * </ul>
 *
 * @see org.melviz.dataset.engine.group Package for grouping algorithm implementations
 * @see org.melviz.dataset.engine.function Package for aggregate function implementations
 * @since 1.0
 */
package org.melviz.dataset.group;
