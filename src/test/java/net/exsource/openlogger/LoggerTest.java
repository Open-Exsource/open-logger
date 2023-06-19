package net.exsource.openlogger;

import net.exsource.openlogger.exception.TestException;
import net.exsource.openlogger.level.LogLevel;
import net.exsource.openlogger.util.ConsoleColor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class LoggerTest {

    private static final Logger log = Logger.getLogger();
    private final List<String> messageList = Arrays.asList("Lorem ipsum dolor sit amet", "labore et dolore magna", "Excepteur sint occaecat cupidatat non", "sunt in culpa qui officia");

    @Test
    void testSingleFatalMessage() {
        log.fatal("Single Message test ( Fatal )");
    }

    @Test
    void testFatalMessageList() {
        log.fatal(messageList);
    }

    @Test
    void testFatalThrowableMessage() {
        log.fatal(new TestException("This is an generated Exception to test"));
    }

    @Test
    void testList() {
        log.list(messageList, "Test List", ConsoleColor.CYAN, LogLevel.WARN);
    }

    @Test
    void testListSameTime() {
        new Thread(() -> log.list(messageList, "Thread One", ConsoleColor.CYAN, LogLevel.WARN)).start();
        new Thread(() -> log.list(messageList, "Thread Two", ConsoleColor.CYAN, LogLevel.WARN)).start();
    }
}
