import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
public class VendingMachineTest {
   private static final double DELTA = 0.0001;
 
    private VendingMachine vm;
 
    @BeforeEach
    void setUp() {
        // Fresh machine for every test so tests never share state
        vm = new VendingMachine();
    }
 
    @Test
    void testAddItemValid() {
        // Arrange
        VendingMachineItem chips = new VendingMachineItem("Chips", 1.25);
 
        // Act
        vm.addItem(chips, "A");
 
        // Assert: the same object is now in slot A
        assertSame(chips, vm.getItem("A"));
    }
 
    @Test
    void testAddItemInvalid() {
        // Arrange: slot A is already occupied
        VendingMachineItem chips = new VendingMachineItem("Chips", 1.25);
        VendingMachineItem gum = new VendingMachineItem("Gum", 0.50);
        vm.addItem(chips, "A");
 
        // Act + Assert: adding to an occupied slot must throw
        assertThrows(VendingMachineException.class, () -> vm.addItem(gum, "A"));
    }
 
    @Test
    void testAddItemOccupiedSlotKeepsOriginalItem() {
        // Arrange
        VendingMachineItem chips = new VendingMachineItem("Chips", 1.25);
        VendingMachineItem gum = new VendingMachineItem("Gum", 0.50);
        vm.addItem(chips, "A");
 
        // Act: failed second add
        assertThrows(VendingMachineException.class, () -> vm.addItem(gum, "A"));
 
        // Assert: original item is untouched
        assertSame(chips, vm.getItem("A"));
    }
 
    @Test
    void testAddItemAllFourSlots() {
        // Arrange
        VendingMachineItem a = new VendingMachineItem("A-item", 1.00);
        VendingMachineItem b = new VendingMachineItem("B-item", 1.10);
        VendingMachineItem c = new VendingMachineItem("C-item", 1.20);
        VendingMachineItem d = new VendingMachineItem("D-item", 1.30);
 
        // Act
        vm.addItem(a, "A");
        vm.addItem(b, "B");
        vm.addItem(c, "C");
        vm.addItem(d, "D");
 
        // Assert: each slot holds its own item (first and last slots included)
        assertSame(a, vm.getItem("A"));
        assertSame(b, vm.getItem("B"));
        assertSame(c, vm.getItem("C"));
        assertSame(d, vm.getItem("D"));
    }
 
    @ParameterizedTest
    @ValueSource(strings = { "E", "a", "", " A", "AA", "1", "Z" })
    void testAddItemInvalidCodeThrowsException(String badCode) {
        // Arrange
        VendingMachineItem item = new VendingMachineItem("Chips", 1.25);
 
        // Act + Assert: every string outside {A,B,C,D} is an invalid code
        assertThrows(VendingMachineException.class, () -> vm.addItem(item, badCode));
    }

    @Test
    void testGetBalanceValid() {
        // Arrange
        vm.insertMoney(2.50);
 
        // Act
        double balance = vm.getBalance();
 
        // Assert
        assertEquals(2.50, balance, DELTA);
    }
 
    @Test
    void testGetBalanceInvalid() {
 
        // Arrange
        vm.insertMoney(1.00);
 
        // Act
        assertThrows(VendingMachineException.class, () -> vm.insertMoney(-0.50));
 
        // Assert
        assertEquals(1.00, vm.getBalance(), DELTA);
    }
 
    @Test
    void testGetBalanceNewMachineIsZero() {
        // Arrange + Act + Assert (Javadoc: machine starts with a 0 balance)
        assertEquals(0.0, vm.getBalance(), DELTA);
    }
 

    @Test
    void testGetItemValid() {
        // Arrange
        VendingMachineItem soda = new VendingMachineItem("Soda", 1.50);
        vm.addItem(soda, "C");
 
        // Act
        VendingMachineItem result = vm.getItem("C");
 
        // Assert
        assertNotNull(result);
        assertSame(soda, result);
    }
 
