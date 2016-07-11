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
        AutoCloseable{
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
            T object = mod.getDataAfter();

            if (mod.getModificationType() == DataObjectModification.ModificationType.WRITE) {
                dataProcessor.create(object);
                LOG.info(object.getClass().getSimpleName() + " created.");
            } else if (mod.getModificationType() == DataObjectModification.ModificationType.DELETE) {
                dataProcessor.delete(object);
                LOG.info(object.getClass().getSimpleName() + " removed.");
            } else {
                dataProcessor.update(object);
                LOG.info(object.getClass().getSimpleName() + " updated.");
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (dataTreeChangeListenerRegistration != null) {
            dataTreeChangeListenerRegistration.close();
            dataTreeChangeListenerRegistration = null;

            LOG.info(this.getClass().getSimpleName() + " closed");
        }
    }
}
