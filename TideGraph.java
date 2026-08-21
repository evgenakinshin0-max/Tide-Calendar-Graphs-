// TideGraph.java
import java.util.*;

public class TideGraph {
    private static final double AMPLITUDE = 1.5;
    private static final double MEAN_LEVEL = 0.5;
    private static final double PERIOD = 12.42;

    private static double tideLevel(double hours) {
        double rad = (hours / PERIOD) * 2 * Math.PI;
        return MEAN_LEVEL + AMPLITUDE * Math.sin(rad - 1.2);
    }

    private static double[][] generateData() {
        int points = 24 * 6; // every 10 minutes
        double[][] data = new double[points][2];
        for (int i = 0; i < points; i++) {
            double h = i * 10 / 60.0;
            data[i][0] = h;
            data[i][1] = tideLevel(h);
        }
        return data;
    }

    private static List<double[]> findExtrema(double[][] data) {
        List<double[]> highs = new ArrayList<>();
        List<double[]> lows = new ArrayList<>();
        for (int i = 1; i < data.length - 1; i++) {
            if (data[i][1] > data[i-1][1] && data[i][1] > data[i+1][1]) {
                highs.add(data[i]);
            } else if (data[i][1] < data[i-1][1] && data[i][1] < data[i+1][1]) {
                lows.add(data[i]);
            }
        }
        List<double[]> result = new ArrayList<>();
        result.addAll(highs);
        result.addAll(lows);
        return result;
    }

    private static String drawGraph(double[][] data) {
        double minLvl = Double.MAX_VALUE, maxLvl = Double.MIN_VALUE;
        for (double[] d : data) {
            if (d[1] < minLvl) minLvl = d[1];
            if (d[1] > maxLvl) maxLvl = d[1];
        }
        minLvl = Math.floor(minLvl - 0.2);
        maxLvl = Math.ceil(maxLvl + 0.2);

        int height = 20;
        double scale = (maxLvl - minLvl) / height;

        int cols = 50;
        int step = data.length / cols;
        int[] rows = new int[cols];
        double[] times = new double[cols];
        double[] levels = new double[cols];
        for (int i = 0; i < cols; i++) {
            int idx = i * step;
            times[i] = data[idx][0];
            levels[i] = data[idx][1];
            int row = (int)Math.round((maxLvl - levels[i]) / scale);
            if (row < 0) row = 0;
            if (row >= height) row = height - 1;
            rows[i] = row;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🌊 Tide Graph (24‑hour forecast)\n\n");
        sb.append("Level (m)\n");

        for (int r = 0; r < height; r++) {
            double lvl = maxLvl - r * scale;
            sb.append(String.format("%5.1f |", lvl));
            for (int c = 0; c < cols; c++) {
                sb.append(rows[c] == r ? "*" : " ");
            }
            sb.append("\n");
        }

        sb.append("      +").append("-".repeat(cols)).append("\n");

        // Time labels
        StringBuilder timeLine = new StringBuilder("       ");
        for (int i = 0; i <= 24; i += 3) {
            int pos = (int)((i / 24.0) * cols);
            String label = String.format("%02d:00", i);
            while (timeLine.length() < pos) timeLine.append(" ");
            timeLine.append(label);
        }
        sb.append(timeLine).append("\n\n");

        // Extrema
        List<double[]> highs = new ArrayList<>(), lows = new ArrayList<>();
        for (int i = 1; i < data.length - 1; i++) {
            if (data[i][1] > data[i-1][1] && data[i][1] > data[i+1][1]) highs.add(data[i]);
            else if (data[i][1] < data[i-1][1] && data[i][1] < data[i+1][1]) lows.add(data[i]);
        }
        if (!highs.isEmpty()) {
            sb.append("High tides: ");
            for (int i = 0; i < Math.min(2, highs.size()); i++) {
                if (i > 0) sb.append(", ");
                double h = highs.get(i)[0];
                int hour = (int)h;
                int min = (int)Math.round((h - hour) * 60);
                sb.append(String.format("%02d:%02d (%.1fm)", hour, min, highs.get(i)[1]));
            }
            sb.append("\n");
        }
        if (!lows.isEmpty()) {
            sb.append("Low tides:  ");
            for (int i = 0; i < Math.min(2, lows.size()); i++) {
                if (i > 0) sb.append(", ");
                double h = lows.get(i)[0];
                int hour = (int)h;
                int min = (int)Math.round((h - hour) * 60);
                sb.append(String.format("%02d:%02d (%.1fm)", hour, min, lows.get(i)[1]));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        double[][] data = generateData();
        System.out.print(drawGraph(data));
    }
}
