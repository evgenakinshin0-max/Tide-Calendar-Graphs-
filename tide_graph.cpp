// tide_graph.cpp
#include <iostream>
#include <vector>
#include <cmath>
#include <iomanip>
#include <string>

const double AMPLITUDE = 1.5;
const double MEAN_LEVEL = 0.5;
const double PERIOD = 12.42;

double tideLevel(double hours) {
    double rad = (hours / PERIOD) * 2 * M_PI;
    return MEAN_LEVEL + AMPLITUDE * sin(rad - 1.2);
}

std::vector<std::pair<double, double>> generateData() {
    std::vector<std::pair<double, double>> data;
    for (int minutes = 0; minutes < 24*60; minutes += 10) {
        double h = minutes / 60.0;
        data.push_back({h, tideLevel(h)});
    }
    return data;
}

std::string drawGraph(const std::vector<std::pair<double, double>>& data) {
    double minLvl = 1e9, maxLvl = -1e9;
    for (auto& p : data) {
        if (p.second < minLvl) minLvl = p.second;
        if (p.second > maxLvl) maxLvl = p.second;
    }
    minLvl = floor(minLvl - 0.2);
    maxLvl = ceil(maxLvl + 0.2);

    int height = 20;
    double scale = (maxLvl - minLvl) / height;

    int cols = 50;
    int step = data.size() / cols;
    std::vector<int> rows(cols);
    std::vector<double> times(cols), levels(cols);
    for (int i = 0; i < cols; i++) {
        int idx = i * step;
        times[i] = data[idx].first;
        levels[i] = data[idx].second;
        int row = round((maxLvl - levels[i]) / scale);
        if (row < 0) row = 0;
        if (row >= height) row = height - 1;
        rows[i] = row;
    }

    std::string result;
    result += "🌊 Tide Graph (24‑hour forecast)\n\n";
    result += "Level (m)\n";

    for (int r = 0; r < height; r++) {
        double lvl = maxLvl - r * scale;
        char buffer[20];
        snprintf(buffer, sizeof(buffer), "%5.1f |", lvl);
        result += buffer;
        for (int c = 0; c < cols; c++) {
            result += (rows[c] == r) ? "*" : " ";
        }
        result += "\n";
    }

    result += "      +";
    result += std::string(cols, '-');
    result += "\n";

    std::string timeLine = "       ";
    for (int i = 0; i <= 24; i += 3) {
        int pos = (int)((i / 24.0) * cols);
        char label[6];
        snprintf(label, sizeof(label), "%02d:00", i);
        while ((int)timeLine.size() < pos) timeLine += " ";
        timeLine += label;
    }
    result += timeLine + "\n\n";

    // Find extrema
    std::vector<std::pair<double, double>> highs, lows;
    for (size_t i = 1; i < data.size() - 1; i++) {
        if (data[i].second > data[i-1].second && data[i].second > data[i+1].second)
            highs.push_back(data[i]);
        else if (data[i].second < data[i-1].second && data[i].second < data[i+1].second)
            lows.push_back(data[i]);
    }

    if (!highs.empty()) {
        result += "High tides: ";
        for (int i = 0; i < (int)std::min(size_t(2), highs.size()); i++) {
            if (i > 0) result += ", ";
            int hour = (int)highs[i].first;
            int min = round((highs[i].first - hour) * 60);
            char buf[20];
            snprintf(buf, sizeof(buf), "%02d:%02d (%.1fm)", hour, min, highs[i].second);
            result += buf;
        }
        result += "\n";
    }
    if (!lows.empty()) {
        result += "Low tides:  ";
        for (int i = 0; i < (int)std::min(size_t(2), lows.size()); i++) {
            if (i > 0) result += ", ";
            int hour = (int)lows[i].first;
            int min = round((lows[i].first - hour) * 60);
            char buf[20];
            snprintf(buf, sizeof(buf), "%02d:%02d (%.1fm)", hour, min, lows[i].second);
            result += buf;
        }
        result += "\n";
    }

    return result;
}

int main() {
    auto data = generateData();
    std::cout << drawGraph(data);
    return 0;
}
