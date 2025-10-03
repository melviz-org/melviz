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
package org.melviz.dataset;

import org.junit.Before;
import org.melviz.DataSetCore;
import org.melviz.dataset.engine.SharedDataSetOpEngine;
import org.melviz.dataset.group.AggregateFunctionType;

import static org.melviz.dataset.ExpenseReportsData.COLUMN_AMOUNT;
import static org.melviz.dataset.ExpenseReportsData.COLUMN_CITY;
import static org.melviz.dataset.ExpenseReportsData.COLUMN_DEPARTMENT;
import static org.melviz.dataset.filter.FilterFactory.equalsTo;

public class DataSetIndexTest {

    public static final String EXPENSE_REPORTS = "expense_reports_dataset";

    /**
     * Group by department and count occurrences
     */
    DataSetLookup groupByDeptAndCount = DataSetLookupFactory.newDataSetLookupBuilder()
            .dataset(EXPENSE_REPORTS)
            .group(COLUMN_DEPARTMENT, "Department")
            .column(AggregateFunctionType.COUNT, "occurrences")
            .buildLookup();

    /**
     * Group by department and sum the amount
     */
    DataSetLookup groupByDeptAndSum = DataSetLookupFactory.newDataSetLookupBuilder()
            .dataset(EXPENSE_REPORTS)
            .group(COLUMN_DEPARTMENT, "Department")
            .column(COLUMN_AMOUNT, AggregateFunctionType.AVERAGE)
            .buildLookup();

    /**
     * Filter by city & department
     */
    DataSetLookup filterByCityAndDept = DataSetLookupFactory.newDataSetLookupBuilder()
            .dataset(EXPENSE_REPORTS)
            .filter(COLUMN_CITY, equalsTo("Barcelona"))
            .filter(COLUMN_DEPARTMENT, equalsTo("Engineering"))
            .buildLookup();

    /**
     * Sort by amount in ascending order
     */
    DataSetLookup sortByAmountAsc = DataSetLookupFactory.newDataSetLookupBuilder()
            .dataset(EXPENSE_REPORTS)
            .sort(COLUMN_AMOUNT, "asc")
            .buildLookup();

    /**
     * Sort by amount in descending order
     */
    DataSetLookup sortByAmountDesc = DataSetLookupFactory.newDataSetLookupBuilder()
            .dataset(EXPENSE_REPORTS)
            .sort(COLUMN_AMOUNT, "desc")
            .buildLookup();

    SharedDataSetOpEngine dataSetOpEngine = DataSetCore.get().getSharedDataSetOpEngine();

    @Before
    public void setUp() throws Exception {
        DataSet dataSet = ExpenseReportsData.INSTANCE.toDataSet();
        dataSet.setUUID(EXPENSE_REPORTS);
        dataSetOpEngine.getIndexRegistry().put(dataSet);
    }

}