    @Test
    void testGetItemInvalid() {
        // Act + Assert: Javadoc says getItem throws if the code is invalid
        assertThrows(VendingMachineException.class, () -> vm.getItem("E"));
    }
 
    @Test
    void testGetItemEmptySlotReturnsNull() {
        // Arrange: nothing added
 
        // Act
        VendingMachineItem result = vm.getItem("B");
 
        // Assert: an empty slot holds null
        assertNull(result);
    }
 
    @Test
    void testInsertMoneyValid() {
        // Arrange + Act
        vm.insertMoney(2.50);
 
        // Assert
        assertEquals(2.50, vm.getBalance(), DELTA);
    }
 
    @Test
    void testInsertMoneyInvalid() {
        // Act + Assert: Javadoc says amount < 0 throws
        assertThrows(VendingMachineException.class, () -> vm.insertMoney(-1.00));
    }
 
    @Test
    void testInsertMoneyMultipleInsertsAccumulate() {
        // Arrange + Act
        vm.insertMoney(1.00);
        vm.insertMoney(0.25);
        vm.insertMoney(0.75);
 
        // Assert: 
        assertEquals(2.00, vm.getBalance(), DELTA);
    }
 
    @Test
    void testInsertMoneyZeroIsAccepted() {
        // Arrange + Act: 
        vm.insertMoney(0.0);
 
        // Assert
        assertEquals(0.0, vm.getBalance(), DELTA);
    }
 
    @Test
    void testInsertMoneySubDollarAmountIsAccepted() {
        // Arrange + Act: 
        vm.insertMoney(0.50);
 
        // Assert
        assertEquals(0.50, vm.getBalance(), DELTA);
    }
 
    @Test
    void testInsertMoneyNegativeLeavesBalanceUnchanged() {
        // Arrange
        vm.insertMoney(1.00);
 
        // Act
        assertThrows(VendingMachineException.class, () -> vm.insertMoney(-0.01));
 
        // Assert
        assertEquals(1.00, vm.getBalance(), DELTA);
    }
 
    @ParameterizedTest
    @CsvSource({
            "-100.00, false",
            "-1.00,   false",
            "-0.01,   false",
            "0.00,    true",
            "0.01,    true",
            "0.50,    true",
            "1.00,    true",
            "100.00,  true"
    })
    void testInsertMoneyAmountBoundary(double amount, boolean shouldSucceed) {
        // Boundary is at 0 
        if (shouldSucceed) {
            // Act
            vm.insertMoney(amount);
            // Assert
            assertEquals(amount, vm.getBalance(), DELTA);
        } else {
            // Act + Assert
            assertThrows(VendingMachineException.class, () -> vm.insertMoney(amount));
            assertEquals(0.0, vm.getBalance(), DELTA);
        }
    }
 
    @Test
    void testMakePurchaseValid() {
        // Arrange
        vm.addItem(new VendingMachineItem("Chips", 1.25), "A");
        vm.insertMoney(3.00);
 
        // Act
        boolean result = vm.makePurchase("A");
 
        // Assert
        assertTrue(result);
    }
 
    @Test
    void testMakePurchaseInvalid() {
        // Arrange
        vm.insertMoney(5.00);
 
        // Act
        boolean result = vm.makePurchase("A");
 
        // Assert 
        assertFalse(result);
    }
 
    @Test
    void testMakePurchaseDeductsPriceFromBalance() {
        // Arrange
        vm.addItem(new VendingMachineItem("Chips", 1.25), "A");
        vm.insertMoney(3.00);
 
        // Act
        vm.makePurchase("A");
 
        // Assert
        assertEquals(1.75, vm.getBalance(), DELTA);
    }
 
    @Test
    void testMakePurchaseEmptiesSlot() {
        // Arrange
        vm.addItem(new VendingMachineItem("Chips", 1.25), "A");
        vm.insertMoney(3.00);
 
        // Act
        vm.makePurchase("A");
 
        // Assert
        assertNull(vm.getItem("A"));
    }
 
