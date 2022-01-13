import java.util.*;


// prodHead > prodRhs 
class Production {
	public Character prodHead; // head 
	public Character[] prodRhs; // right hand side 

	// Constructor 
	public Production(Character prodHead, Character[] prodRhs) {
		this.prodHead = prodHead;
		this.prodRhs = prodRhs;
	}

	public Production(String prodHead, String prodRhs) {
		// assert(prodHead.length() == 1);
		this.prodHead = prodHead.charAt(0);
		this.prodRhs = toCharacterArray(prodRhs.toCharArray());
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof Production)) {
			return false;
		} else {
			Production p = (Production)o;
			return prodHead.equals(p.prodHead) && Arrays.equals(prodRhs, p.prodRhs);
		}
	}

	// Helper 
	private static Character[] toCharacterArray(char[] array) {
		Character[] chArray = new Character[array.length];
		for (int i = 0; i < array.length; ++i) { chArray[i] = array[i]; }
		return chArray;
	}
}


class Grammar {
	public ArrayList<Production> productions; // P 

	// Constructor 
	public Grammar() {
		productions = new ArrayList<Production>();
	}

	public Grammar(ArrayList<Production> productions) {
		this.productions = productions; 
	}

	public boolean addProduction(Production prod) {
		return !productions.contains(prod) && productions.add(prod);
	}

	public boolean addProduction(Character prodHead, Character[] prodRhs) {
		return addProduction(new Production(prodHead, prodRhs));
	}

	public boolean addProduction(String prodHead, String prodRhs) {
		return addProduction(new Production(prodHead, prodRhs));
	}

	// Compute the set of nullable nonterminals: { A in V | A =>* eps } 
	public Set<Character> getNullable() {
		Set<Character> nullSet = new TreeSet<Character>();
		for (Production p : productions) {
			if (p.prodRhs[0] == '@') { nullSet.add(p.prodHead); } //Empty symbol is @
		}
		if (nullSet.size() != 0) {
			boolean isNullable = true; 
			int currentSize = nullSet.size();
			do {
				currentSize = nullSet.size();
				for (Production p : productions) {
					isNullable = true;
					for (Character c : p.prodRhs) {
						if (Character.isLowerCase(c) && Character.isLetter(c) || !nullSet.contains(c)) {
							isNullable = false; break;
						}
					}
					if (isNullable) { nullSet.add(p.prodHead); }
				}
			} while (currentSize != nullSet.size());
		}
		return nullSet;
	}
}

// State 
class State {
	public Production prod;
	public int rhsIdx;
	public int prevSet;

	// Constructor 
	public State(Production prod, int rhsIdx, int prevSet) {
		this.prod = prod; // production 
		this.rhsIdx = rhsIdx; // position of the dot on the right-hand-side of the production 
		this.prevSet = prevSet;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof State)) {
			return false;
		} else {
			State s = (State)o;
			return rhsIdx == s.rhsIdx && prevSet == s.prevSet && prod.equals(s.prod);
		}
	}
}

public class Earley {


	public LinkedList<String> tree = new LinkedList<String>();

	private Grammar g;
	private ArrayList<LinkedList<State> > stateSets;

	public ArrayList<LinkedList<State>> getStateSets() {
		return stateSets;
	}

	public Earley() {
		g = new Grammar();
		stateSets = new ArrayList<LinkedList<State> >();
	}

	// Helper methods 
	private boolean isNonterminal(Character c) {
		return Character.isUpperCase(c) || c.equals('@');
	}

