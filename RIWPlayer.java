package RIW18;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static RIW18.NoveltyTable.extractAvatarFeature;

public class RIWPlayer {
//    private static Logger logger = Logger.getLogger(RIWPlayer.class.getName());

    private static Random rand;
    public static RandomAction randomAction;
    private NoveltyTable noveltyTable;
    private int[] novTableFiller;
    private Node rootNode;

    private Position positionMatrix;
    private StateObservation rootObservation;

    public static final int HUGE_NUMBER = 3000;

    private int numRun;

    RIWPlayer(Random random) {
        rand = random;
        randomAction = new RandomAction(Agent.NUM_ACTIONS, 100, random);
        this.numRun = 0;
    }

    /* Init is invoked every action (from IW), will update the rootObservation and reuse the previously expanded tree */
    public void init(StateObservation gameState) {
        // create position matrix, adds current position to it and initialise noveltyTable.
        // game observation is stored in a new root node.

        if (positionMatrix == null) {
            positionMatrix = new Position(gameState);
        }


        positionMatrix.setPosition(gameState);
        rootObservation = gameState;

//        // TODO check why observableWidth was used
//        int totalDepth = 12; // PLACEHOLDER
//        if (numRun == 0) {
//            // TODO remove this section
//            noveltyTable = new NoveltyTable(gameState);
//        } else {
//            int observableWidth = (int) (totalDepth/numRun + 3);
//            noveltyTable = new NoveltyTable(gameState);
//        }
        noveltyTable = new NoveltyTable(gameState);

        // TODO check if this is deprecated
        noveltyTable.updateNovelties(noveltyTable.extractFeature(gameState));

        rootNode = new Node();

        rootNode.updateScore(value(gameState), distHeuristic(gameState));
//        logger.info("init: "+ rootNode.nodeInfo());
        System.out.println("init: "+ rootNode.nodeInfo());
    }

    // Generate lookahead tree. Could be done in init
    public Types.ACTIONS run(ElapsedCpuTimer elapsedTimer) {
        // From IW::
        // does IW search, finds the best action and increments a run counter. Returns the action (as an int???)

        Node root = getBranch(elapsedTimer, rootObservation, rootNode);
//        logger.info("found branch: "+root.nodeInfo());
        System.out.println("found branch: "+root.nodeInfo());

        this.numRun += 1;
        this.rootNode = root;

        System.out.println("Next: "+rootNode);
        System.out.println("ACTION PICKED: "+root.getAction());

        return root.getAction();
    }

    private Node getBranch(ElapsedCpuTimer elapsedTimer, StateObservation rootObservation, Node root) {

        long remaining = elapsedTimer.remainingTimeMillis();
        double avgTimeTaken = 0;
        int remainingLimit = 10;  // TODO magic number from IW
        int numIters = 0;

        // Initialise novelty table
        noveltyTable.reset();

        // Make the depth 0 for the root node
        root.setRootDepth();
        // TODO Check if node has all children

        while(!root.isSolved() && remaining > remainingLimit && remaining > 2 * avgTimeTaken) {
            rollout(elapsedTimer, rootObservation, root);
            remaining = elapsedTimer.remainingTimeMillis();  // TODO check if this is working correctly
            numIters++;
            avgTimeTaken = (avgTimeTaken * (numIters-1) + elapsedTimer.elapsedMillis()) / numIters;
        }



        // TODO this case works for RIW implementation of nodes where pruned nodes are deleted
        // TODO add getUnprunedChildren
        Types.ACTIONS action;

        //System.out.println(root.getNumChildren());
        root.updateMaxScore();

        if(root.getNumChildren() == 0) {
            // Return a random action
            action = root.unprunedAction();
            System.out.println("CALLED UNPRUNED ACTION #1");
        } else {
            // TODO: might be maxScore
            if(root.getMaxScore() > 0) {
                // TODO best branch implementation
                System.out.println("PRE-CRASH");
                System.out.println(root.nodeInfo());
                action = root.getBestChild().getAction();
            } else {
                action = root.unprunedAction();
                System.out.println("CALLED UNPRUNED ACTION #2");
            }
        }

        if(action == null) {
            System.out.println("THIS WILL BREAK");

        }

        // TODO work out how to get child from direct action
        //int ind = rootObservation.getAvailableActions().indexOf(action);
        int ind = Agent.actions.indexOf(action);
        if (ind < 0) {
            System.out.println("This is what broke: "+action);
            //System.out.println(Agent.actions);
            System.out.println(root.nodeInfo());
            for (Node c : root.children) {
                System.out.println(c.nodeInfo());
                for (Node k : c.children) {
                    System.out.println(k.nodeInfo());
                }
            }
            System.out.println(root.isSolved());
        }
        return root.getChild(ind);
    }



