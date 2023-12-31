package net.exsource.openlogger.template;

import net.exsource.openlogger.Logger;
import net.exsource.openlogger.level.LogLevel;
import net.exsource.openlogger.packet.LogPacked;
import net.exsource.openlogger.util.ConsoleColor;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Todo: Commit this class.
 * @since 1.0
 * @author Niklas Tat, Daniel Ramke
 */
public class DefaultLogger implements Logger {

    private final String format;
    private final String defaultPattern;
    private String logName;

    private File logDirectory;
    private File logFile;

    public DefaultLogger() {
        this.format = ConsoleColor.RESET + "%1$s %2$5s " + ConsoleColor.RESET + "[" + ConsoleColor.PURPLE + "%3$15s" + ConsoleColor.RESET + "] " + ConsoleColor.RESET + "%4$-40s " + ConsoleColor.RESET + ": %5$s";
        this.defaultPattern = "dd.MM.yyyy HH:mm:ss";
    }

    @Override
    public synchronized void print(LogPacked logObject) {
        // Check if the log Level is debug
        if (logObject.getLevel() == LogLevel.DEBUG) {
            // check if log level is activated
            if (!Logger.isDebugEnabled()) {
                return;
            }
        }

        // Check if logObject contains a Message
        if (logObject.getMessage() != null)
            printMessage(logObject);

        // Check if logObject contains a Message list
        if (logObject.getMessageList() != null
                && !logObject.getMessageList().isEmpty()) {
            printMessageList(logObject);
        }
    }

    @Override
    public void debug(LogPacked logObject) {
        logObject.setLevel(LogLevel.DEBUG);
        print(createLogObject(logObject));
    }

    @Override
    public void debug(String message) {
        print(createLogObject(message, LogLevel.DEBUG));
    }

    @Override
    public void debug(List<String> messageList) {
        print(createLogObject(messageList, LogLevel.DEBUG));
    }

    @Override
    public void info(LogPacked logObject) {
        logObject.setLevel(LogLevel.INFO);
        print(createLogObject(logObject));
    }

    @Override
    public void info(String message) {
        print(createLogObject(message, LogLevel.INFO));
    }

    @Override
    public void info(List<String> messageList) {
        print(createLogObject(messageList, LogLevel.INFO));
    }

    @Override
    public void warn(LogPacked logObject) {
        logObject.setLevel(LogLevel.WARN);
        print(createLogObject(logObject));
    }

    @Override
    public void warn(String message) {
        print(createLogObject(message, LogLevel.WARN));
    }

    @Override
    public void warn(List<String> messageList) {
        print(createLogObject(messageList, LogLevel.WARN));
    }

    @Override
    public void error(LogPacked logObject) {
        logObject.setLevel(LogLevel.ERROR);
        print(createLogObject(logObject));
    }

    @Override
    public void error(String message) {
        print(createLogObject(message, LogLevel.ERROR));
    }

    @Override
    public void error(List<String> messageList) {
        print(createLogObject(messageList, LogLevel.ERROR));
    }

    @Override
    public void error(Throwable throwable) {
        printThrowable(throwable, LogLevel.ERROR);
    }

    @Override
    public void fatal(LogPacked logObject) {
        logObject.setLevel(LogLevel.FATAL);
        print(createLogObject(logObject));
    }

    @Override
    public void fatal(String message) {
        print(createLogObject(message, LogLevel.FATAL));
    }

    @Override
    public void fatal(List<String> messageList) {
        print(createLogObject(messageList, LogLevel.FATAL));
    }

    @Override
    public void fatal(Throwable throwable) {
        printThrowable(throwable, LogLevel.FATAL);
    }

    @Override
    public void empty(LogPacked logObject) {
        logObject.setLevel(LogLevel.EMPTY);
        print(createLogObject(logObject));
    }

    @Override
    public void empty(String message) {
        print(createLogObject(message, LogLevel.EMPTY));
    }

    @Override
    public void empty(List<String> messageList) {
        print(createLogObject(messageList, LogLevel.EMPTY));
    }

    @Override
    public void list(List<String> messageList, String headline) {
        list(createLogObject(messageList, LogLevel.INFO), headline);
    }

    @Override
    public void list(List<String> messageList, String headline, LogLevel logLevel) {
        LogPacked logObject = new LogPacked(messageList);
        logObject.setLevel(logLevel);

        list(createLogObject(logObject), headline);
    }

    @Override
    public void list(List<String> messageList, String headline, ConsoleColor consoleColor) {
        list(createLogObject(messageList, LogLevel.INFO), headline, consoleColor);
    }

    @Override
    public void list(List<String> messageList, String headline, ConsoleColor consoleColor, LogLevel logLevel) {
        LogPacked logObject = new LogPacked(messageList);
        logObject.setLevel(logLevel);

        list(createLogObject(logObject), headline, consoleColor);
    }

