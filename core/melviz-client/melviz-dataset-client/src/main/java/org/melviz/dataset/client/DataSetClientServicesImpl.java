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
package org.melviz.dataset.client;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.melviz.dataset.DataSet;
import org.melviz.dataset.DataSetLookup;
import org.melviz.dataset.def.DataSetDef;
import org.melviz.dataset.engine.group.IntervalBuilderLocator;
import org.melviz.dataset.events.DataSetDefRemovedEvent;
import org.melviz.dataset.group.AggregateFunctionManager;
import org.melviz.dataset.service.DataSetDefServices;
import org.melviz.dataset.service.DataSetLookupServices;

/**
 * Default implementation
 */
@ApplicationScoped
public class DataSetClientServicesImpl implements DataSetClientServices {

    private ClientDataSetManager clientDataSetManager;
    private AggregateFunctionManager aggregateFunctionManager;
    private IntervalBuilderLocator intervalBuilderLocator;
    private Caller<DataSetLookupServices> dataSetLookupServices;
    private Caller<DataSetDefServices> dataSetDefServices;

    public DataSetClientServicesImpl() {
    }

    @Inject
    public DataSetClientServicesImpl(ClientDataSetManager clientDataSetManager,
            AggregateFunctionManager aggregateFunctionManager,
            IntervalBuilderLocator intervalBuilderLocator,
            Caller<DataSetLookupServices> dataSetLookupServices,
            Caller<DataSetDefServices> dataSetDefServices) {

        this.clientDataSetManager = clientDataSetManager;
        this.aggregateFunctionManager = aggregateFunctionManager;
        this.intervalBuilderLocator = intervalBuilderLocator;
        this.dataSetLookupServices = dataSetLookupServices;
        this.dataSetDefServices = dataSetDefServices;
    }

    /**
     * Creates a brand new data set definition for the provider type specified
     *
     * @param type The provider type
     * @return A data set definition instance
     */
    public void newDataSet(RemoteCallback<DataSetDef> callback) throws Exception {
        dataSetDefServices.call(callback).createDataSetDef();
    }

    /**
     * Process the specified data set lookup request for a given definition.
     *
     * @param def     The data set definition
     * @param request The data set lookup request
     * @throws Exception It there is an unexpected error trying to execute the
     *                   lookup request.
     */
    public void lookupDataSet(final DataSetDef def,
            final DataSetLookup request,
            final DataSetReadyCallback listener) throws Exception {

        if (dataSetLookupServices != null) {
            // TODO: Check if this is dead code
        }
        // Data set not found on client.
        else {
            listener.notFound();
        }
    }

    /**
     * Process the specified data set lookup request.
     *
     * @param request The data set lookup request
     * @throws Exception It there is an unexpected error trying to execute the
     *                   lookup request.
     */
    public void lookupDataSet(final DataSetLookup request,
            final DataSetReadyCallback listener) throws Exception {

        // Look always into the client data set manager.
        if (clientDataSetManager.getDataSet(request.getDataSetUUID()) != null) {
            DataSet dataSet = clientDataSetManager.lookupDataSet(request);
            listener.callback(dataSet);
        }
        // Data set not found on client.
        else {
            listener.notFound();
        }
    }

    /**
     * @since 0.3.0.Final
     * @deprecated Use <i>getPublicDataSetDefs</i> instead
     */
    public void getRemoteSharedDataSetDefs(RemoteCallback<List<DataSetDef>> callback) {
        getPublicDataSetDefs(callback);
    }

    public void getPublicDataSetDefs(RemoteCallback<List<DataSetDef>> callback) {
        try {
            dataSetDefServices.call(callback).getPublicDataSetDefs();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AggregateFunctionManager getAggregateFunctionManager() {
        return aggregateFunctionManager;
    }

    public IntervalBuilderLocator getIntervalBuilderLocator() {
        return intervalBuilderLocator;
    }

    // Classes for the handling of concurrent lookup requests over any push-able
    // data set

    void onDataSetRemovedEvent(@Observes DataSetDefRemovedEvent event) {
        String uuid = event.getDataSetDef().getUUID();
        clientDataSetManager.removeDataSet(uuid);
    }
}
