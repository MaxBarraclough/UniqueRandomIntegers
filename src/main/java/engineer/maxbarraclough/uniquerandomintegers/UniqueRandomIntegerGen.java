/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engineer.maxbarraclough.uniquerandomintegers;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Generate 500 unique integers in the range 0..2047.
 * Unbiased, never discards a seed random input, never degrades to linear-scan-like time complexity
 * @author mb
 */
public final class UniqueRandomIntegerGen {

    private final HashSet<Integer> hs = new HashSet<Integer>(500);

    // We don't have to go the binary route, we could have thirds, ninths...

    // We also don't have to represent every level. we could do half, eights, 32s, and skip the intermediate levels.
    // The numbers can always be computed with additions.

    private int totalCount = 0;
    private final int[] halfCounts = new int[2];
    private final int[] quarterCounts = new int[4];
    private final int[] eighthsCounts = new int[8];
    private final int[] counts16 = new int[16];
    private final int[] counts32 = new int[32];
    private final int[] counts64 = new int[64];
    private final int[] counts128 = new int[128];

    // We can either bottom-out at 2048s, or stop some way above that
    // and use a HashSet/int[]/whatever to store the 'proprer' sparse array data.
    // Let's stop at 128 and use a HashSet<Integer>

    private boolean sanityCheck()
    {
        final boolean check1 = Arrays.stream(halfCounts).sum() == totalCount; // corresponds to interval of 1024 elements
        final boolean check2 = Arrays.stream(quarterCounts).sum() == totalCount;
        final boolean check3 = Arrays.stream(eighthsCounts).sum() == totalCount;
        final boolean check4 = Arrays.stream(counts16).sum() == totalCount;
        final boolean check5 = Arrays.stream(counts32).sum() == totalCount;
        final boolean check6 = Arrays.stream(counts64).sum() == totalCount;
        final boolean check7 = Arrays.stream(counts128).sum() == totalCount; // corresponds to interval of 16 elements
        final boolean check8 = hs.size() == totalCount;

        // Lastly we ensure that the HashSet representing the sparse array,
        // corresponds with counts128

        boolean check9 = true; // set to false if we spot a problem

        int indexIntoCounts128 = 0;
        int counterForInterval = 0;
        for (int i = 0; i != 2048; ++i) // tick through all 'candidate' numbers
        {
            // At 16, 32, 48... we must do the check and move on to the next element of counts128
            // (but *not* at zero)
            if ((i > 0) && (0 == (i % 16)))
            {
                if (counterForInterval != counts128[indexIntoCounts128]) // the sum over the previous interval, should now match the one in counts128
                {
                    check9 = false;
                    break;
                }
                else
                {
                    ++indexIntoCounts128;
                    counterForInterval = 0;
                }
            }

            // Do the sum (if applicable) for this current number
            if (hs.contains(i))
            {
                ++counterForInterval;
            }
        }

        final boolean ret = check1 && check2 && check3 && check4 && check5 && check6 && check7 && check8 && check9;
        return ret;
    }


    /**
     * Returns false if that value has already been added
     * @param toInsert
     * @return
     */
    public boolean tryInsertValue(int toInsert)
    {
        final boolean setChanged = this.hs.add(toInsert);

        if (setChanged)
        {
            final int halfCountsIndex = toInsert < 1024 ? 0 : 1; // 0..1024 -> 0   1025..2047 -> 1
            final int quarterCountsIndex = toInsert / 512; // 0..511->0   512..1023->1   1024..1535->2   1536..2047->3
            final int eighthsCountsIndex = toInsert / 256;
            final int counts16Index = toInsert / 128;
            final int counts32Index = toInsert / 64;
            final int counts64Index = toInsert / 32;
            final int counts128Index = toInsert / 16;

            ++(this.totalCount);
            ++(this.halfCounts[halfCountsIndex]);
            ++(this.quarterCounts[quarterCountsIndex]);
            ++(this.eighthsCounts[eighthsCountsIndex]);
            ++(this.counts16[counts16Index]);
            ++(this.counts32[counts32Index]);
            ++(this.counts64[counts64Index]);
            ++(this.counts128[counts128Index]);
        }

        return setChanged;
    }


    public static void main(final String[] args)
    {
        final UniqueRandomIntegerGen urig = new UniqueRandomIntegerGen();
        final boolean insOk1 = urig.tryInsertValue(5);
        final boolean insOk2 = urig.tryInsertValue(511);
        final boolean insOk3 = urig.tryInsertValue(0);
        final boolean insOk4 = urig.tryInsertValue(512);
        final boolean insOk5 = urig.tryInsertValue(513);
        final boolean insOk5_rep = urig.tryInsertValue(513); // bad attempt, should return false
        final boolean insOk6 = urig.tryInsertValue(1000);
        final boolean insOk7 = urig.tryInsertValue(2000);
        final boolean insOk8 = urig.tryInsertValue(2047);
        final boolean insOk9 = urig.tryInsertValue(1048);

        final boolean checkedOk = urig.sanityCheck();

        System.out.println("Hello world");
    }
}
