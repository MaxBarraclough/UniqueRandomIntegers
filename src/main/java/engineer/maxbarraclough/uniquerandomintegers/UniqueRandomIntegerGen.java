/*
 *  This file is part of UniqueRandomIntegerGen.
 *
 *  UniqueRandomIntegerGen is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License verion 3 as published by
 *  the Free Software Foundation.
 *
 *  UniqueRandomIntegerGen is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License version 3 for more details.
 *
 *  You should have received a copy of the GNU General Public License version 3
 *  along with UniqueRandomIntegerGen. If not, see <http://www.gnu.org/licenses/>.
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

    public void tryInsertRelative(final int rel) // // TODO return boolean of (success?)
    {
        final int hc0 = this.halfCounts[0];
        if (rel > hc0)
        {
            // then it goes somewhere in the second half of the space
            final int newRemainder = rel - hc0;
            this.tryInsertRelative_ForGivenHalf(1, newRemainder);
        }
        else
        { // then it goes somewhere in the first half of the space
            this.tryInsertRelative_ForGivenHalf(0, rel);
        }
    }

    /**
     * Complete the relative insert op, for a given halfIndex value (0 or 1), and a given remainder
     * (i.e. we've already subtracted the component of the index which decides which half it belongs to,
     * according to how many elements there are in that half).
     * n.b. It's relative; even an input of 0 might correspond to the second half, if we're approaching saturation.
     * @param halfIndex
     * @param remainder
     */
    private void tryInsertRelative_ForGivenHalf(final int halfIndex, final int remainder)
    {
        ++(this.halfCounts[halfIndex]);
        final int indexOfQuarterToFirstCheck = (halfIndex == 0) ? 0 : 2;
        final int thatQuarterCount = this.quarterCounts[indexOfQuarterToFirstCheck];
        final int thatQuarterSpacesFree = 512 - thatQuarterCount;
        final boolean overflowsThatQuarter = (remainder > thatQuarterSpacesFree);

        if (overflowsThatQuarter)
        {
            final int correctQuarterIndex = indexOfQuarterToFirstCheck + 1;
            final int newRemainder = remainder - thatQuarterCount;
            this.tryInsertRelative_ForGivenQuarter(correctQuarterIndex, newRemainder);
        }
        else
        {
            final int correctQuarterIndex = indexOfQuarterToFirstCheck;
            this.tryInsertRelative_ForGivenQuarter(correctQuarterIndex, remainder);
        }
    }


    private void tryInsertRelative_ForGivenQuarter(final int quarterIndex, final int remainder)
    {
        ++(this.quarterCounts[quarterIndex]);
        final int indexOfEighthToFirstCheck = quarterIndex * 2;
        final int count = this.eighthsCounts[indexOfEighthToFirstCheck];
        final int spacesFree = 256 - count;
        final boolean overflows = (remainder > spacesFree);

        if (overflows)
        {
            final int correctEighthIndex = indexOfEighthToFirstCheck + 1;
            final int newRemainder = remainder - count;
            this.tryInsertRelative_ForGivenEighth(correctEighthIndex, newRemainder);
        }
        else
        {
            final int correctEighthIndex = indexOfEighthToFirstCheck;
            this.tryInsertRelative_ForGivenEighth(correctEighthIndex, remainder);
        }
    }

    private void tryInsertRelative_ForGivenEighth(final int intervalIndex, final int remainder)
    {
        ++(this.eighthsCounts[intervalIndex]);
        final int indexOfSubintervalToFirstCheck = intervalIndex * 2;
        final int count = this.counts16[indexOfSubintervalToFirstCheck];
        final int spacesFree = 128 - count;
        final boolean overflows = (remainder > spacesFree);

        if (overflows)
        {
            final int correctSubintervalIndex = indexOfSubintervalToFirstCheck + 1;
            final int newRemainder = remainder - count;
            this.tryInsertRelative_ForGiven16th(correctSubintervalIndex, newRemainder);
        }
        else
        {
            final int correctSubintervalIndex = indexOfSubintervalToFirstCheck;
            this.tryInsertRelative_ForGiven16th(correctSubintervalIndex, remainder);
        }
    }

    private void tryInsertRelative_ForGiven16th(final int intervalIndex, final int remainder)
    {
        ++(this.counts16[intervalIndex]);
        final int indexOfSubintervalToFirstCheck = intervalIndex * 2;
        final int count = this.counts32[indexOfSubintervalToFirstCheck];
        final int spacesFree = 64 - count;
        final boolean overflows = (remainder > spacesFree);

        if (overflows)
        {
            final int correctSubintervalIndex = indexOfSubintervalToFirstCheck + 1;
            final int newRemainder = remainder - count;
            this.tryInsertRelative_ForGiven32th(correctSubintervalIndex, newRemainder);
        }
        else
        {
            final int correctSubintervalIndex = indexOfSubintervalToFirstCheck;
            this.tryInsertRelative_ForGiven32th(correctSubintervalIndex, remainder);
        }
    }

    private void tryInsertRelative_ForGiven32th(final int intervalIndex, final int remainder)
    {
        ++(this.counts32[intervalIndex]);
        final int indexOfSubintervalToFirstCheck = intervalIndex * 2;
        final int count = this.counts64[indexOfSubintervalToFirstCheck];
        final int spacesFree = 32 - count;
        final boolean overflows = (remainder > spacesFree);

        if (overflows)
        {
            final int correctSubintervalIndex = indexOfSubintervalToFirstCheck + 1;
            final int newRemainder = remainder - count;
            this.tryInsertRelative_ForGiven64th(correctSubintervalIndex, newRemainder);
        }
        else
        {
            final int correctSubintervalIndex = indexOfSubintervalToFirstCheck;
            this.tryInsertRelative_ForGiven64th(correctSubintervalIndex, remainder);
        }
    }

    private void tryInsertRelative_ForGiven64th(final int intervalIndex, final int remainder)
    {
        ++(this.counts64[intervalIndex]);
        final int indexOfSubintervalToFirstCheck = intervalIndex * 2;
        final int count = this.counts128[indexOfSubintervalToFirstCheck];
        final int spacesFree = 16 - count;
        final boolean overflows = (remainder > spacesFree);

        if (overflows)
        {
            final int correctSubintervalIndex = indexOfSubintervalToFirstCheck + 1;
            final int newRemainder = remainder - count;
            this.tryInsertRelative_ForGiven128th(correctSubintervalIndex, newRemainder);
        }
        else
        {
            final int correctSubintervalIndex = indexOfSubintervalToFirstCheck;
            this.tryInsertRelative_ForGiven128th(correctSubintervalIndex, remainder);
        }
    }

    private void tryInsertRelative_ForGiven128th(final int intervalIndex, final int remainder) // TODO redundant param?
    {
        ++(this.counts128[intervalIndex]);
        // TODO add to hashset


        // We must now compute the actual absolute index, from the two params we have
        final int absoluteIndex = (intervalIndex * 128) + remainder;

        this.hs.add(absoluteIndex); // if we move away from HashSet, it will be more important that we do this at the 'bottom'
    }


    // We could have a findCountInInterval method, but all our operations are based on
    // the 'opposite', of finding the absolute index s.t. the count before that index, matches a given value


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
