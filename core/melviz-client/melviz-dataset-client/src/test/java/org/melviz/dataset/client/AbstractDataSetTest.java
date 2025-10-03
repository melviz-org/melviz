/*
 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.melviz.dataset.client;

import javax.enterprise.event.Event;

import org.jboss.errai.common.client.api.Caller;
import org.junit.Before;
import org.melviz.dataset.DataSet;
import org.melviz.dataset.DataSetFormatter;
import org.melviz.dataset.ExpenseReportsData;
import org.melviz.dataset.events.DataSetModifiedEvent;
import org.melviz.dataset.events.DataSetPushOkEvent;
import org.melviz.dataset.events.DataSetPushingEvent;
import org.melviz.dataset.service.DataSetDefServices;
import org.melviz.dataset.service.DataSetLookupServices;
import org.mockito.Mock;

public abstract class AbstractDataSetTest {

    @Mock
    protected Event<DataSetPushingEvent> dataSetPushingEvent;

    @Mock
    protected Event<DataSetPushOkEvent> dataSetPushOkEvent;

    @Mock
    protected Event<DataSetModifiedEvent> dataSetModifiedEvent;

    @Mock
    protected DataSetLookupServices dataSetLookupServices;

    @Mock
    protected Caller<DataSetDefServices> dataSetDefServicesCaller;

    protected Caller<DataSetLookupServices> dataSetLookupServicesCaller;
    protected ClientDataSetCore clientDataSetCore;
    protected DataSetClientServices clientServices;
    protected ClientDataSetManager clientDataSetManager;
    protected DataSet expensesDataSet;
    protected DataSetFormatter dataSetFormatter = new DataSetFormatter();

    public static final String EXPENSES = "expenses";

    public void initClientFactory() {
        clientDataSetCore = ClientDataSetCore.get();
        clientDataSetCore.setClientDateFormatter(new ClientDateFormatterMock());
        clientDataSetCore.setChronometer(new ChronometerMock());
    }

    public void initClientDataSetManager() {
        clientDataSetManager = clientDataSetCore.getClientDataSetManager();
    }

    public void initDataSetClientServices() {
        clientServices = new DataSetClientServicesImpl(
                clientDataSetManager,
                clientDataSetCore.getAggregateFunctionManager(),
                clientDataSetCore.getIntervalBuilderLocator(),
                dataSetPushingEvent,
                dataSetPushOkEvent,
                dataSetModifiedEvent,
                dataSetLookupServicesCaller,
                dataSetDefServicesCaller);
    }

    public void registerExpensesDataSet() throws Exception {
        expensesDataSet = ExpenseReportsData.INSTANCE.toDataSet();
        expensesDataSet.setUUID(EXPENSES);
        clientDataSetManager.registerDataSet(expensesDataSet);
    }

    @Before
    public void init() throws Exception {
        initClientFactory();
        initClientDataSetManager();
        initDataSetClientServices();
        registerExpensesDataSet();
    }

    public void printDataSet(DataSet dataSet) {
        System.out.print(dataSetFormatter.formatDataSet(dataSet, "{", "}", ",\n", "\"", "\"", ", ") + "\n\n");
    }
}
