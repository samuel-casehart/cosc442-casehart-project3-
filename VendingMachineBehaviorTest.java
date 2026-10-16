import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Behaviour-oriented suite for VendingMachine, grouped by feature with
 * @Nested classes. Written against the Javadoc contract, not the implementation.
 */
@DisplayName("VendingMachine behaviour")
class VendingMachineBehaviorTest {

    private static final double EPS = 1e-9;

    private static final String MSG_INVALID_CODE = "Invalid code for vending machine item";
    private static final String MSG_INVALID_AMOUNT = "Invalid amount.  Amount must be >= 0";

    private VendingMachine machine;
    private VendingMachineItem chips;
    private VendingMachineItem soda;

    @BeforeEach
    void init() {
        machine = new VendingMachine();
        chips = new VendingMachineItem("Chips", 1.25);
        soda = new VendingMachineItem("Soda", 1.50);
    }

    /** Codes that are not one of the four documented slot codes. */
    static Stream<String> badCodes() {
        return Stream.of("E", "a", "", " A", "AA", "0", "Z", "slot A");
    }

    static Stream<Arguments> balancePriceOutcomes() {
        return Stream.of(
                Arguments.of(0.00, 0.75, false),
                Arguments.of(0.74, 0.75, false),
                Arguments.of(0.75, 0.75, true),
                Arguments.of(0.76, 0.75, true),
                Arguments.of(9.00, 0.75, true));
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("a brand new machine")
    class FreshMachine {

        @Test
        void freshMachine_hasZeroBalance() {
            assertEquals(0.0, machine.getBalance(), EPS);
        }

        @Test
        void freshMachine_firstAndLastSlotsAreEmpty() {
            assertAll(
                    () -> assertNull(machine.getItem("A")),
                    () -> assertNull(machine.getItem("D")));
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("stocking with addItem")
    class Stocking {

        @Test
        void addItem_toEmptySlot_storesTheSameInstance() {
            machine.addItem(chips, "B");

            assertSame(chips, machine.getItem("B"));
        }

        @Test
        void addItem_toDifferentSlots_keepsEachItemSeparate() {
            machine.addItem(chips, "A");
            machine.addItem(soda, "D");

            assertAll(
                    () -> assertSame(chips, machine.getItem("A")),
                    () -> assertSame(soda, machine.getItem("D")),
                    () -> assertNull(machine.getItem("B")));
        }

        @Test
        void addItem_toOccupiedSlot_throwsAndNamesTheSlot() {
            machine.addItem(chips, "A");

            VendingMachineException ex = assertThrows(VendingMachineException.class,
                    () -> machine.addItem(soda, "A"));

            assertEquals("Slot A already occupied", ex.getMessage());
        }

        @Test
        void addItem_toOccupiedSlot_doesNotReplaceExistingItem() {
            machine.addItem(chips, "D");

            assertThrows(VendingMachineException.class, () -> machine.addItem(soda, "D"));

            assertSame(chips, machine.getItem("D"));
        }

        @ParameterizedTest(name = "addItem rejects code \"{0}\"")
        @MethodSource("VendingMachineBehaviorTest#badCodes")
        void addItem_withInvalidCode_throwsInvalidCodeMessage(String code) {
            VendingMachineException ex = assertThrows(VendingMachineException.class,
                    () -> machine.addItem(chips, code));

            assertEquals(MSG_INVALID_CODE, ex.getMessage());
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("removing with removeItem")
    class Removal {

        @Test
        void removeItem_returnsTheItemAndEmptiesTheSlot() {
            machine.addItem(soda, "B");

            VendingMachineItem removed = machine.removeItem("B");

            assertAll(
                    () -> assertSame(soda, removed),
                    () -> assertNull(machine.getItem("B")));
        }

        @Test
        void removeItem_fromEmptySlot_throwsAndNamesTheSlot() {
            VendingMachineException ex = assertThrows(VendingMachineException.class,
                    () -> machine.removeItem("B"));

            assertEquals("Slot B is empty -- cannot remove item", ex.getMessage());
        }

        @ParameterizedTest(name = "removeItem rejects code \"{0}\"")
        @MethodSource("VendingMachineBehaviorTest#badCodes")
        void removeItem_withInvalidCode_throws(String code) {
            assertThrows(VendingMachineException.class, () -> machine.removeItem(code));
        }

        @Test
        void removeItem_leavesNeighbouringSlotsAlone() {
            VendingMachineItem gum = new VendingMachineItem("Gum", 0.50);
            machine.addItem(chips, "A");
            machine.addItem(soda, "B");
            machine.addItem(gum, "D");

            machine.removeItem("B");

            assertAll(
                    () -> assertSame(chips, machine.getItem("A")),
                    () -> assertNull(machine.getItem("B")),
                    () -> assertSame(gum, machine.getItem("D")));
        }

        @Test
        void removedSlot_canBeRestocked() {
            machine.addItem(chips, "A");
            machine.removeItem("A");

            assertDoesNotThrow(() -> machine.addItem(soda, "A"));
            assertSame(soda, machine.getItem("A"));
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("money handling")
    class Money {

        // NOTE: intentionally overlaps with FreshMachine.freshMachine_hasZeroBalance
        @Test
        void getBalance_beforeAnyActivity_isZero() {
            assertEquals(0.0, machine.getBalance(), EPS);
        }

        @ParameterizedTest(name = "insertMoney({0}) sets balance to {0}")
        @ValueSource(doubles = { 0.0, 0.05, 0.99, 1.0, 20.0 })
        void insertMoney_nonNegativeAmount_isAdded(double amount) {
            machine.insertMoney(amount);

            assertEquals(amount, machine.getBalance(), EPS);
        }

        @ParameterizedTest(name = "insertMoney({0}) is rejected")
        @ValueSource(doubles = { -0.01, -1.0, -50.0 })
        void insertMoney_negativeAmount_throwsAndBalanceIsUntouched(double amount) {
            machine.insertMoney(2.00);

            VendingMachineException ex = assertThrows(VendingMachineException.class,
                    () -> machine.insertMoney(amount));

            assertAll(
                    () -> assertEquals(MSG_INVALID_AMOUNT, ex.getMessage()),
                    () -> assertEquals(2.00, machine.getBalance(), EPS));
        }

        @Test
        void insertMoney_severalTimes_sumsTheAmounts() {
            machine.insertMoney(0.25);
            machine.insertMoney(0.25);
            machine.insertMoney(1.00);

            assertEquals(1.50, machine.getBalance(), EPS);
        }

        @Test
        void getBalance_isReadOnly_repeatedCallsAgree() {
            machine.insertMoney(3.00);

            double first = machine.getBalance();
            double second = machine.getBalance();

            assertEquals(first, second, EPS);
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("purchasing with makePurchase")
    class Purchasing {

        @Test
        void makePurchase_withEnoughMoney_succeedsDeductsAndEmptiesSlot() {
            machine.addItem(chips, "A");
            machine.insertMoney(3.00);

            boolean ok = machine.makePurchase("A");

            assertAll(
                    () -> assertTrue(ok),
                    () -> assertEquals(1.75, machine.getBalance(), EPS),
                    () -> assertNull(machine.getItem("A")));
        }

        @Test
        void makePurchase_withExactMoney_succeedsLeavingNothing() {
            machine.addItem(soda, "D");
            machine.insertMoney(1.50);

            assertTrue(machine.makePurchase("D"));
            assertEquals(0.0, machine.getBalance(), EPS);
        }

        @Test
        void makePurchase_withTooLittleMoney_failsWithoutSideEffects() {
            machine.addItem(soda, "B");
            machine.insertMoney(1.49);

            boolean ok = machine.makePurchase("B");

            assertAll(
                    () -> assertFalse(ok),
                    () -> assertEquals(1.49, machine.getBalance(), EPS),
                    () -> assertSame(soda, machine.getItem("B")));
        }

        @Test
        void makePurchase_fromEmptySlot_returnsFalseAndKeepsBalance() {
            machine.insertMoney(5.00);

            boolean ok = machine.makePurchase("D");

            assertAll(
                    () -> assertFalse(ok),
                    () -> assertEquals(5.00, machine.getBalance(), EPS));
        }

        @Test
        void makePurchase_withInvalidCode_throws() {
            machine.insertMoney(5.00);

            assertThrows(VendingMachineException.class, () -> machine.makePurchase("Q"));
        }

        @Test
        void makePurchase_ofFreeItem_worksWithNoMoneyInserted() {
            machine.addItem(new VendingMachineItem("Sample", 0.0), "A");

            assertTrue(machine.makePurchase("A"));
        }

        @Test
        void makePurchase_sameSlotAgain_failsBecauseSlotIsNowEmpty() {
            machine.addItem(chips, "A");
            machine.insertMoney(10.00);
            machine.makePurchase("A");

            assertFalse(machine.makePurchase("A"));
            assertEquals(8.75, machine.getBalance(), EPS);
        }

        @ParameterizedTest(name = "balance {0}, price {1} -> purchase {2}")
        @MethodSource("VendingMachineBehaviorTest#balancePriceOutcomes")
        void makePurchase_succeedsExactlyWhenBalanceCoversPrice(
                double balance, double price, boolean expected) {
            machine.addItem(new VendingMachineItem("Thing", price), "B");
            machine.insertMoney(balance);

            assertEquals(expected, machine.makePurchase("B"));
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("returning change")
    class Change {

        @Test
        void returnChange_handsBackWholeBalanceAndZeroesIt() {
            machine.insertMoney(2.75);

            double change = machine.returnChange();

            assertAll(
                    () -> assertEquals(2.75, change, EPS),
                    () -> assertEquals(0.0, machine.getBalance(), EPS));
        }

        @Test
        void returnChange_withNothingInserted_isZero() {
            assertEquals(0.0, machine.returnChange(), EPS);
        }

        @Test
        void returnChange_secondCallInARow_isZero() {
            machine.insertMoney(1.00);
            machine.returnChange();

            assertEquals(0.0, machine.returnChange(), EPS);
        }

        @Test
        void returnChange_afterPurchase_isWhatIsLeftOver() {
            machine.addItem(chips, "A");
            machine.insertMoney(2.00);
            machine.makePurchase("A");

            assertEquals(0.75, machine.returnChange(), EPS);
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("end-to-end sessions")
    class Sessions {

        @Test
        void twoPurchasesInOneSession_thenCollectChange() {
            VendingMachineItem gum = new VendingMachineItem("Gum", 0.50);
            machine.addItem(chips, "A");
            machine.addItem(soda, "B");
            machine.addItem(gum, "D");
            machine.insertMoney(5.00);

            boolean first = machine.makePurchase("A");
            boolean second = machine.makePurchase("B");
            double change = machine.returnChange();

            assertAll(
                    () -> assertTrue(first),
                    () -> assertTrue(second),
                    () -> assertEquals(2.25, change, EPS),
                    () -> assertNull(machine.getItem("A")),
                    () -> assertNull(machine.getItem("B")),
                    () -> assertSame(gum, machine.getItem("D")));
        }

        @Test
        void restockAfterSellOut_thenSellAgain() {
            machine.addItem(chips, "A");
            machine.insertMoney(1.25);
            machine.makePurchase("A");

            VendingMachineItem fresh = new VendingMachineItem("Fresh chips", 1.00);
            machine.addItem(fresh, "A");
            machine.insertMoney(1.00);

            assertTrue(machine.makePurchase("A"));
            assertEquals(0.0, machine.getBalance(), EPS);
        }
    }
}
