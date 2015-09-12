/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.lisp.type;

public enum LispMessageEnum {
    MapRequest((byte) 1), //
    MapReply((byte) 2), //
    MapRegister((byte) 3), //
    MapNotify((byte) 4), //
    MapReferral((byte) 6), //
    Info((byte) 7), //
    EncapsulatedControlMessage((byte) 8);

    private byte value;

    private LispMessageEnum(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static byte getMaxValue() {
        byte max = 0;
        for (LispMessageEnum lme : LispMessageEnum.values()) {
            if (lme.getValue() > max) {
                max = lme.getValue();
            }
        }
        return max;
    }

    public static LispMessageEnum valueOf(byte i) {
        for (LispMessageEnum lme : LispMessageEnum.values()) {
            if (lme.getValue() == i) {
                return lme;
            }
        }
        return null;
    }
}
