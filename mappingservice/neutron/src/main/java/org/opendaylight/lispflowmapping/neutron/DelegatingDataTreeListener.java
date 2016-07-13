package org.opendaylight.lispflowmapping.neutron;

import com.google.common.base.Preconditions;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegatingDataTreeListener<T extends DataObject> implements ClusteredDataTreeChangeListener<T>,
        AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DelegatingDataTreeListener.class);
    final private DataProcessor<T> dataProcessor;
    private ListenerRegistration<ClusteredDataTreeChangeListener<T>> dataTreeChangeListenerRegistration;

    public DelegatingDataTreeListener(DataProcessor<T> dataProcessor, DataBroker dataBroker, DataTreeIdentifier<T>
            dataTreeIdentifier) {
        Preconditions.checkNotNull(dataBroker, "Can not instantiate Listener! Broker is null!");
        Preconditions.checkNotNull(dataTreeIdentifier, "DataTreeIndentifier can not be null!");
        this.dataProcessor = dataProcessor;
        dataTreeChangeListenerRegistration = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier ,this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<T>> changes) {
        for (DataTreeModification<T> change : changes) {
            DataObjectModification<T> mod = change.getRootNode();

            if (mod.getModificationType() == DataObjectModification.ModificationType.WRITE) {
                T object = mod.getDataAfter();
                dataProcessor.create(object);
                LOG.info(object.toString() + " created.");
            } else if (mod.getModificationType() == DataObjectModification.ModificationType.DELETE) {
                T object = mod.getDataBefore();
                dataProcessor.delete(object);
                LOG.info(object.toString() + " removed.");
            } else {
                T object = mod.getDataAfter();
                dataProcessor.update(object);
                LOG.info(object.toString() + " updated.");
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (dataTreeChangeListenerRegistration != null) {
            dataTreeChangeListenerRegistration.close();
            dataTreeChangeListenerRegistration = null;

            LOG.info(this.toString() + " closed");
        }
    }
}
