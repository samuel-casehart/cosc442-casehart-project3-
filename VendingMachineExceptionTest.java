import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
 
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
 
import org.junit.jupiter.api.Test;

public class VendingMachineExceptionTest {
 
    private static final String MESSAGE = "Slot A already occupied";
 
    @Test
    void testAddSuppressed() {
        // Arrange
        VendingMachineException ex = new VendingMachineException(MESSAGE);
        RuntimeException suppressed = new RuntimeException("suppressed");
 
        // Act
        ex.addSuppressed(suppressed);
 
        // Assert
        assertEquals(1, ex.getSuppressed().length);
        assertSame(suppressed, ex.getSuppressed()[0]);
    }
 
    @Test
    void testFillInStackTrace() {
        // Arrange
        VendingMachineException ex = new VendingMachineException(MESSAGE);
 
        // Act
        Throwable result = ex.fillInStackTrace();
 
        // Assert
        assertSame(ex, result);
    }
 
    @Test
    void testGetCause() {
        // Arrange + Act
        VendingMachineException ex = new VendingMachineException(MESSAGE);
 
        // Assert
        assertNull(ex.getCause());
    }
 
    @Test
    void testGetLocalizedMessage() {
        // Arrange
        VendingMachineException ex = new VendingMachineException(MESSAGE);
 
        // Act + Assert
        assertEquals(MESSAGE, ex.getLocalizedMessage());
    }
 
    @Test
    void testGetMessage() {
        // Arrange + Act
        VendingMachineException ex = new VendingMachineException(MESSAGE);
 
        // Assert
        assertEquals(MESSAGE, ex.getMessage());
    }
 
    @Test
    void testGetMessageEmptyString() {
        // Arrange + Act
        VendingMachineException ex = new VendingMachineException("");
 
        // Assert
        assertEquals("", ex.getMessage());
    }
 
    @Test
    void testGetMessageDefaultConstructorIsNull() {
        // Arrange + Act
        VendingMachineException ex = new VendingMachineException();
 
        // Assert
        assertNull(ex.getMessage());
    }
 
    @Test
    void testGetStackTrace() {
        // Arrange + Act
        VendingMachineException ex = new VendingMachineException(MESSAGE);
        StackTraceElement[] trace = ex.getStackTrace();
 
        // Assert
        assertTrue(trace.length > 0);
        assertEquals("testGetStackTrace", trace[0].getMethodName());
    }
 
    @Test
    void testGetSuppressed() {
        // Arrange + Act
        VendingMachineException ex = new VendingMachineException(MESSAGE);
 
        // Assert: nothing suppressed yet, so an empty array (never null)
        assertEquals(0, ex.getSuppressed().length);
    }
 
    @Test
    void testInitCause() {
        // Arrange
        VendingMachineException ex = new VendingMachineException(MESSAGE);
        RuntimeException cause = new RuntimeException("root cause");
 
        // Act
        Throwable returned = ex.initCause(cause);
 
        // Assert
        assertSame(ex, returned);
        assertSame(cause, ex.getCause());
    }
 
    @Test
    void testPrintStackTrace() {
        // Arrange
        VendingMachineException ex = new VendingMachineException(MESSAGE);
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured));
 
        // Act
        try {
            ex.printStackTrace();
        } finally {
            System.setErr(originalErr);
        }
 
        // Assert
        assertTrue(captured.toString().contains("VendingMachineException: " + MESSAGE));
    }
 
    @Test
    void testPrintStackTrace2() {
        // Arrange
        VendingMachineException ex = new VendingMachineException(MESSAGE);
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
 
        // Act
        ex.printStackTrace(new PrintStream(captured));
 
        // Assert
        assertTrue(captured.toString().contains("VendingMachineException: " + MESSAGE));
    }
 
    @Test
    void testPrintStackTrace3() {
        // Arrange
        VendingMachineException ex = new VendingMachineException(MESSAGE);
        StringWriter captured = new StringWriter();
 
        // Act
        ex.printStackTrace(new PrintWriter(captured));
 
        // Assert
        assertTrue(captured.toString().contains("VendingMachineException: " + MESSAGE));
    }
 
    @Test
    void testSetStackTrace() {
        // Arrange
        VendingMachineException ex = new VendingMachineException(MESSAGE);
        StackTraceElement[] custom = {
                new StackTraceElement("FakeClass", "fakeMethod", "FakeClass.java", 42)
        };
 
        // Act
        ex.setStackTrace(custom);
 
        // Assert
        assertArrayEquals(custom, ex.getStackTrace());
    }
 
    @Test
    void testToString() {
        // Arrange
        VendingMachineException ex = new VendingMachineException(MESSAGE);
 
        // Act + Assert
        assertEquals("VendingMachineException: " + MESSAGE, ex.toString());
    }
 
    @Test
    void testToStringNoMessage() {
        // Arrange
        VendingMachineException ex = new VendingMachineException();
 
        // Act + Assert
        assertEquals("VendingMachineException", ex.toString());
    }
 
    @Test
    void testIsRuntimeException() {
        // Arrange + Act + Assert
        assertTrue(RuntimeException.class.isAssignableFrom(VendingMachineException.class));
    }
}