    @Override
    public void list(LogPacked logObject, String headline) {
        list(createLogObject(logObject), headline, ConsoleColor.RESET);
    }

    @Override
    public void list(List<String> messageList, String headline, char lineChar, LogLevel logLevel) {
        LogPacked logObject = new LogPacked(messageList);
        logObject.setLevel(logLevel);

        list(createLogObject(logObject), headline, lineChar);
    }

    @Override
    public void list(List<String> messageList, String headline, char lineChar) {
        list(createLogObject(messageList, LogLevel.INFO), headline, lineChar);
    }

    @Override
    public void list(LogPacked logObject, String headline, ConsoleColor consoleColor) {
        list(createLogObject(logObject), headline, '*', consoleColor);
    }

    @Override
    public void list(LogPacked logObject, String headline, char lineChar) {
        list(createLogObject(logObject), headline, lineChar, ConsoleColor.RESET);
    }

    @Override
    public void list(List<String> messageList, String headline, char lineChar, ConsoleColor consoleColor, LogLevel logLevel) {
        LogPacked logObject = new LogPacked(messageList);
        logObject.setLevel(logLevel);

        list(createLogObject(logObject), headline, lineChar, consoleColor);
    }

    @Override
    public void list(List<String> messageList, String headline, char lineChar, ConsoleColor consoleColor) {
        list(createLogObject(messageList, LogLevel.INFO), headline, lineChar, consoleColor);
    }

    @Override
    public synchronized void list(LogPacked logObject, String headline, char lineChar, ConsoleColor consoleColor) {
        int lineLength = getMaxLineSize(logObject.getMessageList());
        String regEx = "\\e\\[[\\d;]*[^\\d;]";

        if (lineLength - headline.replaceAll(regEx,"").length() < 4) {
            lineLength = headline.replaceAll(regEx,"").length() + 6;
        }

        if (lineLength % 2 != 0) {
            lineLength++;
        }

        String borderLine = consoleColor + (String.valueOf(lineChar)).repeat(((lineLength - headline.replaceAll(regEx,"").length()) / 2));

        String border = borderLine
                + " [ "
                + ConsoleColor.RESET
                + headline
                + consoleColor
                + " ] "
                + borderLine
                + ConsoleColor.RESET;

        LogPacked logBorder = new LogPacked(border);
        logBorder.setLevel(logObject.getLevel());
        logBorder.setSourceMethodName(logObject.getSourceMethodName());
        logBorder.setSourceClassName(logObject.getSourceClassName());
        logBorder.setThreadId(logObject.getThreadId());

        print(logBorder);

        for (String message : logObject.getMessageList()) {
            LogPacked logMessage = new LogPacked(consoleColor + String.valueOf(lineChar) + ConsoleColor.RESET + " " + message + (" ").repeat(border.replaceAll(regEx,"").length() - 3 - message.replaceAll(regEx,"").length()) + consoleColor + lineChar);
            logMessage.setLevel(logObject.getLevel());
            logMessage.setSourceMethodName(logObject.getSourceMethodName());
            logMessage.setSourceClassName(logObject.getSourceClassName());
            logMessage.setThreadId(logObject.getThreadId());

            print(logMessage);
        }

        print(logBorder);
    }

    @Override
    public void setLogPath(String path) {
        this.logDirectory = new File(path);

        if (this.logDirectory.mkdirs()) {
            debug("Missing log directories have been created");
        }
    }

    @Override
    public String getLogPath() {
        return null;
    }

    private void printThrowable(Throwable throwable, LogLevel logLevel) {
        List<String> messages = new ArrayList<>();

        for (StackTraceElement element : throwable.getStackTrace()) {
            messages.add(ConsoleColor.RED + "Class "
                    + ConsoleColor.RESET + element.getClassName()
                    + ConsoleColor.RED + " Method "
                    + ConsoleColor.RESET + element.getMethodName()
                    + ConsoleColor.RED + " Line "
                    + ConsoleColor.RESET + " ( " + element.getLineNumber() + " )");
        }

        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];

        LogPacked logObject = new LogPacked(messages);
        logObject.setLevel(logLevel);
        logObject.setSourceClassName(checkSourceSizeCheck(stackTraceElement.getClassName()));
        logObject.setSourceMethodName(stackTraceElement.getMethodName());
        logObject.setThreadId(Thread.currentThread().getId());

