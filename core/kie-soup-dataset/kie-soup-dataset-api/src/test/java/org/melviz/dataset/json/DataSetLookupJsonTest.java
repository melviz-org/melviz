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
package org.melviz.dataset.json;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.melviz.dataset.DataSetLookup;
import org.melviz.dataset.DataSetLookupFactory;
import org.melviz.dataset.filter.CoreFunctionFilter;
import org.melviz.dataset.filter.CoreFunctionType;
import org.melviz.dataset.filter.LogicalExprFilter;
import org.melviz.dataset.filter.LogicalExprType;
import org.melviz.dataset.group.AggregateFunctionType;
import org.melviz.dataset.group.DateIntervalType;
import org.melviz.dataset.sort.SortOrder;
import org.melviz.json.JsonArray;
import org.melviz.json.JsonBoolean;
import org.melviz.json.JsonFactory;
import org.melviz.json.JsonNull;
import org.melviz.json.JsonNumber;
import org.melviz.json.JsonObject;
import org.melviz.json.JsonString;
import org.melviz.json.JsonValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.melviz.dataset.filter.FilterFactory.OR;
import static org.melviz.dataset.filter.FilterFactory.between;
import static org.melviz.dataset.filter.FilterFactory.greaterOrEqualsTo;
import static org.melviz.dataset.filter.FilterFactory.greaterThan;
import static org.melviz.dataset.filter.FilterFactory.isNull;
import static org.melviz.dataset.filter.FilterFactory.notEqualsTo;
import static org.melviz.dataset.json.DataSetLookupJSONMarshaller.COLUMN;
import static org.melviz.dataset.json.DataSetLookupJSONMarshaller.FUNCTION;
import static org.melviz.dataset.json.DataSetLookupJSONMarshaller.FUNCTION_ARGS;
import static org.melviz.dataset.json.DataSetLookupJSONMarshaller.FUNCTION_LABEL_VALUE;

public class DataSetLookupJsonTest {

    DataSetLookupJSONMarshaller jsonMarshaller = DataSetLookupJSONMarshaller.get();

    @Test
    public void testDataSetLookupMarshalling() {
        DataSetLookup original = DataSetLookupFactory.newDataSetLookupBuilder()
                .dataset("mydataset")
                .filter(OR(notEqualsTo("department", "IT"), greaterOrEqualsTo("amount", 100d)))
                .filter("department", notEqualsTo("IT"))
                .filter("amount", between(100d, 200d))
                .filter("date", greaterThan(jsonMarshaller.parseDate("2018-01-01 00:00:00")))
                .filter("country", isNull())
                .group("department").select("Services")
                .group("date", "year").dynamic(DateIntervalType.YEAR, true)
                .column("date")
                .column("amount", AggregateFunctionType.SUM, "total")
                .sort("date", SortOrder.ASCENDING)
                .buildLookup();

        JsonObject _jsonObj = jsonMarshaller.toJson(original);
        assertNotNull(_jsonObj.toString());

        DataSetLookup unmarshalled = jsonMarshaller.fromJson(_jsonObj);
        assertEquals(unmarshalled, original);
    }

    @Test
    public void testDateFormat() {
        String d1 = "2020-11-10 23:59:59";
        Date d2 = jsonMarshaller.parseDate(d1);
        String d3 = jsonMarshaller.formatDate(d2);
        assertEquals(d1, d3);

        d1 = "2020-01-01 00:00:00";
        d2 = jsonMarshaller.parseDate(d1);
        d3 = jsonMarshaller.formatDate(d2);
        assertEquals(d1, d3);
    }

    @Test
    public void test_DASHBUILDE_83() {
        JsonValue jsonNull = jsonMarshaller.formatValue(null);
        JsonValue jsonBoolean = jsonMarshaller.formatValue(true);
        JsonValue jsonNumber = jsonMarshaller.formatValue(100d);
        JsonValue jsonDate = jsonMarshaller.formatValue(new Date());
        JsonValue jsonString = jsonMarshaller.formatValue("string");

        assertTrue(jsonNull instanceof JsonNull);
        assertTrue(jsonBoolean instanceof JsonBoolean);
        assertTrue(jsonNumber instanceof JsonNumber);
        assertTrue(jsonDate instanceof JsonString);
        assertTrue(jsonString instanceof JsonString);
    }

