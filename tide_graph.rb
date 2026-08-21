# tide_graph.rb
AMPLITUDE = 1.5
MEAN_LEVEL = 0.5
PERIOD = 12.42

def tide_level(hours)
  rad = (hours / PERIOD) * 2 * Math::PI
  MEAN_LEVEL + AMPLITUDE * Math.sin(rad - 1.2)
end

def generate_data
  data = []
  (0...24*60).step(10) do |minutes|
    h = minutes / 60.0
    lvl = tide_level(h)
    data << [h, lvl]
  end
  data
end

def find_extrema(data)
  highs = []
  lows = []
  (1...data.length-1).each do |i|
    prev, curr, nxt = data[i-1], data[i], data[i+1]
    if curr[1] > prev[1] && curr[1] > nxt[1]
      highs << curr
    elsif curr[1] < prev[1] && curr[1] < nxt[1]
      lows << curr
    end
  end
  [highs, lows]
end

def draw_graph(data)
  levels = data.map { |d| d[1] }
  min_lvl = levels.min - 0.2
  max_lvl = levels.max + 0.2

  height = 20
  scale = (max_lvl - min_lvl) / height

  cols = 50
  step = data.length / cols
  graph_data = []
  cols.times do |i|
    idx = i * step
    h, lvl = data[idx]
    row = ((max_lvl - lvl) / scale).round
    row = 0 if row < 0
    row = height - 1 if row >= height
    graph_data << { h: h, lvl: lvl, row: row }
  end

  lines = []
  lines << "🌊 Tide Graph (24‑hour forecast)"
  lines << ""
  lines << "Level (m)"

  height.times do |r|
    lvl = max_lvl - r * scale
    line = format("%5.1f |", lvl)
    cols.times do |c|
      line << (graph_data[c][:row] == r ? "*" : " ")
    end
    lines << line
  end

  lines << "      +" + "-" * cols

  time_line = "       "
  (0..24).step(3) do |i|
    pos = (i / 24.0 * cols).to_i
    label = format("%02d:00", i)
    time_line << " " * (pos - time_line.length) if time_line.length < pos
    time_line << label
  end
  lines << time_line
  lines << ""

  highs, lows = find_extrema(data)
  unless highs.empty?
    str = "High tides: "
    highs.first(2).each_with_index do |h, idx|
      str << ", " if idx > 0
      hour = h[0].to_i
      min = ((h[0] - hour) * 60).round
      str << format("%02d:%02d (%.1fm)", hour, min, h[1])
    end
    lines << str
  end
  unless lows.empty?
    str = "Low tides:  "
    lows.first(2).each_with_index do |l, idx|
      str << ", " if idx > 0
      hour = l[0].to_i
      min = ((l[0] - hour) * 60).round
      str << format("%02d:%02d (%.1fm)", hour, min, l[1])
    end
    lines << str
  end

  lines.join("\n")
end

puts draw_graph(generate_data)
