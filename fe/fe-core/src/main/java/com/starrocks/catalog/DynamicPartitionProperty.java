// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// This file is based on code available under the Apache license here:
//   https://github.com/apache/incubator-doris/blob/master/fe/fe-core/src/main/java/org/apache/doris/catalog/DynamicPartitionProperty.java

// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.starrocks.catalog;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.starrocks.analysis.TimestampArithmeticExpr.TimeUnit;
import com.starrocks.common.FeConstants;
import com.starrocks.common.util.DynamicPartitionUtil.StartOfDate;
import com.starrocks.common.util.TimeUtils;

import java.util.Map;
import java.util.TimeZone;

public class DynamicPartitionProperty {
    public static final String DYNAMIC_PARTITION_PROPERTY_PREFIX = "dynamic_partition";
    public static final String TIME_UNIT = "dynamic_partition.time_unit";
    public static final String START = "dynamic_partition.start";
    public static final String END = "dynamic_partition.end";
    public static final String PREFIX = "dynamic_partition.prefix";
    public static final String BUCKETS = "dynamic_partition.buckets";
    public static final String ENABLE = "dynamic_partition.enable";
    public static final String START_DAY_OF_WEEK = "dynamic_partition.start_day_of_week";
    public static final String START_DAY_OF_MONTH = "dynamic_partition.start_day_of_month";
    public static final String TIME_ZONE = "dynamic_partition.time_zone";
    public static final String REPLICATION_NUM = "dynamic_partition.replication_num";
    public static final String HISTORY_PARTITION_NUM = "dynamic_partition.history_partition_num";
    public static final int MIN_START_OFFSET = Integer.MIN_VALUE;
    public static final int DEFAULT_END_OFFSET = 0;
    public static final int NOT_SET_REPLICATION_NUM = -1;
    public static final int NOT_SET_HISTORY_PARTITION_NUM = 0;
    public static final String NOT_SET_PREFIX = "p";

    private final boolean exists;

    private boolean enabled;
    private String timeUnit;
    private int start;
    private int end;
    private String prefix;
    private int buckets;
    private StartOfDate startOfWeek;
    private StartOfDate startOfMonth;
    private TimeZone tz = TimeUtils.getSystemTimeZone();
    private int replicationNum;
    private int historyPartitionNum;
    public DynamicPartitionProperty(Map<String, String> properties) {
        if (properties != null && !properties.isEmpty()) {
            this.exists = true;
            this.enabled = Boolean.parseBoolean(properties.get(ENABLE));
            this.timeUnit = properties.get(TIME_UNIT);
            this.tz = TimeUtils.getOrSystemTimeZone(properties.get(TIME_ZONE));
            // In order to compatible dynamic add partition version
            this.start = Integer.parseInt(properties.getOrDefault(START, String.valueOf(MIN_START_OFFSET)));
            this.end = Integer.parseInt(properties.get(END));
            this.prefix = properties.getOrDefault(PREFIX, NOT_SET_PREFIX);
            this.buckets = Integer.parseInt(properties.getOrDefault(BUCKETS, "0"));
            this.replicationNum =
                    Integer.parseInt(properties.getOrDefault(REPLICATION_NUM, String.valueOf(NOT_SET_REPLICATION_NUM)));
            this.historyPartitionNum = Integer.parseInt(properties.getOrDefault(
                    HISTORY_PARTITION_NUM, String.valueOf(NOT_SET_HISTORY_PARTITION_NUM)));
            createStartOfs(properties);
        } else {
            this.exists = false;
        }
    }

