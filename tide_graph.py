# tide_graph.py
import math
import datetime

# Tide model parameters (semi‑diurnal, M2 + S2 approximation)
AMPLITUDE = 1.5
MEAN_LEVEL = 0.5
PERIOD_HOURS = 12.42  # M2 period

def tide_level(hours):
    """Return tide level in meters for a given hour of the day."""
    # Two tidal constituents: M2 (principal lunar) and S2 (solar)
    # Phase offset to make high tides around 02:00 and 14:30
    phase = 0.0  # we'll adjust with time
    rad = (hours / PERIOD_HOURS) * 2 * math.pi
    level = MEAN_LEVEL + AMPLITUDE * math.sin(rad - 1.2)
    return level

def generate_data():
    """Generate (hour, level) pairs for 24 hours, sampled every 10 minutes."""
    data = []
    for minutes in range(0, 24*60, 10):
        h = minutes / 60.0
        level = tide_level(h)
        data.append((h, level))
    return data

def find_extrema(data):
    """Find high and low tide times and levels."""
    highs, lows = [], []
    for i in range(1, len(data)-1):
        prev, curr, nxt = data[i-1], data[i], data[i+1]
        if curr[1] > prev[1] and curr[1] > nxt[1]:
            highs.append(curr)
        elif curr[1] < prev[1] and curr[1] < nxt[1]:
            lows.append(curr)
    return highs, lows

def draw_graph(data):
    """Draw an ASCII graph of the tide data."""
    # Determine min and max level
    levels = [d[1] for d in data]
    min_lvl, max_lvl = min(levels), max(levels)
    # Add some padding
    min_lvl = min(round(min_lvl - 0.2, 1), -1.5)
    max_lvl = max(round(max_lvl + 0.2, 1), 1.5)

    # Scale to fit graph height (20 rows)
    height = 20
    scale = (max_lvl - min_lvl) / height

    # Create graph grid (rows = levels, columns = time points)
    # We'll use 50 columns for time (one per ~30 minutes)
    cols = 50
    step = len(data) // cols
    graph_data = []
    for i in range(cols):
        idx = i * step
        h, lvl = data[idx]
        # Map level to row (0 = top, height-1 = bottom)
        row = int(round((max_lvl - lvl) / scale))
        if row < 0: row = 0
        if row >= height: row = height - 1
        graph_data.append((h, lvl, row))

    # Build the graph string
    lines = []
    # Title
    lines.append("🌊 Tide Graph (24‑hour forecast)")
    lines.append("")
    # Y-axis label
    lines.append("Level (m)")

    # For each row
    for r in range(height):
        lvl = max_lvl - r * scale
        line = f"{lvl:5.1f} |"
        # Fill columns
        for col in range(cols):
            if graph_data[col][2] == r:
                # Use a star for the tide line
                line += "*"
            else:
                # Use space, but add a dot for zero level?
                line += " "
        lines.append(line)

    # X-axis
    x_axis = "      +" + "-" * cols
    lines.append(x_axis)
    # Time labels
    time_labels = ""
    for i in range(0, 25, 3):
        label = f"{i:02d}:00"
        # Position label under the graph (approx)
        pos = int(i / 24 * cols)
        # We'll just print a separate line with labels
    # Actually easier: print a time line
    time_line = "       "
    for i in range(0, 25, 3):
        pos = int(i / 24 * cols)
        time_line += " " * (pos - len(time_line)) + f"{i:02d}:00"
    lines.append(time_line)
    lines.append("")

    # Find extrema
    highs, lows = find_extrema(data)
    if highs:
        high_str = ", ".join([f"{int(h[0]):02d}:{int((h[0]%1)*60):02d} ({h[1]:.1f}m)" for h in highs[:2]])
        lines.append(f"High tides: {high_str}")
    if lows:
        low_str = ", ".join([f"{int(l[0]):02d}:{int((l[0]%1)*60):02d} ({l[1]:.1f}m)" for l in lows[:2]])
        lines.append(f"Low tides:  {low_str}")

    return "\n".join(lines)

def main():
    data = generate_data()
    graph = draw_graph(data)
    print(graph)

if __name__ == "__main__":
    main()
