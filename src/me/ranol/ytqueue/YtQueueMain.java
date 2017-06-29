package me.ranol.ytqueue;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;

public class YtQueueMain {
    public static void main(String[] args) {
        boolean found = new NativeDiscovery().discover();
        if (!found) {
            JOptionPane.showMessageDialog(null,
                                          "VLC media player를 찾을 수 없습니다.\n\n\nhttps://nightlies.videolan.org/ 해당 URL을 통해 3.0.0 이상의 Nightly 빌드를 받아주세요.",
                                          "오류", JOptionPane.ERROR_MESSAGE);
        }
        System.out.println(LibVlc.INSTANCE.libvlc_get_version());
        YtQueueFrame.INSTANCE.setVisible(true);
        // Youtube.search("KK - 결벽증").page(1);
        //YoutubeVideo video = new YoutubeVideo("5FiUImljGf8");
        //video.parse();
    }
}