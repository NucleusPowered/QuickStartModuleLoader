package uk.co.drnaylor.quickstart.util;

@FunctionalInterface
public interface ObjectFactory {

    <T> T create(Class<T> object) throws Exception;

}
