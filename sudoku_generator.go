// sudoku_generator.go — Go версия

package main

import (
	"encoding/json"
	"fmt"
	"math/rand"
	"os"
	"strconv"
	"time"
)

type SudokuGenerator struct {
	Size          int
	BoxSize       int
	Board         [][]int
	Solution      [][]int
	CellsToRemove int
	Difficulty    string
	Seed          int64
}

func NewSudokuGenerator(difficulty string, seed int64) *SudokuGenerator {
	remove := 45
	if difficulty == "easy" {
		remove = 35
	} else if difficulty == "hard" {
		remove = 50
	}
	board := make([][]int, 9)
	solution := make([][]int, 9)
	for i := range board {
		board[i] = make([]int, 9)
		solution[i] = make([]int, 9)
	}
	return &SudokuGenerator{
		Size:          9,
		BoxSize:       3,
		Board:         board,
		Solution:      solution,
		CellsToRemove: remove,
		Difficulty:    difficulty,
		Seed:          seed,
	}
}

func (s *SudokuGenerator) generate() {
	if s.Seed != 0 {
		rand.Seed(s.Seed)
	} else {
		rand.Seed(time.Now().UnixNano())
	}
	s.solve(s.Board)
	for i := 0; i < s.Size; i++ {
		for j := 0; j < s.Size; j++ {
			s.Solution[i][j] = s.Board[i][j]
		}
	}
	s.removeCells()
}

func (s *SudokuGenerator) solve(board [][]int) bool {
	row, col, found := s.findEmpty(board)
	if !found {
		return true
	}
	nums := rand.Perm(9)
	for _, n := range nums {
		num := n + 1
		if s.isValid(board, row, col, num) {
			board[row][col] = num
			if s.solve(board) {
				return true
			}
			board[row][col] = 0
		}
	}
	return false
}

func (s *SudokuGenerator) findEmpty(board [][]int) (int, int, bool) {
	for i := 0; i < s.Size; i++ {
		for j := 0; j < s.Size; j++ {
			if board[i][j] == 0 {
				return i, j, true
			}
		}
	}
	return 0, 0, false
}

func (s *SudokuGenerator) isValid(board [][]int, row, col, num int) bool {
	for j := 0; j < s.Size; j++ {
		if board[row][j] == num {
			return false
		}
	}
	for i := 0; i < s.Size; i++ {
		if board[i][col] == num {
			return false
		}
	}
	startRow := (row / s.BoxSize) * s.BoxSize
	startCol := (col / s.BoxSize) * s.BoxSize
	for i := startRow; i < startRow+s.BoxSize; i++ {
		for j := startCol; j < startCol+s.BoxSize; j++ {
			if board[i][j] == num {
				return false
			}
		}
	}
	return true
}

func (s *SudokuGenerator) removeCells() {
	cells := make([][2]int, 0)
	for i := 0; i < s.Size; i++ {
		for j := 0; j < s.Size; j++ {
			cells = append(cells, [2]int{i, j})
		}
	}
	rand.Shuffle(len(cells), func(i, j int) { cells[i], cells[j] = cells[j], cells[i] })
	removed := 0
	for _, cell := range cells {
		if removed >= s.CellsToRemove {
			break
		}
		s.Board[cell[0]][cell[1]] = 0
		removed++
	}
}

func (s *SudokuGenerator) printBoard(showSolution bool) {
	board := s.Board
	if showSolution {
		board = s.Solution
	}
	fmt.Println("┌───────┬───────┬───────┐")
	for i := 0; i < s.Size; i++ {
		line := "│"
		for j := 0; j < s.Size; j++ {
			if board[i][j] == 0 {
				line += " . "
			} else {
				line += fmt.Sprintf(" %d ", board[i][j])
			}
			if j%3 == 2 && j < s.Size-1 {
				line += "│"
			}
		}
		line += "│"
		fmt.Println(line)
		if i%3 == 2 && i < s.Size-1 {
			fmt.Println("├───────┼───────┼───────┤")
		}
	}
	fmt.Println("└───────┴───────┴───────┘")
}

func (s *SudokuGenerator) saveTXT(filename string) {
	f, _ := os.Create(filename)
	defer f.Close()
	for _, row := range s.Board {
		for j, cell := range row {
			if cell == 0 {
				f.WriteString(". ")
			} else {
				f.WriteString(strconv.Itoa(cell) + " ")
			}
			if j == 8 {
				f.WriteString("\n")
			}
		}
	}
	fmt.Printf("💾 Сохранено: %s\n", filename)
}

func (s *SudokuGenerator) saveJSON(filename string) {
	data := map[string]interface{}{
		"puzzle":         s.Board,
		"solution":       s.Solution,
		"difficulty":     s.Difficulty,
		"cells_removed":  s.CellsToRemove,
	}
	jsonData, _ := json.MarshalIndent(data, "", "  ")
	os.WriteFile(filename, jsonData, 0644)
	fmt.Printf("💾 Сохранено: %s\n", filename)
}

func (s *SudokuGenerator) saveHTML(filename string) {
	html := `<!DOCTYPE html>
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
`
	for i := 0; i < s.Size; i++ {
		html += "<tr>"
		for j := 0; j < s.Size; j++ {
			val := s.Board[i][j]
			cls := "given"
			if val == 0 {
				cls = "empty"
			}
			display := "·"
			if val != 0 {
				display = strconv.Itoa(val)
			}
			html += fmt.Sprintf("<td class='%s'>%s</td>", cls, display)
		}
		html += "</tr>"
	}
	html += `</table>
<p style="text-align:center;">💡 Решение доступно в JSON файле</p>
</body></html>`
	os.WriteFile(filename, []byte(html), 0644)
	fmt.Printf("💾 Сохранено: %s\n", filename)
}

func main() {
	fmt.Println("🧩 Sudoku Generator (Medium) (Go)")
	gen := NewSudokuGenerator("medium", 0)
	gen.generate()

	fmt.Println("\nГоловоломка:")
	gen.printBoard(false)

	fmt.Print("\nПоказать решение? (y/n): ")
	var ans string
	fmt.Scanln(&ans)
	if ans == "y" || ans == "Y" {
		fmt.Println("\nРешение:")
		gen.printBoard(true)
	}

	gen.saveTXT("sudoku_puzzle.txt")
	gen.saveJSON("sudoku_puzzle.json")
	gen.saveHTML("sudoku_puzzle.html")
}
