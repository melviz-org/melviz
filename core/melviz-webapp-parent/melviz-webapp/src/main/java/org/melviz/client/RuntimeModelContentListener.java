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
package org.melviz.client;

import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import elemental2.dom.DomGlobal;
import elemental2.dom.MessageEvent;
import elemental2.dom.Window;
import jsinterop.base.Js;
import org.melviz.client.screens.Router;
import org.melviz.displayer.external.ExternalComponentMessage;

@ApplicationScoped
public class RuntimeModelContentListener {

    private static final String READY = "ready";

    @Inject
    RuntimeCommunication runtimeCommunication;

    @Inject
    Router routerScreen;

    public void start(Consumer<String> contentConsumer) {
        if (!hasEnvelope()) {
            DomGlobal.window.addEventListener("message", evt -> {
                MessageEvent<Object> message = Js.cast(evt);

                if (!READY.equals(message.data) && !(message.data instanceof ExternalComponentMessage)) {
                    contentConsumer.accept((String) message.data);
                    DomGlobal.console.log("Dashboard Updated");
                }

            });
            if (DomGlobal.window.parent != null) {
                DomGlobal.window.parent.postMessage(READY, Window.PostMessageTargetOriginOrOptionsUnionType.of("*"));
            }
        }
    }

    private boolean hasEnvelope() {
        return DomGlobal.document.getElementById("envelope-app") != null;
    }


}
