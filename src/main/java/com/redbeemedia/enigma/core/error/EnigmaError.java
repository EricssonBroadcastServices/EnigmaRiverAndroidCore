package com.redbeemedia.enigma.core.error;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public abstract class EnigmaError {
    private EnigmaError cause;
    private String message;
    private StackTraceElement[] errorStackTrace;

    /*package-protected*/ EnigmaError() {
        this(null, null);
    }

    /*package-protected*/ EnigmaError(String message) {
        this(message, null);
    }

    /*package-protected*/ EnigmaError(EnigmaError cause) {
        this(null, cause);
    }

    /*package-protected*/ EnigmaError(String message, EnigmaError cause) {
        this.message = message;
        this.cause = cause;
        this.errorStackTrace = createStackTrace();
    }

    private StackTraceElement[] createStackTrace() {
        int i = 2;
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        int stackTraceStart = -1;
        while(++i < trace.length) {
            try {
                if(!EnigmaError.class.isAssignableFrom(Class.forName(trace[i].getClassName()))) {
                    stackTraceStart = i;
                    break;
                }
            } catch (ClassNotFoundException e) {
                continue;
            }
        }
        if(stackTraceStart != -1 && trace.length-stackTraceStart >= 0) {
            StackTraceElement[] stackTrace = new StackTraceElement[trace.length-stackTraceStart];
            System.arraycopy(trace, stackTraceStart, stackTrace, 0, stackTrace.length);
            return stackTrace;
        } else {
            return new StackTraceElement[0];
        }
    }

    public String getTrace() {
        StringWriter stringWriter = new StringWriter();
        try {
            writeTrace(stringWriter);
        } catch (IOException e) {
            stringWriter.getBuffer().setLength(0);
            stringWriter.write("Could not get trace:");
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            printWriter.flush();
        }
        return stringWriter.getBuffer().toString();
    }

    public void printStackTrace() {
        try {
            writeTrace(new PrintWriter(System.err));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void logStackTrace(String tag) {
        logStackTrace(Log.ERROR, tag);
    }

    public void logStackTrace(int level, String tag) {
        Log.println(level, tag, getTrace());
    }

    public void writeTrace(Writer writer) throws IOException {
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.println(this);
        for(StackTraceElement traceElement : errorStackTrace) {
            printWriter.println("\tat "+traceElement);
        }
        printWriter.flush();

        if(cause != null) {
            writer.write("\n");
            writer.write("Caused by: ");
            cause.writeTrace(writer);
        }
        writer.flush();
    }

    @Override
    public String toString() {
        String name = getClass().getName();
        String mess = getMessage();
        return (mess != null) ? (name + ": " + mess) : name;
    }

    private String getMessage() {
        return message;
    }

    public EnigmaError getCause() {
        return cause;
    }

    public abstract int getErrorCode();

    protected static void addExceptionStackTrace(Writer writer, Exception exception) throws IOException {
        if(exception != null) {
            writer.write("\n");
            writer.write("Caused by: ");
            exception.printStackTrace(new PrintWriter(writer, true));
        }
        writer.flush();
    }
}
