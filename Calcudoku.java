import java.util.*;

public class Calcudoku {

	private static final String[] operators = {"*", "+", "/", "-"};
	private static int[][] board = new int[6][6];

	private static int[][] cageInput;
	private static String[] cageValueOperators;
	private static int cageCount;
	private static int[] cageTargets;
	private static int[] cageSize;
	private static char[] cageOperators;
	private static StringBuilder[] cageCoordinates;

	public static boolean rowColumnValid(int number, int row, int column) {
		for (int i = 0; i < 6; i++) {
			if (board[row][i] == number) {
				return false;
			}
		}

		for (int j = 0; j < 6; j++) {
			if (board[j][column] == number) {
				return false;
			}
		}

		return true;
	}

	public static boolean cageValid(int number, int row, int column) {
		board[row][column] = number;

		int cageNumber = cageInput[row][column];
		int target = cageTargets[cageNumber];
		int size = cageSize[cageNumber];
		int[] values = new int[size];
		String[] coordinates = cageCoordinates[cageNumber].toString().split(" ");
		boolean emptySlots = false;

		for (int i = 0, j = 0; i < size && j < 2 * size; i++) {
			int coordinateRow = Integer.parseInt(coordinates[j]);
			j++;
			int coordinateCol = Integer.parseInt(coordinates[j]);
			j++;
			values[i] = board[coordinateRow][coordinateCol];

			if (values[i] == 0) {
				emptySlots = true;
			}
		}

		switch (cageOperators[cageNumber]) {
			case 'n':
				if (number == target) {
					return true;
				} else {
					board[row][column] = 0;
					return false;
				}
			case '*':
				int multTarget = values[0];
				for (int i = 1; i < size; i++) {
					if (values[i] != 0) {
						multTarget = multTarget * values[i];
					}
				}

				if (emptySlots) {
					if (target % number == 0 && multTarget <= target) {
						return true;
					} else {
						board[row][column] = 0;
						return false;
					}
				} else {
					if (multTarget == target) {
						return true;
					} else {
						board[row][column] = 0;
						return false;
					}
				}
			case '+':
				int addTarget = values[0];
				for (int i = 1; i < size; i++) {
					addTarget = addTarget + values[i];
				}
				if (emptySlots) {
					if (addTarget < target) {
						return true;
					} else {
						board[row][column] = 0;
						return false;
					}
				} else {
					if (addTarget == target) {
						return true;
					} else {
						board[row][column] = 0;
						return false;
					}
				}
			case '/':
				int divTarget = 0;
				if (values[0] != 0 && values[1] != 0) {
					divTarget = Math.max(values[0], values[1])
									/ Math.min(values[0], values[1]);
				}
				if (emptySlots) {
					if (divTarget * target <= 6 || (divTarget / target >= 1 && divTarget % target == 0)) {
						return true;
					} else {
						board[row][column] = 0;
						return false;
					}
				} else {
					if (divTarget == target) {
						return true;
					} else {
						board[row][column] = 0;
						return false;
					}
				}
			case '-':
				int subtractTarget = Math.abs(values[0] - values[1]);

				if (emptySlots) {
					if (subtractTarget - target >= 1 || target + subtractTarget <= 6) {
						return true;
					} else {
						board[row][column] = 0;
						return false;
					}
				} else {
					if (subtractTarget == target) {
						return true;
					} else {
						board[row][column] = 0;
						return false;
					}
				}
		}

		return false;
	}

	public static boolean solve(int row, int column) {

		if (column == 6) {
			return solve(row + 1, 0);
		}

		if (row == 6) {
			return true;
		}

		if (board[row][column] != 0) {
			return solve(row, column + 1);
		}

		for (int i = 1; i <= 6; i++) {
			if (rowColumnValid(i, row, column) && cageValid(i, row, column)) {
				board[row][column] = i;
				if (solve(row, column + 1)) {
					return true;
				}
			}
		}

		board[row][column] = 0;
		return false;
	}

	public static void readInput() {
		Scanner keyboard = new Scanner(System.in);

		cageInput = new int[6][6];

		try {
			
			for (int i = 0; i < 6; i++) {
				String[] cageHolder = keyboard.nextLine().split(" ");
				for (int j = 0; j < 6; j++) {
					cageInput[i][j] = Integer.parseInt(cageHolder[j]);
				}
			}
			cageValueOperators = keyboard.nextLine().split(" ");

		} catch (ArrayIndexOutOfBoundsException|NumberFormatException e) {
			System.out.println("\nInvalid input.");
			System.exit(0);
		}

		keyboard.close();

		splitOperators();
		cageSizes();
	}

	public static void splitOperators() {
		cageCount = cageValueOperators.length;

		cageTargets = new int[cageCount];
		cageOperators = new char[cageCount];

		for (int i = 0; i < cageCount; i++) {
			for (String operator : operators) {
				if (cageValueOperators[i].endsWith(operator)) {
					int cutIndex = cageValueOperators[i].length() - 1;
					String stem = cageValueOperators[i].substring(0, cutIndex);

					cageTargets[i] = Integer.parseInt(stem);
					cageOperators[i] = operator.charAt(0);
				}
			}

			if (cageTargets[i] == 0) {
				cageTargets[i] = Integer.parseInt(cageValueOperators[i]);
				cageOperators[i] = 'n';
			}

		}
	}

	public static void cageSizes() {
		cageSize = new int[cageCount];
		cageCoordinates = new StringBuilder[cageCount];

		for (int i = 0; i < cageCount; i++) {
			cageCoordinates[i] = new StringBuilder();
			for (int j = 0; j < 6; j++) {
				for (int k = 0; k < 6; k++) {
					if (cageInput[j][k] == i) {
						cageSize[i]++;
						cageCoordinates[i].append(j + " " + k + " ");
					}
				}
			}
		}
	}

	public static void setSingleSquares() {
		boolean validSquares = true;

		for (int i = 0; i < cageCount; i++) {
			if (cageSize[i] == 1) {
				int number = cageTargets[i];

				String[] coordinates = cageCoordinates[i].toString().split(" ");
				int row = Integer.parseInt(coordinates[0]);
				int column = Integer.parseInt(coordinates[1]);

				if (rowColumnValid(number, row, column)) {
					board[row][column] = number;
				} else {
					validSquares = false;
					break;
				}
			}
		}

		if (!validSquares) {
			noSolution();
		}
	}

	public static void noSolution() {
		System.out.println("\nNo solution.");
		System.exit(0);
	}

	public static void printSolution() {
		System.out.println("\nSolution:\n");
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 5; j++) {
				System.out.print(board[i][j] + " ");
			}
			System.out.println(board[i][5]);
		}
	}
	
	public static void findSolution() {
		setSingleSquares();

		boolean solution = solve(0, 0);

		if (!solution) {
			noSolution();
		} else {
			printSolution();
		}
	}

	public static void main(String[] args) {

		readInput();
		findSolution();

	}
}
