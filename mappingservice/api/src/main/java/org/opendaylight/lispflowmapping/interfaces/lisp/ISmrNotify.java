package org.opendaylight.lispflowmapping.interfaces.lisp;

import org.opendaylight.lispflowmapping.interfaces.dao.SmrEvent;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;

/**
 * This interface is used to notify of a received SMR-invoked request.
 */
public interface ISmrNotify {

    /**
     * This method is fired when a new smr-invoked request is received.
     *
     * @param event This object carries the nonce of a smr-invoked request and the subscriber's {@link Address}
     *              that sent this request.
     */
    void onSmrInvokedReceived(SmrEvent event);
}