    @Test
    void testMakePurchaseInsufficientBalanceReturnsFalse() {
        // Arrange
        vm.addItem(new VendingMachineItem("Chips", 1.25), "A");
        vm.insertMoney(1.00);
 
        // Act
        boolean result = vm.makePurchase("A");
 
        // Assert
        assertFalse(result);
    }
 
    @Test
    void testMakePurchaseInsufficientBalanceChangesNothing() {
        // Arrange
        VendingMachineItem chips = new VendingMachineItem("Chips", 1.25);
        vm.addItem(chips, "A");
        vm.insertMoney(1.00);
 
        // Act
        vm.makePurchase("A");
 
        // Assert
        assertEquals(1.00, vm.getBalance(), DELTA);
        assertSame(chips, vm.getItem("A"));
    }
 
    @Test
    void testMakePurchaseEmptySlotLeavesBalanceUnchanged() {
        // Arrange
        vm.insertMoney(2.00);
 
        // Act
        vm.makePurchase("B");
 
        // Assert
        assertEquals(2.00, vm.getBalance(), DELTA);
    }
 
    @Test
    void testMakePurchaseInvalidCodeThrowsException() {
        // Arrange
        vm.insertMoney(2.00);
 
        // Act + Assert
        assertThrows(VendingMachineException.class, () -> vm.makePurchase("E"));
    }
 
    @Test
    void testMakePurchaseExactBalanceCompletesPurchase() {
        // Arrange
        vm.addItem(new VendingMachineItem("Chips", 1.25), "A");
        vm.insertMoney(1.25);
 
        // Act
        boolean result = vm.makePurchase("A");
 
        // Assert: exact balance is enough, leaving 0
        assertTrue(result);
        assertEquals(0.0, vm.getBalance(), DELTA);
    }
 
    @Test
    void testMakePurchaseFreeItemWithZeroBalanceSucceeds() {
        // Arrange
        vm.addItem(new VendingMachineItem("Sample", 0.00), "D");
 
        // Act
        boolean result = vm.makePurchase("D");
 
        // Assert
        assertTrue(result);
    }
 
    @Test
    void testMakePurchaseSameSlotTwiceSecondReturnsFalse() {
        // Arrange
        vm.addItem(new VendingMachineItem("Chips", 1.00), "A");
        vm.insertMoney(5.00);
        vm.makePurchase("A");
 
        // Act: slot is now empty
        boolean second = vm.makePurchase("A");
 
        // Assert
        assertFalse(second);
    }
 
    @ParameterizedTest
    @CsvSource({
            "0.00, 1.00, false",
            "0.99, 1.00, false",
            "1.00, 1.00, true",
            "1.01, 1.00, true",
            "5.00, 1.00, true",
            "0.00, 0.00, true",
            "0.49, 0.50, false",
            "0.50, 0.50, true"
    })
    void testMakePurchaseBalanceVsPrice(double balance, double price, boolean expected) {
        // Arrange
        vm.addItem(new VendingMachineItem("Item", price), "A");
        vm.insertMoney(balance);
 
        // Act
        boolean result = vm.makePurchase("A");
 
        // Assert: purchase succeeds exactly when balance >= price
        assertEquals(expected, result);
    }
 
    @Test
    void testRemoveItemValid() {
        // Arrange
        VendingMachineItem soda = new VendingMachineItem("Soda", 1.50);
        vm.addItem(soda, "B");
 
        // Act
        VendingMachineItem removed = vm.removeItem("B");
 
        // Assert: Javadoc says the removed item is returned
        assertSame(soda, removed);
    }
 
    @Test
    void testRemoveItemInvalid() {
        // Act + Assert
        assertThrows(VendingMachineException.class, () -> vm.removeItem("A"));
    }
 