	// Prints state set contents 
	private void print() {
		System.out.println("Production Head , Dot Location; Origin\n");
		for (int idx = 0; idx < stateSets.size(); ++idx) {
			System.out.println("------ State set " + idx + " -----------");
			for (State s : stateSets.get(idx)) {
				String rhs = Arrays.toString(s.prod.prodRhs).replaceAll(", ", "").substring(1, s.prod.prodRhs.length + 1);
				System.out.println(s.prod.prodHead + " -> " + rhs + ", " + s.rhsIdx + " ; " + s.prevSet);
			}
		}
	}
	// Store set contents in a LinkedList and add "1" after each state set
	public void tree() {
		String temp = "";		
		for (int idx = 0; idx < stateSets.size(); ++idx) {
			for (State s : stateSets.get(idx)) {
				String rhs = Arrays.toString(s.prod.prodRhs).replaceAll(", ", "").substring(1, s.prod.prodRhs.length + 1);
				if(rhs.length() == s.rhsIdx && s.prod.prodHead.charValue() != '@') {
					temp = temp + (s.prod.prodHead + " > " + rhs);
					tree.add(temp);
					temp = "";
				}
			}
			tree.add("1");	
		}
	}
	// Scanner function
	private void scanner(State state, int currentIndex) {
		State newState = new State(state.prod, state.rhsIdx+1, state.prevSet);
		if (!stateSets.get(currentIndex+1).contains(newState)) {
			stateSets.get(currentIndex+1).add(newState);
		}
	}

	// Predictor function
	private void predictor(State state, int currentIndex, Set<Character> nullableVars) {
		Character B = state.prod.prodRhs[state.rhsIdx];
		for (Production p : g.productions) {
			if (p.prodHead.equals(B)) {
				State newState = new State(p, 0, currentIndex);
				if (!stateSets.get(currentIndex).contains(newState)) {
					stateSets.get(currentIndex).add(newState);
				}
			}
		}
		if (nullableVars.contains(B)) { 
			State newState = new State(state.prod, state.rhsIdx+1, state.prevSet);
			if (!stateSets.get(currentIndex).contains(newState)) {
				stateSets.get(currentIndex).add(newState);
			}
		}
	}
	// Completer function
	private void completer(State state, int currentIndex) {
		int j = state.prevSet;
		LinkedList<State> stateSet = stateSets.get(j);
		for (int i = 0; i < stateSet.size(); ++i) {
			State s = stateSet.get(i);
			if (s.rhsIdx < s.prod.prodRhs.length && s.prod.prodRhs[s.rhsIdx].equals(state.prod.prodHead)) {
				State newState = new State(s.prod, s.rhsIdx+1, s.prevSet);
				if (!stateSets.get(currentIndex).contains(newState)) {
					stateSets.get(currentIndex).add(newState);
				}
			}
		}
	}

	private void initialize(int n) {
		// Add the initial state set S_0
		Production newProd = new Production("@", "E");
		LinkedList<State> initState = new LinkedList<State>();
		initState.add(new State(newProd, 0, 0));
		stateSets.add(initState);

		for (int i = 0; i < n; ++i) {
			stateSets.add(new LinkedList<State>());
		}
	}

	private void process(int i, Character a, Set<Character> nullableVars) {
		LinkedList<State> stateSet = stateSets.get(i);
		int currentSize = 0;
		do {
			currentSize = stateSet.size();
			// Iterate over the states from the current state set 
			for (int j = 0; j < currentSize; ++j) {
				State state = stateSet.get(j);
				// Apply either scanner, predictor, or completer 
				if (state.rhsIdx == state.prod.prodRhs.length) {
					completer(state, i); 
				} else {
					if (isNonterminal(state.prod.prodRhs[state.rhsIdx])) {
						predictor(state, i, nullableVars);
					} else if (state.prod.prodRhs[state.rhsIdx].equals(a)) {
						scanner(state, i);
					} // Else Nothing left to do 
				}
			}
		} while (currentSize != stateSet.size());
	}
	// Solve function
	public boolean solve(String[] grammar, String word) {
		// Load the grammar 
		for (String s : grammar) {
			for (String rhs : s.split(">")[1].split("\\|")) {
				g.addProduction(s, rhs);
			}
		}
		// Compute nullable symbols 
		Set<Character> nullableVars = g.getNullable();
		// Run the earley recognizer 
		initialize(word.length());
		for (int i = 0; i < word.length(); ++i) {
			Character a = word.charAt(i);
			process(i, a, nullableVars);
		}
		process(word.length(), '/', nullableVars); 

		State finalState = new State(new Production("@", "E"), 1, 0);
		LinkedList<State> lastStateSet = stateSets.get(word.length());
		print();	// Printing states
		tree();		// Getting sets for printing tree
		for (State s : lastStateSet) {
			if (s.equals(finalState)) {
				return true;
			}
		}
		return false;
	}
}