    private void createStartOfs(Map<String, String> properties) {
        if (properties.containsKey(START_DAY_OF_WEEK)) {
            startOfWeek = new StartOfDate(-1, -1, Integer.parseInt(properties.get(START_DAY_OF_WEEK)));
        } else {
            // default:
            startOfWeek = new StartOfDate(-1, -1, 1 /* start from MONDAY */);
        }

        if (properties.containsKey(START_DAY_OF_MONTH)) {
            startOfMonth = new StartOfDate(-1, Integer.parseInt(properties.get(START_DAY_OF_MONTH)), -1);
        } else {
            // default:
            startOfMonth = new StartOfDate(-1, 1 /* 1st of month */, -1);
        }
    }

    public boolean isExists() {
        return exists;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getBuckets() {
        return buckets;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public StartOfDate getStartOfWeek() {
        return startOfWeek;
    }

    public StartOfDate getStartOfMonth() {
        return startOfMonth;
    }

    public String getStartOfInfo() {
        if (getTimeUnit().equalsIgnoreCase(TimeUnit.WEEK.toString())) {
            return startOfWeek.toDisplayInfo();
        } else if (getTimeUnit().equalsIgnoreCase(TimeUnit.MONTH.toString())) {
            return startOfMonth.toDisplayInfo();
        } else {
            return FeConstants.NULL_STRING;
        }
    }

    public TimeZone getTimeZone() {
        return tz;
    }

    public int getReplicationNum() {
        return replicationNum;
    }


    public int getHistoryPartitionNum() {
        return historyPartitionNum;
    }

    public Map<String, String> getProperties() {
        Map<String, String> properties = Maps.newHashMap();
        properties.put(ENABLE, String.valueOf(enabled));
        properties.put(TIME_UNIT, timeUnit);
        properties.put(TIME_ZONE, tz.getID());
        properties.put(START, String.valueOf(start));
        properties.put(END, String.valueOf(end));
        properties.put(PREFIX, prefix);
        if (buckets > 0) {
            properties.put(BUCKETS, String.valueOf(buckets));
        }
        properties.put(HISTORY_PARTITION_NUM, String.valueOf(historyPartitionNum));
        if (replicationNum != NOT_SET_REPLICATION_NUM) {
            properties.put(REPLICATION_NUM, String.valueOf(replicationNum));
        }
        if (getTimeUnit().equalsIgnoreCase(TimeUnit.WEEK.toString())) {
            properties.put(START_DAY_OF_WEEK, String.valueOf(startOfWeek.dayOfWeek));
        } else if (getTimeUnit().equalsIgnoreCase(TimeUnit.MONTH.toString())) {
            properties.put(START_DAY_OF_MONTH, String.valueOf(startOfMonth.day));
        }
        return properties;
    }

    @VisibleForTesting
    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public String toString() {
        String res = ",\n\"" + ENABLE + "\" = \"" + enabled + "\""
                + ",\n\"" + TIME_UNIT + "\" = \"" + timeUnit + "\""
                + ",\n\"" + TIME_ZONE + "\" = \"" + tz.getID() + "\""
                + ",\n\"" + START + "\" = \"" + start + "\""
                + ",\n\"" + END + "\" = \"" + end + "\""
                + ",\n\"" + PREFIX + "\" = \"" + prefix + "\"";
        if (buckets > 0) {
            res += ",\n\"" + BUCKETS + "\" = \"" + buckets + "\"";
        }
        res += ",\n\"" + HISTORY_PARTITION_NUM + "\" = \"" + historyPartitionNum + "\"";
        if (replicationNum != NOT_SET_REPLICATION_NUM) {
            res += ",\n\"" + REPLICATION_NUM + "\" = \"" + replicationNum + "\"";
        }
        if (getTimeUnit().equalsIgnoreCase(TimeUnit.WEEK.toString())) {
            res += ",\n\"" + START_DAY_OF_WEEK + "\" = \"" + startOfWeek.dayOfWeek + "\"";
        } else if (getTimeUnit().equalsIgnoreCase(TimeUnit.MONTH.toString())) {
            res += ",\n\"" + START_DAY_OF_MONTH + "\" = \"" + startOfMonth.day + "\"";
        }
        return res;
    }
}
