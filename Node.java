package RIW18;

import ontology.Types;

import java.util.ArrayList;
import java.util.Random;

/**
 * Node structure based off the TreeNodes used in NovelTS.
 * Handles structure, scoring and expansion of nodes.
 */
public class Node {

    public boolean solved;
    public boolean visited;
    public boolean terminal;

    private int depth;
    public Node parent;
    public Node[] children;
    private double novelty;
    private Types.ACTIONS action;
    private Random rand;
    private ArrayList<Node> unsolved;

    private double heuristic;
    private double score;
    public int numGameOver; // Not needed
    public int numVisit;  // Not needed

    private double maxScore;
    private double minScore;


    public Node() {
        this.solved = false;
        this.visited = false;
        this.terminal = false;
        this.depth = 0; // Root node

        this.parent = null;

        this.children = new Node[Agent.NUM_ACTIONS];

        this.novelty = 1;
        this.rand = new Random();

        // Set the maxScore to be below the losing score. This could cause issues.
        this.maxScore = -1 - RIWPlayer.HUGE_NUMBER;
    }

    public Node(Node parent, Types.ACTIONS action, double novelty) {
        this.parent = parent;
        this.novelty = novelty;
        this.action = action;

        this.solved = false;
        this.visited = false;
        this.terminal = false;

        this.children = new Node[Agent.NUM_ACTIONS];

        if(parent != null) {
            this.depth = parent.getDepth() + 1;
        } else {
            this.depth = 0;
        }

        this.rand = new Random();

        // Set the maxScore to be below the losing score. This could cause issues.
        this.maxScore = -1 - RIWPlayer.HUGE_NUMBER;

    }

    // Returns an ArrayList of unsolved children. Children are not updated.
    public ArrayList<Node> getUnsolvedChildren() {
        ArrayList<Node> unsolved = new ArrayList<>();
//        System.out.println("Getting puzzled kids");
        for(Node c : this.children) {
            if (c != null) {
                if(!c.isSolved()) {
                    unsolved.add(c);
                }
            }
        }
//        System.out.println("Unsolved:"+unsolved.size());
        if(unsolved.size() > 0) {
            return unsolved;
        }
//        System.out.println("RETURNED NO UNSOLVED @ "+this.depth);
        return unsolved;
    }

    public void updateUnsolvedChildren() {
        ArrayList<Node> unsolved = new ArrayList<>();
        for (Node c : this.children) {
            if (c != null) {
                if (!c.isSolved()) {
                    unsolved.add(c);
                }
            }
        }
        if (unsolved.size() > 0) {
            this.unsolved = unsolved;
        } else {
            this.unsolved.clear();
        }
    }

    public ArrayList<Node> getUnsolvedChildren2() {
        return this.unsolved;
    }

    public int getDepth() {
        // return the depth of the node
        return this.depth;
    }

    public boolean isSolved() {
        return this.solved;
    }

    public Node getChild(int i) {
        return this.children[i];
    }

    public void expand(ArrayList<Types.ACTIONS> actions) {
        // expand from the rollout algorithm.
        int i = 0;
        for(Types.ACTIONS act : actions) {
            Node child = new Node(this, act, 1 + this.depth);  // TODO REWORK NOVELTY
            this.addChild(child, i);
            i++;
        }
    }

    public int getNumChildren() {
        int count = 0;
        for (Node c : this.children) {
            if (c != null) {
                count += 1;
            }
        }
        return count;
    }

    public void addChild(Node node, int index) {
        this.children[index] = node;
    }

    public void updateScore(double score, double heuristic) {
        if (score == -RIWPlayer.HUGE_NUMBER) {
            this.numGameOver += 1;  // Relic of NovelTS, not needed for RIW
        } else {
            this.score = score;
            this.heuristic = heuristic;
        }

        if ((float) numGameOver / numVisit > 0.1) {
            this.score = 0;
            this.heuristic = 0;
        }
        this.numVisit += 1;

        this.maxScore = Math.max(this.score, this.maxScore);
    }

    // Assumes unpruned children have already been updated
    public Types.ACTIONS unprunedAction() {
        ArrayList<Node> kids = this.getUnsolvedChildren2();
        if (kids.size() == 0) {
//            System.out.println("UNPRUNED ACTION RETURNED NULL");
            return pickAction();
        } else {
            return kids.get(rand.nextInt(kids.size())).getAction();
        }
    }

    private Types.ACTIONS pickAction() {
        ArrayList<Node> unpruned = new ArrayList<>();
        for (Node c : this.children) {
            if (c.getScore() > RIWPlayer.HUGE_NUMBER) {
                unpruned.add(c);
            }
        }
        if (unpruned.size() > 0) {
            return unpruned.get(rand.nextInt(unpruned.size())).getAction();
        } else {
            // TODO Rework how it accepts defeat.
            return Types.ACTIONS.ACTION_NIL;
        }
    }

    public Types.ACTIONS getAction() {
        return this.action;
    }

    public void setRootDepth() {
        this.depth = 0;
    }

    public double getScore() {
        return this.score;
    }

    public double getMaxScore() {
        return this.maxScore;
    }

    // Assumes scores have been calculated for all children on the root node
    public Node getBestChild() {
        ArrayList<Node> best = new ArrayList<>(children.length);
        for(Node c : children) {
//            System.out.println(c.nodeInfo());
            if(c.getMaxScore() == this.maxScore) {
//                System.out.println("Added to best: "+c);
                best.add(c);
            }
        }

        return best.get(rand.nextInt(best.size()));
    }

    public void updateMaxScore() {
        double maxS = -1 * RIWPlayer.HUGE_NUMBER - 1;
        for(Node child : children) {
            if (child != null) {
                maxS = Math.max(maxS, Math.max(child.maxScore, child.getScore()));
            }
        }
        this.maxScore = maxS;
    }

    public String nodeInfo() {
        return "depth:"+this.depth+" score:"+this.score+" action:"+this.action+" maxScore:"+this.maxScore;
    }

    @Override
    public String toString() {
        return "Depth "+this.depth+" "+this.action;
    }
}
