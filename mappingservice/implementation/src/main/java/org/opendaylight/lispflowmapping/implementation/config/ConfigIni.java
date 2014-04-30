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
            if (!str.trim().equalsIgnoreCase("false")) {
                this.mappingOverwrite = true;
            } else {
                this.mappingOverwrite = false;
            }
        }
    }

    private void initSmr() {
        String str = System.getProperty("lisp.smr");
        if (str != null) {
            if (!str.trim().equalsIgnoreCase("true")) {
                this.mappingOverwrite = false;
            } else {
                this.smr = true;
            }
        }
    }

    public boolean mappingOverwriteIsSet() {
        return mappingOverwrite;
    }

    public boolean smrIsSet() {
        return smr;
    }
}
