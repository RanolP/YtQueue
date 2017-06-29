package me.ranol.ytqueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import uk.co.caprica.vlcj.filter.swing.SwingFileFilterFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

public class SearchPanel extends JPanel {
    public static final SearchPanel INSTANCE = new SearchPanel();

    private SearchPanel() {
        setBackground(Color.GRAY);
        setLayout(new BorderLayout());
        SearchField<String> search = new SearchField<>();
        add(search, BorderLayout.CENTER);
        JButton fromFile = new JButton("From file");
        add(fromFile, BorderLayout.WEST);
        JPanel top = new JPanel();
        top.setBackground(Color.DARK_GRAY);
        add(top, BorderLayout.NORTH);
        File desktop = new File(System.getProperty("user.home") + "/Desktop");
        search.setSearcher(s -> {
            Map<String, String> result = new HashMap<>();
            try {
                Document d = Jsoup.connect("https://www.youtube.com/results?q=" + s).get();
                for (Element e : d.select("div.yt-lockup-content > h3 > a")) {
                    result.put(e.attr("title"), "https://youtube.com" + e.attr("href"));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return result;
        });
        search.addSearchListener(e -> VideoViewer.INSTANCE.setUrl(e.getValue()).pause().play());
        fromFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(desktop);
            chooser.setFileFilter(SwingFileFilterFactory.newVideoFileFilter());
            chooser.setFileFilter(SwingFileFilterFactory.newPlayListFileFilter());
            chooser.showOpenDialog(YtQueueFrame.INSTANCE);
            File f = chooser.getSelectedFile();
            if (f != null) {
                VideoViewer.INSTANCE.setUrl(f.getAbsolutePath()).pause().play();
            }
        });
    }
}