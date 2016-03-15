package uk.co.drnaylor.quickstart.exceptions;

import java.text.MessageFormat;

public class NoModuleException extends Exception {

    private final String module;

    public NoModuleException(String module) {
        super(MessageFormat.format("The module \"{0}\" does not exist", module));
        this.module = module;
    }

    public String getModule() {
        return module;
    }
}
