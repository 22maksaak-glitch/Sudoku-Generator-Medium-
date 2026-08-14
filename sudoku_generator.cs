// sudoku_generator.cs — C# версия

using System;
using System.Collections.Generic;
using System.IO;
using System.Text.Json;

class SudokuGenerator {
    private int size = 9;
    private int boxSize = 3;
    private int[,] board;
    private int[,] solution;
    private int cellsToRemove;
    private string difficulty;
    private Random rand;

    public SudokuGenerator(string difficulty, int seed = 0) {
        board = new int[size, size];
        solution = new int[size, size];
        this.difficulty = difficulty;
        this.rand = seed != 0 ? new Random(seed) : new Random();
        this.cellsToRemove = difficulty == "medium" ? 45 : difficulty == "hard" ? 50 : 35;
    }

    public void Generate() {
        Solve(board);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                solution[i, j] = board[i, j];
            }
        }
        RemoveCells();
    }

    private bool Solve(int[,] board) {
        var empty = FindEmpty(board);
        if (empty == null) return true;
        int row = empty[0], col = empty[1];
        var nums = new List<int>();
        for (int i = 1; i <= 9; i++) nums.Add(i);
        Shuffle(nums);
        foreach (int num in nums) {
            if (IsValid(board, row, col, num)) {
                board[row, col] = num;
                if (Solve(board)) return true;
                board[row, col] = 0;
            }
        }
        return false;
    }

    private int[] FindEmpty(int[,] board) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i, j] == 0) return new int[] { i, j };
            }
        }
        return null;
    }

    private bool IsValid(int[,] board, int row, int col, int num) {
        for (int j = 0; j < size; j++) {
            if (board[row, j] == num) return false;
        }
        for (int i = 0; i < size; i++) {
            if (board[i, col] == num) return false;
        }
        int startRow = (row / boxSize) * boxSize;
        int startCol = (col / boxSize) * boxSize;
        for (int i = startRow; i < startRow + boxSize; i++) {
            for (int j = startCol; j < startCol + boxSize; j++) {
                if (board[i, j] == num) return false;
            }
        }
        return true;
    }

    private void RemoveCells() {
        var cells = new List<(int, int)>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells.Add((i, j));
            }
        }
        Shuffle(cells);
        int removed = 0;
        foreach (var cell in cells) {
            if (removed >= cellsToRemove) break;
            board[cell.Item1, cell.Item2] = 0;
            removed++;
        }
    }

    private void Shuffle<T>(List<T> list) {
        for (int i = list.Count - 1; i > 0; i--) {
            int j = rand.Next(i + 1);
            (list[i], list[j]) = (list[j], list[i]);
        }
    }

    public void PrintBoard(bool showSolution) {
        var boardToPrint = showSolution ? solution : board;
        Console.WriteLine("┌───────┬───────┬───────┐");
        for (int i = 0; i < size; i++) {
            Console.Write("│");
            for (int j = 0; j < size; j++) {
                if (boardToPrint[i, j] == 0) {
                    Console.Write(" . ");
                } else {
                    Console.Write($" {boardToPrint[i, j]} ");
                }
                if (j % 3 == 2 && j < size - 1) Console.Write("│");
            }
            Console.WriteLine("│");
            if (i % 3 == 2 && i < size - 1) {
                Console.WriteLine("├───────┼───────┼───────┤");
            }
        }
        Console.WriteLine("└───────┴───────┴───────┘");
    }

    public void SaveTXT(string filename) {
        using var writer = new StreamWriter(filename);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                writer.Write((board[i, j] == 0 ? "." : board[i, j].ToString()) + (j < size - 1 ? " " : ""));
            }
            writer.WriteLine();
        }
        Console.WriteLine($"💾 Сохранено: {filename}");
    }

    public void SaveJSON(string filename) {
        var data = new {
            puzzle = board,
            solution = solution,
            difficulty = difficulty,
            cells_removed = cellsToRemove
        };
        string json = JsonSerializer.Serialize(data, new JsonSerializerOptions { WriteIndented = true });
        File.WriteAllText(filename, json);
        Console.WriteLine($"💾 Сохранено: {filename}");
    }

    public void SaveHTML(string filename) {
        string html = @"<!DOCTYPE html>
<html>
<head><meta charset=""UTF-8""><title>Sudoku</title>
<style>
body { font-family: monospace; background: #1a1a2e; color: #fff; padding: 20px; }
table { border-collapse: collapse; margin: 20px auto; }
td { width: 40px; height: 40px; text-align: center; font-size: 20px; border: 1px solid #444; }
td:nth-child(3n) { border-right: 3px solid #fff; }
tr:nth-child(3n) td { border-bottom: 3px solid #fff; }
.empty { color: #555; }
.given { color: #4fc3f7; }
</style>
</head>
<body>
<h1 style=""text-align:center;"">🧩 Sudoku (Medium)</h1>
<table>
";
        for (int i = 0; i < size; i++) {
            html += "<tr>";
            for (int j = 0; j < size; j++) {
                int val = board[i, j];
                string cls = val == 0 ? "empty" : "given";
                string display = val == 0 ? "·" : val.ToString();
                html += $"<td class=\"{cls}\">{display}</td>";
            }
            html += "</tr>\n";
        }
        html += @"</table>
<p style=""text-align:center;"">💡 Решение доступно в JSON файле</p>
</body>
</html>";
        File.WriteAllText(filename, html);
        Console.WriteLine($"💾 Сохранено: {filename}");
    }

    public static void Main() {
        Console.WriteLine("🧩 Sudoku Generator (Medium) (C#)");
        var gen = new SudokuGenerator("medium");
        gen.Generate();

        Console.WriteLine("\nГоловоломка:");
        gen.PrintBoard(false);

        Console.Write("\nПоказать решение? (y/n): ");
        if (Console.ReadLine().Trim().ToLower() == "y") {
            Console.WriteLine("\nРешение:");
            gen.PrintBoard(true);
        }

        gen.SaveTXT("sudoku_puzzle.txt");
        gen.SaveJSON("sudoku_puzzle.json");
        gen.SaveHTML("sudoku_puzzle.html");
    }
}