    @Test
    void testRemoveItemSlotBecomesEmpty() {
        // Arrange
        vm.addItem(new VendingMachineItem("Soda", 1.50), "B");
 
        // Act
        vm.removeItem("B");
 
        // Assert
        assertNull(vm.getItem("B"));
    }
 
    @Test
    void testRemoveItemInvalidCodeThrowsException() {
        // Act + Assert
        assertThrows(VendingMachineException.class, () -> vm.removeItem("E"));
    }
 
    @Test
    void testRemoveItemTwiceSecondThrowsException() {
        // Arrange
        vm.addItem(new VendingMachineItem("Soda", 1.50), "C");
        vm.removeItem("C");
 
        // Act + Assert
        assertThrows(VendingMachineException.class, () -> vm.removeItem("C"));
    }
 
    @Test
    void testRemoveItemDoesNotAffectOtherSlots() {
        // Arrange
        VendingMachineItem a = new VendingMachineItem("A-item", 1.00);
        VendingMachineItem b = new VendingMachineItem("B-item", 1.10);
        vm.addItem(a, "A");
        vm.addItem(b, "B");
 
        // Act
        vm.removeItem("A");
 
        // Assert
        assertSame(b, vm.getItem("B"));
    }
 
    @Test
    void testRemoveItemLastSlotD() {
        // Arrange
        VendingMachineItem d = new VendingMachineItem("D-item", 1.30);
        vm.addItem(d, "D");
 
        // Act
        VendingMachineItem removed = vm.removeItem("D");
 
        // Assert
        assertSame(d, removed);
    }
 
    @Test
    void testRemoveItemThenAddAgainSucceeds() {
        // Arrange
        vm.addItem(new VendingMachineItem("Old", 1.00), "A");
        vm.removeItem("A");
        VendingMachineItem replacement = new VendingMachineItem("New", 2.00);
 
        // Act
        vm.addItem(replacement, "A");
 
        // Assert
        assertSame(replacement, vm.getItem("A"));
    }
 
    @Test
    void testReturnChange() {
        // Arrange
        vm.insertMoney(2.75);
 
        // Act
        double change = vm.returnChange();
 
        // Assert
        assertEquals(2.75, change, DELTA);
    }
 
    @Test
    void testReturnChangeInvalid() {
        // Arrange
 
        // Act
        double change = vm.returnChange();
 
        // Assert
        assertEquals(0.0, change, DELTA);
    }
 
    @Test
    void testReturnChangeResetsBalanceToZero() {
        // Arrange
        vm.insertMoney(2.75);
 
        // Act
        vm.returnChange();
 
        // Assert
        assertEquals(0.0, vm.getBalance(), DELTA);
    }
 
    @Test
    void testReturnChangeCalledTwiceSecondReturnsZero() {
        // Arrange
        vm.insertMoney(1.50);
        vm.returnChange();
 
        // Act
        double second = vm.returnChange();
 
        // Assert
        assertEquals(0.0, second, DELTA);
    }
 
    @Test
    void testReturnChangeAfterPurchaseReturnsRemainder() {
        // Arrange
        vm.addItem(new VendingMachineItem("Chips", 1.25), "A");
        vm.insertMoney(3.00);
        vm.makePurchase("A");
 
        // Act
        double change = vm.returnChange();
 
        // Assert: manual calculation 3.00 - 1.25 = 1.75
        assertEquals(1.75, change, DELTA);
    }
 
    @Test
    void testWorkflowAddInsertPurchaseReturnChange() {
        // Arrange
        vm.addItem(new VendingMachineItem("Candy", 0.85), "D");
        vm.insertMoney(1.00);
 
        // Act
        boolean purchased = vm.makePurchase("D");
        double change = vm.returnChange();
 
        // Assert: 1.00 - 0.85 = 0.15 change, slot empty, balance 0
        assertTrue(purchased);
        assertEquals(0.15, change, DELTA);
        assertNull(vm.getItem("D"));
        assertEquals(0.0, vm.getBalance(), DELTA);
    }
}
