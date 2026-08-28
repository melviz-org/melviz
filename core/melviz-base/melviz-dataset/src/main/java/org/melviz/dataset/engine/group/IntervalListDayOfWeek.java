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
package org.melviz.dataset.engine.group;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.melviz.dataset.date.DayOfWeek;
import org.melviz.dataset.group.ColumnGroup;
import org.melviz.dataset.group.Interval;

/**
 * List of the 7-days intervals present in a week.
 */
public class IntervalListDayOfWeek extends IntervalList {

    protected Map<Integer,Interval> intervalMap;

    public IntervalListDayOfWeek(ColumnGroup columnGroup) {
        super(columnGroup);
        intervalMap = new HashMap<Integer, Interval>();

        DayOfWeek firstDay = columnGroup.getFirstDayOfWeek();
        int index = firstDay.getIndex();
        DayOfWeek[] array = DayOfWeek.getAll();

        for (int i = 0; i < array.length; i++) {
            DayOfWeek dayOfWeek = array[index-1];
            Interval interval = new Interval(Integer.toString(dayOfWeek.getIndex()), i);
            interval.setType(columnGroup.getIntervalSize());
            this.add(interval);

            intervalMap.put(index-1, interval);
            index = DayOfWeek.nextIndex(index);
        }
    }

    public Interval locateInterval(Object value) {
        Date d = (Date) value;
        return intervalMap.get(d.getDay());
    }

    /**
     * Creates an independent deep copy of this interval list. The copy holds
     * new {@link Interval} objects with the same names, positions, types,
     * min/max values and indexed rows, in the same list order.
     * @return A deep copy of this interval list.
     */
    public IntervalListDayOfWeek clone() {
        IntervalListDayOfWeek copy = new IntervalListDayOfWeek(columnGroup);
        copy.intervalType = intervalType;
        copy.minValue = minValue;
        copy.maxValue = maxValue;
        for (int i = 0; i < this.size(); i++) {
            Interval src = this.get(i);
            Interval dst = copy.get(i);
            dst.setName(src.getName());
            dst.setIndex(src.getIndex());
            dst.setType(src.getType());
            dst.setMinValue(src.getMinValue());
            dst.setMaxValue(src.getMaxValue());
            dst.getRows().addAll(src.getRows());
        }
        Map<Integer, Interval> map = new HashMap<Integer, Interval>();
        for (Map.Entry<Integer, Interval> entry : intervalMap.entrySet()) {
            map.put(entry.getKey(), copy.get(this.indexOf(entry.getValue())));
        }
        copy.intervalMap = map;
        return copy;
    }
}
