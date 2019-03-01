package RIW18;

import core.game.StateObservation;

import java.util.LinkedList;

public class Position {

    private int numPos;
    private int blockSize;

    private LinkedList<Atom> positionHistory;

    private static final double ALPHA = 0.5;
    private static final double EPSILON = 0.01;

    public Position(StateObservation so) {
        this.blockSize = so.getBlockSize();
        this.positionHistory = new LinkedList<Atom>();
        this.numPos = 0;
    }

    public void setPosition(StateObservation so) {
        this.positionHistory.addFirst(NoveltyTable.extractAvatarFeature(so));
        this.numPos += 1;
    }

    public double getScore(StateObservation so) {
        Atom point = NoveltyTable.extractAvatarFeature(so);
        double score = 0;
        int distance;
        boolean novelDistance = true;
        double currentAlpha = ALPHA;
        for (Atom historyPoint : positionHistory) {
            distance = getDistance(historyPoint, point);
            if (distance != 0) {
                score += Math.log(distance) / Math.log(2) * currentAlpha;
            } else {
                novelDistance = false;
                if (point.equals(historyPoint)) {
                    break;
                }
            }
            currentAlpha *= ALPHA;
        }
        if (novelDistance) {
            score *= 2;
        }
        score *= EPSILON;
        return score;
    }

    private int getDistance(Atom a1, Atom a2) {
        return Math.abs(a1.getData1() - a2.getData1()) + Math.abs(a1.getData2() - a2.getData2());
    }
}
