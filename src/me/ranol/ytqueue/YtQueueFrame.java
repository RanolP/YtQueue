package me.ranol.ytqueue;

import com.jtattoo.plaf.fast.FastLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

public class YtQueueFrame extends JFrame {
    public static final YtQueueFrame INSTANCE = new YtQueueFrame();

    public static class MouseListener extends MouseAdapter {
        public static final MouseListener INSTANCE = new MouseListener();
        Point pressed;
        Map<Integer, Integer> cursors = new HashMap<>();
        Insets i = new Insets(5, 5, 5, 5);
        int WEST = 1;
        int EAST = 2;
        int NORTH = 4;
        int SOUTH = 8;

        private MouseListener() {
            cursors.put(WEST, Cursor.W_RESIZE_CURSOR);
            cursors.put(EAST, Cursor.E_RESIZE_CURSOR);
            cursors.put(NORTH, Cursor.N_RESIZE_CURSOR);
            cursors.put(NORTH + WEST, Cursor.NW_RESIZE_CURSOR);
            cursors.put(NORTH + EAST, Cursor.NE_RESIZE_CURSOR);
            cursors.put(SOUTH, Cursor.S_RESIZE_CURSOR);
            cursors.put(SOUTH + WEST, Cursor.SW_RESIZE_CURSOR);
            cursors.put(SOUTH + EAST, Cursor.SE_RESIZE_CURSOR);
        }

        private void updateVisibility(boolean val) {
            SearchPanel.INSTANCE.setVisible(val);
        }

        private void close() {
            VideoViewer.INSTANCE.exit();
            JFrame f = YtQueueFrame.INSTANCE;
            int x = f.getX(), y = f.getY();
            while (f.getHeight() > 10) {
                f.setBounds(x, y += 1, f.getWidth(), f.getHeight() - 2);
            }
            while (f.getWidth() > 0) {
                f.setBounds(x += 1, y, f.getWidth() - 2, f.getHeight());
            }
            f.setVisible(false);
            f.dispose();
            System.exit(0);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isControlDown() || e.isShiftDown() && e.getButton() != MouseEvent.BUTTON3) {
                VideoViewer viewer = VideoViewer.INSTANCE;
                if (viewer.PLAYER.isPlaying()) {
                    Dimension dim = viewer.PLAYER.getVideoDimension();
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        updateVisibility(false);
                    } else {
                        updateVisibility(true);
                    }
                    YtQueueFrame frame = YtQueueFrame.INSTANCE;
                    int x = frame.getX(), y = frame.getY(), w = frame.getWidth(), h = frame.getHeight();
                    double simillar = dim.width / (double) w;
                    if (e.isShiftDown()) {
                        simillar = 1;
                    }
                    dim.width = (int) (dim.width / simillar);
                    dim.height = (int) (dim.height / simillar) +
                                 (e.getButton() != MouseEvent.BUTTON2 ? 0 : SearchPanel.INSTANCE.getHeight() +
                                                                            VideoViewer.INSTANCE.MANAGER.getHeight());
                    frame.setBounds(x + w - dim.width, y + h - dim.height, dim.width, dim.height);
                }
            }
            if (e.getClickCount() == 2) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    updateVisibility(!SearchPanel.INSTANCE.isVisible());
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    close();
                }
            }
        }

        private int direction;

        @Override
        public void mouseMoved(MouseEvent e) {
            YtQueueFrame f = YtQueueFrame.INSTANCE;
            Point p = e.getLocationOnScreen();
            p.x -= f.getX();
            p.y -= f.getY();
            direction = 0;
            if (p.x < i.left) {
                direction += WEST;
            }
            if (p.x > f.getWidth() - i.right) {
                direction += EAST;
            }
            if (p.y < i.top) {
                direction += NORTH;
            }
            if (p.y > f.getHeight() - i.bottom) {
                direction += SOUTH;
            }
            if (direction == 0) {
                f.setCursor(Cursor.getDefaultCursor());
            } else {
                f.setCursor(Cursor.getPredefinedCursor(cursors.get(direction)));
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            pressed = e.getLocationOnScreen();
            YtQueueFrame f = YtQueueFrame.INSTANCE;
            pressed.x -= f.getX();
            pressed.y -= f.getY();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            JFrame f = YtQueueFrame.INSTANCE;
            Point p = e.getLocationOnScreen();
            boolean resized = false;
            if ((direction & NORTH) == NORTH) {
                int a = f.getY() - p.y;
                f.setBounds(f.getX(), f.getY() - a, f.getWidth(), f.getHeight() + a);
                resized = true;
            }
            if ((direction & SOUTH) == SOUTH) {
                int a = p.y - f.getHeight() - f.getY();
                f.setBounds(f.getX(), f.getY(), f.getWidth(), f.getHeight() + a);
                resized = true;
            }
            if ((direction & WEST) == WEST) {
                int a = f.getX() - p.x;
                f.setBounds(f.getX() - a, f.getY(), f.getWidth() + a, f.getHeight());
                resized = true;
            }
            if ((direction & EAST) == EAST) {
                int a = p.x - f.getWidth() - f.getX();
                f.setBounds(f.getX(), f.getY(), f.getWidth() + a, f.getHeight());
                resized = true;
            }
            if (!resized && pressed != null) {
                f.setLocation(p.x - pressed.x, p.y - pressed.y);
            }
            if (resized) {
                f.repaint();
            }
        }
    }

    private class Converter {
        int min;

        public Converter(int min) {
            this.min = min;
        }

        int convert(Rectangle r, int i) {
            return i < 0 ? 0 : Math.min(i, min);
        }
    }

    private int width = 0;
    private int height = 0;
    private int x = 0;
    private int y = 0;

    private void caching() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        Rectangle DESKTOP = gc.getBounds();
        width = DESKTOP.width - insets.left - insets.right;
        height = DESKTOP.height - insets.top - insets.bottom;
        x = DESKTOP.width - insets.right;
        y = DESKTOP.height - insets.bottom;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        width = width > 0 ? Math.min(width, this.width) : 0;
        height = height > 0 ? Math.min(height, this.height) : 0;
        x = x > 0 ? Math.min(x, this.x - width) : 0;
        y = y > 0 ? Math.min(y, this.y - height) : 0;
        super.setBounds(x, y, width, height);
    }

    private YtQueueFrame() {
        caching();
        setSize(600, 400);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("YtQueue");
        setAlwaysOnTop(true);
        setLayout(new BorderLayout());
        add(SearchPanel.INSTANCE, BorderLayout.NORTH);
        try {

            UIManager.setLookAndFeel(new FastLookAndFeel());

            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        add(VideoViewer.INSTANCE, BorderLayout.CENTER);
        setUndecorated(true);
        addMouseListener(MouseListener.INSTANCE);
        addMouseMotionListener(MouseListener.INSTANCE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                VideoViewer.INSTANCE.exit();
            }
        });
    }
}