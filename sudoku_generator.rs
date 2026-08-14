// sudoku_generator.rs — Rust версия

use rand::seq::SliceRandom;
use rand::thread_rng;
use std::fs;
use std::io::{self, Write};
use serde_json;

struct SudokuGenerator {
    size: usize,
    box_size: usize,
    board: Vec<Vec<u8>>,
    solution: Vec<Vec<u8>>,
    cells_to_remove: usize,
    difficulty: String,
}

impl SudokuGenerator {
    fn new(difficulty: &str) -> Self {
        let remove = if difficulty == "medium" { 45 } else if difficulty == "hard" { 50 } else { 35 };
        SudokuGenerator {
            size: 9,
            box_size: 3,
            board: vec![vec![0; 9]; 9],
            solution: vec![vec![0; 9]; 9],
            cells_to_remove: remove,
            difficulty: difficulty.to_string(),
        }
    }

    fn generate(&mut self) {
        self.solve(&mut self.board);
        for i in 0..self.size {
            for j in 0..self.size {
                self.solution[i][j] = self.board[i][j];
            }
        }
        self.remove_cells();
    }

    fn solve(&self, board: &mut Vec<Vec<u8>>) -> bool {
        let empty = self.find_empty(board);
        if empty.is_none() {
            return true;
        }
        let (row, col) = empty.unwrap();
        let mut nums: Vec<u8> = (1..=9).collect();
        nums.shuffle(&mut thread_rng());
        for num in nums {
            if self.is_valid(board, row, col, num) {
                board[row][col] = num;
                if self.solve(board) {
                    return true;
                }
                board[row][col] = 0;
            }
        }
        false
    }

    fn find_empty(&self, board: &Vec<Vec<u8>>) -> Option<(usize, usize)> {
        for i in 0..self.size {
            for j in 0..self.size {
                if board[i][j] == 0 {
                    return Some((i, j));
                }
            }
        }
        None
    }

    fn is_valid(&self, board: &Vec<Vec<u8>>, row: usize, col: usize, num: u8) -> bool {
        for j in 0..self.size {
            if board[row][j] == num {
                return false;
            }
        }
        for i in 0..self.size {
            if board[i][col] == num {
                return false;
            }
        }
        let start_row = (row / self.box_size) * self.box_size;
        let start_col = (col / self.box_size) * self.box_size;
        for i in start_row..start_row + self.box_size {
            for j in start_col..start_col + self.box_size {
                if board[i][j] == num {
                    return false;
                }
            }
        }
        true
    }

    fn remove_cells(&mut self) {
        let mut cells = Vec::new();
        for i in 0..self.size {
            for j in 0..self.size {
                cells.push((i, j));
            }
        }
        cells.shuffle(&mut thread_rng());
        let mut removed = 0;
        for (i, j) in cells {
            if removed >= self.cells_to_remove {
                break;
            }
            self.board[i][j] = 0;
            removed += 1;
        }
    }

    fn print_board(&self, show_solution: bool) {
        let board = if show_solution { &self.solution } else { &self.board };
        println!("┌───────┬───────┬───────┐");
        for i in 0..self.size {
            print!("│");
            for j in 0..self.size {
                if board[i][j] == 0 {
                    print!(" . ");
                } else {
                    print!(" {} ", board[i][j]);
                }
                if j % 3 == 2 && j < self.size - 1 {
                    print!("│");
                }
            }
            println!("│");
            if i % 3 == 2 && i < self.size - 1 {
                println!("├───────┼───────┼───────┤");
            }
        }
        println!("└───────┴───────┴───────┘");
    }

    fn save_txt(&self, filename: &str) {
        let mut content = String::new();
        for row in &self.board {
            let line: Vec<String> = row.iter().map(|&c| if c == 0 { ".".to_string() } else { c.to_string() }).collect();
            content.push_str(&line.join(" "));
            content.push('\n');
        }
        fs::write(filename, content).unwrap();
        println!("💾 Сохранено: {}", filename);
    }

    fn save_json(&self, filename: &str) {
        let data = serde_json::json!({
            "puzzle": self.board,
            "solution": self.solution,
            "difficulty": self.difficulty,
            "cells_removed": self.cells_to_remove
        });
        let json = serde_json::to_string_pretty(&data).unwrap();
        fs::write(filename, json).unwrap();
        println!("💾 Сохранено: {}", filename);
    }

    fn save_html(&self, filename: &str) {
        let mut html = String::from(r#"<!DOCTYPE html>
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
"#);
        for i in 0..self.size {
            html.push_str("<tr>");
            for j in 0..self.size {
                let val = self.board[i][j];
                let cls = if val == 0 { "empty" } else { "given" };
                let display = if val == 0 { "·" } else { val.to_string() };
                html.push_str(&format!("<td class=\"{}\">{}</td>", cls, display));
            }
            html.push_str("</tr>\n");
        }
        html.push_str(r#"</table>
<p style="text-align:center;">💡 Решение доступно в JSON файле</p>
</body>
</html>"#);
        fs::write(filename, html).unwrap();
        println!("💾 Сохранено: {}", filename);
    }
}

fn main() {
    println!("🧩 Sudoku Generator (Medium) (Rust)");
    let mut gen = SudokuGenerator::new("medium");
    gen.generate();

    println!("\nГоловоломка:");
    gen.print_board(false);

    print!("\nПоказать решение? (y/n): ");
    io::stdout().flush().unwrap();
    let mut ans = String::new();
    io::stdin().read_line(&mut ans).unwrap();
    if ans.trim().to_lowercase() == "y" {
        println!("\nРешение:");
        gen.print_board(true);
    }

    gen.save_txt("sudoku_puzzle.txt");
    gen.save_json("sudoku_puzzle.json");
    gen.save_html("sudoku_puzzle.html");
}
