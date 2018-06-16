import java.util.*;

public class Calcudoku2018 {
	private static final String[] OPERATORS = {"+", "-", "*", "/"};
	private static int boardSize = 0;

	private static class Square {
		private int cageID;
		private int value;

		public Square(int cage) {
			cageID = cage;
			value = 0;
		}

		public int getCageID() {
			return cageID;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int newValue) {
			value = newValue;
		}
	}

	private static class Cage {
		private List<Square> squares;
		private int size;
		private int target;
		private char operation;

		public Cage(List<Square> squareList, int target, char op) {
			this.squares = squareList;
			this.size = 0;
			this.target = target;
			this.operation = op;
		}

		public void addSquare(Square s) {
			squares.add(s);
			size++;
		}

		public int getSize() {
			return size;
		}

		public int getTarget() {
			return target;
		}

		public char getOperator() {
			return operation;
		}

		public int emptySquareCount() {
			int count = 0;
			for (Square s : this.squares) {
				if (s.getValue() == 0) {
					count++;
				}
			}

			return count;
		}

		public boolean testNewNumber(int proposedValue) {

			if (proposedValue <= 0 || proposedValue > boardSize) {
				return false;
			}

			int emptyCount = this.emptySquareCount();

			if (emptyCount == 0) {
				return false;
			}

			if (operation == 'n') {
				return (proposedValue == this.target);
			}

			if (operation == '+') {
				int currentSum = 0;
				for (Square s : squares) {
					currentSum += s.getValue();
				}

				if (emptyCount == 1) {
					return (currentSum + proposedValue == target);
				}

				return (currentSum + proposedValue < target);
			}

			if (operation == '-') {
				if (emptyCount == 2) {
					return (proposedValue - target >= 1 || target + proposedValue <= boardSize);
				}

				if (emptyCount == 1) {
					int currentTotal = squares.get(0).getValue();
					return Math.abs(currentTotal - proposedValue) == target;
				}

				return false;
			}

			if (operation == '*') {
				int currentTotal = 1;
				for (Square s : squares) {
					if (s.getValue() != 0) {
						currentTotal *= s.getValue();
					}
				}

				if (emptyCount == 1) {
					return (currentTotal * proposedValue == target);
				}

				return (target % (currentTotal * proposedValue) == 0);
			}

			if (operation == '/') {
				if (emptyCount == 1) {
					int currentTotal = squares.get(0).getValue();
					int min = Math.min(currentTotal, proposedValue);
					int max = Math.max(currentTotal, proposedValue);
					return ((max % min == 0) && (max / min == target));
				}

				return (proposedValue * target <= boardSize || proposedValue % target == 0);
			}

			return false;
		}

	}

	private static class Pair<A, B> {
		private final A a;
		private final B b;

		public Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}

		public A getA() {
			return this.a;
		}

