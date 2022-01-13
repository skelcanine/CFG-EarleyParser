import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {

	public static void main(String [] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		// Reading CFG from file
		Path fileName = Path.of("CFG.txt");
		String g = Files.readString(fileName);
		g = g.replace("\n", "");
		System.out.println( g + "\n" ); // Printing file content

		// Creating an object of Early class
		Earley ep = new Earley();
		// For getting input from user
		System.out.print("Enter a string to check: ");		
		String w = scanner.nextLine();
		System.out.println();
		// Getting the result
		boolean result = (ep.solve(g.split("\r"), w));
		System.out.println();

		if (result == true) {
			System.out.println("This string is accepted by given CFG\n");
		}

		else {
			System.out.println("This string is not accepted by given CFG");
			System.exit(0);
		}
		// Printing the tree
		printParse(ep.tree);

	}

	public static void printParse(LinkedList<String> tree) {
		int space = 0;		// For storing space value
		int tempeplt = 0;	// For storing "E+T" space value
		int temptf = 0;		// For storing "T*F" space value
		int tfcreate = 0;	// For storing which loop it created
		int epltcreate = 0;	// For storing which loop it created
		// LinkedLists for printing parse tree
		LinkedList<String> tree1 = new LinkedList<String>();
		LinkedList<String> tree2 = new LinkedList<String>();

		// Copying and reversing its order
		copyLists(tree,tree1);
		reverse(tree1);
		// Removing first element and transferring to a new one
		while (!tree1.isEmpty()) {
			removeOnes(tree1);
			transfer(tree1, tree2);
		}
		// For loop for printing
		for(int i = 0; i < tree2.size(); i++) {
			char[] spaces = new char[space];
			Arrays.fill(spaces, ' ');
			String temp3 = tree2.get(i);


			if (i != 0 && i != tree2.size()) {
				System.out.println(new String(spaces) + "|");
			}

			System.out.println(new String(spaces) + temp3);


			if(temp3.contains("(E)")) {
				if(temp3.contains("T*F")) {
					tfcreate = i;
					temptf = space +13;
				}
				if(temp3.contains("E+T")) {
					epltcreate = i;
					tempeplt = space +4;
				}
				space += temp3.length()-6;
			}
			if(temp3.contains("T*F")) {
				tfcreate = i;
				if (tfcreate > epltcreate) {
					space += 13;
				}
				if (tfcreate < epltcreate) {
					temptf = space + 13;
				}				
			}

			if(temp3.contains("E+T")){
				epltcreate = i;
				if (epltcreate > tfcreate) {
					space += 4;
				}
				if (epltcreate < tfcreate) {
					tempeplt = space +4;
				}					
			}

			if(!(temp3.contains("E+T")) && !(temp3.contains("T*F")) && !(temp3.contains("(E)"))) {

				if (epltcreate > tfcreate ) {
					space = temptf;

				}
				else if (tfcreate > epltcreate) {
					space = tempeplt;
				}
			}			
		}
	}

	// Function for removing added
	public static LinkedList<String> removeOnes(LinkedList<String> tree) {
		while ( !tree.isEmpty() && tree.getFirst().equals("1")) {
			tree.pollFirst();
		}
		return tree;
	}
	// Function for transferring
	public static LinkedList<String> transfer(LinkedList<String> tree, LinkedList<String> tree2){
		String temp = "";
		while ( !tree.isEmpty() && !tree.getFirst().equals("1")) {
			temp += tree.pollFirst();
			temp += " -> ";
		}
		if (temp != "") {
			tree2.add(temp);
		}
		return tree2;
	}
	// Function for reversing order
	public static LinkedList<String> reverse(LinkedList<String> tree){
		LinkedList<String> temp = new LinkedList<String>();
		while (!tree.isEmpty()) {
			temp.add(tree.pollLast());
		}
		copyLists(temp,tree);
		temp.clear();
		return tree;
	}
	// Function for copy lists
	public static void copyLists(LinkedList<String> tree, LinkedList<String> tree2) {
		for (int i = 0; i < tree.size(); i++) {
			tree2.add(i,tree.get(i));
		}
	}
}