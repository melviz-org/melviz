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

package org.melviz.displayer.client.widgets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.melviz.displayer.client.component.ExternalComponentDispatcher;
import org.melviz.displayer.client.widgets.ExternalComponentPresenter.View;
import org.melviz.displayer.external.ExternalComponentMessage;
import org.melviz.displayer.external.ExternalComponentMessageHelper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ExternalComponentPresenterTest {

    @Mock
    View view;

    @Mock
    ExternalComponentDispatcher dispatcher;

    @Mock
    ExternalComponentMessageHelper messageHelper;

    @InjectMocks
    ExternalComponentPresenter externalComponentPresenter;

    @Test
    public void testSendMessage() {
        ExternalComponentMessage message = new ExternalComponentMessage();

        externalComponentPresenter.sendMessage(message);

        verify(messageHelper).withId(eq(message), eq(externalComponentPresenter.getId()));
        verify(view).postMessage(eq(message));
    }

    @Test
    public void testBuildUrlWithCustomHostTest() {
        String expectedUrl = "http://custom.com/partition/mycomp/index.html";
        externalComponentPresenter.withComponentBaseUrlIdAndPartition("http://custom.com/", "myComp", "partition");
        verify(view).setComponentURL(eq(expectedUrl));

    }

}
