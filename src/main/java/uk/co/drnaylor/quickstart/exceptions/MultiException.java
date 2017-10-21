/*
 * This file is part of QuickStart Module Loader, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.quickstart.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MultiException extends Exception {

    private final List<Exception> exceptionList;

    public MultiException(Exception... exceptions) {
        this(Arrays.asList(exceptions));
    }

    public MultiException(List<Exception> exceptionList) {
        this.exceptionList = exceptionList;
    }

    @Override public String getMessage() {
        return this.exceptionList.stream().map(Throwable::getMessage).collect(Collectors.joining(System.lineSeparator()));
    }

    @Override public void printStackTrace(PrintStream s) {
        this.exceptionList.forEach(x -> x.printStackTrace(s));
    }

    @Override public void printStackTrace(PrintWriter s) {
        this.exceptionList.forEach(x -> x.printStackTrace(s));
    }
}
