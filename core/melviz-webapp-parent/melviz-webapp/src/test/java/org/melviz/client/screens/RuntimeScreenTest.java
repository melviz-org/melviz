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

package org.melviz.client.screens;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.melviz.client.navigation.plugin.PerspectivePluginManager;
import org.melviz.client.place.PlaceManager;
import org.melviz.shared.model.RuntimeModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RuntimeScreenTest {

	@Mock
	PlaceManager placeManager;

	@Mock
	RuntimeModel runtimeModel;

	@Mock
	RuntimeScreen.View view;

	@Mock
	PerspectivePluginManager pluginManager;

	@InjectMocks
	RuntimeScreen runtimeScreen;

	@Test
	public void testGoToIndexWithIndexPage() {
		String randomPage = "randomPage";
		List<LayoutTemplate> templates = Arrays.asList(new LayoutTemplate(randomPage),
				new LayoutTemplate(RuntimeScreen.INDEX_PAGE_NAME));

		runtimeScreen.goToIndex(templates);

		verify(pluginManager).buildPerspectiveWidget(eq(RuntimeScreen.INDEX_PAGE_NAME), any());
		verify(pluginManager, times(0)).buildPerspectiveWidget(eq(randomPage), any());
	}

	@Test
	public void testGoToIndexWithSinglePage() {
		String randomPage = "randomPage";
		List<LayoutTemplate> templates = Arrays.asList(new LayoutTemplate(randomPage));

		runtimeScreen.goToIndex(templates);

		verify(pluginManager).buildPerspectiveWidget(eq(randomPage), any());
	}

	@Test
	public void testGoToIndexWithoutIndex() {
		List<LayoutTemplate> templates = Arrays.asList(new LayoutTemplate("page1"), new LayoutTemplate("page2"));
		runtimeScreen.goToIndex(templates);
		verify(placeManager, times(0)).goTo(anyString());
	}

}
