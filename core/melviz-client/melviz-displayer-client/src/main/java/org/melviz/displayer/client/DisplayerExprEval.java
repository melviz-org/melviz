/*
 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.melviz.displayer.client;

import elemental2.core.Global;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.melviz.common.client.StringUtils;
import org.melviz.displayer.client.resources.i18n.DisplayerConstants;

public class DisplayerExprEval implements AbstractDisplayer.ExpressionEval {

	public static final String[] _jsMalicious = { "document.", "window.", "alert(", "eval(", ".innerHTML" };

	AbstractDisplayer presenter = null;

	public DisplayerExprEval(AbstractDisplayer presenter) {
		this.presenter = presenter;
	}

	@Override
	public String evalExpression(String val, String expr) {
		if (StringUtils.isBlank(expr) || "value".equals(expr)) {
			return val;
		}
		for (String keyword : _jsMalicious) {
			if (expr.contains(keyword)) {
				presenter.handleError(DisplayerConstants.displayer_keyword_not_allowed(expr));
				throw new RuntimeException(DisplayerConstants.displayer_keyword_not_allowed(expr));
			}
		}
		try {
			return _evalExpression(val, expr);
		} catch (Exception e) {
			presenter.handleError(DisplayerConstants.displayer_expr_invalid_syntax(expr), e);
			throw new RuntimeException(DisplayerConstants.displayer_expr_invalid_syntax(expr));
		}
	}

	@JsMethod
	protected String _evalExpression(String value, String expr) {		
		JsWindowWrapper.value = value;
		return Global.eval(expr) + "";
	}
	
	@JsType(isNative=true, namespace=JsPackage.GLOBAL, name="window")
	public static class JsWindowWrapper {
	    public static String value;
	}
}
