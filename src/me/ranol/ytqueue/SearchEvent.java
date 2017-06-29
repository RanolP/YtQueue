package me.ranol.ytqueue;

import java.awt.*;

public class SearchEvent<E> extends AWTEvent {
    private final String selected;
    private final E value;

    public SearchEvent(Object source, int id, String selected, E value) {
        super(source, id);
        this.selected = selected;
        this.value = value;
    }

    public String getSelected() {
        return selected;
    }

    public E getValue() {
        return value;
    }
}
