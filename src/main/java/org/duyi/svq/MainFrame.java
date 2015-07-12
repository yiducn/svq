/**
 * 
 */
package org.duyi.svq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.duyi.utils.FrameUtils;

/**
 * @author	duyi
 * @date	2014年6月16日
 */
public class MainFrame extends JFrame {
	JMenuBar	jmenubar	=	null;
	JMenu		jmenuMain	=	null;
	JMenuItem	jmenuitemCreate	=	null;//create one query
	JMenuItem	jmenuitemExit	=	null;//exit
	
	JSplitPane	jsplitpaneMain	=	null;
	JPanel		jpanelQuery		=	null;
	JPanel		jpanelResult	=	null;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @throws java.awt.HeadlessException
	 */
	public MainFrame() throws HeadlessException {
		this.setTitle("Sketch Visual Querying");
		initUIs();
	}

	private void initUIs() {
		this.add(createMenuBar(), BorderLayout.NORTH);
		this.add(createSplit(), BorderLayout.CENTER);
	}

	private Component createSplit() {
		if(jsplitpaneMain == null){
			jsplitpaneMain = new JSplitPane();
			jsplitpaneMain.setLeftComponent(createQueryPanel());
			jsplitpaneMain.setRightComponent(createResultPanel());
			jsplitpaneMain.setDividerLocation(0.2);
			jsplitpaneMain.setOneTouchExpandable(true);
		}
		return jsplitpaneMain;
	}

	private Component createResultPanel() {
		if(jpanelResult == null){
			jpanelResult = new JPanel();
			jpanelResult.setLayout(new GridLayout(1, 1));
//			jpanelResult.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width*5/6,
//					jpanelResult.getHeight()));
			jpanelResult.add(new SearchPanel());
		}
		return jpanelResult;
	}

	private Component createQueryPanel() {
		if(jpanelQuery == null){
			jpanelQuery = new JPanel();
//			jpanelQuery.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width/6,
//					jpanelQuery.getHeight()));
			jpanelQuery.setBackground(Color.white);
		}
		return jpanelQuery;
	}

	private JComponent createMenuBar() {
		if(jmenubar == null){
			jmenubar = new JMenuBar();
			jmenubar.add(createMenuMain());
		}
		return jmenubar;
	}

	private JMenu createMenuMain() {
		if(jmenuMain == null){
			jmenuMain = new JMenu("Operation");
			if(jmenuitemCreate == null){
				jmenuitemCreate = new JMenuItem("Create one query");
			}
			jmenuMain.add(jmenuitemCreate);
			if(jmenuitemExit == null){
				jmenuitemExit = new JMenuItem("Exit");
				jmenuitemExit.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						actionPerformedExit(e);
					}
				});
			}
			jmenuMain.add(jmenuitemExit);
		}
		return jmenuMain;
	}

	protected void actionPerformedExit(ActionEvent e) {
		//make sure to exit, save data
		System.exit(0);
	}

	/**
	 * @param gc
	 */
	public MainFrame(GraphicsConfiguration gc) {
		super(gc);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param title
	 * @throws java.awt.HeadlessException
	 */
	public MainFrame(String title) throws HeadlessException {
		super(title);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param title
	 * @param gc
	 */
	public MainFrame(String title, GraphicsConfiguration gc) {
		super(title, gc);
		// TODO Auto-generated constructor stub
	}

}