    // Rollout(n,d):
    private void rollout(ElapsedCpuTimer elapsedTimer, StateObservation rootObservation, Node n) {

        double avgTimeTaken = 0;
        double accumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();      // TODO DEPRECATED
        int numIters = 0;
        StateObservation tempState;
        int remainingLimit = 10;  // TODO work out why this magic number exists

        // Fill node
        // fillNode(n); TODO check there isn't a purpose to this

        NoveltyTable d = noveltyTable;

        tempState = rootObservation.copy();

        System.out.println("Setup for loop");

        while(!n.solved && remaining > remainingLimit && remaining > 2 * avgTimeTaken) {

            System.out.println("Loop #"+numIters);

            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

//            Node selected = selectNode(tempState, rootNode);  // This may override expand if needed

            // if n isn't expanded then Expand(n)
            //System.out.println("Pre-expand");
            expandIfNeeded(n, rootObservation); // TODO add check for if n isn't expanded first
            //System.out.println("Expanded!");


            n.updateUnsolvedChildren();
            ArrayList<Node> children = n.getUnsolvedChildren2();

            //System.out.println(children);

            // TODO Not used
            if (children.size() == 0) {
                System.out.println("Uh oh "+n.getDepth());
                System.out.println(n.isSolved());
            }


            n = children.get(rand.nextInt(children.size()));
            tempState.advance(n.getAction());
            n.updateScore(value(tempState), distHeuristic(tempState));
            n.updateMaxScore();

            System.out.println("ROllout: "+n.nodeInfo());


            fillNode(n);

            if(n.terminal) {
                n.visited = true;
                System.out.println("Case 0");
                solveAndPropagate(n);

                numIters++;
                accumTimeTaken += elapsedTimerIteration.elapsedMillis();
                avgTimeTaken = accumTimeTaken / numIters;
                remaining = elapsedTimer.remainingTimeMillis();
                break;
            } else {
                Atom f = extractAvatarFeature(tempState);
                int novelty = noveltyTable.getNovelty(f);
                if(n.getDepth() < novelty) {
                    // case 1
                    System.out.println("Case 1");
                    //System.out.println(f);
                    n.visited = true; // TODO potentially fix this for better protection
                    updateFeatureTable(n, f);
                } else if (!n.visited && n.getDepth() >= novelty) {
                    // case 2
                    System.out.println("Case 2");
                    //System.out.println(f);
                    n.visited = true;
                    solveAndPropagate(n);

                    numIters++;
                    accumTimeTaken += elapsedTimerIteration.elapsedMillis();
                    avgTimeTaken = accumTimeTaken / numIters;
                    remaining = elapsedTimer.remainingTimeMillis();
                    break;
                } else if(n.visited && novelty < n.getDepth()) {
                    // case 3
                    System.out.println("Case 3");
                    solveAndPropagate(n);

                    numIters++;
                    accumTimeTaken += elapsedTimerIteration.elapsedMillis();
                    avgTimeTaken = accumTimeTaken / numIters;
                    remaining = elapsedTimer.remainingTimeMillis();
                    break;
                }
                    // else case 4: Continue rollout
                System.out.println("Case 4");

                numIters++;
                accumTimeTaken += elapsedTimerIteration.elapsedMillis();
                avgTimeTaken = accumTimeTaken / numIters;
                remaining = elapsedTimer.remainingTimeMillis();

                System.out.println(elapsedTimerIteration.elapsedMillis());
                System.out.println("remaining: "+remaining);
            }
        }
    }

    // function fillNode(n)
    private void fillNode(Node n) {
        // Generate structure for the root node.
    }

    private void expandIfNeeded(Node n, StateObservation so) {
        if(n.getNumChildren() == 0) {
            // C++ goes on to check frame reps.
            n.expand(so.getAvailableActions());
        }
    }

    private void solveAndPropagate(Node n) {
        // I believe it marks the node as solved and propagates up to parent.
        boolean propagate = true;
        Node child = n;
        // stop when the node has unsolved children
        // TODO check this operates correctly around null.
        // TODO can refactor this into a recursive internal function in Node for better security
        while(propagate && child != null) {
            child.solved = true;
            if (child.parent == null) {
                break;
            }
            child.parent.updateUnsolvedChildren();
            //System.out.println("SNP"+n);
            if (!child.parent.getUnsolvedChildren2().isEmpty()) {
                //System.out.println("Props");
                for (Node c : child.parent.getUnsolvedChildren2()) {
                    if (!c.isSolved()) {
                        propagate = false;
                        //System.out.println("PROP BROKEN");
                        break;
                    }
                }
            }
            child.updateMaxScore();
            child = child.parent;
        }
    }

    private void updateFeatureTable(Node n, Atom f) {
        noveltyTable.updateNovelty(f, n.getDepth());
    }

    // function getNovelFeature(n,d)
    private int getNovelFeature(Node n, int[] d) {
        // Return novelty of that node (possibly)
        return 0;
    }

    private double distHeuristic(StateObservation state) {
        return positionMatrix.getScore(state);
    }

    private double value(StateObservation state) {
        if (state.isGameOver()) {
            if(state.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                return HUGE_NUMBER;
            } else if (state.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
                return -HUGE_NUMBER;
            } else {
                return state.getGameScore() / 2;
            }
        }
        return state.getGameScore();
    }

    // TODO this function looks like it controls how IW was run, hence not useful for RIW
//    private Node selectNode(StateObservation state, Node parent) {
//
//        if (state.isGameOver() || parent.isPruned()) {
//            return parent;
//        } else if (parent.isExpanded()) {
//            Types.ACTIONS nextAction = parent.unprunedAction();
//            state.advance(nextAction);
//            parent = parent.getChild(nextAction);
//
//            parent.updateScore(value(state), distHeuristic(state));
//            return selectNode(state, parent);
//        } else {
//            return expand(parent, state);
//        }
//    }

    private void updateNovelty(int depth, int[] featureAtoms) {
        // TODO note that Blai returns the number of updated entries, may be useful

    }


}
