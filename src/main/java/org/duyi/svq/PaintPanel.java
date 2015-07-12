/**
 * 
 */
package org.duyi.svq;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.duyi.svq.models.SearchModel;

/**
 * 中间区域草图绘制面板
 * @author	duyi
 * @date	2014年6月17日
 */
public class PaintPanel extends JPanel implements MouseListener, MouseMotionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	SearchModel model;
	
	boolean		isdrawing	=	false;
	
	ArrayList<Stroke>	strokeList	=	null;
	Stroke				currentStroke	=	null;
	
	/**
	 * 
	 */
	public PaintPanel(SearchModel model) {
		this.model = model;
		strokeList = new ArrayList<Stroke>();
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		Dimension d = this.getSize();
		g2.drawLine(d.width/20, d.height - d.height/20, d.width-d.width/20, d.height - d.height/20);
		g2.drawLine(d.width/20, d.height - d.height/20, d.width/20, d.height/20);
		
		drawStrokes(g);
	}
	
	private void drawStrokes(Graphics g) {
		for(Stroke s : strokeList){
			g.drawPolyline(s.getXs(), s.getYs(), s.getSize());
		}
	}
	public void mouseDragged(MouseEvent e) {
		currentStroke.addPoint(e.getPoint());
		repaint();
	}
	public void mouseMoved(MouseEvent e) {
	
	}
	
	public void mouseClicked(MouseEvent e) {
		
	}
	public void mousePressed(MouseEvent e) {
		isdrawing = true;
		currentStroke = new Stroke();
		strokeList.add(currentStroke);
		repaint();
		
	}
	public void mouseReleased(MouseEvent e) {
		isdrawing = false;
	}
	public void mouseEntered(MouseEvent e) {
		
	}
	public void mouseExited(MouseEvent e) {
		isdrawing = false;
		
	}

}

/**
 * 笔迹
 * @author	duyi
 * @date	2014年6月17日
 */
class Stroke{
	ArrayList<Point> points = new ArrayList<Point>();
	public Stroke(){
		
	}
	
	public void addPoint(int x, int y){
		points.add(new Point(x, y));
	}
	
	public void addPoint(Point p){
		points.add(p);
	}
	
	public int[] getXs(){
		int [] r = new int[points.size()];
		for(int i = 0; i < points.size(); i++){
			r[i] = points.get(i).x;
		}
		return r;
	}
	
	public int[] getYs(){
		int [] r = new int[points.size()];
		for(int i = 0; i < points.size(); i++){
			r[i] = points.get(i).y;
		}
		return r;
	}
	
	public int getSize(){
		return points.size();
	}
}
