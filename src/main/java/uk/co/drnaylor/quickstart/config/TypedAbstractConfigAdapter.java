/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

/**
 * Handy subclass of {@link AbstractConfigAdapter} that provides utility methods for most configuration modules.
 *
 * @param <R> The class that represents the structure of the node. Must have a {@link TypeSerializer} or a {@link ConfigSerializable} annotation.
 */
public abstract class TypedAbstractConfigAdapter<R> extends AbstractConfigAdapter<R> {

    /**
     * Gets the value of the node, or failing that, the default object.
     *
     * @return The object of type {@link R}
     */
    public final R getNodeOrDefault() {
        try {
            R node = getNode();
            if (node != null) {
                return node;
            }
        } catch (ObjectMappingException e) {
            //
        }

        return getDefaultObject();
    }

    /**
     * The default object.
     *
     * @return The default object.
     */
    protected abstract R getDefaultObject();

    /**
     * Generates the default {@link ConfigurationNode} to populate.
     *
     * @param node An empty node to be populated.
     * @return The populated node.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected ConfigurationNode generateDefaults(ConfigurationNode node) {
        R o = getDefaultObject();
        try {
            return node.setValue(TypeToken.of((Class<R>)o.getClass()), o);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
            return node;
        }
    }

    /**
     * A simple {@link TypedAbstractConfigAdapter} that just requires a default object to be defined.
     *
     * @param <R> The type of object this represents.
     */
    public abstract static class Standard<R> extends TypedAbstractConfigAdapter<R> {

        /**
         * The {@link TypeToken} that represents {@link R}
         */
        final TypeToken<R> typeToken;

        /**
         * Constructs this {@link TypedAbstractConfigAdapter.Standard}, using a {@link Class} to get around type erasure.
         *
         * @param clazz The class of type {@link R}
         */
        public Standard(Class<R> clazz) {
            this(TypeToken.of(clazz));
        }

        /**
         * Constructs this {@link TypedAbstractConfigAdapter.Standard}, using a {@link TypeToken} to get around type erasure.
         *
         * @param typeToken The {@link TypeToken} of type {@link R}
         */
        public Standard(TypeToken<R> typeToken) {
            this.typeToken = typeToken;
        }

        /**
         * Converts to an object of type {@link R} from a {@link ConfigurationNode}
         *
         * @param node The node to convert into the object of type {@link R}
         * @return The object of type {@link R}
         * @throws ObjectMappingException Thrown if the conversion was not successful.
         */
        @Override
        protected R convertFromConfigurateNode(ConfigurationNode node) throws ObjectMappingException {
            return node.getValue(typeToken, getDefaultObject());
        }

        /**
         * Converts from an object of type {@link R} to a {@link ConfigurationNode}
         *
         * @param newNode A new {@link ConfigurationNode} to populate.
         * @param data The data to convert.
         * @return The populated {@link ConfigurationNode}
         * @throws ObjectMappingException Thrown if the conversion was not successful.
         */
        @Override
        protected ConfigurationNode insertIntoConfigurateNode(ConfigurationNode newNode, R data) throws ObjectMappingException {
            return newNode.setValue(typeToken, data);
        }
    }

    /**
     * An even simpler {@link TypedAbstractConfigAdapter} that assumes the default object as a no-args constructor and instantiates it via reflection.
     *
     * @param <R> The type of object.
     */
    public abstract static class StandardWithSimpleDefault<R> extends TypedAbstractConfigAdapter.Standard<R> {

        /**
         * Constructs this {@link TypedAbstractConfigAdapter.StandardWithSimpleDefault}, using a {@link Class} to get around type erasure.
         *
         * @param clazz The class of type {@link R}
         */
        public StandardWithSimpleDefault(Class<R> clazz) {
            super(clazz);
        }

        /**
         * Constructs this {@link TypedAbstractConfigAdapter.StandardWithSimpleDefault}, using a {@link TypeToken} to get around type erasure.
         *
         * @param typeToken The {@link TypeToken} of type {@link R}
         */
        public StandardWithSimpleDefault(TypeToken<R> typeToken) {
            super(typeToken);
        }

        /**
         * Gets the default object of type {@link R}
         *
         * @return The default object of type {@link R}
         * @throws RuntimeException thrown if the type could not be constructed.
         */
        @Override
        @SuppressWarnings("unchecked")
        protected R getDefaultObject() {
            try {
                return (R) typeToken.getRawType().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