        list(logObject,throwable.getClass().getSimpleName() + " - " + throwable.getMessage(), '#', ConsoleColor.RED_BOLD);
    }

    private LogPacked createLogObject(@NotNull LogPacked logObject) {
        if (logObject.getSourceClassName() == null
                || logObject.getSourceMethodName() == null) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];

            logObject.setSourceClassName(checkSourceSizeCheck(stackTraceElement.getClassName()));
            logObject.setSourceMethodName(stackTraceElement.getMethodName());
        }

        if (logObject.getThreadId() == 0L) {
            logObject.setThreadId(Thread.currentThread().getId());
        }

        if (logObject.getLevel() == null)
            logObject.setLevel(LogLevel.INFO);

        return logObject;
    }

    private @NotNull LogPacked createLogObject(String message, LogLevel logLevel) {
        LogPacked logObject = new LogPacked(message, logLevel);

        return createLogObject(logObject, Thread.currentThread().getStackTrace()[3]);
    }

    private @NotNull LogPacked createLogObject(List<String> messageList, LogLevel logLevel) {
        LogPacked logObject = new LogPacked(messageList, logLevel);

        return createLogObject(logObject, Thread.currentThread().getStackTrace()[3]);
    }

    private LogPacked createLogObject(LogPacked logObject, StackTraceElement stackTraceElement) {
        logObject.setThreadId(Thread.currentThread().getId());
        logObject.setSourceClassName(checkSourceSizeCheck(stackTraceElement.getClassName()));
        logObject.setSourceMethodName(stackTraceElement.getMethodName());

        return logObject;
    }

    private void printMessage(@NotNull LogPacked logObject) {
        if (logObject.getLevel() == LogLevel.EMPTY) {
            System.out.println(logObject.getLevel().getColor() + logObject.getMessage());
        } else {
            String message = String.format(format,
                    new SimpleDateFormat(defaultPattern).format(new Date(logObject.getMillis())),
                    logObject.getLevel().getColor() + logObject.getLevel().getPrefix(),
                    ManagementFactory.getThreadMXBean().getThreadInfo(logObject.getThreadId()).getThreadName(),
                    logObject.getSourceClassName(),
                    logObject.getMessage());

            System.out.println(message);

            if (logObject.getLevel() != LogLevel.DEBUG)
                logMessage(logObject.getMillis(), message);
        }
    }

    private void printMessageList(@NotNull LogPacked logObject) {
        for (String message : logObject.getMessageList()) {

            if (logObject.getLevel() == LogLevel.EMPTY) {
                System.out.println(logObject.getLevel().getColor() + message);
            } else {
                String logMessage = String.format(format,
                        new SimpleDateFormat(defaultPattern).format(new Date(logObject.getMillis())),
                        logObject.getLevel().getColor() + logObject.getLevel().getPrefix(),
                        ManagementFactory.getThreadMXBean().getThreadInfo(logObject.getThreadId()).getThreadName(),
                        logObject.getSourceClassName(),
                        message);

                System.out.println(logMessage);

                if (logObject.getLevel() != LogLevel.DEBUG)
                    logMessage(logObject.getMillis(), message);
            }
        }
    }

    private void logMessage(long timeMillis, String message) {
        if (Logger.isLogging()) {
            if (this.logDirectory == null)
                setLogPath("logs");
            createLogFile(timeMillis);

            try {
                FileUtils.writeStringToFile(logFile, message.replaceAll("\\e\\[[\\d;]*[^\\d;]", "") + System.lineSeparator(), StandardCharsets.UTF_8, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void createLogFile(long timeMillis) {
        String rightLogName = new SimpleDateFormat("dd.MM.yyyy").format(timeMillis) + ".log";

        if (!Objects.equals(this.logName, rightLogName)) {
            this.logName = rightLogName;

            this.logFile = new File(logDirectory, logName);
            try {

                if (logFile.createNewFile()) {
                    debug("Missing log File have been created!");
                } else if (!logFile.createNewFile()){
                    logMessage(System.currentTimeMillis(), " ");
                    logMessage(System.currentTimeMillis(), "######################################## [ Start of protocol ] ########################################");
                    debug("Created some space between the old log and the new one");
                }
            } catch (IOException ex) {
                error(ex);
            }
        }
    }

    private int getMaxLineSize(List<String> messageList) {
        int lineSize = 0;

        for (String message : messageList) {
            message = message.replaceAll("\\e\\[[\\d;]*[^\\d;]","");
            if (message.length() > lineSize)
                lineSize = message.length();
        }

        return lineSize + 6;
    }

    private String checkSourceSizeCheck(@NotNull String className) {
        int index = 0;
        while (className.length() > 40) {
            String[] path = className.split("\\.");

            if (path[index] == null)
                break;

            if (path[index].length() > 1) {
                path[index] = String.valueOf(path[index].charAt(0));
            }
            index++;

            className = join(path, ".");
        }

        return className;
    }

    public String join(String[] array, String cement) {
        StringBuilder stringBuilder = new StringBuilder();

        if (array == null
                || array.length == 0) {
            return null;
        }

        for (String s : array) {
            stringBuilder.append(s).append(cement);
        }

        stringBuilder.delete(stringBuilder.length() - cement.length(), stringBuilder.length());

        return stringBuilder.toString();
    }
}
