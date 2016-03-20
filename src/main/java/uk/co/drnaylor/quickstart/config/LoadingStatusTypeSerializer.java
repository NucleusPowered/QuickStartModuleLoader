/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

public class LoadingStatusTypeSerializer implements TypeSerializer<LoadingStatus> {
    @Override
    public LoadingStatus deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        try {
            return LoadingStatus.valueOf(value.getValue().toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            return LoadingStatus.ENABLED;
        }
    }

    @Override
    public void serialize(TypeToken<?> type, LoadingStatus obj, ConfigurationNode value) throws ObjectMappingException {
        value.setValue(obj.name().toLowerCase());
    }
}
