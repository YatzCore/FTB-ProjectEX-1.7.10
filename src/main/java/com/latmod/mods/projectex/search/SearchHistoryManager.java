package com.latmod.mods.projectex.search;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryManager {
    private static final int MAX_HISTORY = 30;
    private static final List<String> HISTORY = new ArrayList<String>();
    private static int historyIndex = -1;
    private static String tempCurrentText = "";

    public static synchronized void addHistory(String query) {
        if (query == null) return;
        query = query.trim();
        if (query.isEmpty()) return;

        // Don't duplicate if same as most recent
        if (!HISTORY.isEmpty() && HISTORY.get(HISTORY.size() - 1).equalsIgnoreCase(query)) {
            historyIndex = -1;
            return;
        }

        HISTORY.remove(query);
        HISTORY.add(query);
        if (HISTORY.size() > MAX_HISTORY) {
            HISTORY.remove(0);
        }
        historyIndex = -1;
    }

    public static synchronized void resetCursor() {
        historyIndex = -1;
        tempCurrentText = "";
    }

    public static synchronized String navigateUp(String currentInput) {
        if (HISTORY.isEmpty()) {
            return currentInput;
        }

        if (historyIndex == -1) {
            tempCurrentText = currentInput != null ? currentInput : "";
            historyIndex = HISTORY.size() - 1;
            return HISTORY.get(historyIndex);
        } else if (historyIndex > 0) {
            historyIndex--;
            return HISTORY.get(historyIndex);
        } else {
            return HISTORY.get(0);
        }
    }

    public static synchronized String navigateDown(String currentInput) {
        if (HISTORY.isEmpty() || historyIndex == -1) {
            return currentInput;
        }

        if (historyIndex < HISTORY.size() - 1) {
            historyIndex++;
            return HISTORY.get(historyIndex);
        } else {
            historyIndex = -1;
            return tempCurrentText;
        }
    }

    public static List<String> getHistory() {
        return new ArrayList<String>(HISTORY);
    }
}
