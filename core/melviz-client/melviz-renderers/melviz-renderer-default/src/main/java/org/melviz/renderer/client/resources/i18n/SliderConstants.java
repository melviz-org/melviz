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

package org.melviz.renderer.client.resources.i18n;

public class SliderConstants {

	private SliderConstants() {

	}

	public static String sliderTooltip(String from, String to) {
		return "Selects values from " + from + " to " + to;
	}

	public static String sliderColumnName() {
		return "Column name";
	}

	public static String textColumnsNotSupported() {
		return "Columns not supported";
	}
}
