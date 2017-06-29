package me.ranol.ytqueue;

import com.jtattoo.plaf.mcwin.McWinLookAndFeel;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VideoViewer extends JPanel {
    private static final String WAIT = "영상 불러오는 중";
    public static final VideoViewer INSTANCE = new VideoViewer();
    private String url;
    private EmbeddedMediaPlayerComponent component = new EmbeddedMediaPlayerComponent();
    public final EmbeddedMediaPlayer PLAYER = component.getMediaPlayer();
    private final ExecutorService SERVICE = Executors.newSingleThreadExecutor();
    public final VideoManager MANAGER = new VideoManager();

    public class VideoManager extends Panel {
        JLabel max = new JLabel();

        {
            setLayout(new BorderLayout());
            // setBackground(new Color(0, 0, 0, 0));
            setBorder(new LineBorder(Color.BLACK));
            JButton stop = new JButton("❚❚");
            stop.setEnabled(false);

            stop.addActionListener(e -> {
                if (!PLAYER.isPlayable()) { return; }
                if (PLAYER.isPlaying()) {
                    PLAYER.pause();
                    stop.setText("▶");
                } else {
                    PLAYER.start();
                    stop.setText("❚❚");
                }
            });
            JPanel p = new JPanel();
            p.add(stop, BorderLayout.WEST);
            JSlider volumeSlider = new JSlider(0, 100, 50);
            JComboBox<?> box = new JComboBox();
            volumeSlider.addChangeListener(e -> {
                PLAYER.setVolume(volumeSlider.getValue());
            });
            p.add(volumeSlider, BorderLayout.CENTER);
            add(p, BorderLayout.WEST);
            JSlider timeSlider = new JSlider(0, 100, 0);
            JPanel p2 = new JPanel();
            p2.setLayout(new BorderLayout());
            p2.add(timeSlider, BorderLayout.CENTER);
            add(p2, BorderLayout.CENTER);
            timeSlider.addChangeListener(e -> {
                if (!PLAYER.isPlayable()) {
                    timeSlider.setValue(0);
                }
            });
            timeSlider.addMouseListener(new MouseAdapter() {
                void click() {
                    stop.setText("▶");
                    if (PLAYER.isPlaying()) {
                        PLAYER.pause();
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    click();
                    release();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    click();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    click();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    release();
                }

                public void release() {
                    PLAYER.setTime(TimeUnit.SECONDS.toMillis(timeSlider.getValue()));
                    stop.setText("❚❚");
                    SwingUtilities.invokeLater(() -> {
                        if (!PLAYER.isPlaying() && PLAYER.isPlayable()) {
                            PLAYER.start();
                        }
                    });
                }
            });
            PLAYER.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                @Override
                public void playing(MediaPlayer mediaPlayer) {
                    stop.setEnabled(true);
                    stop.setText("❚❚");
                    long len = PLAYER.getLength();
                    timeSlider.setMaximum((int) TimeUnit.MILLISECONDS.toSeconds(len));
                    max.setText(TimeUnit.MILLISECONDS.toHours(len) +
                                ":" +
                                TimeUnit.MILLISECONDS.toMinutes(len) +
                                ":" +
                                TimeUnit.MILLISECONDS.toSeconds(len));
                }

                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    stop.setEnabled(false);
                    stop.setText("▶");
                    timeSlider.setValue(0);
                    component.getVideoSurface().repaint();
                }

                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                    timeSlider.setValue((int) TimeUnit.MILLISECONDS.toSeconds(newTime));
                }

                @Override
                public void lengthChanged(MediaPlayer mediaPlayer, long len) {
                    timeSlider.setMaximum((int) TimeUnit.MILLISECONDS.toSeconds(len));
                }
            });
        }
    }

    private VideoViewer() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        Canvas c = component.getVideoSurface();
        add(c, BorderLayout.CENTER);
        add(MANAGER, BorderLayout.SOUTH);
        c.addMouseListener(YtQueueFrame.MouseListener.INSTANCE);
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                MANAGER.setVisible(SearchPanel.INSTANCE.isVisible() || e.getPoint().y >= c.getHeight() - 45);
            }
        });
        c.addMouseMotionListener(YtQueueFrame.MouseListener.INSTANCE);
        c.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                c.repaint();
            }
        });
        PLAYER.setPlaySubItems(true);
        PLAYER.setVolume(50);
        MANAGER.setVisible(true);
        try {

            UIManager.setLookAndFeel(new McWinLookAndFeel());

            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public VideoViewer setUrl(String url) {
        this.url = url;
        return this;
    }

    public VideoViewer pause() {
        if (component.getMediaPlayer().isPlaying()) {
            component.getMediaPlayer().stop();
        }
        return this;
    }

    public VideoViewer play() {
        if (!component.getMediaPlayer().isPlaying() && url != null) {
            SERVICE.execute(() -> {
                component.getMediaPlayer().playMedia(url);
            });
        }
        return this;
    }

    public void exit() {
        pause();
        component.getMediaPlayer().release();
        SERVICE.shutdownNow();
    }
}