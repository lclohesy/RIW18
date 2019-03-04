package RIW18;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Random;

public class Agent extends AbstractPlayer {

    public static int NUM_ACTIONS;

    public static ArrayList<Types.ACTIONS> actions;

    private final RIWPlayer riwPlayer;

    public Agent(StateObservation so,ElapsedCpuTimer elapsedTimer) {

        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
//        System.out.println("Setup: "+act.size());
        NUM_ACTIONS = act.size();

        actions = new ArrayList<>(act.size());
        for (int i = 0; i < act.size(); ++i) {
            actions.add(i, act.get(i));
        }

        riwPlayer = new RIWPlayer(new Random());
//        System.out.println("Setup complete");
    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        
//        System.out.println("Made it to act");

        riwPlayer.init(stateObs);
//        System.out.println("Made it past init");

        Types.ACTIONS action = riwPlayer.run(elapsedTimer);
        return action;
    }
}
