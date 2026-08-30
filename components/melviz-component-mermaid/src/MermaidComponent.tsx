/*
 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import * as React from "react";
import { useEffect, useState } from "react";
import { ComponentController, DataSet } from "@melviz/component-api";
import { MermaidDiagram } from "./MermaidDiagram";

const DEFINITION_PARAM = "definition";

const MISSING_PARAM_MSG = "You must provide the mermaid diagram definition using the parameter 'definition'.";

export function MermaidComponent(props: { controller: ComponentController }) {
  const [definition, setDefinition] = useState<string>();

  useEffect(() => {
    const updateDefinition = (params: Map<string, any>) => {
      const def = params.get(DEFINITION_PARAM);
      if (!def) {
        props.controller.requireConfigurationFix(MISSING_PARAM_MSG);
        setDefinition(undefined);
        return;
      }
      props.controller.configurationOk();
      setDefinition(def);
    };

    props.controller.setOnInit((params: Map<string, any>) => updateDefinition(params));
    props.controller.setOnDataSet((_ds: DataSet, params: Map<string, any> = new Map<string, any>()) => {
      // The mermaid definition can be updated when the dataset changes
      updateDefinition(params);
    });
  }, [props.controller]);

  return definition ? <MermaidDiagram definition={definition} /> : <em>{MISSING_PARAM_MSG}</em>;
}
