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
package org.melviz.dataset;

import org.junit.Test;
import org.melviz.dataset.def.ExternalDataSetDef;
import org.melviz.dataset.impl.ExternalDataSetDefBuilderImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DataSetDefTest {

    ExternalDataSetDef externalDef = (ExternalDataSetDef) new ExternalDataSetDefBuilderImpl()
            .uuid("external")
            .name("external dataset")
            .url("http://datasets.com/dataset")
            .buildDef();
    
    ExternalDataSetDef externalDef2 = (ExternalDataSetDef) new ExternalDataSetDefBuilderImpl()
            .uuid("external")
            .name("external dataset")
            .url("http://datasets.com/dataset")
            .buildDef();

    @Test
    public void testExternalHashCode() throws Exception {
        assertEquals(externalDef.hashCode(), externalDef2.clone().hashCode());
        externalDef.setUrl("http://otherurl.com");
        assertNotEquals(externalDef.hashCode(), externalDef2.clone().hashCode());
    }
}
