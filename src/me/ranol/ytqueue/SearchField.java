package me.ranol.ytqueue;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchField<E> extends JComboBox<String> {
    private static class Renderer extends BasicComboBoxRenderer {
        private SearchField<?> field;

        public Renderer(SearchField<?> field) {
            this.field = field;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            if (index == -1) {
                field.removeAllItems();
                field.setEditable(true);
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    @FunctionalInterface
    public interface Searcher<E> {
        Map<String, E> search(String s);
    }

    private Searcher<E> searcher;
    private HashMap<String, E> map = new HashMap<>();
    private Vector<String> data;
    private final ExecutorService SERVICE = Executors.newSingleThreadExecutor();

    private SearchField(Vector<String> vec) {
        super(vec);
        this.data = vec;
        setEditable(true);
        getEditor().addActionListener(e -> searchNow());
        addActionListener(e -> searchNow());
        addItemListener(e -> {
            setRenderer(new Renderer(this));
            if (e.getStateChange() == ItemEvent.SELECTED) {
                SwingUtilities.invokeLater(() -> {
                    String s = String.valueOf(e.getItem());
                    SearchEvent<E> event = new SearchEvent<>(this, 0, s, map.get(s));
                    for (@SuppressWarnings("unchecked") SearchListener<E> listener : listenerList.getListeners(
                            SearchListener.class)) {
                        listener.onSearched(event);
                    }
                    removeAllItems();
                    setEditable(true);
                });
            }
        });
    }

    public SearchField() {
        this(new Vector<>());
    }

    public void setSearcher(Searcher<E> searcher) {
        this.searcher = searcher;
    }

    public String getText() {
        Object r = getSelectedItem();
        return r == null ? "" : r.toString();
    }

    public void searchNow() {
        if (!isEditable()) {
            return;
        }
        String keyword = getText();
        if (getItemCount() > 1 || keyword == null || keyword.isEmpty()) {
            return;
        }
        setEditable(false);
        if (getItemCount() > 0) {
            removeAllItems();
        }
        map.clear();
        if (searcher != null) {
            removeAllItems();
            SERVICE.execute(() -> {
                Map<String, E> get = searcher.search(keyword);
                map.putAll(get);
                SwingUtilities.invokeLater(() -> {
                    removeAllItems();
                    if (get.isEmpty()) {
                        setEditable(true);
                        repaint();
                        return;
                    }
                    data.addAll(get.keySet());
                    showPopup();
                    repaint();
                });
            });
        } else {
            setEditable(true);
        }
    }

    public void addSearchListener(SearchListener<E> l) {
        listenerList.add(SearchListener.class, l);
    }
}
