package com.redbeemedia.enigma.core.error;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public abstract class Error {
    private Error cause;
    private String message;
    private StackTraceElement creationPoint;

    /*package-protected*/ Error() {
        this(null, null);
    }

    /*package-protected*/ Error(String message) {
        this(message, null);
    }

    /*package-protected*/ Error(Error cause) {
        this(null, cause);
    }

    /*package-protected*/ Error(String message, Error cause) {
        this.message = message;
        this.cause = cause;
        this.creationPoint = getCreationPoint();
    }

    public String getTrace() throws IOException {
        StringWriter stringWriter = new StringWriter();
        writeTrace(stringWriter);
        return stringWriter.getBuffer().toString();
    }

    public void writeTrace(Writer writer) throws IOException {
        writer.write(this.getClass().getName());
        writer.write(" at ");
        writer.write(creationPoint.getFileName()+" line "+creationPoint.getLineNumber());
        String mess = this.getMessage();
        if(mess != null) {
            writer.write(": ");
            writer.write(mess);
        }
        if(cause != null) {
            writer.write("\n");
            writer.write("Caused by ");
            cause.writeTrace(writer);
        }
        writer.flush();
    }

    private static StackTraceElement getCreationPoint() {
        int i = 2;
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        while(++i < trace.length) {
            try {
                if(!Error.class.isAssignableFrom(Class.forName(trace[i].getClassName()))) {
                    return trace[i];
                }
            } catch (ClassNotFoundException e) {
                continue;
            }
        }
        return trace[0];
    }

    private String getMessage() {
        return message;
    }

    public Error getCause() {
        return cause;
    }

    public abstract int getErrorCode();
}
