package org.opendaylight.lispflowmapping.interfaces.dao;

import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;

/**
 * The mapping service key in the DAO.
 */
public interface IMappingServiceKey {

    /**
     * @return The eid of the key
     */
    LispAddressContainer getEID();

    /**
     * @return The mask of the eid
     */
    int getMask();

}
