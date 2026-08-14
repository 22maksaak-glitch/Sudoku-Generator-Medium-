
### 1. `sudoku_generator.py` (Python)

```python
# sudoku_generator.py — Python версия

import random
import json
import sys
from colorama import init, Fore, Style

init(autoreset=True)

class SudokuGenerator:
    def __init__(self, difficulty='medium', seed=None):
        self.difficulty = difficulty
        self.size = 9
        self.box_size = 3
        self.board = [[0 for _ in range(self.size)] for _ in range(self.size)]
        self.solution = [[0 for _ in range(self.size)] for _ in range(self.size)]
        self.cells_to_remove = 45 if difficulty == 'medium' else 50 if difficulty == 'hard' else 35
        if seed is not None:
            random.seed(seed)

    def generate(self):
        """Генерирует полное решённое поле."""
        self._solve(self.board)
        for i in range(self.size):
            for j in range(self.size):
                self.solution[i][j] = self.board[i][j]

        # Удаляем клетки для создания головоломки
        self._remove_cells()
        return self.board

    def _solve(self, board):
        """Решает судоку с помощью backtracking."""
        empty = self._find_empty(board)
        if not empty:
            return True
        row, col = empty
        numbers = list(range(1, 10))
        random.shuffle(numbers)
        for num in numbers:
            if self._is_valid(board, row, col, num):
                board[row][col] = num
                if self._solve(board):
                    return True
                board[row][col] = 0
        return False

    def _find_empty(self, board):
        for i in range(self.size):
            for j in range(self.size):
                if board[i][j] == 0:
                    return (i, j)
        return None

    def _is_valid(self, board, row, col, num):
        for j in range(self.size):
            if board[row][j] == num:
                return False
        for i in range(self.size):
            if board[i][col] == num:
                return False
        start_row = (row // self.box_size) * self.box_size
        start_col = (col // self.box_size) * self.box_size
        for i in range(start_row, start_row + self.box_size):
            for j in range(start_col, start_col + self.box_size):
                if board[i][j] == num:
                    return False
        return True

    def _remove_cells(self):
        """Удаляет клетки для создания головоломки."""
        cells = [(i, j) for i in range(self.size) for j in range(self.size)]
        random.shuffle(cells)
        removed = 0
        for row, col in cells:
            if removed >= self.cells_to_remove:
                break
            self.board[row][col] = 0
            removed += 1

    def has_unique_solution(self):
        """Проверяет, имеет ли головоломка единственное решение."""
        board_copy = [row[:] for row in self.board]
        count = self._count_solutions(board_copy)
        return count == 1

    def _count_solutions(self, board):
        """Считает количество решений (максимум 2)."""
        empty = self._find_empty(board)
        if not empty:
            return 1
        row, col = empty
        count = 0
        for num in range(1, 10):
            if self._is_valid(board, row, col, num):
                board[row][col] = num
                count += self._count_solutions(board)
                if count >= 2:
                    board[row][col] = 0
                    return count
                board[row][col] = 0
        return count

    def print_board(self, show_solution=False):
        """Выводит поле в красивом формате."""
        board = self.solution if show_solution else self.board
        print("┌───────┬───────┬───────┐")
        for i in range(self.size):
            line = "│"
            for j in range(self.size):
                if board[i][j] == 0:
                    line += " . "
                else:
                    line += f" {Fore.GREEN}{board[i][j]}{Style.RESET_ALL} " if not show_solution else f" {board[i][j]} "
                if j % 3 == 2 and j < self.size - 1:
                    line += "│"
            line += "│"
            print(line)
            if i % 3 == 2 and i < self.size - 1:
                print("├───────┼───────┼───────┤")
        print("└───────┴───────┴───────┘")

    def save_txt(self, filename="sudoku_puzzle.txt"):
        with open(filename, 'w') as f:
            for row in self.board:
                f.write(' '.join(str(cell) if cell != 0 else '.' for cell in row) + '\n')
        print(f"💾 Сохранено: {filename}")

    def save_json(self, filename="sudoku_puzzle.json"):
        data = {
            "puzzle": self.board,
            "solution": self.solution,
            "difficulty": self.difficulty,
            "cells_removed": self.cells_to_remove
        }
        with open(filename, 'w') as f:
            json.dump(data, f, indent=2)
        print(f"💾 Сохранено: {filename}")

    def save_html(self, filename="sudoku_puzzle.html"):
        html = """<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Sudoku</title>
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
<h1 style="text-align:center;">🧩 Sudoku (Medium)</h1>
<table>
"""
        for i in range(self.size):
            html += "<tr>"
            for j in range(self.size):
                val = self.board[i][j]
                cls = "given" if val != 0 else "empty"
                display = str(val) if val != 0 else "·"
                html += f"<td class='{cls}'>{display}</td>"
            html += "</tr>"
        html += """</table>
<p style="text-align:center;">💡 Решение доступно в JSON файле</p>
</body></html>"""
        with open(filename, 'w', encoding='utf-8') as f:
            f.write(html)
        print(f"💾 Сохранено: {filename}")

    def solve(self):
        """Решает головоломку (если возможно)."""
        board_copy = [row[:] for row in self.board]
        if self._solve(board_copy):
            return board_copy
        return None

def main():
    print("🧩 Sudoku Generator (Medium) (Python)")
    gen = SudokuGenerator('medium')
    gen.generate()

    print("\nГоловоломка:")
    gen.print_board()

    print("\nПоказать решение? (y/n): ", end="")
    if input().strip().lower() == 'y':
        print("\nРешение:")
        gen.print_board(show_solution=True)

    gen.save_txt()
    gen.save_json()
    gen.save_html()

if __name__ == "__main__":
    main()
