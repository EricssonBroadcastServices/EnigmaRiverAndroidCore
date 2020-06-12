package com.redbeemedia.enigma.core.util;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class StateMachineBuilder<S> implements IStateMachineBuilder<S> {
    private Set<S> states = new HashSet<>();
    private List<DirectTransition> directTransitions = new ArrayList<>();
    private boolean initialStateSupplied = false;
    private S initialState;

    @Override
    public void addState(S state) {
        states.add(state);
    }

    @Override
    public void setInitialState(S initialState) {
        if(!states.contains(initialState)) {
            throw new IllegalArgumentException("State "+initialState+" not added");
        }
        initialStateSupplied = true;
        this.initialState = initialState;
    }

    @Override
    public void addDirectTransition(S fromState, S toState) {
        if(!states.contains(fromState)) {
            throw new IllegalArgumentException("State "+fromState+" not added");
        }
        if(!states.contains(toState)) {
            throw new IllegalArgumentException("State "+toState+" not added");
        }
        directTransitions.add(new DirectTransition(fromState, toState));
    }

    private StateMachineTopology<S> validateInternal() throws ValidationException {
        if(!initialStateSupplied) {
            throw new ValidationException("No initial state supplied");
        }
        return calculateStateMachineTopology();
    }

    private StateMachineTopology<S> calculateStateMachineTopology() throws ValidationException {
        return new StateMachineTopology<>(calculateShortestPaths());
    }

    private Map<S,Map<S, IPath<S>>> calculateShortestPaths() throws ValidationException {
        Map<S,Map<S, IPath<S>>> distanceMap = new HashMap<>();
        for(S state : states) {
            Map<S,IPath<S>> distances = new HashMap<>();
            distances.put(state, new Path());
            distanceMap.put(state, distances);
        }

        //Fill with direct transitions
        for(DirectTransition directTransition : directTransitions) {
            Map<S,IPath<S>> distances = distanceMap.get(directTransition.fromState);
            IPath<S> existingDistance = distances.get(directTransition.toState);
            if(existingDistance == null) {
                distances.put(directTransition.toState, new Path(directTransition.toState));
            } else {
                throw new DuplicateDirectTransitionSupplied(directTransition.fromState, directTransition.toState);
            }
        }

        //Propagate (need to do this max states.length()-2 times)
        for(int iteration = 0; iteration < states.size()-2; ++iteration) {
            for(S from : distanceMap.keySet()) {
                for(S to : new HashMap<>(distanceMap.get(from)).keySet()) {
                    if(equal(from, to)) {
                        continue;
                    }
                    Map<S, IPath<S>> pathsFromTo = distanceMap.get(to);
                    for(S toTo : pathsFromTo.keySet()) {
                        if(equal(to, toTo)) {
                            continue;
                        }
                        IPath<S> pathFromFromToToTo = distanceMap.get(from).get(to).addToEnd(pathsFromTo.get(toTo));
                        IPath<S> currentShortest = distanceMap.get(from).get(toTo);
                        if(currentShortest == null || currentShortest.length() > pathFromFromToToTo.length()) {
                            distanceMap.get(from).put(toTo, pathFromFromToToTo);
                        } else if(currentShortest.length() == pathFromFromToToTo.length()) {
                            if(!equal(currentShortest, pathFromFromToToTo)) {
                                distanceMap.get(from).put(toTo, new NonUniquePath(currentShortest, pathFromFromToToTo));
                            }
                        }
                    }
                }
            }
        }

        //Validate that all shortest paths are unique
        for(Map.Entry<S, Map<S, IPath<S>>> fromEntry : distanceMap.entrySet()) {
            for(Map.Entry<S, IPath<S>> toEntry : fromEntry.getValue().entrySet()) {
                if(NonUniquePath.class.isInstance(toEntry.getValue())) {
                    NonUniquePath nonUniquePath = ((NonUniquePath) toEntry.getValue());
                    throw new NonUniqueShortestPathException(fromEntry.getKey(), toEntry.getKey(), pickUniquePath(nonUniquePath.path1), pickUniquePath(nonUniquePath.path2));
                }
            }
        }

        return distanceMap;
    }


    private IPath<S> pickUniquePath(IPath<S> path) {
        if(NonUniquePath.class.isInstance(path)) {
            return pickUniquePath(((NonUniquePath) path).path1);
        }
        return path;
    }

    private static boolean equal(Object a, Object b) {
        return a == null ? b == null : (a == b || a.equals(b));
    }

    @Override
    public IStateMachine<S> build() {
        try {
            return new StateMachine<>(validateInternal(), initialState);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private class DirectTransition {
        public final S fromState;
        public final S toState;

        public DirectTransition(S fromState, S toState) {
            this.fromState = fromState;
            this.toState = toState;
        }
    }

    private static class StateMachineTopology<S> {
        private Map<Pair<S,S>,IPath<S>> shortestPaths;

        public StateMachineTopology(Map<S,Map<S,IPath<S>>> shortestPathsMap) {
            this.shortestPaths = new HashMap<>();
            for(Map.Entry<S, Map<S, IPath<S>>> entry : shortestPathsMap.entrySet()) {
                S fromState = entry.getKey();
                for(Map.Entry<S, IPath<S>> entry2 : entry.getValue().entrySet()) {
                    S toState = entry2.getKey();
                    this.shortestPaths.put(Pair.create(fromState, toState), entry2.getValue());
                }
            }
        }

        public IPath<S> getShortestPath(S from, S to) {
            return shortestPaths.get(Pair.create(from, to));
        }
    }

    private static class StateMachine<S> implements IStateMachine<S> {
        private final StateMachineTopology<S> topology;
        private S state;
        private Collector<IStateChangedListener<S>> collector = new Collector(IStateChangedListener.class);

        public StateMachine(StateMachineTopology<S> topology, S initialState) {
            this.topology = topology;
            this.state = initialState;
        }

        @Override
        public boolean addListener(IStateChangedListener<S> listener) {
            return collector.addListener(listener);
        }

        @Override
        public boolean removeListener(IStateChangedListener<S> listener) {
            return collector.removeListener(listener);
        }

        @Override
        public S getState() {
            return state;
        }

        @Override
        public void setState(S newState) {
            Iterable<S> shortestPath = topology.getShortestPath(state, newState);
            if(shortestPath == null) {
                throw new IllegalArgumentException("Illegal state transition: "+state+" -> "+newState);
            }
            for(S node : shortestPath) {
                S oldState = state;
                state = node;
                fireOnChange(oldState, state);
            }
        }

        private void fireOnChange(S from, S to) {
            collector.forEach(listener -> listener.onStateChanged(from, to));
        }
    }


    private interface IPath<S> extends Iterable<S> {
        int length();
        IPath<S> addToEnd(IPath<S> path);
    }

    private class Path implements IPath<S> {
        private final Object[] path;

        public Path() {
            this(new Object[0]);
        }

        public Path(S object) {
            this(new Object[]{object});
        }

        private Path(Object[] path) {
            this.path = path;
        }

        @Override
        public int length() {
            return path.length;
        }

        @Override
        public IPath<S> addToEnd(IPath<S> path) {
            if(NonUniquePath.class.isInstance(path)) {
                NonUniquePath nonUniquePath = (NonUniquePath) path;
                return new NonUniquePath(addToEnd(nonUniquePath.path1), addToEnd(nonUniquePath.path2));
            } else {
                Object[] otherPath = ((Path) path).path;
                Object[] newPath = new Object[this.path.length + otherPath.length];
                System.arraycopy(this.path, 0, newPath, 0, this.path.length);
                System.arraycopy(otherPath, 0, newPath, this.path.length, otherPath.length);
                return new Path(newPath);
            }
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            if(path.length > 0) {
                stringBuilder.append(path[0]);
                for (int i = 1; i < path.length; ++i) {
                    stringBuilder.append("->");
                    stringBuilder.append(path[i]);
                }
            }
            return stringBuilder.toString();
        }

        @Override
        public int hashCode() {
            int hashCode = path.length;
            for(Object node : path) {
                hashCode = (hashCode*37) ^ (node == null ? 0 : node.hashCode());
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof IPath)) {
                return false;
            }
            if(NonUniquePath.class.isInstance(obj)) {
                return false;
            }
            if(this.length() != ((IPath<S>) obj).length()) {
                return false;
            }
            Iterator<S> p1it = this.iterator();
            Iterator<S> p2it = ((IPath<S>) obj).iterator();
            while(p1it.hasNext()) {
                S fromP1 = p1it.next();
                S fromP2 = p2it.next();
                if(!equal(fromP1, fromP2)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Iterator<S> iterator() {
            return new Iterator<S>() {
                private int nextIndex = 0;
                @Override
                public boolean hasNext() {
                    return nextIndex < path.length;
                }

                @Override
                public S next() {
                    return (S) path[nextIndex++];
                }
            };
        }
    }

    private class NonUniquePath implements IPath<S> {
        private IPath<S> path1;
        private IPath<S> path2;

        public NonUniquePath(IPath<S> path1, IPath<S> path2) {
            if(NonUniquePath.class.isInstance(path1)) {
                path1 = pickUniquePath(path1);
            }
            if(NonUniquePath.class.isInstance(path2)) {
                path2 = pickUniquePath(path2);
            }
            this.path1 = path1;
            this.path2 = path2;
            if(path1.length() != path2.length()) {
                throw new IllegalStateException();
            }
        }

        @Override
        public int length() {
            return path1.length();
        }

        @Override
        public IPath<S> addToEnd(IPath<S> path) {
            return new NonUniquePath(path1.addToEnd(path), path2.addToEnd(path));
        }

        @Override
        public Iterator<S> iterator() {
            throw new UnsupportedOperationException();
        }
    }

    private static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    private static class NonUniqueShortestPathException extends ValidationException {
        public NonUniqueShortestPathException(Object from, Object to, Iterable<?> shortestPath, Iterable<?> otherShortestPath) {
            super(from+"->"+to+" has multiple shortest paths: "+shortestPath+" and "+otherShortestPath);
        }
    }

    private static class DuplicateDirectTransitionSupplied extends ValidationException {
        public DuplicateDirectTransitionSupplied(Object fromState, Object toState) {
            super("Duplicate transition supplied for "+fromState+" -> "+toState);
        }
    }
}
