package me.ranol.ytqueue;

import java.util.EventListener;

@FunctionalInterface
public interface SearchListener<E> extends EventListener {
    void onSearched(SearchEvent<E> e);
}
