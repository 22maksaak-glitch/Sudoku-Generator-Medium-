# sudoku_generator.rb — Ruby версия

require 'json'

class SudokuGenerator
  attr_reader :board, :solution, :difficulty, :cells_to_remove

  def initialize(difficulty = 'medium', seed = nil)
    @size = 9
    @box_size = 3
    @board = Array.new(@size) { Array.new(@size, 0) }
    @solution = Array.new(@size) { Array.new(@size, 0) }
    @difficulty = difficulty
    @cells_to_remove = difficulty == 'medium' ? 45 : difficulty == 'hard' ? 50 : 35
    @rng = seed ? Random.new(seed) : Random.new
  end

  def generate
    solve(@board)
    @size.times do |i|
      @size.times do |j|
        @solution[i][j] = @board[i][j]
      end
    end
    remove_cells
  end

  def solve(board)
    empty = find_empty(board)
    return true if empty.nil?
    row, col = empty
    (1..9).to_a.shuffle(random: @rng).each do |num|
      if valid?(board, row, col, num)
        board[row][col] = num
        return true if solve(board)
        board[row][col] = 0
      end
    end
    false
  end

  def find_empty(board)
    @size.times do |i|
      @size.times do |j|
        return [i, j] if board[i][j] == 0
      end
    end
    nil
  end

  def valid?(board, row, col, num)
    @size.times do |j|
      return false if board[row][j] == num
    end
    @size.times do |i|
      return false if board[i][col] == num
    end
    start_row = (row / @box_size) * @box_size
    start_col = (col / @box_size) * @box_size
    start_row.upto(start_row + @box_size - 1) do |i|
      start_col.upto(start_col + @box_size - 1) do |j|
        return false if board[i][j] == num
      end
    end
    true
  end

  def remove_cells
    cells = []
    @size.times { |i| @size.times { |j| cells << [i, j] } }
    cells.shuffle!(random: @rng)
    removed = 0
    cells.each do |row, col|
      break if removed >= @cells_to_remove
      @board[row][col] = 0
      removed += 1
    end
  end

  def print_board(show_solution = false)
    board_to_print = show_solution ? @solution : @board
    puts "┌───────┬───────┬───────┐"
    @size.times do |i|
      print "│"
      @size.times do |j|
        if board_to_print[i][j] == 0
          print " . "
        else
          print " #{board_to_print[i][j]} "
        end
        print "│" if j % 3 == 2 && j < @size - 1
      end
      puts "│"
      puts "├───────┼───────┼───────┤" if i % 3 == 2 && i < @size - 1
    end
    puts "└───────┴───────┴───────┘"
  end

  def save_txt(filename = 'sudoku_puzzle.txt')
    File.open(filename, 'w') do |f|
      @board.each do |row|
        f.puts row.map { |c| c == 0 ? '.' : c }.join(' ')
      end
    end
    puts "💾 Сохранено: #{filename}"
  end

  def save_json(filename = 'sudoku_puzzle.json')
    data = { puzzle: @board, solution: @solution, difficulty: @difficulty, cells_removed: @cells_to_remove }
    File.write(filename, JSON.pretty_generate(data))
    puts "💾 Сохранено: #{filename}"
  end

  def save_html(filename = 'sudoku_puzzle.html')
    html = <<~HTML
      <!DOCTYPE html>
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
    HTML
    @board.each do |row|
      html << "<tr>"
      row.each do |val|
        cls = val == 0 ? 'empty' : 'given'
        display = val == 0 ? '·' : val.to_s
        html << "<td class=\"#{cls}\">#{display}</td>"
      end
      html << "</tr>\n"
    end
    html << <<~HTML
      </table>
      <p style="text-align:center;">💡 Решение доступно в JSON файле</p>
      </body>
      </html>
    HTML
    File.write(filename, html)
    puts "💾 Сохранено: #{filename}"
  end
end

def main
  puts "🧩 Sudoku Generator (Medium) (Ruby)"
  gen = SudokuGenerator.new('medium')
  gen.generate

  puts "\nГоловоломка:"
  gen.print_board

  print "\nПоказать решение? (y/n): "
  if gets.chomp.downcase == 'y'
    puts "\nРешение:"
    gen.print_board(true)
  end

  gen.save_txt
  gen.save_json
  gen.save_html
end

main if __FILE__ == $0
