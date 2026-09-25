package io.arson.client.ui;

import io.arson.client.module.HudModule;
import java.util.ArrayList;
import java.util.List;

/** Pure row-shaping rules shared by the HUD renderer and unit tests. */
public final class HudRowFormatter {
    public record Row(String first, String second) {
        public boolean hasSecond() { return !second.isEmpty(); }
    }

    private HudRowFormatter() {}

    /** Calculates the pixel width needed by a formatted two-column row. */
    public static int columnWidth(Row row, int firstColumnWidth, int secondColumnWidth, int gap) {
        if (row == null) return 0;
        int first = Math.max(0, firstColumnWidth);
        if (!row.hasSecond()) return first;
        return first + Math.max(0, gap) + Math.max(0, secondColumnWidth);
    }

    public static List<Row> format(List<String> values, HudModule.RowFormat format) {
        return format(values, format, "  |  ");
    }

    public static List<Row> format(List<String> values, HudModule.RowFormat format, String compactSeparator) {
        if (values == null || values.isEmpty()) return List.of();
        List<String> safe = values.stream().map(value -> value == null ? "" : value).toList();
        HudModule.RowFormat mode = format == null ? HudModule.RowFormat.STACKED : format;
        if (mode == HudModule.RowFormat.COMPACT) {
            String separator = compactSeparator == null || compactSeparator.isBlank() ? "  |  " : compactSeparator.replace((char) 10, ' ').replace((char) 13, ' ');
            return List.of(new Row(String.join(separator, safe), ""));
        }
        ArrayList<Row> result = new ArrayList<>();
        if (mode == HudModule.RowFormat.TWO_COLUMN) {
            for (int index = 0; index < safe.size(); index += 2) {
                String second = index + 1 < safe.size() ? safe.get(index + 1) : "";
                result.add(new Row(safe.get(index), second));
            }
        } else {
            for (String value : safe) result.add(new Row(value, ""));
        }
        return List.copyOf(result);
    }
}
