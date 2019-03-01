package RIW18;

import java.util.ArrayList;

public class TreeNode {

    // TODO: rename
    public int numGameOver;

    public int numVisit;

    public TreeNode parent;

    private TreeNode[] children;

    public int numChild;

    private int depth;

    private double score;

    public double bestRoute;

    public int bestChild;

    private double novelty;

    private double heuristic;

    public int childDepth;

    public boolean solved;

    private final int HUGE_NUMBER = 3000;

    public TreeNode() {
        this.novelty = 1;
        this.parent = null;

        children = new TreeNode[Agent.NUM_ACTIONS];

        bestRoute = -HUGE_NUMBER - 1;

        bestChild = -1;
    }

    public TreeNode(TreeNode parent, int action, double novelty) {
        this.parent = parent;
        this.novelty = novelty;
        this.solved = false;

        this.children = new TreeNode[Agent.NUM_ACTIONS];

        if (parent != null) {
            depth = parent.depth + 1;
        }

        bestChild = -1;
    }

    public int unprunedAction() {
        int minDepth = 10000;
        int bestAction = -1;
        ArrayList<Integer> rnd = RIWPlayer.randomAction.randomSequence();
        for (int i : rnd) {
            if (!children[i].isPruned()) {
                if (children[i].childDepth < minDepth) {
                    bestAction = i;
                    minDepth = children[i].childDepth;
                }
            }
        }
        if (bestAction != -1) {
            return bestAction;
        }
        this.novelty = 0;
        return rnd.get(0);
    }

    public int nextChild() {
        ArrayList<Integer> rnd = RIWPlayer.randomAction.randomSequence();
        for (int i : rnd) {
            if (children[i] == null) {
                numChild += 1;
                return i;
            }
        }
        return 0;
    }

    public void update() {
        this.bestChild = -1;
        this.bestRoute = -HUGE_NUMBER - 1;
        this.childDepth = 0;

        double route = 0;
        ArrayList<Integer> rnd = RIWPlayer.randomAction.randomSequence();

        int numPruned = 0;

        int numDanger = 0;
        int bestChildVisit = 0;

        for (int i : rnd) {
            if (children[i] != null) {
                if (children[i].childDepth + 1 > this.childDepth) {
                    this.childDepth = children[i].childDepth + 1;
                }

                // if the child is not a leaf
                if (children[i].numChild != 0) {
                    // TODO: Why?
                    route = children[i].score - this.score + heuristic + children[i].bestRoute * 0.9;
                } else {
                    route = children[i].score - this.score + heuristic;
                }

                // penalty if the child is pruned
                if (children[i].isPruned()) {
                    route *= 0.9;
                    numPruned += 1;
                }

                if (children[i].numGameOver != 0) {
                    numDanger += 1;
                }

                if (route > this.bestRoute) {
                    this.bestRoute = route;
                    this.bestChild = i;
                    bestChildVisit = children[i].numVisit;
                }

                if (Math.abs(route - this.bestRoute) < 0.01) {
                    if (children[i].numVisit > bestChildVisit) {
                        this.bestRoute = route;
                        this.bestChild = i;
                        bestChildVisit = children[i].numVisit;
                    }
                }
            }
        }

        if (Agent.NUM_ACTIONS == numPruned) {
            this.novelty = 0;
        }

        // if the children are dangerous, then set the current node to dangerous.
        if (Agent.NUM_ACTIONS == numDanger) {
            this.novelty = 0;
            this.bestRoute = -HUGE_NUMBER;
            this.bestChild = -1;
            this.score = 0;
        }

    }

    public int bestAction() {
        if (this.bestChild != -1) {
            return this.bestChild;
        }

        ArrayList<Integer> rnd = RIWPlayer.randomAction.randomSequence();
        int maxVisit = 0;
        for (int i : rnd) {
            if (children[i] != null) {
                if (maxVisit < children[i].numVisit) {
                    maxVisit = children[i].numVisit;
                    this.bestChild = i;
                }
            }
        }
        return bestChild;
    }

    public int countChild() {
        int count = 0;
        for (int i = 0; i < Agent.NUM_ACTIONS; i++) {
            if (children[i] != null) {
                count += children[i].countChild();
            }
        }
        return count + 1;
    }

    public void addChild(TreeNode node, int index) {
        this.children[index] = node;
    }

    public TreeNode getChild(int i) {
        return children[i];
    }

    public double getScore() {
        return this.score;
    }

    public boolean isExpanded() {
        return numChild == Agent.NUM_ACTIONS;
    }

    public boolean isPruned() {
        return numChild == Agent.NUM_ACTIONS;
    }

    public void updateScore(double score, double heuristic) {
        if (score == -HUGE_NUMBER) {
            this.numGameOver += 1;
        } else {
            this.score = score;
            this.heuristic = heuristic;
        }

        if ((float) numGameOver / numVisit > 0.1) {
            this.score = 0;
            this.heuristic = 0;
        }
        this.numVisit += 1;
    }

}