		public B getB() {
			return this.b;
		}
	}

	public static boolean solveBoard(Square[][] board, Cage[] cages) {
		return solve(board, cages, 0, 0);
	}

	public static boolean solve(Square[][] board, Cage[] cages, int x, int y) {

		if (x == boardSize) {
			return solve(board, cages, 0, y + 1);
		}

		if (y == boardSize) {
			return true;
		}

		if (board[y][x].getValue() != 0) {
			return solve(board, cages, x + 1, y);
		}

		Square currentSquare = board[y][x];
		Cage currentCage = cages[currentSquare.getCageID()];

		for (int i = 1; i <= boardSize; i++) {
			if (checkRowColumn(i, x, y, board) && currentCage.testNewNumber(i)) {
				board[y][x].setValue(i);
				if (solve(board, cages, x + 1, y)) {
					return true;
				}
			}
			board[y][x].setValue(0);
		}

		return false;
	}

	public static boolean checkRowColumn(int value, int x, int y, Square[][] board) {

		for (int i = 0; i < boardSize; i++) {
			if (board[y][i].getValue() == value) {
				return false;
			}
		}

		for (int j = 0; j < boardSize; j++) {
			if (board[j][x].getValue() == value) {
				return false;
			}
		}
		return true;
	}

	public static Pair<Square[][], Cage[]> readInput() {
		Scanner keyboard = new Scanner(System.in);

		/*
		try {
			System.out.print("Enter board size (1-6): ");
			boardSize =  Integer.parseInt(keyboard.nextLine());
		} catch (NumberFormatException e) {
			keyboard.close();
			badInput("board size must be integer");
		}

		if (boardSize > 6 || boardSize <= 0) {
			keyboard.close();
			badInput("board size must be 1-6");
		}
		*/
		boardSize = 6;

		String[][] inputLines = new String[boardSize][];
		Square[][] board = new Square[boardSize][boardSize];
		int cageCount = -1;
		Cage[] cages = null;

		try {

			for (int i = 0; i < boardSize; i++) {
				inputLines[i] = keyboard.nextLine().split(" ");
				Integer.parseInt(inputLines[0][0]);
				if (inputLines[i].length != boardSize) {
					keyboard.close();
					badInput("input size must match board size");
				}
			}

			for (int y = 0; y < boardSize; y++) {
				for (int x = 0; x < boardSize; x++) {
					int cageNumber = Integer.parseInt(inputLines[y][x]);
					if (cageNumber < 0) {
						keyboard.close();
						badInput("cage ID cannot be less than 0");
					}
					board[y][x] = new Square(cageNumber);
					if (cageCount <= cageNumber) {
						cageCount = cageNumber + 1;
					}
				}
			}

			if (cageCount <= 0) {
				keyboard.close();
				badInput("no cages");
			}

			if (board[0][0].getCageID() != 0) {
				keyboard.close();
				badInput("top left corner must be cage 0");
			}

			String[] cageOperationInput = keyboard.nextLine().split(" ");
			if (cageOperationInput.length != cageCount) {
				keyboard.close();
				badInput("too many or too few cage operators");
			}

			cages = new Cage[cageCount];

			for (int i = 0; i < cageCount; i++) {
				String opInput = cageOperationInput[i];
				int cageTarget = -1;
				char cageOperator = 0;

				for (String operator : OPERATORS) {
					if (opInput.endsWith(operator)) {
						int cutIndex = opInput.length() - 1;
						cageTarget = Integer.parseInt(opInput.substring(0, cutIndex));
						cageOperator = opInput.charAt(cutIndex);
					}
				}

				if (opInput.length() == 1) {
					cageTarget = Integer.parseInt(opInput);
					cageOperator = 'n';
				}

				if (cageTarget < 1) {
					keyboard.close();
					badInput("cage target must be greater than 0");
				}

				cages[i] = new Cage(new ArrayList<Square>(), cageTarget, cageOperator);
			}

		} catch (NumberFormatException e) {
			keyboard.close();
			badInput("wrong format");
		}

		keyboard.close();

		return new Pair<Square[][], Cage[]>(board, cages);

	}

	public static void initializeCages(Square[][] board, Cage[] cages) {
		if (board == null || cages == null) {
			badInput("error: board or cage is null");
		}

		for (int y = 0; y < boardSize; y++) {
			for (int x = 0; x < boardSize; x++) {
				Square s = board[y][x];
				int cageID = s.getCageID();
				Cage c = cages[cageID];

				if (c.getOperator() == 'n') {
					if (c.getSize() != 0) {
						badInput("size of cage with no operator must be 1");
					}

					int target = c.getTarget();
					if (!checkRowColumn(target, x, y, board)) {
						noSolution("single cages are invalid");
					}
					s.setValue(target);
				}

				if (c.getOperator() == '+') {
					if (c.getTarget() <= 1) {
						noSolution("cage with addition operator should have target greater than 1");
					}
				}

				if (c.getOperator() == '-') {
					if (c.getSize() >= 2) {
						badInput("size of cage with subtract operator must be 2");
					}

					if (c.getTarget() <= 0) {
						noSolution("cage with subtract operator should have target greater than 0");
					}

					if (c.getTarget() >= boardSize) {
						noSolution("cage with subtract operator cannot have target greater than or equal to board dimension");
					}
				}

				if (c.getOperator() == '/') {
					if (c.getSize() >= 2) {
						badInput("size of cage with divide operator must be 2");
					}

					if (c.getTarget() <= 1) {
						noSolution("cage with divide operator should have target greater than 1");
					}

					if (c.getTarget() > boardSize) {
						noSolution("cage with divide operator cannot have target greater than board dimensions");
					}
				}

				if (c.getOperator() == '*') {
					if (c.getTarget() <= 1) {
						noSolution("cage with multiply operator should have target greater than 1");
					}
				}

				c.addSquare(s);
			}
		}

		for (int i = 0; i < cages.length; i++) {
			if (cages[i].getOperator() != 'n' && cages[i].getSize() == 1) {
				badInput("size of cage with operator must be at least 2");
			}
		}
	}

	public static void badInput(String s) {
		System.out.println("Invalid input (" + s + ").");
		System.exit(1);
	}

	public static void noSolution() {
		System.out.println("\nNo solution.");
		System.exit(0);
	}

	public static void noSolution(String s) {
		System.out.print("\nNo solution");
		// System.out.print(" (" + s + ")");
		System.out.println(".");
		System.exit(0);
	}

	public static void printSolution(Square[][] board) {
		System.out.println("\nSolution:\n");

		for (int y = 0; y < boardSize; y++) {
			for (int x = 0; x < boardSize; x++) {
				System.out.print(board[y][x].getValue());
				if (x == boardSize - 1) {
					System.out.println();
				} else {
					System.out.print(" ");
				}
			}
		}
	}

	public static void testInputParsing(Square[][] board, Cage[] cages) {
		System.out.println("Board (size: " + boardSize + ")");
		for (int y = 0; y < boardSize; y++) {
			for (int x = 0; x < boardSize; x++) {
				System.out.print(board[y][x].getCageID());
				if (x == boardSize - 1) {
					System.out.println();
				} else {
					System.out.print(" ");
				}
			}
		}

		int cageCount = cages.length;
		System.out.println("\nCages (count: " + cageCount + ")");
		for (int i = 0; i < cageCount; i++) {
			Cage c = cages[i];
			int cageTarget = c.getTarget();
			char cageOp = c.getOperator();
			int cageSize = c.getSize();
			int empty = c.emptySquareCount();

			System.out.printf("Cage %d (size %d) (empty %d): %d%c%n", i, cageSize, empty, cageTarget, cageOp);
		}
	}

	public static void main(String[] args) {

		Pair<Square[][], Cage[]> pair = readInput();
		Square[][] board = (Square[][])pair.getA();
		Cage[] cages = (Cage[])pair.getB();

		initializeCages(board, cages);

		// TODO: check cage connections?
		// testInputParsing(board, cages);

		if (solveBoard(board, cages)) {
			printSolution(board);
		} else {
			noSolution("could not find solution");
		}

	}
}
