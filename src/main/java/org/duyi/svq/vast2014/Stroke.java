package org.duyi.svq.vast2014;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * 笔迹
 * @author	duyi
 * @date	2014年6月17日
 */
public class Stroke{
	ArrayList<Point2D> points = new ArrayList<Point2D>();
	double minX = -1,minY=-1,maxX=-1,maxY=-1;
	
	public Stroke(){
		
	}
	public void addPoint(double x, double y){
		
	}
	public void addPoint(int x, int y){
		addPoint(new Point(x, y));
	}
	
	public void addPoint(Point2D p){
		if(minX == -1)
			minX = p.getX();
		else if(p.getX() < minX)
			minX = p.getX();
		if(minY == -1)
			minY = p.getY();
		else if(p.getY() < minY)
			minY = p.getY();
		if(maxX == -1)
			maxX = p.getX();
		else if(p.getX() > maxX)
			maxX = p.getX();
		if(maxY == -1 || p.getY() > maxY)
			maxY = p.getY();
		points.add(p);
	}
	
	public double[] getXs(){
		double [] r = new double[points.size()];
		for(int i = 0; i < points.size(); i++){
			r[i] = points.get(i).getX();
		}
		return r;
	}
	
	public int[] getIntXs(){
		int [] r = new int[points.size()];
		for(int i = 0; i < points.size(); i++){
			r[i] = (int)points.get(i).getX();
		}
		return r;
	}
	
	public double[] getYs(){
		double [] r = new double[points.size()];
		for(int i = 0; i < points.size(); i++){
			r[i] = points.get(i).getY();
		}
		return r;
	}
	
	public int[] getIntYs(){
		int [] r = new int[points.size()];
		for(int i = 0; i < points.size(); i++){
			r[i] = (int)points.get(i).getY();
		}
		return r;
	}
	
	public int getSize(){
		return points.size();
	}
	
	public Rectangle2D getBoundingBox(){		
		return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
	}
	
	public double getInterval(){
		return maxX - minX;
	}
	public ArrayList<Point2D> getNormalize(){
		ArrayList<Point2D> r = new ArrayList<Point2D>();
		for(Point2D p : points){
			r.add(new Point2D.Double((p.getX()-minX)/(maxX-minX), (p.getY()-minY)/(maxX-minX)));
		}
		return r;
	}
	
	public ArrayList<Point2D> getPoints(){
		return points;
	}
}