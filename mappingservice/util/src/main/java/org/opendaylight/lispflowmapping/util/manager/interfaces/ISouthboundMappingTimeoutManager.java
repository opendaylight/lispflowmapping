package org.opendaylight.lispflowmapping.util.manager.interfaces;

/**
 * Created by sheikahm on 9/21/16.
 */
public interface ISouthboundMappingTimeoutManager {

    int addMapping(Object mappingRecordReference);

    int refreshMapping(Object mappingRecrdReference);

    void cleanTimeoutMapping();
}
