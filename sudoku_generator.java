// sudoku_generator.java — Java версия

import java.io.*;
import java.util.*;

public class sudoku_generator {
    private int size = 9;
    private int boxSize = 3;
    private int[][] board;
    private int[][] solution;
    private int cellsToRemove;
    private String difficulty;
    private Random rand;

    public sudoku_generator(String difficulty, long seed) {
        this.board = new int[size][size];
        this.solution = new int[size][size];
        this.difficulty = difficulty;
        this.rand = seed != 0 ? new Random(seed) : new Random();
        this.cellsToRemove = difficulty.equals("medium") ? 45 : difficulty.equals("hard") ? 50 : 35;
    }

    public void generate() {
        solve(board);
        for (int i = 0; i < size; i++) {
            System.arraycopy(board[i], 0, solution[i], 0, size);
        }
        removeCells();
    }

    private boolean solve(int[][] board) {
        int[] empty = findEmpty(board);
        if (empty == null) return true;
        int row = empty[0], col = empty[1];
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= 9; i++) nums.add(i);
        Collections.shuffle(nums, rand);
        for (int num : nums) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num;
                if (solve(board)) return true;
                board[row][col] = 0;
            }
        }
        return false;
    }

    private int[] findEmpty(int[][] board) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 0) return new int[]{i, j};
            }
        }
        return null;
    }

    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int j = 0; j < size; j++) {
            if (board[row][j] == num) return false;
        }
        for (int i = 0; i < size; i++) {
            if (board[i][col] == num) return false;
        }
        int startRow = (row / boxSize) * boxSize;
        int startCol = (col / boxSize) * boxSize;
        for (int i = startRow; i < startRow + boxSize; i++) {
            for (int j = startCol; j < startCol + boxSize; j++) {
                if (board[i][j] == num) return false;
            }
        }
        return true;
    }

    private void removeCells() {
        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells.add(new int[]{i, j});
            }
        }
        Collections.shuffle(cells, rand);
        int removed = 0;
        for (int[] cell : cells) {
            if (removed >= cellsToRemove) break;
            board[cell[0]][cell[1]] = 0;
            removed++;
        }
    }

    public void printBoard(boolean showSolution) {
        int[][] boardToPrint = showSolution ? solution : board;
        System.out.println("┌───────┬───────┬───────┐");
        for (int i = 0; i < size; i++) {
            System.out.print("│");
            for (int j = 0; j < size; j++) {
                if (boardToPrint[i][j] == 0) {
                    System.out.print(" . ");
                } else {
                    System.out.print(" " + boardToPrint[i][j] + " ");
                }
                if (j % 3 == 2 && j < size - 1) System.out.print("│");
            }
            System.out.println("│");
            if (i % 3 == 2 && i < size - 1) {
                System.out.println("├───────┼───────┼───────┤");
            }
        }
        System.out.println("└───────┴───────┴───────┘");
    }

    public void saveTXT(String filename) throws IOException {
        try (FileWriter fw = new FileWriter(filename)) {
            for (int[] row : board) {
                for (int j = 0; j < row.length; j++) {
                    fw.write((row[j] == 0 ? "." : String.valueOf(row[j])) + (j < row.length - 1 ? " " : ""));
                }
                fw.write("\n");
            }
        }
        System.out.println("💾 Сохранено: " + filename);
    }

    public void saveJSON(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"puzzle\":[");
        for (int i = 0; i < board.length; i++) {
            sb.append("[");
            for (int j = 0; j < board[i].length; j++) {
                sb.append(board[i][j]);
                if (j < board[i].length - 1) sb.append(",");
            }
            sb.append("]");
            if (i < board.length - 1) sb.append(",");
        }
        sb.append("],\"solution\":[");
        for (int i = 0; i < solution.length; i++) {
            sb.append("[");
            for (int j = 0; j < solution[i].length; j++) {
                sb.append(solution[i][j]);
                if (j < solution[i].length - 1) sb.append(",");
            }
            sb.append("]");
            if (i < solution.length - 1) sb.append(",");
        }
        sb.append("],\"difficulty\":\"").append(difficulty).append("\",\"cells_removed\":").append(cellsToRemove).append("}");
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(sb.toString());
        }
        System.out.println("💾 Сохранено: " + filename);
    }

    public void saveHTML(String filename) throws IOException {
        String html = "<!DOCTYPE html>\n<html>\n<head><meta charset=\"UTF-8\"><title>Sudoku</title>\n" +
            "<style>\nbody { font-family: monospace; background: #1a1a2e; color: #fff; padding: 20px; }\n" +
            "table { border-collapse: collapse; margin: 20px auto; }\n" +
            "td { width: 40px; height: 40px; text-align: center; font-size: 20px; border: 1px solid #444; }\n" +
            "td:nth-child(3n) { border-right: 3px solid #fff; }\n" +
            "tr:nth-child(3n) td { border-bottom: 3px solid #fff; }\n" +
            ".empty { color: #555; }\n.given { color: #4fc3f7; }\n</style>\n</head>\n<body>\n" +
            "<h1 style=\"text-align:center;\">🧩 Sudoku (Medium)</h1>\n<table>\n";
        for (int i = 0; i < size; i++) {
            html += "<tr>";
            for (int j = 0; j < size; j++) {
                int val = board[i][j];
                String cls = val == 0 ? "empty" : "given";
                String display = val == 0 ? "·" : String.valueOf(val);
                html += "<td class=\"" + cls + "\">" + display + "</td>";
            }
            html += "</tr>\n";
        }
        html += "</table>\n<p style=\"text-align:center;\">💡 Решение доступно в JSON файле</p>\n</body>\n</html>";
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(html);
        }
        System.out.println("💾 Сохранено: " + filename);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("🧩 Sudoku Generator (Medium) (Java)");
        sudoku_generator gen = new sudoku_generator("medium", 0);
        gen.generate();

        System.out.println("\nГоловоломка:");
        gen.printBoard(false);

        Scanner scanner = new Scanner(System.in);
        System.out.print("\nПоказать решение? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("\nРешение:");
            gen.printBoard(true);
        }

        gen.saveTXT("sudoku_puzzle.txt");
        gen.saveJSON("sudoku_puzzle.json");
        gen.saveHTML("sudoku_puzzle.html");
        scanner.close();
    }
}
