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
package org.melviz.dataprovider.external;

import org.melviz.dataprovider.DataSetProvider;
import org.melviz.dataprovider.DataSetProviderType;
import org.melviz.dataprovider.StaticDataSetProvider;
import org.melviz.dataset.DataSet;
import org.melviz.dataset.DataSetLookup;
import org.melviz.dataset.DataSetMetadata;
import org.melviz.dataset.date.TimeAmount;
import org.melviz.dataset.def.DataSetDef;
import org.melviz.dataset.def.ExternalDataSetDef;
import org.melviz.dataset.impl.DataSetImpl;
import org.melviz.dataset.impl.DataSetMetadataImpl;
import org.melviz.scheduler.DataSetInvalidationTask;
import org.melviz.scheduler.Scheduler;

public class ExternalDataSetProvider implements DataSetProvider {

    private ExternalDataSetCaller caller;

    protected StaticDataSetProvider staticDataSetProvider;

    private Scheduler scheduler;

    public ExternalDataSetProvider() {
        super();
    }

    public ExternalDataSetProvider(ExternalDataSetCaller caller,
                                   StaticDataSetProvider staticDataSetProvider,
                                   Scheduler scheduler) {
        this.caller = caller;
        this.staticDataSetProvider = staticDataSetProvider;
        this.scheduler = scheduler;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public DataSetProviderType getType() {
        return DataSetProviderType.EXTERNAL;
    }

    @Override
    public DataSetMetadata getDataSetMetadata(DataSetDef def) throws Exception {
        checkExternal(def);
        var externalDef = (ExternalDataSetDef) def;
        if (externalDef.isDynamic()) {
            return caller.retrieveMetadata(externalDef);
        } else {
            var dataSet = lookupDataSet(def, null);
            return new DataSetMetadataImpl((DataSetImpl) dataSet);
        }
    }

    @Override
    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        var uuid = def.getUUID();
        if (uuid == null) {
            throw new IllegalArgumentException("DataSet UUID can't be null");
        }
        checkExternal(def);
        var isTest = lookup != null && lookup.testMode();
        var resultDataSet = staticDataSetProvider.lookupDataSet(def, lookup);
        if (resultDataSet == null || isTest) {
            registerDataSet(def);
            resultDataSet = staticDataSetProvider.lookupDataSet(def, lookup);
        }

        // do not keep test dataSets, but need static because it handles lookup
        if (isTest || !def.isCacheEnabled()) {
            staticDataSetProvider.removeDataSet(uuid);
        }
        return resultDataSet;
    }

    @Override
    public boolean isDataSetOutdated(DataSetDef def) {
        // we will handle caching internally
        return true;
    }

    private void checkExternal(DataSetDef def) {
        if (!(def instanceof ExternalDataSetDef)) {
            throw new IllegalArgumentException("Not an external DataSet definition");
        }
    }

    private void registerDataSet(DataSetDef def) {
        var externalDef = (ExternalDataSetDef) def;
        var newDataSet = caller.retrieveDataSet(externalDef);
        newDataSet.setDefinition(def);
        newDataSet.setUUID(def.getUUID());
        staticDataSetProvider.registerDataSet(newDataSet);

        var taskKey = DataSetInvalidationTask.key(def);
        scheduler.unschedule(taskKey);
        if (def.isCacheEnabled()) {
            if (def.getRefreshTime() != null && def.getRefreshTime().trim().length() > 0) {
                var tf = TimeAmount.parse(def.getRefreshTime());
                var seconds = tf.toMillis() / 1000;
                scheduler.schedule(new DataSetInvalidationTask(def, staticDataSetProvider::removeDataSet), seconds);
            }
        }
    }
}
