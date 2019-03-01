package RIW18;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class RandomAction {

    private int num;
    private int size;
    private ArrayList<ArrayList<Integer>> randomArray;
    private Random random;

    public RandomAction(int num, int size, Random random) {
        this.random = random;
        this.num = num;
        this.size = size;
        this.randomArray = new ArrayList<ArrayList<Integer>>();
        createArray();
    }

    public ArrayList<Integer> randomSequence() {
        int seed = random.nextInt(size);
        return randomArray.get(seed);
    }

    private void createArray() {
        for (int count = 0; count < size; count++) {
            ArrayList<Integer> array = new ArrayList<Integer>();
            for (int i = 0; i < num; i++) {
                array.add(i);
            }
            Collections.shuffle(array, random);
            this.randomArray.add(array);
        }
    }
}
