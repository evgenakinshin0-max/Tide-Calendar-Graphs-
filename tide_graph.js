// tide_graph.js
const AMPLITUDE = 1.5;
const MEAN_LEVEL = 0.5;
const PERIOD = 12.42;

function tideLevel(hours) {
    const rad = (hours / PERIOD) * 2 * Math.PI;
    return MEAN_LEVEL + AMPLITUDE * Math.sin(rad - 1.2);
}

function generateData() {
    const data = [];
    for (let minutes = 0; minutes < 24*60; minutes += 10) {
        const h = minutes / 60;
        const lvl = tideLevel(h);
        data.push([h, lvl]);
    }
    return data;
}

function findExtrema(data) {
    const highs = [], lows = [];
    for (let i = 1; i < data.length - 1; i++) {
        const prev = data[i-1], curr = data[i], next = data[i+1];
        if (curr[1] > prev[1] && curr[1] > next[1]) highs.push(curr);
        else if (curr[1] < prev[1] && curr[1] < next[1]) lows.push(curr);
    }
    return { highs, lows };
}

function drawGraph(data) {
    const levels = data.map(d => d[1]);
    let minLvl = Math.min(...levels);
    let maxLvl = Math.max(...levels);
    minLvl = Math.floor(minLvl - 0.2);
    maxLvl = Math.ceil(maxLvl + 0.2);

    const height = 20;
    const scale = (maxLvl - minLvl) / height;

    const cols = 50;
    const step = Math.floor(data.length / cols);
    const graphData = [];
    for (let i = 0; i < cols; i++) {
        const idx = i * step;
        const h = data[idx][0];
        const lvl = data[idx][1];
        let row = Math.round((maxLvl - lvl) / scale);
        if (row < 0) row = 0;
        if (row >= height) row = height - 1;
        graphData.push({ h, lvl, row });
    }

    let lines = [];
    lines.push("🌊 Tide Graph (24‑hour forecast)");
    lines.push("");
    lines.push("Level (m)");

    for (let r = 0; r < height; r++) {
        const lvl = maxLvl - r * scale;
        let line = lvl.toFixed(1).padStart(5) + " |";
        for (let c = 0; c < cols; c++) {
            line += graphData[c].row === r ? "*" : " ";
        }
        lines.push(line);
    }

    lines.push("      +" + "-".repeat(cols));

    // Time labels
    let timeLine = "       ";
    for (let i = 0; i <= 24; i += 3) {
        const pos = Math.floor((i / 24) * cols);
        const label = String(i).padStart(2, '0') + ":00";
        while (timeLine.length < pos) timeLine += " ";
        timeLine += label;
    }
    lines.push(timeLine);
    lines.push("");

    const { highs, lows } = findExtrema(data);
    if (highs.length > 0) {
        let str = "High tides: ";
        for (let i = 0; i < Math.min(2, highs.length); i++) {
            if (i > 0) str += ", ";
            const h = highs[i][0];
            const hour = Math.floor(h);
            const min = Math.round((h - hour) * 60);
            str += `${String(hour).padStart(2,'0')}:${String(min).padStart(2,'0')} (${highs[i][1].toFixed(1)}m)`;
        }
        lines.push(str);
    }
    if (lows.length > 0) {
        let str = "Low tides:  ";
        for (let i = 0; i < Math.min(2, lows.length); i++) {
            if (i > 0) str += ", ";
            const h = lows[i][0];
            const hour = Math.floor(h);
            const min = Math.round((h - hour) * 60);
            str += `${String(hour).padStart(2,'0')}:${String(min).padStart(2,'0')} (${lows[i][1].toFixed(1)}m)`;
        }
        lines.push(str);
    }

    return lines.join("\n");
}

console.log(drawGraph(generateData()));
