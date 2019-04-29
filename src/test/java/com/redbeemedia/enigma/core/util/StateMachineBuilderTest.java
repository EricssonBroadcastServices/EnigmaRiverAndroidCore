package com.redbeemedia.enigma.core.util;

import com.redbeemedia.enigma.core.player.EnigmaPlayerState;

import org.junit.Assert;
import org.junit.Test;

public class StateMachineBuilderTest {
    @Test
    public void testExample() {
        IStateMachineBuilder<String> example = new StateMachineBuilder<>();
        example.addState("A");
        example.addState("B");
        example.addState("C");
        example.addState("D");
        example.addState("E");
        example.addState("F");
        example.addState("G");
        example.addDirectTransition("A", "B");
        example.addDirectTransition("A", "E");
        example.addDirectTransition("B", "C");
        example.addDirectTransition("C", "D");
        example.addDirectTransition("D", "B");
        example.addDirectTransition("D", "G");
        example.addDirectTransition("E", "D");
        example.addDirectTransition("F", "A");
        example.addDirectTransition("F", "G");
        example.addDirectTransition("G", "F");
        example.setInitialState("A");
        IStateMachine<String> stateMachine = example.build();

        StringBuilder log = new StringBuilder();
        log.append(stateMachine.getState());
        stateMachine.addListener(new IStateChangedListener<String>() {
            private String last = stateMachine.getState();
            @Override
            public void onStateChanged(String from, String to) {
                Assert.assertEquals(last, from);
                log.append("->"+to);
                last = to;
            }
        });

        stateMachine.setState("B");
        Assert.assertEquals("A->B", log.toString());
        stateMachine.setState("B");
        Assert.assertEquals("A->B", log.toString());
        stateMachine.setState("A");
        Assert.assertEquals("A->B->C->D->G->F->A", log.toString());
        stateMachine.setState("F");
        Assert.assertEquals("A->B->C->D->G->F->A->E->D->G->F", log.toString());
        stateMachine.setState("G");
        Assert.assertEquals("A->B->C->D->G->F->A->E->D->G->F->G", log.toString());
        stateMachine.setState("F");
        Assert.assertEquals("A->B->C->D->G->F->A->E->D->G->F->G->F", log.toString());
        stateMachine.setState("G");
        Assert.assertEquals("A->B->C->D->G->F->A->E->D->G->F->G->F->G", log.toString());
        stateMachine.setState("C");
        Assert.assertEquals("A->B->C->D->G->F->A->E->D->G->F->G->F->G->F->A->B->C", log.toString());
    }

    @Test
    public void testPerformance() {
        IStateMachineBuilder<Integer> builder = new StateMachineBuilder<>();
        for(int i = 0; i < 200; ++i) {
            builder.addState(i);
        }
        builder.setInitialState(0);
        for(int i = 0; i < 200; ++i) {
            if(3*i+1 < 200) {
                builder.addDirectTransition(i, 3*i+1);
            }
            if(7*(i+1)-1 < 200) {
                builder.addDirectTransition(i, 7*(i+1)-1);
            }
        }
        IStateMachine<Integer> stateMachine = builder.build();
        StringBuilder log = new StringBuilder();
        stateMachine.addListener((from, to) -> log.append("["+from+"->"+to+"]"));
        Assert.assertEquals("", log.toString());
        stateMachine.setState(175);
        Assert.assertEquals("[0->6][6->19][19->58][58->175]", log.toString());
    }
}
