/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.lisp.address;



public class ReencapHop extends LispAddress{

    private LispAddress hop;
    private short resv3;
    private boolean lookup;
    private boolean RLOCProbe;
    private boolean strict;

    public ReencapHop(LispAddress hop, short resv3, boolean lookup, boolean RLOCProbe, boolean strict) {
        super(hop.getAfi());
        this.hop = hop;
        this.resv3 = resv3;
        this.lookup = lookup;
        this.RLOCProbe = RLOCProbe;
        this.strict = strict;
    }

    public LispAddress getHop() {
        return hop;
    }

    public void setHop(LispAddress hop) {
        this.hop = hop;
    }

    public short getResv3() {
        return resv3;
    }

    public void setResv3(short resv3) {
        this.resv3 = resv3;
    }

    public boolean isLookup() {
        return lookup;
    }

    public void setLookup(boolean lookup) {
        this.lookup = lookup;
    }

    public boolean isRLOCProbe() {
        return RLOCProbe;
    }

    public void setRLOCProbe(boolean rLOCProbe) {
        RLOCProbe = rLOCProbe;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    @Override
    public String toString() {
        return "ReencapHop#[hop=" + hop + "]" + super.toString();
    }




}
