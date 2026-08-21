// tide_graph.go
package main

import (
	"fmt"
	"math"
)

const (
	amplitude  = 1.5
	meanLevel  = 0.5
	period     = 12.42 // hours
)

func tideLevel(hours float64) float64 {
	rad := (hours / period) * 2 * math.Pi
	return meanLevel + amplitude*math.Sin(rad-1.2)
}

func generateData() [][2]float64 {
	data := make([][2]float64, 0, 24*6)
	for minutes := 0; minutes < 24*60; minutes += 10 {
		h := float64(minutes) / 60.0
		lvl := tideLevel(h)
		data = append(data, [2]float64{h, lvl})
	}
	return data
}

func findExtrema(data [][2]float64) ([][2]float64, [][2]float64) {
	var highs, lows [][2]float64
	for i := 1; i < len(data)-1; i++ {
		prev, curr, next := data[i-1], data[i], data[i+1]
		if curr[1] > prev[1] && curr[1] > next[1] {
			highs = append(highs, curr)
		} else if curr[1] < prev[1] && curr[1] < next[1] {
			lows = append(lows, curr)
		}
	}
	return highs, lows
}

func drawGraph(data [][2]float64) string {
	levels := make([]float64, len(data))
	minLvl, maxLvl := 0.0, 0.0
	for i, d := range data {
		levels[i] = d[1]
		if i == 0 || d[1] < minLvl {
			minLvl = d[1]
		}
		if i == 0 || d[1] > maxLvl {
			maxLvl = d[1]
		}
	}
	// Add padding
	minLvl = math.Floor(minLvl-0.2)
	maxLvl = math.Ceil(maxLvl+0.2)

	height := 20
	scale := (maxLvl - minLvl) / float64(height)

	cols := 50
	step := len(data) / cols
	graphData := make([]struct{ h, lvl float64; row int }, cols)
	for i := 0; i < cols; i++ {
		idx := i * step
		h, lvl := data[idx][0], data[idx][1]
		row := int(math.Round((maxLvl - lvl) / scale))
		if row < 0 {
			row = 0
		}
		if row >= height {
			row = height - 1
		}
		graphData[i] = struct{ h, lvl float64; row int }{h, lvl, row}
	}

	var lines []string
	lines = append(lines, "🌊 Tide Graph (24‑hour forecast)")
	lines = append(lines, "")
	lines = append(lines, "Level (m)")

	for r := 0; r < height; r++ {
		lvl := maxLvl - float64(r)*scale
		line := fmt.Sprintf("%5.1f |", lvl)
		for c := 0; c < cols; c++ {
			if graphData[c].row == r {
				line += "*"
			} else {
				line += " "
			}
		}
		lines = append(lines, line)
	}

	// X-axis
	lines = append(lines, "      +"+strings.Repeat("-", cols))

	// Time labels
	timeLine := "       "
	for i := 0; i <= 24; i += 3 {
		pos := int(float64(i) / 24.0 * float64(cols))
		label := fmt.Sprintf("%02d:00", i)
		for len(timeLine) < pos {
			timeLine += " "
		}
		timeLine += label
	}
	lines = append(lines, timeLine)
	lines = append(lines, "")

	highs, lows := findExtrema(data)
	if len(highs) > 0 {
		str := "High tides: "
		for i, h := range highs[:2] {
			if i > 0 {
				str += ", "
			}
			hour := int(h[0])
			min := int((h[0] - float64(hour)) * 60)
			str += fmt.Sprintf("%02d:%02d (%.1fm)", hour, min, h[1])
		}
		lines = append(lines, str)
	}
	if len(lows) > 0 {
		str := "Low tides:  "
		for i, l := range lows[:2] {
			if i > 0 {
				str += ", "
			}
			hour := int(l[0])
			min := int((l[0] - float64(hour)) * 60)
			str += fmt.Sprintf("%02d:%02d (%.1fm)", hour, min, l[1])
		}
		lines = append(lines, str)
	}

	result := ""
	for _, line := range lines {
		result += line + "\n"
	}
	return result
}

func main() {
	data := generateData()
	graph := drawGraph(data)
	fmt.Print(graph)
}
