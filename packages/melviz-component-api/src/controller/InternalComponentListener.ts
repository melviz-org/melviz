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

import { ComponentMessage } from "../message";
import { MelvizComponentController } from "./MelvizComponentController";

/*
 * Listener with methods that should not be exposed to components
 */
export interface InternalComponentDispatcher {
  /*
   * The component controller responsible to be the component's entry point to interact with Melviz
   */
  componentController: MelvizComponentController | undefined;

  /**
   * Starts waiting for messages to dispatch
   */
  init(): void;

  /*
   * Sends a message to Melviz
   */
  sendMessage(componentMessage: ComponentMessage): void;
}
