# tide_graph.php
<?php
define('AMPLITUDE', 1.5);
define('MEAN_LEVEL', 0.5);
define('PERIOD', 12.42);

function tideLevel($hours) {
    $rad = ($hours / PERIOD) * 2 * M_PI;
    return MEAN_LEVEL + AMPLITUDE * sin($rad - 1.2);
}

function generateData() {
    $data = [];
    for ($minutes = 0; $minutes < 24*60; $minutes += 10) {
        $h = $minutes / 60.0;
        $lvl = tideLevel($h);
        $data[] = [$h, $lvl];
    }
    return $data;
}

function findExtrema($data) {
    $highs = $lows = [];
    for ($i = 1; $i < count($data)-1; $i++) {
        $prev = $data[$i-1];
        $curr = $data[$i];
        $next = $data[$i+1];
        if ($curr[1] > $prev[1] && $curr[1] > $next[1]) $highs[] = $curr;
        elseif ($curr[1] < $prev[1] && $curr[1] < $next[1]) $lows[] = $curr;
    }
    return [$highs, $lows];
}

function drawGraph($data) {
    $levels = array_column($data, 1);
    $min_lvl = min($levels) - 0.2;
    $max_lvl = max($levels) + 0.2;

    $height = 20;
    $scale = ($max_lvl - $min_lvl) / $height;

    $cols = 50;
    $step = floor(count($data) / $cols);
    $graphData = [];
    for ($i = 0; $i < $cols; $i++) {
        $idx = $i * $step;
        $h = $data[$idx][0];
        $lvl = $data[$idx][1];
        $row = round(($max_lvl - $lvl) / $scale);
        if ($row < 0) $row = 0;
        if ($row >= $height) $row = $height - 1;
        $graphData[] = ['h' => $h, 'lvl' => $lvl, 'row' => $row];
    }

    $lines = [];
    $lines[] = "🌊 Tide Graph (24‑hour forecast)";
    $lines[] = "";
    $lines[] = "Level (m)";

    for ($r = 0; $r < $height; $r++) {
        $lvl = $max_lvl - $r * $scale;
        $line = sprintf("%5.1f |", $lvl);
        for ($c = 0; $c < $cols; $c++) {
            $line .= $graphData[$c]['row'] == $r ? "*" : " ";
        }
        $lines[] = $line;
    }

    $lines[] = "      +" . str_repeat("-", $cols);

    $timeLine = "       ";
    for ($i = 0; $i <= 24; $i += 3) {
        $pos = (int)($i / 24 * $cols);
        $label = sprintf("%02d:00", $i);
        while (strlen($timeLine) < $pos) $timeLine .= " ";
        $timeLine .= $label;
    }
    $lines[] = $timeLine;
    $lines[] = "";

    list($highs, $lows) = findExtrema($data);
    if (!empty($highs)) {
        $str = "High tides: ";
        foreach (array_slice($highs, 0, 2) as $idx => $h) {
            if ($idx > 0) $str .= ", ";
            $hour = (int)$h[0];
            $min = round(($h[0] - $hour) * 60);
            $str .= sprintf("%02d:%02d (%.1fm)", $hour, $min, $h[1]);
        }
        $lines[] = $str;
    }
    if (!empty($lows)) {
        $str = "Low tides:  ";
        foreach (array_slice($lows, 0, 2) as $idx => $l) {
            if ($idx > 0) $str .= ", ";
            $hour = (int)$l[0];
            $min = round(($l[0] - $hour) * 60);
            $str .= sprintf("%02d:%02d (%.1fm)", $hour, $min, $l[1]);
        }
        $lines[] = $str;
    }

    return implode("\n", $lines);
}

echo drawGraph(generateData()) . "\n";
?>
