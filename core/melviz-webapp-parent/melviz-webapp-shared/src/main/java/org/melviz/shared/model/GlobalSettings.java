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
package org.melviz.shared.model;

import java.util.Optional;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.melviz.dataset.def.ExternalDataSetDef;
import org.melviz.displayer.DisplayerSettings;
import org.melviz.displayer.Mode;

@Portable
public class GlobalSettings {

    private Mode mode = Mode.LIGHT;
    private DisplayerSettings settings;
    private ExternalDataSetDef def;
    private boolean allowUrlProperties;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public DisplayerSettings getSettings() {
        return settings;
    }

    public void setSettings(DisplayerSettings settings) {
        this.settings = settings;
    }

    public boolean isAllowUrlProperties() {
        return allowUrlProperties;
    }

    public void setAllowUrlProperties(boolean allowUrlProperties) {
        this.allowUrlProperties = allowUrlProperties;
    }

    public Optional<ExternalDataSetDef> getDataSetDef() {
        return Optional.ofNullable(def);
    }

    public void setDataSetDef(ExternalDataSetDef def) {
        this.def = def;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        GlobalSettings that = (GlobalSettings) obj;
        if (allowUrlProperties != that.allowUrlProperties) return false;
        if (mode != that.mode) return false;
        if (settings != null ? !settings.equals(that.settings) : that.settings != null) return false;
        return def != null ? def.equals(that.def) : that.def == null;
    }

    @Override
    public int hashCode() {
        int result = mode != null ? mode.hashCode() : 0;
        result = 31 * result + (settings != null ? settings.hashCode() : 0);
        result = 31 * result + (def != null ? def.hashCode() : 0);
        result = 31 * result + (allowUrlProperties ? 1 : 0);
        return result;
    }

}
