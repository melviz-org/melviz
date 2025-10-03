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
package org.melviz.client.services;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.melviz.client.RuntimeClientLoader;
import org.melviz.client.error.DefaultRuntimeErrorCallback;
import org.melviz.client.error.ErrorResponseVerifier;
import org.melviz.client.external.ExternalDataSetClientProvider;
import org.melviz.dataprovider.DataSetProviderType;
import org.melviz.dataset.DataSetLookup;
import org.melviz.dataset.DataSetMetadata;
import org.melviz.dataset.client.ClientDataSetManager;
import org.melviz.dataset.client.DataSetClientServices;
import org.melviz.dataset.client.DataSetExportReadyCallback;
import org.melviz.dataset.client.DataSetMetadataCallback;
import org.melviz.dataset.client.DataSetReadyCallback;
import org.melviz.dataset.def.DataSetDef;
import org.melviz.dataset.events.DataSetDefRemovedEvent;

@Alternative
@ApplicationScoped
public class RuntimeDataSetClientServices implements DataSetClientServices {

    @Inject
    ErrorResponseVerifier verifier;

    @Inject
    DefaultRuntimeErrorCallback errorCallback;

    @Inject
    ClientDataSetManager clientDataSetManager;

    @Inject
    RuntimeClientLoader loader;

    @Inject
    ExternalDataSetClientProvider externalDataSetClientProvider;

    @Inject
    JoinDataSetsService joinDataSetsService;

    public RuntimeDataSetClientServices() {
        // empty
    }

    @Override
    public void setPushRemoteDataSetEnabled(boolean pushRemoteDataSetEnabled) {
        // ignored
    }

    @Override
    public void fetchMetadata(String uuid, DataSetMetadataCallback listener) throws Exception {
        // empty        
    }

    @Override
    public DataSetMetadata getMetadata(String uuid) {
        // empty
        return null;
    }

    @Override
    public void lookupDataSet(DataSetDef def, DataSetLookup lookup, DataSetReadyCallback listener) throws Exception {
        var clientDataSet = clientDataSetManager.lookupDataSet(lookup);
        var uuid = lookup.getDataSetUUID();
        if (!isAccumulate(uuid) && clientDataSet != null) {
            listener.callback(clientDataSet);
            return;
        }

        var join = getJoin(uuid);
        if (!join.isEmpty()) {
            var externalDef = externalDataSetClientProvider.get(uuid).get();
            joinDataSetsService.joinDataSets(externalDef, lookup, listener);
            return;
        }

        externalDataSetClientProvider.fetchAndRegister(uuid, lookup, listener);
    }

    @Override
    public void lookupDataSet(DataSetLookup request, DataSetReadyCallback listener) throws Exception {
        this.lookupDataSet(null, request, listener);
    }

    @Override
    public void exportDataSetCSV(DataSetLookup request, DataSetExportReadyCallback listener) throws Exception {
        throw new IllegalArgumentException("Export to CSV not supported");
    }

    @Override
    public void exportDataSetExcel(DataSetLookup request, DataSetExportReadyCallback listener) throws Exception {
        throw new IllegalArgumentException("Export to excel not supported");
    }

    @Override
    public void newDataSet(DataSetProviderType type, RemoteCallback<DataSetDef> callback) throws Exception {
        throw new IllegalArgumentException("New data sets are not supported");
    }

    @Override
    public void getPublicDataSetDefs(RemoteCallback<List<DataSetDef>> callback) {
        // ignored in runtime
    }

    void onDataSetDefRemovedEvent(@Observes DataSetDefRemovedEvent evt) {
        if (evt.getDataSetDef() != null) {
            var uuid = evt.getDataSetDef().getUUID();
            externalDataSetClientProvider.unregister(uuid);
            clientDataSetManager.removeDataSet(uuid);
        }

    }

    private Collection<String> getJoin(String uuid) {
        return externalDataSetClientProvider.get(uuid).filter(def -> def.getJoin() != null)
                .map(def -> def.getJoin())
                .orElse(Collections.emptyList());

    }

    private boolean isAccumulate(String uuid) {
        return externalDataSetClientProvider.get(uuid).map(def -> def.isAccumulate()).orElse(false);
    }

}
