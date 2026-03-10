package net.manameta.manaenchants.common.gui;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

final class GUIContext {
    private final Deque<InventoryGUI> stack = new ArrayDeque<>();

    void push(InventoryGUI gui) {
        stack.push(gui);
    }

    @Nullable InventoryGUI pop() {
        return stack.poll();
    }

    @Nullable InventoryGUI peek() {
        return stack.peek();
    }

    public boolean isEmpty() { return stack.isEmpty(); }

    int size() { return stack.size(); }

    private boolean navigatingBack;
    public boolean isNavigatingBack() { return navigatingBack; }
    void setNavigatingBack(boolean value) { navigatingBack = value;}
}