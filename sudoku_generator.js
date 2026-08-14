// sudoku_generator.js — JavaScript версия

const fs = require('fs');

class SudokuGenerator {
    constructor(difficulty = 'medium', seed = null) {
        this.size = 9;
        this.boxSize = 3;
        this.board = Array.from({ length: 9 }, () => Array(9).fill(0));
        this.solution = Array.from({ length: 9 }, () => Array(9).fill(0));
        this.cellsToRemove = difficulty === 'medium' ? 45 : difficulty === 'hard' ? 50 : 35;
        this.difficulty = difficulty;
        if (seed !== null) {
            this._seed = seed;
        }
    }

    _rand() {
        if (this._seed !== undefined) {
            this._seed = (this._seed * 9301 + 49297) % 233280;
            return this._seed / 233280;
        }
        return Math.random();
    }

    generate() {
        this._solve(this.board);
        for (let i = 0; i < this.size; i++) {
            for (let j = 0; j < this.size; j++) {
                this.solution[i][j] = this.board[i][j];
            }
        }
        this._removeCells();
        return this.board;
    }

    _solve(board) {
        const empty = this._findEmpty(board);
        if (!empty) return true;
        const [row, col] = empty;
        const nums = this._shuffle([1, 2, 3, 4, 5, 6, 7, 8, 9]);
        for (const num of nums) {
            if (this._isValid(board, row, col, num)) {
                board[row][col] = num;
                if (this._solve(board)) return true;
                board[row][col] = 0;
            }
        }
        return false;
    }

    _findEmpty(board) {
        for (let i = 0; i < this.size; i++) {
            for (let j = 0; j < this.size; j++) {
                if (board[i][j] === 0) return [i, j];
            }
        }
        return null;
    }

    _isValid(board, row, col, num) {
        for (let j = 0; j < this.size; j++) {
            if (board[row][j] === num) return false;
        }
        for (let i = 0; i < this.size; i++) {
            if (board[i][col] === num) return false;
        }
        const startRow = Math.floor(row / this.boxSize) * this.boxSize;
        const startCol = Math.floor(col / this.boxSize) * this.boxSize;
        for (let i = startRow; i < startRow + this.boxSize; i++) {
            for (let j = startCol; j < startCol + this.boxSize; j++) {
                if (board[i][j] === num) return false;
            }
        }
        return true;
    }

    _removeCells() {
        const cells = [];
        for (let i = 0; i < this.size; i++) {
            for (let j = 0; j < this.size; j++) {
                cells.push([i, j]);
            }
        }
        this._shuffle(cells);
        let removed = 0;
        for (const [row, col] of cells) {
            if (removed >= this.cellsToRemove) break;
            this.board[row][col] = 0;
            removed++;
        }
    }

    _shuffle(arr) {
        for (let i = arr.length - 1; i > 0; i--) {
            const j = Math.floor(this._rand() * (i + 1));
            [arr[i], arr[j]] = [arr[j], arr[i]];
        }
        return arr;
    }

    hasUniqueSolution() {
        const boardCopy = this.board.map(row => [...row]);
        let count = 0;
        const countSolutions = (board) => {
            const empty = this._findEmpty(board);
            if (!empty) return 1;
            const [row, col] = empty;
            let c = 0;
            for (let num = 1; num <= 9; num++) {
                if (this._isValid(board, row, col, num)) {
                    board[row][col] = num;
                    c += countSolutions(board);
                    if (c >= 2) { board[row][col] = 0; return c; }
                    board[row][col] = 0;
                }
            }
            return c;
        };
        count = countSolutions(boardCopy);
        return count === 1;
    }

    printBoard(showSolution = false) {
        const board = showSolution ? this.solution : this.board;
        console.log('┌───────┬───────┬───────┐');
        for (let i = 0; i < this.size; i++) {
            let line = '│';
            for (let j = 0; j < this.size; j++) {
                line += board[i][j] === 0 ? ' . ' : ` ${board[i][j]} `;
                if (j % 3 === 2 && j < this.size - 1) line += '│';
            }
            line += '│';
            console.log(line);
            if (i % 3 === 2 && i < this.size - 1) {
                console.log('├───────┼───────┼───────┤');
            }
        }
        console.log('└───────┴───────┴───────┘');
    }

    saveTXT(filename = 'sudoku_puzzle.txt') {
        let content = '';
        for (const row of this.board) {
            content += row.map(c => c === 0 ? '.' : c).join(' ') + '\n';
        }
        fs.writeFileSync(filename, content);
        console.log(`💾 Сохранено: ${filename}`);
    }

    saveJSON(filename = 'sudoku_puzzle.json') {
        const data = {
            puzzle: this.board,
            solution: this.solution,
            difficulty: this.difficulty,
            cells_removed: this.cellsToRemove
        };
        fs.writeFileSync(filename, JSON.stringify(data, null, 2));
        console.log(`💾 Сохранено: ${filename}`);
    }

    saveHTML(filename = 'sudoku_puzzle.html') {
        let html = `<!DOCTYPE html>
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
`;
        for (let i = 0; i < this.size; i++) {
            html += '<tr>';
            for (let j = 0; j < this.size; j++) {
                const val = this.board[i][j];
                const cls = val === 0 ? 'empty' : 'given';
                const display = val === 0 ? '·' : String(val);
                html += `<td class="${cls}">${display}</td>`;
            }
            html += '</tr>';
        }
        html += `</table>
<p style="text-align:center;">💡 Решение доступно в JSON файле</p>
</body></html>`;
        fs.writeFileSync(filename, html);
        console.log(`💾 Сохранено: ${filename}`);
    }
}

function main() {
    console.log('🧩 Sudoku Generator (Medium) (JavaScript)');
    const gen = new SudokuGenerator('medium');
    gen.generate();

    console.log('\nГоловоломка:');
    gen.printBoard();

    const readline = require('readline').createInterface({
        input: process.stdin,
        output: process.stdout
    });
    readline.question('\nПоказать решение? (y/n): ', (ans) => {
        if (ans.toLowerCase() === 'y') {
            console.log('\nРешение:');
            gen.printBoard(true);
        }
        gen.saveTXT();
        gen.saveJSON();
        gen.saveHTML();
        readline.close();
    });
}

if (require.main === module) main();
