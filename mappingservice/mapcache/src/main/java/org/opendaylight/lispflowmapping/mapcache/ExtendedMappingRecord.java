/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.mapcache;

import java.util.Date;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lfm.lisp.proto.rev151105.mapping.record.container.MappingRecord;

/**
 * Wrapper class for MappingRecord with timestamp added.
 *
 * @author Lorand Jakab
 *
 */
public class ExtendedMappingRecord {
    private MappingRecord record;
    private Date timestamp;

    public ExtendedMappingRecord(MappingRecord record, Date timestamp) {
        setRecord(record);
        setTimestamp(timestamp);
    }

    public ExtendedMappingRecord(MappingRecord record) {
        this(record, null);
    }

    public MappingRecord getRecord() {
        return record;
    }

    public void setRecord(MappingRecord record) {
        this.record = record;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
