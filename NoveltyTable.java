package RIW18;

import core.game.Observation;
import core.game.StateObservation;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/* Uses NovelTS feature selection */
public class NoveltyTable {

    private int height;
    private int width;

    // total pixels per grid
    private int blocksize;

    private HashSet<Atom> noveltyHash;
    private HashMap<Atom, Integer> table;

    // initial positions
    private int initX;
    private int initY;

    // TODO how do we determine observable width? IW uses magic number 20.
    private int observableWidth = 20;

    public NoveltyTable(StateObservation so) {
        blocksize = so.getBlockSize();
        height = so.getWorldDimension().height / blocksize;
        width = so.getWorldDimension().width / blocksize;
        noveltyHash = new HashSet<Atom>();
        table = new HashMap<>();

        this.reset();

        Vector2d v = so.getAvatarPosition();
        initX = (int) v.x / blocksize;
        initY = (int) v.y / blocksize;
    }


    public void reset() {
        this.table.clear();
    }

    // TODO: Change this to be based on Nir's logic from papers.
    public double getOldNovelty(ArrayList<Atom> features) {
        int novelty = 0;
        for (Atom atom : features) {
            if (!noveltyHash.contains(atom)) {
                // this is wrong
                novelty += 1;
            }
        }
        return novelty;
    }

    public int getNovelty(Atom feature) {
        if (table.containsKey(feature)) {
            return table.get(feature);
        }
        // TODO print if somehow the table isn't working properly
        return RIWPlayer.HUGE_NUMBER;
    }

    public void updateNovelties(ArrayList<Atom> features) {
        noveltyHash.addAll(features); // TODO check if addAll works
        //for (Atom atom : features) {
           // noveltyHash.add(atom);
        //}
    }

    // Returns the table value if atom exists in the table, null otherwise
    public void updateNovelty(Atom f, int d) {
        table.put(f, d);
    }

    public ArrayList<Atom> extractFeature(StateObservation so) {
        ArrayList<Atom> features = new ArrayList<>();
        ArrayList<Observation> obsArray[][] = so.getObservationGrid();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (Math.abs(x - initX) < observableWidth && Math.abs(y - initY) < observableWidth) {
                    ArrayList<Observation> observations = obsArray[x][y];
                    for (Observation obs : observations) {
                        Atom atom = new Atom(x, y, obs.category);
                        features.add(atom);
                    }
                }
            }
        }
        features.add(extractAvatarFeature(so));

        return features;
    }

    // TODO: why is blocksize found locally? Used in position
    public static Atom extractAvatarFeature(StateObservation so) {
        Vector2d pos = so.getAvatarPosition();
        int blocksize = so.getBlockSize();
        Vector2d orientation = so.getAvatarOrientation();
        int x = (int) pos.x / blocksize;
        int y = (int) pos.y / blocksize;
        int rotationX = (int) orientation.x +1;
        int rotationY = (int) orientation.y -1;

        return new Atom(x, y, (rotationY * 3 + rotationX) * 27);
    }
}
