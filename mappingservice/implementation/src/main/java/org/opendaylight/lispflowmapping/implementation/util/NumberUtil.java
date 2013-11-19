package org.opendaylight.lispflowmapping.implementation.util;

public class NumberUtil {

    public static int asInt(Integer number) {
        if (number != null) {
            return number;
        }
        return 0;
    }

    public static byte asByte(Byte number) {
        if (number != null) {
            return number;
        }
        return 0;
    }

    public static short asShort(Short number) {
        if (number != null) {
            return number;
        }
        return 0;
    }

    public static long asLong(Long number) {
        if (number != null) {
            return number;
        }
        return 0;
    }

}
