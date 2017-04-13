/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.mapcache.lisp;

import java.util.Set;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.interfaces.dao.SubKeys;
import org.opendaylight.lispflowmapping.interfaces.dao.Subscriber;
import org.opendaylight.lispflowmapping.lisp.type.MappingData;
import org.opendaylight.lispflowmapping.lisp.util.Constants;
import org.opendaylight.lispflowmapping.lisp.util.LispAddressStringifier;
import org.opendaylight.lispflowmapping.lisp.util.Stringifier;

public class LispMapCacheStringifier {
    public static String printKeys(ILispDAO dao) {
        final StringBuffer sb = new StringBuffer();
        sb.append("Keys\tValues\n");

        final IRowVisitor innerVisitor = (new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("\n" + key + "\t");
                }
                sb.append(valueKey + "=" + value + "\t");
                lastKey = key;
            }
        });

        dao.getAll(new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("\n" + key + "\t");
                }
                if (valueKey.equals(SubKeys.VNI)) {
                    sb.append(valueKey + "= { ");
                    ((ILispDAO)value).getAll(innerVisitor);
                    sb.append("}\t");
                } else {
                    sb.append(valueKey + "=" + value + "\t");
                }
                lastKey = key;
            }
        });
        sb.append("\n");
        return sb.toString();
    }

    public static String printFMCMappings(ILispDAO dao) {
        final StringBuffer sb = new StringBuffer();
        sb.append("Keys\tValues\n");
        dao.getAll(new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("\n" + key + "\t");
                }
                sb.append(valueKey + "=" + value + "\t");
                lastKey = key;
            }
        });
        sb.append("\n");
        return sb.toString();
    }

    public static String prettyPrintFMCMappings(ILispDAO dao) {
        final StringBuffer sb = new StringBuffer();
        dao.getAll(new IRowVisitor() {
            public void visitRow(Object keyId, String valueKey, Object value) {
                switch (valueKey) {
                    case SubKeys.RECORD:
                        MappingData md = (MappingData) value;
                        sb.append(Stringifier.getString(md.getRecord(), 2));
                        sb.append("\n");
                        break;
                    default:
                        break;
                }
            }
        });
        sb.append("\n");
        return sb.toString();
    }

    public static String printMTMCMappings(ILispDAO dao) {
        final StringBuffer sb = new StringBuffer();
        sb.append("Keys\tValues\n");
        final IRowVisitor innerVisitor = (new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("\n" + key + "\t");
                }
                sb.append(valueKey + "=" + value + "\t");
                lastKey = key;
            }
        });
        final IRowVisitor vniVisitor = (new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append(key + "\t");
                }
                if ((valueKey.equals(SubKeys.LCAF_SRCDST))) {
                    sb.append(valueKey + "= { ");
                    ((ILispDAO)value).getAll(innerVisitor);
                    sb.append("}\t");
                } else {
                    sb.append(valueKey + "=" + value + "\t");
                }
                lastKey = key;
            }
        });
        dao.getAll(new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("\n" + key + "\t");
                }
                if (valueKey.equals(SubKeys.VNI)) {
                    sb.append(valueKey + "= { ");
                    ((ILispDAO)value).getAll(vniVisitor);
                    sb.append("}\t");
                } else {
                    sb.append(valueKey + "=" + value + "\t");
                }
                lastKey = key;
            }
        });
        sb.append("\n");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static String prettyPrintMTMCMappings(ILispDAO dao) {
        final StringBuffer sb = new StringBuffer();

        final IRowVisitor mappingVisitor = (new IRowVisitor() {
            public void visitRow(Object keyId, String valueKey, Object value) {
                switch (valueKey) {
                    case SubKeys.RECORD:
                        MappingData md = (MappingData) value;
                        sb.append(Stringifier.getString(md.getRecord(), 2));
                        sb.append("\n");
                        break;
                    case SubKeys.SUBSCRIBERS:
                        Set<Subscriber> subscribers = (Set<Subscriber>) value;
                        sb.append(prettyPrintSubscriberSet(subscribers, 4));
                        sb.append("\n");
                        break;
                    case SubKeys.LCAF_SRCDST:
                        ((ILispDAO)value).getAll(this);
                        break;
                    default:
                        break;
                }
            }
        });

        dao.getAll(new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("Instance ID " + keyId + "\n");
                }
                if (valueKey.equals(SubKeys.VNI)) {
                    ((ILispDAO)value).getAll(mappingVisitor);
                }
                lastKey = key;
            }
        });
        return sb.toString();
    }

    public static String printSMCMappings(ILispDAO dao) {
        final StringBuffer sb = new StringBuffer();
        sb.append("Keys\tValues\n");

        final IRowVisitor innerVisitor = (new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("\n" + key + "\t");
                }
                sb.append(valueKey + "=" + value + "\t");
                lastKey = key;
            }
        });

        dao.getAll(new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("\n" + key + "\t");
                }
                if (valueKey.equals(SubKeys.VNI)) {
                    sb.append(valueKey + "= { ");
                    ((ILispDAO)value).getAll(innerVisitor);
                    sb.append("}\t");
                } else {
                    sb.append(valueKey + "=" + value + "\t");
                }
                lastKey = key;
            }
        });
        sb.append("\n");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static String prettyPrintSMCMappings(ILispDAO dao) {
        final StringBuffer sb = new StringBuffer();

        final IRowVisitor mappingVisitor = (new IRowVisitor() {
            public void visitRow(Object keyId, String valueKey, Object value) {
                switch (valueKey) {
                    case SubKeys.RECORD:
                        MappingData md = (MappingData) value;
                        sb.append(Stringifier.getString(md.getRecord(), 2));
                        sb.append("\n");
                        break;
                    case SubKeys.SUBSCRIBERS:
                        Set<Subscriber> subscribers = (Set<Subscriber>) value;
                        sb.append(prettyPrintSubscriberSet(subscribers, 4));
                        sb.append("\n");
                        break;
                    default:
                        break;
                }
            }
        });

        dao.getAll(new IRowVisitor() {
            String lastKey = "";

            public void visitRow(Object keyId, String valueKey, Object value) {
                String key = keyId.getClass().getSimpleName() + "#" + keyId;
                if (!lastKey.equals(key)) {
                    sb.append("Instance ID " + keyId + "\n");
                }
                if (valueKey.equals(SubKeys.VNI)) {
                    ((ILispDAO)value).getAll(mappingVisitor);
                }
                lastKey = key;
            }
        });
        return sb.toString();
    }

    /**
     * Given a Set of Subscriber objects, and the level of indentation, create a nicely formatted String to be added
     * to a map-cache print-out in a tabular form.
     *
     * @param subscribers the Set of Subscriber objects to be printed
     * @param indentation indentation level
     * @return the formatted String
     */
    public static String prettyPrintSubscriberSet(Set<Subscriber> subscribers, int indentation) {
        final String indent = new String(new char[indentation]).replace("\0", " ");

        StringBuilder sb = new StringBuilder(indent);
        sb.append("   -----------------------------------------------------------------\n");
        sb.append(indent);
        sb.append("-> Subscriber RLOC                                 Subscriber EID\n   ");
        sb.append(indent);
        boolean first = true;
        for (Subscriber subscriber : subscribers) {
            if (first) {
                first = false;
            } else {
                sb.append("\n   ");
                sb.append(indent);
            }
            String srcRloc = LispAddressStringifier.getString(subscriber.getSrcRloc());
            int padLen = Constants.INET6_ADDRSTRLEN + 2 - srcRloc.length();
            sb.append(srcRloc);
            sb.append(new String(new char[padLen]).replace("\0", " "));
            sb.append(LispAddressStringifier.getString(subscriber.getSrcEid()));
        }
        return sb.toString();
    }
}
