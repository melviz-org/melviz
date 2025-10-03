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
package org.melviz.dataprovider;

import org.melviz.dataset.DataSet;
import org.melviz.dataset.DataSetFactory;
import org.melviz.dataset.DataSetLookup;
import org.melviz.dataset.DataSetMetadata;
import org.melviz.dataset.def.DataSetDef;

/**
 * An example of custom data set provider implementation
 */
public class CustomDataSetProvider implements DataSetProvider {

    public static final DataSetProviderType TYPE = () -> "CUSTOM";

    private static CustomDataSetProvider SINGLETON = null;

    public static CustomDataSetProvider get() {
        if (SINGLETON == null) {
            SINGLETON = new CustomDataSetProvider ();
        }
        return SINGLETON;
    }

    @Override
    public DataSetProviderType getType() {
        return TYPE;
    }

    @Override
    public DataSetMetadata getDataSetMetadata(DataSetDef def) throws Exception {
        DataSet dataSet = lookupDataSet(def, null);
        if (dataSet == null) {
            return null;
        }
        return dataSet.getMetadata();
    }

    @Override
    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        return DataSetFactory.newDataSetBuilder()
                .label("name")
                .row("david")
                .row("maciejs")
                .buildDataSet();
    }

    @Override
    public boolean isDataSetOutdated(DataSetDef def) {
        return false;
    }
}
