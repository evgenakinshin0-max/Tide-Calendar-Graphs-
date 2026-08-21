// TideGraph.cs
using System;
using System.Collections.Generic;
using System.Linq;

class TideGraph
{
    const double AMPLITUDE = 1.5;
    const double MEAN_LEVEL = 0.5;
    const double PERIOD = 12.42;

    static double TideLevel(double hours)
    {
        double rad = (hours / PERIOD) * 2 * Math.PI;
        return MEAN_LEVEL + AMPLITUDE * Math.Sin(rad - 1.2);
    }

    static List<(double hour, double level)> GenerateData()
    {
        var data = new List<(double, double)>();
        for (int minutes = 0; minutes < 24*60; minutes += 10)
        {
            double h = minutes / 60.0;
            data.Add((h, TideLevel(h)));
        }
        return data;
    }

    static string DrawGraph(List<(double hour, double level)> data)
    {
        var levels = data.Select(d => d.level).ToList();
        double minLvl = levels.Min() - 0.2;
        double maxLvl = levels.Max() + 0.2;

        int height = 20;
        double scale = (maxLvl - minLvl) / height;

        int cols = 50;
        int step = data.Count / cols;
        int[] rows = new int[cols];
        double[] times = new double[cols];
        double[] lvls = new double[cols];
        for (int i = 0; i < cols; i++)
        {
            int idx = i * step;
            times[i] = data[idx].hour;
            lvls[i] = data[idx].level;
            int row = (int)Math.Round((maxLvl - lvls[i]) / scale);
            if (row < 0) row = 0;
            if (row >= height) row = height - 1;
            rows[i] = row;
        }

        var lines = new List<string>();
        lines.Add("🌊 Tide Graph (24‑hour forecast)");
        lines.Add("");
        lines.Add("Level (m)");

        for (int r = 0; r < height; r++)
        {
            double lvl = maxLvl - r * scale;
            string line = $"{lvl,5:F1} |";
            for (int c = 0; c < cols; c++)
                line += rows[c] == r ? "*" : " ";
            lines.Add(line);
        }

        lines.Add("      +" + new string('-', cols));

        string timeLine = "       ";
        for (int i = 0; i <= 24; i += 3)
        {
            int pos = (int)((i / 24.0) * cols);
            string label = $"{i:D2}:00";
            while (timeLine.Length < pos) timeLine += " ";
            timeLine += label;
        }
        lines.Add(timeLine);
        lines.Add("");

        // Extrema
        var highs = new List<(double hour, double level)>();
        var lows = new List<(double hour, double level)>();
        for (int i = 1; i < data.Count - 1; i++)
        {
            if (data[i].level > data[i-1].level && data[i].level > data[i+1].level)
                highs.Add(data[i]);
            else if (data[i].level < data[i-1].level && data[i].level < data[i+1].level)
                lows.Add(data[i]);
        }

        if (highs.Any())
        {
            string str = "High tides: ";
            for (int i = 0; i < Math.Min(2, highs.Count); i++)
            {
                if (i > 0) str += ", ";
                int hour = (int)highs[i].hour;
                int min = (int)Math.Round((highs[i].hour - hour) * 60);
                str += $"{hour:D2}:{min:D2} ({highs[i].level:F1}m)";
            }
            lines.Add(str);
        }
        if (lows.Any())
        {
            string str = "Low tides:  ";
            for (int i = 0; i < Math.Min(2, lows.Count); i++)
            {
                if (i > 0) str += ", ";
                int hour = (int)lows[i].hour;
                int min = (int)Math.Round((lows[i].hour - hour) * 60);
                str += $"{hour:D2}:{min:D2} ({lows[i].level:F1}m)";
            }
            lines.Add(str);
        }

        return string.Join("\n", lines);
    }

    static void Main()
    {
        var data = GenerateData();
        Console.Write(DrawGraph(data));
    }
}
