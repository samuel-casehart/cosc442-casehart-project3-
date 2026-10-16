import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VendingMachineItemTest {
 
    // Tolerance for comparing double prices
    private static final double DELTA = 0.0001;
 
    // Message documented by the class's PRICE_LESS_THAN_ZERO_MESSAGE constant
    private static final String EXPECTED_MESSAGE = "Price cannot be less than zero";
 
    @Test
    void testConstructorValidStoresName() {
        // Arrange + Act
        VendingMachineItem item = new VendingMachineItem("Chips", 1.25);
 
        // Assert (Javadoc postcondition: name is set to the argument)
        assertEquals("Chips", item.getName());
    }
 
    @Test
    void testConstructorValidStoresPrice() {
        // Arrange + Act
        VendingMachineItem item = new VendingMachineItem("Chips", 1.25);
 
        // Assert (Javadoc postcondition: price is set to the argument)
        assertEquals(1.25, item.getPrice(), DELTA);
    }
 
    @Test
    void testConstructorNegativePriceThrowsException() {
        // Act + Assert: Javadoc says a price less than zero throws
        assertThrows(VendingMachineException.class,
                () -> new VendingMachineItem("Chips", -5.00));
    }
 
    @Test
    void testConstructorJustBelowZeroThrowsException() {
        // Boundary: immediately below 0 is invalid
        assertThrows(VendingMachineException.class,
                () -> new VendingMachineItem("Chips", -0.01));
    }
 
    @Test
    void testConstructorZeroPriceIsAccepted() {
        // Boundary: Javadoc precondition is price >= 0, so exactly 0 is valid
        // Arrange + Act
        VendingMachineItem item = new VendingMachineItem("Sample", 0.00);
 
        // Assert
        assertEquals(0.00, item.getPrice(), DELTA);
    }
 
    @Test
    void testConstructorSmallPositivePriceIsAccepted() {
        // Boundary: immediately above 0 is valid
        // Arrange + Act
        VendingMachineItem item = new VendingMachineItem("Penny candy", 0.01);
 
        // Assert
        assertEquals(0.01, item.getPrice(), DELTA);
    }
 
    @Test
    void testConstructorNegativePriceHasExpectedMessage() {
        // Act
        VendingMachineException ex = assertThrows(VendingMachineException.class,
                () -> new VendingMachineItem("Chips", -1.00));
 
        // Assert
        assertEquals(EXPECTED_MESSAGE, ex.getMessage());
    }
}