import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Specification-style suite for VendingMachineItem, grouped by concern.
 */
@DisplayName("VendingMachineItem specification")
class VendingMachineItemSpecTest {

    private static final double EPS = 1e-9;
    private static final String NEGATIVE_PRICE_MESSAGE = "Price cannot be less than zero";

    @Nested
    @DisplayName("construction with a valid price")
    class ValidConstruction {

        @Test
        void storesBothNameAndPrice() {
            VendingMachineItem item = new VendingMachineItem("Pretzels", 2.10);

            assertAll(
                    () -> assertEquals("Pretzels", item.getName()),
                    () -> assertEquals(2.10, item.getPrice(), EPS));
        }

        @ParameterizedTest(name = "{0} at ${1} is stored as given")
        @CsvSource({
                "Free sample, 0.00",
                "Penny candy, 0.01",
                "Gum,         0.50",
                "Soda,        1.75",
                "Premium box, 250.00"
        })
        void acceptsAnyNonNegativePrice(String name, double price) {
            VendingMachineItem item = new VendingMachineItem(name, price);

            assertAll(
                    () -> assertEquals(name, item.getName()),
                    () -> assertEquals(price, item.getPrice(), EPS));
        }
    }

    @Nested
    @DisplayName("construction with a negative price")
    class InvalidConstruction {

        @ParameterizedTest(name = "price {0} is rejected")
        @ValueSource(doubles = { -0.01, -1.0, -99.99, -1_000_000.0, Double.NEGATIVE_INFINITY })
        void rejectsNegativePrices(double badPrice) {
            assertThrows(VendingMachineException.class,
                    () -> new VendingMachineItem("Anything", badPrice));
        }

        @Test
        void rejectionCarriesTheDocumentedMessage() {
            VendingMachineException ex = assertThrows(VendingMachineException.class,
                    () -> new VendingMachineItem("Anything", -3.00));

            assertEquals(NEGATIVE_PRICE_MESSAGE, ex.getMessage());
        }

        @Test
        void rejectionIsAnUncheckedException() {
            // Callers are not forced to catch it: it must be a RuntimeException
            assertThrows(RuntimeException.class,
                    () -> new VendingMachineItem("Anything", -0.50));
        }
    }

    @Nested
    @DisplayName("accessors")
    class Accessors {

        // NOTE: intentionally overlaps with the name assertion in
        // ValidConstruction.storesBothNameAndPrice
        @Test
        void getName_returnsWhatWasPassedToTheConstructor() {
            VendingMachineItem item = new VendingMachineItem("Pretzels", 2.10);

            assertEquals("Pretzels", item.getName());
        }

        @Test
        void getPrice_isStableAcrossRepeatedCalls() {
            VendingMachineItem item = new VendingMachineItem("Cookie", 0.95);

            double first = item.getPrice();
            double second = item.getPrice();

            assertEquals(first, second, EPS);
            assertEquals(0.95, second, EPS);
        }

        @Test
        void twoItemsDoNotShareState() {
            VendingMachineItem cheap = new VendingMachineItem("Cheap", 0.25);
            VendingMachineItem pricey = new VendingMachineItem("Pricey", 9.99);

            assertAll(
                    () -> assertEquals("Cheap", cheap.getName()),
                    () -> assertEquals(0.25, cheap.getPrice(), EPS),
                    () -> assertEquals("Pricey", pricey.getName()),
                    () -> assertEquals(9.99, pricey.getPrice(), EPS));
        }
    }
}
