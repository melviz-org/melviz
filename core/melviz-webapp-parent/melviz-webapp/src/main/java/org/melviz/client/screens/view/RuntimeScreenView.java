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
package org.melviz.client.screens.view;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.dom.elemental2.Elemental2DomUtil;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.melviz.client.RuntimeCommunication;
import org.melviz.client.navigation.widget.NavTilesWidget;
import org.melviz.client.screens.RuntimeScreen;
import org.melviz.navigation.NavTree;

@Dependent
@Templated
public class RuntimeScreenView implements RuntimeScreen.View {

    @Inject
    @DataField
    HTMLDivElement runtimePage;

    @Inject
    RuntimeCommunication runtimeCommunication;

    @Inject
    NavTilesWidget tilesWidget;

    @Inject
    Elemental2DomUtil elementalUtil;

    @Override
    public HTMLElement getElement() {
        return runtimePage;
    }

    @Override
    public void init(RuntimeScreen presenter) {
        runtimePage.appendChild(tilesWidget.getElement());
    }

    @Override
    public void loadNavTree(NavTree navTree, boolean keepHistory) {
        tilesWidget.clearSelectedItem();
        tilesWidget.show(navTree.getRootItems(), !keepHistory);
    }

    @Override
    public void setContent(HTMLElement element) {
        elementalUtil.removeAllElementChildren(runtimePage);
        runtimePage.appendChild(element);
        
    }

}