    @Test
    public void testFormatColumnFilter() {
        CoreFunctionFilter columnFilter = new CoreFunctionFilter();
        columnFilter.setColumnId("test_id");
        columnFilter.setLabelValue("test_label_value");
        columnFilter.setType(CoreFunctionType.EQUALS_TO);
        columnFilter.setParameters(Arrays.asList("param0", "param1", "param2"));
        JsonObject columnFilterJsonObject = jsonMarshaller.formatColumnFilter(columnFilter);
        assertColumnFilterFromJsonObject(columnFilterJsonObject);

        LogicalExprFilter logicalExprFilter = new LogicalExprFilter();
        logicalExprFilter.setColumnId("test_id");
        logicalExprFilter.setLogicalOperator(LogicalExprType.AND);
        logicalExprFilter.setLogicalTerms(Arrays.asList(columnFilter));
        JsonObject logicalExprFilterJsonObject = jsonMarshaller.formatColumnFilter(logicalExprFilter);
        assertEquals("test_id", logicalExprFilterJsonObject.getString(COLUMN));
        assertEquals("AND", logicalExprFilterJsonObject.getString(FUNCTION));
        assertColumnFilterFromJsonObject((JsonObject) logicalExprFilterJsonObject.getArray(FUNCTION_ARGS).get(0));
    }

    private void assertColumnFilterFromJsonObject(JsonObject columnFilterJsonObject) {
        assertEquals("test_label_value", columnFilterJsonObject.getString(FUNCTION_LABEL_VALUE));
        assertEquals("test_id", columnFilterJsonObject.getString(COLUMN));
        assertEquals("EQUALS_TO", columnFilterJsonObject.getString(FUNCTION));
        JsonArray columnFilterJsonArray = columnFilterJsonObject.getArray(FUNCTION_ARGS);
        for (int i = 0; i < columnFilterJsonArray.length(); i++) {
            assertEquals("param" + i, columnFilterJsonArray.get(i).asString());
        }
    }

    @Test
    public void testParseColumnFilter() {
        JsonFactory jsonFactory = new JsonFactory();
        JsonObject coreFunctionFilterJsonObject = jsonFactory.parse("{\n" +
                                                                            "  \"column\": \"test_id\",\n" +
                                                                            "  \"function\": \"EQUALS_TO\",\n" +
                                                                            "  \"labelValue\": \"test_label_value\",\n" +
                                                                            "  \"args\": [\n" +
                                                                            "    \"param0\",\n" +
                                                                            "    \"param1\",\n" +
                                                                            "    \"param2\"\n" +
                                                                            "  ]\n" +
                                                                            "}");
        CoreFunctionFilter coreFunctionFilter = (CoreFunctionFilter) jsonMarshaller.parseColumnFilter(coreFunctionFilterJsonObject);
        assertColumnFilterFromColumnFilter(coreFunctionFilter);

        JsonObject logicalExprFilterJsonObject = jsonFactory.parse("{\n" +
                                                                           "  \"column\": \"test_id\",\n" +
                                                                           "  \"function\": \"AND\",\n" +
                                                                           "  \"args\": [\n" +
                                                                           "    {\n" +
                                                                           "      \"column\": \"test_id\",\n" +
                                                                           "      \"function\": \"EQUALS_TO\",\n" +
                                                                           "      \"labelValue\": \"test_label_value\",\n" +
                                                                           "      \"args\": [\n" +
                                                                           "        \"param0\",\n" +
                                                                           "        \"param1\",\n" +
                                                                           "        \"param2\"\n" +
                                                                           "      ]\n" +
                                                                           "    }\n" +
                                                                           "  ]\n" +
                                                                           "}");

        LogicalExprFilter logicalExprFilter = (LogicalExprFilter) jsonMarshaller.parseColumnFilter(logicalExprFilterJsonObject);
        assertEquals("test_id", logicalExprFilter.getColumnId());
        assertEquals("AND", logicalExprFilter.getLogicalOperator().toString());

        logicalExprFilter.getLogicalTerms().forEach(object -> {
            assertColumnFilterFromColumnFilter((CoreFunctionFilter) object);
        });
    }

    private void assertColumnFilterFromColumnFilter(CoreFunctionFilter coreFunctionFilter) {
        assertEquals("test_label_value", coreFunctionFilter.getLabelValue());
        assertEquals("test_id", coreFunctionFilter.getColumnId());
        assertEquals("EQUALS_TO", coreFunctionFilter.getType().toString());
        List<String> params = coreFunctionFilter.getParameters();
        for (int i = 0; i < params.size(); i++) {
            assertEquals("param" + i, params.get(i));
        }
    }
}
