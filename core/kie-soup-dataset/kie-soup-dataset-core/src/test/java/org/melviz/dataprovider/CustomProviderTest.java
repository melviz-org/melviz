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
package org.melviz.dataprovider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.melviz.DataSetCore;
import org.melviz.dataset.DataSet;
import org.melviz.dataset.DataSetLookup;
import org.melviz.dataset.DataSetLookupFactory;
import org.melviz.dataset.DataSetManager;
import org.melviz.dataset.DataSetMetadata;
import org.melviz.dataset.def.DataSetDef;
import org.melviz.dataset.def.DataSetDefRegistry;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CustomProviderTest {

    CustomDataSetProvider customProvider = spy(CustomDataSetProvider.get());
    DataSetProviderRegistry providerRegistry = DataSetCore.get().getDataSetProviderRegistry();
    DataSetDefRegistry dataSetDefRegistry = DataSetCore.get().getDataSetDefRegistry();
    DataSetManager dataSetManager = DataSetCore.get().getDataSetManager();
    DataSetDef customDef = new DataSetDef();

    @Before
    public void setUp() {
        providerRegistry.registerDataProvider(customProvider);

        customDef.setProvider(customProvider.getType());
        customDef.setUUID("test");
        dataSetDefRegistry.registerDataSetDef(customDef);
    }

    @Test
    public void testRegistry() throws Exception {
        DataSetProviderType type = providerRegistry.getProviderTypeByName("CUSTOM");
        assertEquals(customProvider.getType(), CustomDataSetProvider.TYPE);
        assertEquals(type, CustomDataSetProvider.TYPE);
    }

    @Test
    public void testMetadata() throws Exception {
        DataSetMetadata medatata = dataSetManager.getDataSetMetadata("test");

        verify(customProvider).getDataSetMetadata(customDef);
        assertEquals(medatata.getNumberOfColumns(), 1);
        assertEquals(medatata.getColumnId(0), "name");
    }

    @Test
    public void testLookup() throws Exception {
        DataSetLookup lookup = DataSetLookupFactory
                .newDataSetLookupBuilder().dataset("test")
                .buildLookup();

        DataSet dataSet = dataSetManager.lookupDataSet(lookup);

        verify(customProvider).lookupDataSet(customDef, lookup);
        assertEquals(dataSet.getRowCount(), 2);
        assertEquals(dataSet.getValueAt(0, 0), "david");
        assertEquals(dataSet.getValueAt(1, 0), "maciejs");
   }
}