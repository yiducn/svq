/**
 * 
 */
package org.duyi.svq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListDataListener;

import org.duyi.svq.models.GroupOperationModel;
import org.duyi.svq.models.SearchModel;
import org.duyi.svq.models.TimeModel;
import org.duyi.svq.vast2014.ConstantsSVQ;
import org.duyi.svq.vast2014.PaintPanel2;
import org.duyi.svq.vast2014.ResultPanel;
import org.duyi.svq.vast2014.SketchQuery;

import prefuse.data.Table;
import prefuse.data.io.DelimitedTextTableReader;

/**
 * 核心面板
 * @author	duyi
 * @date	2014年6月16日
 */
public class SearchPanel extends JPanel {
	JScrollPane	jpanelLeft	=	null;
	JPanel	jpanelRight	=	null;
	JPanel	jpanelBottom	=	null;
	ResultPanel	jpanelTop		=	null;//草图操作panel
	PaintPanel2	jpanelCenter	=	null;
	
	SearchModel	model		=	null;
	
	JButton	jbuttonTimeYear	=	null;
	JButton	jbuttonTimeMonth	=	null;
	JButton	jbuttonTimeDay	=	null;
	JButton	jbuttonTimeHour	=	null;
	JButton	jbuttonTimeMin	=	null;
	JButton	jbuttonTimeSec	=	null;
	
	JButton	jbuttonSketch	=	null;
	
	JButton jbuttonFishAction	=	null;	
	JButton	jbuttonTimeIntervalAdd	=	null;
	JButton	jbuttonTimeIntervalRem	=	null;//缩小
	JButton	jbuttonOrgInk			=	null;
	JComboBox<String> jcomboxTimeGroup	=	null;
	
//	JButton
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public SearchPanel() {
		initData();
		initUIs();
	}

	private void initData() {
		if(model == null){
			model = new SearchModel();
		}
	}

	private void initUIs() {
		this.setLayout(new BorderLayout());
		add(createBottom(), BorderLayout.NORTH);
		add(createCenter(), BorderLayout.CENTER);
		add(createLeft(),BorderLayout.WEST);
//		add(createRight(), BorderLayout.EAST);
		add(createTop(), BorderLayout.SOUTH);		
	}

	private Component createTop() {
		if(jpanelTop == null){
			jpanelTop = new ResultPanel();
			jpanelTop.setPreferredSize(new Dimension(jpanelTop.getWidth(), 200));
		}
		return jpanelTop;
	}

	private Component createRight() {
		if(jpanelRight == null){
			jpanelRight = new JPanel();
			jpanelRight.setMinimumSize(new Dimension(50, 200));
			jpanelRight = new JPanel();
			BoxLayout layout = new BoxLayout(jpanelRight, BoxLayout.Y_AXIS);
			jpanelRight.setLayout(layout);
			
			JLabel jlabelGroup = new JLabel("Group");
			
			jpanelRight.add(jlabelGroup);
			jpanelRight.add(new JButton("test"));
		}
		return jpanelRight;
	}

	private Component createCenter() {
		if(jpanelCenter == null){
			String data = ConstantsSVQ.PARSE_DATA;//"/NASDAQ_daily_prices_J4.txt";///demo.txt";//"/fisher.iris.txt";//"/NASDAQ_daily_prices_J2.txt";
			Table table = null;
	        try {
	        	table = (new DelimitedTextTableReader()).readTable(data);
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.exit(1);
	        }
	        if(jpanelTop == null)
	        	createTop();
			jpanelCenter = new PaintPanel2(table, jpanelTop);
//			jpanelCenter = new PaintPanel(model);
//			jpanelCenter.setMinimumSize(new Dimension(800, 600));
//			jpanelCenter.setBackground(Color.WHITE);
		}
		return jpanelCenter;
	}

