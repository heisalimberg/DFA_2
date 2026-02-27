package fa.nfa;


import java.util.*;

import fa.State;

/**
 * CS361 Project 2.
 * @author Abdulalim Ciftci
 */
public class NFAState extends State implements Comparable<NFAState> {

    // The name of this state.
    public String name;
    
    // This is the core of the state: a map where
    // Key = the character (like 'a' or 'e')
    // Value = a set of states we can go to on that char.
    public Map<Character, HashSet<NFAState>> transitions;

    /**
     * Constructor for an NFAState object
     * @param name name for state
     */
    public NFAState(String name) {
        // We set our *own* public 'name' field.
        this.name = name;
  
        // Always initialize the map!
        this.transitions = new HashMap<Character, HashSet<NFAState>>();
    }

    /**
     * Simple getter for the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param state the object to be compared
     * @return int for comparison
     */
    public int compareTo(NFAState state) {
         return state.getName().compareTo(this.getName());
    }

    /*
     * Adds a set of destination states for a given symbol.
	 * @param toStates is the set of NFAStates where the transitions end
	 * @param onSymb is the symbol (char) that the transition is taken on
     */
    public void addTransition(HashSet<NFAState> toStates, char onSymb) {
        // This one line does everything:
        // 1. It tries to get the Set for 'onSymb'.
        // 2. If it can't find one (i.e., it's a new symbol),
        //    it runs the 'k -> new HashSet<>()' part to create a new, empty set
        //    and puts it in the map.
        // 3. It then returns either the *existing* set or the *new* set.
        // 4. Finally, .addAll(toStates) adds all the new destinations
        //    to whichever set was returned.
        this.transitions.computeIfAbsent(onSymb, k -> new HashSet<>()).addAll(toStates);
    }

}