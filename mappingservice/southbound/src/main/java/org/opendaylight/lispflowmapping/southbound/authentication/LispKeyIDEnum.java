package org.opendaylight.lispflowmapping.southbound.authentication;


public enum LispKeyIDEnum {
	NONE(0, LispNoAuthentication.class), //
    SHA1(1, LispSHA1Authentication.class), //
    SHA256(2, LispSHA256Authentication.class), //
    UNKNOWN(-1, null);
	
	private short keyID;
    private Class<? extends ILispAuthentication> lispAuthenticationClass;

    private LispKeyIDEnum(int keyID, Class<? extends ILispAuthentication> lispAuthenticationClass) {
        this.keyID = (short) keyID;
        this.lispAuthenticationClass = lispAuthenticationClass;
    }

    public Class<? extends ILispAuthentication> getLispAddressClass() {
        return lispAuthenticationClass;
    }

    public short getKeyID() {
        return keyID;
    }

    public static LispKeyIDEnum valueOf(short keyID) {
        for (LispKeyIDEnum val : values()) {
            if (val.getKeyID() == keyID) {
                return val;
            }
        }
        return UNKNOWN;
    }
}
