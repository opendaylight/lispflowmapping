/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.implementation.config;

public class ConfigIni {
    private boolean mappingOverwrite;
    private boolean smr;

    public ConfigIni() {
        initMappingOverwrite();
        initSmr();
    }

    private void initMappingOverwrite() {
        String str = System.getProperty("lisp.mappingOverwrite");
        if (str != null) {
            if (str.trim().equalsIgnoreCase("false")) {
                this.mappingOverwrite = false;
                return;
            }
        }
        this.mappingOverwrite = true;
    }

    private void initSmr() {
        String str = System.getProperty("lisp.smr");
        if (str != null) {
            if (str.trim().equalsIgnoreCase("true")) {
                this.smr = true;
                return;
            }
        }
        this.smr = false;
    }

    public boolean mappingOverwriteIsSet() {
        return mappingOverwrite;
    }

    public boolean smrIsSet() {
        return smr;
    }
}
