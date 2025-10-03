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
package org.melviz.dataset.client.engine;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.melviz.dataset.ColumnType;
import org.melviz.dataset.client.resources.i18n.CommonConstants;
import org.melviz.dataset.engine.group.IntervalBuilder;
import org.melviz.dataset.engine.group.IntervalBuilderDynamicLabel;
import org.melviz.dataset.engine.group.IntervalBuilderFixedDate;
import org.melviz.dataset.engine.group.IntervalBuilderLocator;
import org.melviz.dataset.group.GroupStrategy;

@ApplicationScoped
public class ClientIntervalBuilderLocator implements IntervalBuilderLocator {

    IntervalBuilderDynamicLabel intervalBuilderDynamicLabel;
    ClientIntervalBuilderDynamicDate intervalBuilderDynamicDate;
    IntervalBuilderFixedDate intervalBuilderFixedDate;

    public ClientIntervalBuilderLocator() {
    }

    @Inject
    public ClientIntervalBuilderLocator(IntervalBuilderDynamicLabel intervalBuilderDynamicLabel,
                                        ClientIntervalBuilderDynamicDate intervalBuilderDynamicDate,
                                        IntervalBuilderFixedDate intervalBuilderFixedDate) {

        this.intervalBuilderDynamicLabel = intervalBuilderDynamicLabel;
        this.intervalBuilderDynamicDate = intervalBuilderDynamicDate;
        this.intervalBuilderFixedDate = intervalBuilderFixedDate;
    }

    public IntervalBuilder lookup(ColumnType columnType, GroupStrategy strategy) {
        if (ColumnType.LABEL.equals(columnType)) {
            if (GroupStrategy.FIXED.equals(strategy)) return intervalBuilderDynamicLabel;
            if (GroupStrategy.DYNAMIC.equals(strategy)) return intervalBuilderDynamicLabel;
        }
        if (ColumnType.DATE.equals(columnType)) {
            if (GroupStrategy.FIXED.equals(strategy)) return intervalBuilderFixedDate;
            if (GroupStrategy.DYNAMIC.equals(strategy)) return intervalBuilderDynamicDate;
            return intervalBuilderDynamicDate;
        }
        if (ColumnType.NUMBER.equals(columnType)) {
            return intervalBuilderDynamicLabel;
        }
        if (ColumnType.TEXT.equals(columnType)) {
            throw new IllegalArgumentException( CommonConstants.exc_text_columns_no_grouping());
        }
        return null;
    }

}
