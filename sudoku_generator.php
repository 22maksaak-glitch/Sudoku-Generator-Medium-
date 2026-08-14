<?php
// sudoku_generator.php — PHP версия

class SudokuGenerator {
    private $size = 9;
    private $boxSize = 3;
    private $board = [];
    private $solution = [];
    private $cellsToRemove;
    private $difficulty;

    public function __construct($difficulty = 'medium', $seed = null) {
        $this->board = array_fill(0, $this->size, array_fill(0, $this->size, 0));
        $this->solution = array_fill(0, $this->size, array_fill(0, $this->size, 0));
        $this->difficulty = $difficulty;
        $this->cellsToRemove = $difficulty == 'medium' ? 45 : ($difficulty == 'hard' ? 50 : 35);
        if ($seed !== null) mt_srand($seed);
    }

    public function generate() {
        $this->solve($this->board);
        for ($i = 0; $i < $this->size; $i++) {
            for ($j = 0; $j < $this->size; $j++) {
                $this->solution[$i][$j] = $this->board[$i][$j];
            }
        }
        $this->removeCells();
        return $this->board;
    }

    private function solve(&$board) {
        $empty = $this->findEmpty($board);
        if ($empty === null) return true;
        list($row, $col) = $empty;
        $nums = range(1, 9);
        shuffle($nums);
        foreach ($nums as $num) {
            if ($this->isValid($board, $row, $col, $num)) {
                $board[$row][$col] = $num;
                if ($this->solve($board)) return true;
                $board[$row][$col] = 0;
            }
        }
        return false;
    }

    private function findEmpty($board) {
        for ($i = 0; $i < $this->size; $i++) {
            for ($j = 0; $j < $this->size; $j++) {
                if ($board[$i][$j] == 0) return [$i, $j];
            }
        }
        return null;
    }

    private function isValid($board, $row, $col, $num) {
        for ($j = 0; $j < $this->size; $j++) {
            if ($board[$row][$j] == $num) return false;
        }
        for ($i = 0; $i < $this->size; $i++) {
            if ($board[$i][$col] == $num) return false;
        }
        $startRow = intdiv($row, $this->boxSize) * $this->boxSize;
        $startCol = intdiv($col, $this->boxSize) * $this->boxSize;
        for ($i = $startRow; $i < $startRow + $this->boxSize; $i++) {
            for ($j = $startCol; $j < $startCol + $this->boxSize; $j++) {
                if ($board[$i][$j] == $num) return false;
            }
        }
        return true;
    }

    private function removeCells() {
        $cells = [];
        for ($i = 0; $i < $this->size; $i++) {
            for ($j = 0; $j < $this->size; $j++) {
                $cells[] = [$i, $j];
            }
        }
        shuffle($cells);
        $removed = 0;
        foreach ($cells as $cell) {
            if ($removed >= $this->cellsToRemove) break;
            $this->board[$cell[0]][$cell[1]] = 0;
            $removed++;
        }
    }

    public function printBoard($showSolution = false) {
        $board = $showSolution ? $this->solution : $this->board;
        echo "┌───────┬───────┬───────┐\n";
        for ($i = 0; $i < $this->size; $i++) {
            echo "│";
            for ($j = 0; $j < $this->size; $j++) {
                if ($board[$i][$j] == 0) {
                    echo " . ";
                } else {
                    echo " {$board[$i][$j]} ";
                }
                if ($j % 3 == 2 && $j < $this->size - 1) echo "│";
            }
            echo "│\n";
            if ($i % 3 == 2 && $i < $this->size - 1) {
                echo "├───────┼───────┼───────┤\n";
            }
        }
        echo "└───────┴───────┴───────┘\n";
    }

    public function saveTXT($filename = 'sudoku_puzzle.txt') {
        $content = "";
        foreach ($this->board as $row) {
            $content .= implode(' ', array_map(function($c) { return $c == 0 ? '.' : $c; }, $row)) . "\n";
        }
        file_put_contents($filename, $content);
        echo "💾 Сохранено: $filename\n";
    }

    public function saveJSON($filename = 'sudoku_puzzle.json') {
        $data = [
            'puzzle' => $this->board,
            'solution' => $this->solution,
            'difficulty' => $this->difficulty,
            'cells_removed' => $this->cellsToRemove
        ];
        file_put_contents($filename, json_encode($data, JSON_PRETTY_PRINT));
        echo "💾 Сохранено: $filename\n";
    }

    public function saveHTML($filename = 'sudoku_puzzle.html') {
        $html = '<!DOCTYPE html>
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
';
        for ($i = 0; $i < $this->size; $i++) {
            $html .= "<tr>";
            for ($j = 0; $j < $this->size; $j++) {
                $val = $this->board[$i][$j];
                $cls = $val == 0 ? 'empty' : 'given';
                $display = $val == 0 ? '·' : (string)$val;
                $html .= "<td class=\"$cls\">$display</td>";
            }
            $html .= "</tr>\n";
        }
        $html .= '</table>
<p style="text-align:center;">💡 Решение доступно в JSON файле</p>
</body>
</html>';
        file_put_contents($filename, $html);
        echo "💾 Сохранено: $filename\n";
    }
}

function main() {
    echo "🧩 Sudoku Generator (Medium) (PHP)\n";
    $gen = new SudokuGenerator('medium');
    $gen->generate();

    echo "\nГоловоломка:\n";
    $gen->printBoard();

    echo "\nПоказать решение? (y/n): ";
    $ans = trim(fgets(STDIN));
    if (strtolower($ans) == 'y') {
        echo "\nРешение:\n";
        $gen->printBoard(true);
    }

    $gen->saveTXT();
    $gen->saveJSON();
    $gen->saveHTML();
}

main();
?>
