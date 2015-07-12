package org.duyi.svq;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.duyi.utils.FrameUtils;

/**
 * Hello world!
 * 正常会出现out of memory 错误，需要设置vm的最大内存。
 * -Xms128m -Xmx1024m
 *
 */
public class App 
{
    public static void main( String[] args )
    {
       MainFrame mf = new MainFrame();
       FrameUtils.fullscreen(mf);
       mf.setVisible(true);
       mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
