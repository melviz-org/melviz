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
package org.melviz.dataset.service;

import org.jboss.errai.bus.server.annotations.Remote;
import org.melviz.dataset.DataSet;
import org.melviz.dataset.DataSetLookup;
import org.melviz.dataset.DataSetMetadata;
import org.melviz.dataset.def.DataSetDef;

/**
 * Data set lookup services
 */
@Remote
public interface DataSetLookupServices {

    /**
     * Apply a sequence of operations (filter, sort, group, ...) on a remote data set.
     *
     * @return A brand new data set with all the calculations applied.
     */
    DataSet lookupDataSet(DataSetLookup lookup) throws Exception;

    /**
     * Load a data set and apply several operations (filter, sort, group, ...) on top of it for a given definition.
     * Index and cache are not used.
     * @return null, if the data set can be retrieved.
     */
    DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception;

    /**
     * Same as lookupDataSet but only retrieves the metadata of the resulting data set.
     *
     * @return A DataSetMetadata instance containing general information about the data set.
     */
    DataSetMetadata lookupDataSetMetadata(String uuid) throws Exception;
}
