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
 * Dataset filtering API using the Strategy pattern for flexible row filtering.
 * <p>
 * This package provides a comprehensive filtering system that supports simple column
 * predicates, complex logical expressions, and custom filter functions. Filters can
 * be composed using AND, OR, and NOT logical operators.
 * </p>
 *
 * <h2>Filter Types</h2>
 * <p>
 * The package provides three main filter strategies:
 * </p>
 * <ul>
 * <li>{@link org.melviz.dataset.filter.CoreFunctionFilter} - Built-in filter functions
 * (equals, not_equals, greater_than, less_than, between, like, is_null, etc.)</li>
 * <li>{@link org.melviz.dataset.filter.LogicalExprFilter} - Logical combination of filters
 * using AND, OR, NOT operators</li>
 * <li>{@link org.melviz.dataset.filter.CustomFunctionFilter} - User-defined filter functions
 * for custom filtering logic</li>
 * </ul>
 *
 * <h2>Core Filter Functions</h2>
 * <p>
 * Available through {@link org.melviz.dataset.filter.CoreFunctionType}:
 * </p>
 * <ul>
 * <li><b>Comparison</b>: EQUALS_TO, NOT_EQUALS_TO, GREATER_THAN, LOWER_THAN, GREATER_OR_EQUALS_TO, LOWER_OR_EQUALS_TO</li>
 * <li><b>Range</b>: BETWEEN, NOT_BETWEEN</li>
 * <li><b>Set</b>: IN, NOT_IN</li>
 * <li><b>String</b>: LIKE_TO, TIME_FRAME</li>
 * <li><b>Null checking</b>: IS_NULL, NOT_NULL</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Simple filter: revenue > 1000
 * DataSetFilter filter1 = FilterFactory.greaterThan("revenue", 1000);
 *
 * // Range filter: date between 2024-01-01 and 2024-12-31
 * DataSetFilter filter2 = FilterFactory.between("date",
 *     parseDate("2024-01-01"), parseDate("2024-12-31"));
 *
 * // Logical combination: (revenue > 1000) AND (category IN ['A', 'B'])
 * DataSetFilter composite = FilterFactory.AND(
 *     FilterFactory.greaterThan("revenue", 1000),
 *     FilterFactory.in("category", "A", "B")
 * );
 *
 * // Apply to lookup
 * DataSetLookup lookup = DataSetLookupFactory.newDataSetLookupBuilder()
 *     .dataset("sales")
 *     .filter(composite)
 *     .buildLookup();
 * }</pre>
 *
 * <h2>Column Filters</h2>
 * <p>
 * {@link org.melviz.dataset.filter.ColumnFilter} provides a specialized interface for
 * filtering on specific columns with type-safe parameters.
 * </p>
 *
 * <h2>Filter Factory</h2>
 * <p>
 * {@link org.melviz.dataset.filter.FilterFactory} provides convenient static methods for
 * creating common filter types without direct constructor invocation.
 * </p>
 *
 * <h2>Performance Considerations</h2>
 * <p>
 * Filters are evaluated row-by-row during dataset operations. For optimal performance:
 * </p>
 * <ul>
 * <li>Place most selective filters first in AND combinations</li>
 * <li>Use indexed columns when possible</li>
 * <li>Consider caching filtered results for repeated queries</li>
 * <li>Avoid complex regex patterns in LIKE filters on large datasets</li>
 * </ul>
 *
 * @see org.melviz.dataset.engine.filter Package for filter algorithm implementations
 * @see org.melviz.dataset.DataSetLookup For applying filters to datasets
 * @since 1.0
 */
package org.melviz.dataset.filter;
