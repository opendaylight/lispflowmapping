package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.lfm.ncc.provider.impl.RpcRegistry;
import org.opendaylight.yangtools.yang.binding.Augmentation;


/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl} instances.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl
 */
public class LfmNccProviderImplBuilder {

    private RpcRegistry _rpcRegistry;

    private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>> augmentation = new HashMap<>();

    public LfmNccProviderImplBuilder() {
    } 
    

    public LfmNccProviderImplBuilder(LfmNccProviderImpl base) {
        this._rpcRegistry = base.getRpcRegistry();
        if (base instanceof LfmNccProviderImplImpl) {
            LfmNccProviderImplImpl _impl = (LfmNccProviderImplImpl) base;
            this.augmentation = new HashMap<>(_impl.augmentation);
        }
    }


    public RpcRegistry getRpcRegistry() {
        return _rpcRegistry;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public LfmNccProviderImplBuilder setRpcRegistry(RpcRegistry value) {
        this._rpcRegistry = value;
        return this;
    }
    
    public LfmNccProviderImplBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl> augmentation) {
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public LfmNccProviderImpl build() {
        return new LfmNccProviderImplImpl(this);
    }

    private static final class LfmNccProviderImplImpl implements LfmNccProviderImpl {

        public java.lang.Class<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl.class;
        }

        private final RpcRegistry _rpcRegistry;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>> augmentation = new HashMap<>();

        private LfmNccProviderImplImpl(LfmNccProviderImplBuilder base) {
            this._rpcRegistry = base.getRpcRegistry();
                switch (base.augmentation.size()) {
                case 0:
                    this.augmentation = Collections.emptyMap();
                    break;
                    case 1:
                        final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>> e = base.augmentation.entrySet().iterator().next();
                        this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>>singletonMap(e.getKey(), e.getValue());       
                    break;
                default :
                    this.augmentation = new HashMap<>(base.augmentation);
                }
        }

        @Override
        public RpcRegistry getRpcRegistry() {
            return _rpcRegistry;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_rpcRegistry == null) ? 0 : _rpcRegistry.hashCode());
            result = prime * result + ((augmentation == null) ? 0 : augmentation.hashCode());
            return result;
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DataObject)) {
                return false;
            }
            if (!org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl other = (org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl)obj;
            if (_rpcRegistry == null) {
                if (other.getRpcRegistry() != null) {
                    return false;
                }
            } else if(!_rpcRegistry.equals(other.getRpcRegistry())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                LfmNccProviderImplImpl otherImpl = (LfmNccProviderImplImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if(!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>>, Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lispflowmapping.netconf.impl.rev140706.modules.module.configuration.LfmNccProviderImpl>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder ("LfmNccProviderImpl [");
            boolean first = true;
        
            if (_rpcRegistry != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_rpcRegistry=");
                builder.append(_rpcRegistry);
             }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

}
