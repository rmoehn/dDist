package ddist;

import java.util.*;

/**
 * Helper class for making operations on numbers with a certain bit-length.
 */
public class ChordRing {
    /// Number of bits to use.
    private final int bits;
    private final Random random = new Random();

    public ChordRing(int bits) {
        this.bits = bits;
    }

    /**
     * Get the maximum number allowed + 1 according to bits
     */
    public int getMax() {
        return 1 << bits;
    }

    public int getBitSize() {
        return this.bits;
    }

    /**
     * Get a randum number within the interval 0..2 ** bits - 1
     */
    public int random() {
        return random.nextInt(getMax());
    }

    /**
     * Return whether a < k <= b modulo 2 ** bits
     */
    public boolean between(int k, int a, int b) {
        k = k % getMax();
        if (b <= a) { // We go past 0
            b += getMax();
            if (k <= a)
                k += getMax();
        }
        return (a < k && k <= b);
    }
}
