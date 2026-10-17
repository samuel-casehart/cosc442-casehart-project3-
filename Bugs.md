## Bug 1:
for (int i = 0; i <= NUM_SLOTS; i++) { 
    itemArray[i] = null;
}
This bug is shown by testAddItemAllFourSlots

Fix: i<NUM_SLOTS;

## BUG 2:
if (amount < 1) 
    throw new VendingMachineException(...);

This bug is shown by edge cases