	private Component createBottom() {
		if(jpanelBottom == null){
			jpanelBottom = new JPanel();
			jpanelBottom.setBackground(Color.LIGHT_GRAY);
			FlowLayout layout = new FlowLayout();
			layout.setAlignment(FlowLayout.LEFT);
			jpanelBottom.setLayout(layout);
			
			JLabel	jlabelTime	=	new JLabel("Time Selection:");
			jpanelBottom.add(jlabelTime);
			jbuttonFishAction = new JButton("Fish Eye Control");
			jbuttonTimeYear = new JButton(TimeModel.UNITS[1]);
			jbuttonTimeMonth = new JButton(TimeModel.UNITS[2]);
			jbuttonTimeDay = new JButton(TimeModel.UNITS[3]);
			jbuttonTimeHour = new JButton(TimeModel.UNITS[4]);
			jbuttonTimeMin = new JButton(TimeModel.UNITS[5]);
			jbuttonTimeSec = new JButton(TimeModel.UNITS[6]);
			//TODO setenable
			
			jbuttonTimeIntervalAdd = new JButton("+");
			jbuttonTimeIntervalRem = new JButton("-");
			
			jbuttonFishAction.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					actionPerformedFishAction(e);
				}
			});
			
			jbuttonOrgInk = new JButton("Ink");			
			jbuttonOrgInk.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					actionPerformedOrgInk(e);
				}
			});
			
			
			jpanelBottom.add(jbuttonOrgInk);
			jpanelBottom.add(jbuttonFishAction);
			jpanelBottom.add(jbuttonTimeYear);
			jpanelBottom.add(jbuttonTimeMonth);
			jpanelBottom.add(jbuttonTimeDay);
			jpanelBottom.add(jbuttonTimeHour);
			jpanelBottom.add(jbuttonTimeMin);
			jpanelBottom.add(jbuttonTimeSec);
			
			JLabel	jlabelGroup	=	new JLabel("Group method:");			
			jcomboxTimeGroup = new JComboBox<String>();
			DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(GroupOperationModel.GROUP_OPERATIONS);
			jcomboxTimeGroup.setModel(model);
			jpanelBottom.add(jlabelGroup);
			jpanelBottom.add(jcomboxTimeGroup);
			
			jpanelBottom.add(jbuttonTimeIntervalAdd);
			jpanelBottom.add(jbuttonTimeIntervalRem);
			
		}
		return jpanelBottom;
	}


	private Component createLeft() {
		if(jpanelLeft == null){
			jpanelLeft = new JScrollPane();
			jpanelLeft.setAutoscrolls(true);
			jpanelLeft.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			JPanel pane = new JPanel();			
			BoxLayout layout = new BoxLayout(pane, BoxLayout.Y_AXIS);
			pane.setLayout(layout);
			
			jbuttonSketch = new JButton("SketchQ");
			jbuttonSketch.setMargin(new Insets(0, 0, 0, 0));
			jbuttonSketch.setFont(new Font("Arial", Font.PLAIN, 8));
			pane.add(jbuttonSketch);
			jbuttonSketch.setPreferredSize(new Dimension(50,20));
			jbuttonSketch.addActionListener(new ActionListener() {				
				public void actionPerformed(ActionEvent e) {
					actionPerformedSketch(e);
				}
			});
			
			JButton jLabel = new JButton("All");
			jLabel.setFont(new Font("Arial", Font.PLAIN, 8));
			jLabel.setBackground(Color.BLACK);		
			jLabel.setForeground(Color.white);
			jLabel.setPreferredSize(new Dimension(50, 20));
			jLabel.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					actionPerformedChangeColor(e);
				}
			});
			pane.add(jLabel);
			Map m = jpanelCenter.getColorMap();
			for(Object o: m.keySet()){
				jLabel = new JButton();//o.toString());
				jLabel.setToolTipText(o.toString());
				jLabel.setPreferredSize(new Dimension(50, 10));
//				jLabel.setPreferredSize(new Dimension((int)jpanelCenter.getAvgByField("stock_price_adj_close","stock_symbol", o.toString()), 10));
				jLabel.setBackground(new Color(Integer.parseInt(m.get(o).toString())));
				jLabel.setForeground(Color.black);//new Color(Integer.parseInt(m.get(o).toString())));
				jLabel.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						actionPerformedChangeColor(e);
					}
				});
				pane.add(jLabel);
			}	
			jpanelLeft.setViewportView(pane);
			
		}		
		
		return jpanelLeft;
	}
	

	protected void actionPerformedOrgInk(ActionEvent e) {
		jpanelCenter.setActionType(PaintPanel2.ACTION_INK);
	}
	
	/**
	 * 点击后显示鱼眼
	 * @param e
	 */
	private void actionPerformedFishAction(ActionEvent e) {
		jpanelCenter.setActionType(PaintPanel2.ACTION_FISHEYE);
	}
	/**
	 * 点击后显示sketch
	 * @param e
	 */
	protected void actionPerformedSketch(ActionEvent e) {
		jpanelCenter.setActionType(PaintPanel2.ACTION_SKETCH_QUERY);
	}	
	
	/**
	 * 根据按钮内容改变颜色
	 * @param e
	 */
	protected void actionPerformedChangeColor(ActionEvent e) {
		jpanelCenter.setActionType(PaintPanel2.ACTION_SKETCH_QUERY);
		if(e.getSource() instanceof JButton){
			JButton b = (JButton)e.getSource();
			if(b.getText().equals("All")){
				jpanelCenter.setStrokeColor(Color.BLACK);
				jpanelCenter.getSq().setQueryContentType(SketchQuery.QCONTENT_TYPE_ALL, null);
			}else{				
				jpanelCenter.getSq().setQueryContentType(SketchQuery.QCONTENT_TYPE_ONE, b.getToolTipText());
				Map m = jpanelCenter.getColorMap();
				jpanelCenter.setStrokeColor(new Color(Integer.parseInt(m.get(b.getToolTipText()).toString())));
			}
			
		}
	}

	void updateMainPanel(){
		model.getTime();
		redrawXaxis();
		redrawYaxis();
		
	}

	private void redrawYaxis() {
		// TODO Auto-generated method stub
		
	}

	private void redrawXaxis() {
		// TODO Auto-generated method stub
		
	}
}
