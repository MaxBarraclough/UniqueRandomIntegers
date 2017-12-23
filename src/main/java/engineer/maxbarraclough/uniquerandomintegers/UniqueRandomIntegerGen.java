/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engineer.maxbarraclough.uniquerandomintegers;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Generate 500 unique integers in the range 1..2048. (Yes, that excludes 0.)
 * Unbiased, never discards a seed random input, never degrades to linear-scan-like time complexity
 * @author mb
 */
public final class UniqueRandomIntegerGen {

    final HashSet<Integer> hs = new HashSet<Integer>(500);

    // We don't have to go the binary route, we could have thirds, ninths...

    // We also don't have to represent every level. we could do half, eights, 32s, and skip the intermediate levels.
    // The numbers can always be computed with additions.

    int totalCount = 0;
    final int[] halfCounts = new int[2];
    final int[] quarterCounts = new int[4];
    final int[] eightsCounts = new int[8];
    final int[] counts16 = new int[16];
    final int[] counts32 = new int[32];
    final int[] counts64 = new int[64];
    final int[] counts128 = new int[128];

    // We can either bottom-out at 2048s, or stop some way above that
    // and use a HashSet/int[]/whatever to store the 'proprer' sparse array data.
    // Let's stop at 128 and use a HashSet<Integer>

    private boolean sanityCheck()
    {
        final boolean check1 = Arrays.stream(halfCounts).sum() == totalCount;
        final boolean check2 = Arrays.stream(quarterCounts).sum() == totalCount;
        final boolean check3 = Arrays.stream(eightsCounts).sum() == totalCount;
        final boolean check4 = Arrays.stream(counts16).sum() == totalCount;
        final boolean check5 = Arrays.stream(counts32).sum() == totalCount;
        final boolean check6 = Arrays.stream(counts64).sum() == totalCount;
        final boolean check7 = Arrays.stream(counts128).sum() == totalCount;
        final boolean check8 = hs.size() == totalCount;

        // Lastly we ensure that the HashSet representing the sparse array,
        // corresponds with counts128

        boolean check9 = true; // set to false if we spot a problem

        int indexIntoCounts128 = 0;
        int counterForInterval = 0;
        for (int i = 1; i != 2049; ++i)
        {
            // At 9, 17, 25... we must do the check and move on to the next element of counts128
            if (0 == (i % 8))
            {
                if (counterForInterval != counts128[indexIntoCounts128])
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

            if (hs.contains(i))
            {
                ++counterForInterval;
            }
        }

        final boolean ret = check1 && check2 && check3 && check4 && check5 && check6 && check7 && check8 && check9;
        return ret;
    }


    public static void main(final String[] args)
    {
        System.out.println("Hello world");
    }
}
