package ddist;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class ChordNodeTest {
    public static final int ID_BITS   = 4;
    public static final int RING_SIZE = 1 << ID_BITS;

    public static final Random random = new Random();

    public static ChordRing chordRing;
    public static ChordNode firstNode;
    public static List<ChordNode> nodes;

    @Before
    public void setUp() {
        nodes = new ArrayList<>(RING_SIZE);

        chordRing = new ChordRing(4);
        firstNode = new ChordNode(chordRing);
        nodes.add(firstNode);

        firstNode.newNetwork();

        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    @After
    public void tearDown() {
        nodes = null;
    }

//    @Test
//    public void testNewNetwork() {
//        System.out.println(firstNode);
//    }

    @Test
    public void testMoreNodes() {
        for (int i = 1; i < (RING_SIZE / 2); ++i) {
            ChordNode node = new ChordNode(chordRing);
            nodes.add(node);

            ChordNode ref = nodes.get(random.nextInt(i));
            node.join(ref);
        }

        printNodes(nodes);

        for (int i = 0; i < (RING_SIZE / 2); ++i) {
            ChordNode node = nodes.get( random.nextInt(nodes.size()) );
            nodes.remove(node);
            node.leave();
            System.out.println("-----------------");
            printNodes(nodes);
        }
    }

    private static void printNodes(List<ChordNode> nodes) {
        Collections.sort(nodes);
        for (ChordNode n : nodes) {
            System.out.println(n);
            System.out.println();
        }
    }
}
