package io.arson.client.ui;

import io.arson.client.module.HudModule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HudRowFormatterTest {
    @Test
    void stackedAndDenseKeepEachValueOnItsOwnRow() {
        var expected = List.of(
            new HudRowFormatter.Row("Health 20", ""),
            new HudRowFormatter.Row("Food 18", "")
        );
        assertEquals(expected, HudRowFormatter.format(List.of("Health 20", "Food 18"), HudModule.RowFormat.STACKED));
        assertEquals(expected, HudRowFormatter.format(List.of("Health 20", "Food 18"), HudModule.RowFormat.DENSE));
    }

    @Test
    void compactKeepsItsExistingSingleRowSeparator() {
        assertEquals(
            List.of(new HudRowFormatter.Row("Health 20  |  Food 18", "")),
            HudRowFormatter.format(List.of("Health 20", "Food 18"), HudModule.RowFormat.COMPACT)
        );
    }

    @Test
    void compactUsesCustomSeparatorAndKeepsBlankValuesOnOneLine() {
        assertEquals(
            List.of(new HudRowFormatter.Row("Health 20 · Food 18", "")),
            HudRowFormatter.format(List.of("Health 20", "Food 18"), HudModule.RowFormat.COMPACT, " · ")
        );
        assertEquals(
            List.of(new HudRowFormatter.Row("Health 20  |  Food 18", "")),
            HudRowFormatter.format(List.of("Health 20", "Food 18"), HudModule.RowFormat.COMPACT, " \n ")
        );
        assertEquals(
            HudRowFormatter.format(List.of("Health 20", "Food 18"), HudModule.RowFormat.COMPACT),
            HudRowFormatter.format(List.of("Health 20", "Food 18"), HudModule.RowFormat.COMPACT, "")
        );
    }

    @Test
    void twoColumnRowsPairValuesAndKeepAnOddLastValue() {
        assertEquals(
            List.of(
                new HudRowFormatter.Row("Health 20", "Food 18"),
                new HudRowFormatter.Row("Armor 8", "XP 12")
            ),
            HudRowFormatter.format(
                List.of("Health 20", "Food 18", "Armor 8", "XP 12"),
                HudModule.RowFormat.TWO_COLUMN
            )
        );
        assertEquals(
            List.of(
                new HudRowFormatter.Row("Health 20", "Food 18"),
                new HudRowFormatter.Row("Armor 8", "")
            ),
            HudRowFormatter.format(
                List.of("Health 20", "Food 18", "Armor 8"),
                HudModule.RowFormat.TWO_COLUMN
            )
        );
    }

    @Test
    void twoColumnWidthAccountsForConfiguredGapAndOddRows() {
        var paired = new HudRowFormatter.Row("Health", "Food");
        var odd = new HudRowFormatter.Row("Armor", "");
        assertEquals(85, HudRowFormatter.columnWidth(paired, 40, 33, 12));
        assertEquals(40, HudRowFormatter.columnWidth(odd, 40, 33, 12));
        assertEquals(73, HudRowFormatter.columnWidth(paired, 40, 33, -4));
        assertEquals(0, HudRowFormatter.columnWidth(null, 40, 33, 12));
    }

    @Test
    void emptyInputProducesNoRows() {
        assertEquals(List.of(), HudRowFormatter.format(List.of(), HudModule.RowFormat.TWO_COLUMN));
        assertEquals(List.of(), HudRowFormatter.format(null, HudModule.RowFormat.TWO_COLUMN));
    }
}
