package com.example.modloader.api.gui;

import java.util.HashMap;
import java.util.Map;

public class GridLayout implements Layout {
    private final int rows;
    private final int cols;

    public GridLayout(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    @Override
    public Map<Integer, Component> arrangeComponents(Map<Component, Object> componentConstraints) {
        Map<Integer, Component> arranged = new HashMap<>();
        for (Map.Entry<Component, Object> entry : componentConstraints.entrySet()) {
            Component component = entry.getKey();
            GridConstraints constraints = (GridConstraints) entry.getValue();
            int slot = constraints.getRow() * cols + constraints.getCol();
            arranged.put(slot, component);
        }
        return arranged;
    }

    public static class GridConstraints {
        private final int row;
        private final int col;

        public GridConstraints(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }
    }
}