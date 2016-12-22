/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.lisp.type;

import com.google.common.base.Optional;
import java.util.Date;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.MappingRecordUtil;
import org.opendaylight.lispflowmapping.lisp.util.Stringifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.XtrId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;

/**
 * Wrapper class for MappingRecord with timestamp added.
 *
 * @author Lorand Jakab
 *
 */
public class MappingData {
    private MappingRecord record = null;
    private Date timestamp = null;
    private XtrId xtrId = null;
    private boolean mergeEnabled = false;

    public MappingData(MappingRecord record, Long timestamp) {
        this(record, new Date(timestamp));
    }

    public MappingData(MappingRecord record, Date timestamp) {
        setRecord(record);
        setTimestamp(timestamp);
    }

    public MappingData(MappingRecord record) {
        setRecord(record);
    }

    public synchronized MappingRecord getRecord() {
        return record;
    }

    public synchronized void setRecord(MappingRecord record) {
        this.record = record;
    }

    public synchronized Date getTimestamp() {
        return timestamp;
    }

    public synchronized void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public synchronized XtrId getXtrId() {
        return xtrId;
    }

    public synchronized void setXtrId(XtrId xtrId) {
        this.xtrId = xtrId;
    }

    public synchronized boolean isMergeEnabled() {
        return mergeEnabled;
    }

    public synchronized void setMergeEnabled(boolean mergeEnabled) {
        this.mergeEnabled = mergeEnabled;
    }

    public Optional<Boolean> isNegative() {
        if (record != null) {
            return Optional.of(MappingRecordUtil.isNegativeMapping(record));
        } else {
            return Optional.absent();
        }
    }

    public Optional<Boolean> isPositive() {
        if (record != null) {
            return Optional.of(MappingRecordUtil.isPositiveMapping(record));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MappingData [");

        sb.append("merge=");
        sb.append(mergeEnabled);

        if (xtrId != null) {
            sb.append(", xTR-ID=");
            sb.append(xtrId);
        }

        if (timestamp != null) {
            sb.append(", timestamp=");
            sb.append(timestamp);
        }

        if (record != null) {
            sb.append(", record=");
            sb.append(record);
        }

        return sb.append(']').toString();
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Merge: ");
        sb.append(mergeEnabled ? "on" : "off");

        if (xtrId != null) {
            sb.append(", xTR-ID: ");
            sb.append(LispAddressStringifier.getString(xtrId));
        }

        if (timestamp != null) {
            sb.append(", timestamp: ");
            sb.append(timestamp.toString());
        }

        if (record != null) {
            sb.append("\n");
            sb.append(Stringifier.getString(record));
            sb.append("\n");
        }

        return sb.append(']').toString();
    }
}
