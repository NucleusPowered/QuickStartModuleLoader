package uk.co.drnaylor.quickstart.config;

import ninja.leaping.configurate.transformation.ConfigurationTransformation;

import java.util.Collection;

@FunctionalInterface
public interface ConfigTransformer {

    Collection<ConfigurationTransformation> transformations();

}
