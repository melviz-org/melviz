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
import { useCallback, useEffect, useRef, useState } from "react";
import mermaid from "mermaid";

export interface MermaidDiagramProps {
  definition: string;
}

let initialized = false;
let idCounter = 0;

const nextId = () => `mermaid-svg-${++idCounter}`;

/**
 * Renders a Mermaid diagram from its textual definition.
 *
 * The diagram is rendered with `mermaid.render` into an SVG that is injected
 * into the component container.
 */
export function MermaidDiagram(props: MermaidDiagramProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [error, setError] = useState<string>();

  const renderDiagram = useCallback(async () => {
    const container = containerRef.current;
    if (!container) {
      return;
    }
    if (!initialized) {
      mermaid.initialize({ startOnLoad: false, securityLevel: "loose" });
      initialized = true;
    }
    try {
      const id = nextId();
      const { svg } = await mermaid.render(id, props.definition);
      container.innerHTML = svg;
      const svgEl = container.querySelector("svg");
      if (svgEl) {
        svgEl.setAttribute("style", "max-width: 100%; height: auto;");
      }
      setError(undefined);
    } catch (e: any) {
      console.error("Error rendering mermaid diagram", e);
      setError(e?.message ? String(e.message) : String(e));
    }
  }, [props.definition]);

  useEffect(() => {
    renderDiagram();
  }, [renderDiagram]);

  return <div ref={containerRef}>{error ? <em>{error}</em> : null}</div>;
}
