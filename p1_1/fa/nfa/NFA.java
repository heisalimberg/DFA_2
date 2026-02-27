package fa.nfa;

import fa.nfa.NFAState;
import fa.nfa.NFAInterface;

import java.util.*;

/**
 * CS361 Project 2 (non-deterministic automata)
 * @author Abdulalim Ciftci
 */
public class NFA implements NFAInterface {
    
    private Set<Character> alphabet;
    
    // Using a Map to store states by their name (String).
    private Map<String, NFAState> states;
    
    // The single start state
    private NFAState startState;
    
    // Set of all accepting states
    private Set<NFAState> finalStates;
    

    /**
     * Basic constructor. Just initialize all the sets and maps.
     */
    public NFA() {
        this.alphabet = new HashSet<>();
        this.states = new HashMap<>();
        this.startState = null;
        this.finalStates = new HashSet<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public void addSigma(char symbol) {
        this.alphabet.add(symbol);
    }
	
    /**
     * {@inheritDoc}
     */
    @Override
	public Set<Character> getSigma() {
        return this.alphabet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public boolean addState(String name) {
        // Check if the state name already exists in our map.
        if (this.states.containsKey(name)) {
            return false;
        }
        // If not, add it.
        this.states.put(name, new NFAState(name));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public NFAState getState(String name) {
        // Just pull the state directly from the map.
        return this.states.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public boolean setStart(String name) {
        NFAState state = this.states.get(name);
        if (state != null) {
            this.startState = state;
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public boolean setFinal(String name) {
        // Find the state by name, then add the *object* to the finalStates set.
        NFAState state = this.states.get(name);
        if (state != null) {
            this.finalStates.add(state);
            return true;
        }
        return false;
    }
	
    /**
     * {@inheritDoc}
     */
    @Override
	public boolean isStart(String name) {
        if (name == null || this.startState == null) {
            return false;
        }
        // Using .equals() for strings.
        return this.startState.getName().equals(name);
    }
	
    /**
     * {@inheritDoc}
     */
    @Override
	public boolean isFinal(String name) {
        NFAState state = this.states.get(name);
        if (state == null) {
            return false; 
        }
        // Check if the state object is in our set of final states.
        return this.finalStates.contains(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public boolean addTransition(String fromStateName, Set<String> toStateNames, char onSymb) {
        // Get the actual NFAState object from the name
        NFAState from = this.states.get(fromStateName);
        if (from == null) {
            return false; 
        }

        // Check if the symbol is 'e' or if it's in our alphabet
        if (onSymb != 'e' && !this.alphabet.contains(onSymb)) {
            return false; 
        }

        
        HashSet<NFAState> destinationStates = new HashSet<>();
        for (String toName : toStateNames) {
            NFAState to = this.states.get(toName);
            if (to == null) {
                // one of the destination states doesn't exist
                return false; 
            }
            destinationStates.add(to);
        }
        
        // Now, tell the 'from' state to add this transition.
        from.addTransition(destinationStates, onSymb);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public Set<NFAState> getToState(NFAState from, char onSymb) {
        
    	Set<NFAState> result = new HashSet<>();
        if(from.transitions.containsKey(onSymb)) {
            result.addAll(from.transitions.get(onSymb));
        }
        result.addAll(eClosure(from)); 
        return result;
    }
	
    /**
     * {@inheritDoc}
     * This finds all states reachable by only epsilon ('e') moves.
     * Using a Stack to do a Depth-First Search (DFS).
     */
    @Override
	public Set<NFAState> eClosure(NFAState s) {
        Set<NFAState> closure = new HashSet<>();
        Stack<NFAState> stack = new Stack<>();
        
        // Make sure the state is valid before we start
        if (s == null || !this.states.containsKey(s.getName())) {
            return closure; // return empty set
        }

        // Start by adding the first state to the stack and the closure set
        stack.push(s);
        closure.add(s);
        
        // Loop while we still have states to check
        while (!stack.isEmpty()) {
            NFAState current = stack.pop();
            // Check if this state has 'e' transitions
            if (current.transitions.containsKey('e')) {
                // Loop over all states it can reach with 'e'
                for (NFAState nextState : current.transitions.get('e')) {
                    // If we haven't seen this state yet...
                    if (!closure.contains(nextState)) {
                        stack.push(nextState); // ...add it to the stack to check later
                        closure.add(nextState); // ...and add it to our set.
                    }
                }
            }
        }
        return closure;
    }

    /**
     * {@inheritDoc}
     * This runs the "machine" on the input string.
     */
    @Override
	public boolean accepts(String s) {
        // 'currentStates' holds all the states we are currently "in".
        // Start with the e-closure of the start state.
        Set<NFAState> currentStates = eClosure(this.startState);
        
        // Loop through each character in the string
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // 'nextStates' will be all the states we can get to from our current ones
            Set<NFAState> nextStates = new HashSet<>();

            for (NFAState state : currentStates) {
                // Check if there's a transition on this char
                if (state.transitions.containsKey(c)) {
                    // If yes, find all target states
                    for (NFAState targetState : state.transitions.get(c)) {
                        // And add their e-closures to the next set
                        nextStates.addAll(eClosure(targetState));
                    }
                }
            }
            // Update our current states for the next loop
            currentStates = nextStates;
        }
        
        // After the loop, check if *any* of our final states
        // are in the 'finalStates' set.
        for (NFAState state : currentStates) {
            if (this.finalStates.contains(state)) {
                return true; // Found one! It's accepted.
            }
        }

        return false; // No current state was a final state.
    }
	

    /**
     * {@inheritDoc}
     */
    @Override
	public int maxCopies(String s) {
        Set<NFAState> currentStates = eClosure(this.startState);
        int copyMax = currentStates.size();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            Set<NFAState> nextStates = new HashSet<>();

            for (NFAState state : currentStates) {
                if (state.transitions.containsKey(c)) {
                    for (NFAState targetState : state.transitions.get(c)) {
                        nextStates.addAll(eClosure(targetState));
                    }

                     if (nextStates.size() > copyMax) {
                        copyMax = nextStates.size();
                    }
                }
            }
            currentStates = nextStates;
        }

        return copyMax;
    }
	
    /**
     * {@inheritDoc}
     * A DFA has two rules:
     * 1. No 'e' moves.
     * 2. Only one move per symbol from any state.
     */
    @Override
	public boolean isDFA() {
        if (this.states.isEmpty()) {
            return false;
        }

        for (NFAState state : this.states.values()) {
            
            // Rule 1: Check for epsilon transitions
            if (state.transitions.containsKey('e') && !state.transitions.get('e').isEmpty()) {
                return false; // Found an 'e' move
            }

            // Rule 2: Check for multiple transitions on the same symbol
            for (char symbol : this.alphabet) {
                if (state.transitions.containsKey(symbol)) {
                    if (state.transitions.get(symbol).size() > 1) {
                        return false; // Found multiple moves on one symbol
                    }
                }
            }
        }
        
        // If we get through all states and rules, it's a DFA!
        return true;
    }

}