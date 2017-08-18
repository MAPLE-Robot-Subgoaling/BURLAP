package burlap.behavior.singleagent.options;

import java.util.Random;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.support.AnnotatedAction;
import burlap.behavior.singleagent.Episode;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

public class SubgoalBoundedOption extends SubgoalOption {

    public static int maxSteps = -1;

    public SubgoalBoundedOption(String name, Policy optionPolicy, StateConditionTest initiationConditionTest,
                                StateConditionTest terminationConditionTest) {
        super(name, optionPolicy, initiationConditionTest, terminationConditionTest);
    }

    @Override
    public EnvironmentOptionOutcome control(Environment env, double discount) {
        return SubgoalBoundedOption.control(this, env, discount);
    }

    public static EnvironmentOptionOutcome control(Option o, Environment env, double discount){
        Random rand = RandomFactory.getMapped(0);
        State initial = env.currentObservation();
        State cur = initial;

        Episode episode = new Episode(cur);
        Episode history = new Episode(cur);
        double roll;
        double pT;
        int nsteps = 0;
        double r = 0.;
        double cd = 1.;
        do{
            Action a = o.policy(cur, history);
            EnvironmentOutcome eo = env.executeAction(a);
            nsteps++;
            r += cd*eo.r;
            cur = eo.op;
            cd *= discount;


            history.transition(a, eo.op, eo.r);

            AnnotatedAction annotatedAction = new AnnotatedAction(a, o.toString() + "(" + nsteps + ")");
            episode.transition(annotatedAction, eo.op, r);


            pT = o.probabilityOfTermination(eo.op, history);
            roll = rand.nextDouble();

        }while(roll > pT && !env.isInTerminalState() && (maxSteps < 0 || nsteps < maxSteps));

        EnvironmentOptionOutcome eoo = new EnvironmentOptionOutcome(initial, o, cur, r, env.isInTerminalState(), discount, episode);

        return eoo;

    }


}