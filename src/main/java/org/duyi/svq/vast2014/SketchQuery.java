/**
 * 
 */
package org.duyi.svq.vast2014;

import java.awt.geom.Point2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.DelimitedTextTableReader;

/**
 * @author	duyi
 * @date	2014年6月23日
 */
public class SketchQuery {
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	//检索的时间范围(每天一个点，就是点数)
	private static final int NUM_CPOINTS = 12;//360;//5;
	 
	private	int 	queryContentType = 0;//0检索全部数据、1检索某个特定数据、2检索某个特定时间段内的全部数据
	private	String	queryContentString	=	null;
	public	static	final	int	QCONTENT_TYPE_ALL	=	0;
	public	static	final	int	QCONTENT_TYPE_ONE	=	1;
	
	private	int		queryType		=0;//0趋势检索、1范围检索[命令]
	private	Stroke	query	= new Stroke();
	
	private	ArrayList<Double> similarity = new ArrayList<Double>();
	
	private	String yalabel = ConstantsSVQ.XLABEL;//"avgg";//"avgg";//stock_price_open";//"avg";//stock_price_open");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SketchQuery().run();
	}

	public ArrayList<Table> run() {
		ArrayList<Table> tables = new ArrayList<Table>();
		Table table = null;
        try {
        	table = (new DelimitedTextTableReader()).readTable(ConstantsSVQ.QUERY_DATA);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        Iterator iter = table.tuplesReversed();
                
        ArrayList<Point2D> search = query.getNormalize();//query.getPoints();//
        ArrayList<Tuple> source = new ArrayList<Tuple>();
        ArrayList<Tuple> allSource = new ArrayList<Tuple>();
        
        if(queryContentString != null){
            while(iter.hasNext()){            	
            	Tuple t = (Tuple)iter.next();
//            	System.out.println(t.getString("stock_symbol")+":"+queryContentString);
            	if(t.getString("stock_symbol").equals(queryContentString)){
            		source.add(t);	
            	}
            }
            //这段代码用来把实际数值的y轴的最大值最小值求出来
            double maxY = source.get(0).getDouble(yalabel),
            		minY= source.get(0).getDouble(yalabel);
            for(int i = 1; i < source.size()-NUM_CPOINTS; i ++){
            	double temp = source.get(i).getDouble(yalabel);
            	if(temp > maxY)
            		maxY = temp;
            	if(temp < minY)
            		minY = temp;
            }
            
            similarity = new ArrayList<Double>();
            for(int i = 0; i < source.size()-NUM_CPOINTS; i ++){
            	try {
    				ArrayList<Point2D> sourcePoints = getNormized(source, i, i + NUM_CPOINTS, minY, maxY, 800/(double)source.size());
    				ArrayList<Point2D> p = getRegression(sourcePoints);
    				similarity.add(computeSimilarity(p, search));
    			} catch (ParseException e) {
    				e.printStackTrace();
    			}
            }        	
        }else{
	        Tuple tuple = null;
	        String previousQuery = "";//上一个检索的股票
	        if(iter.hasNext()){
	        	tuple = (Tuple)iter.next();
	        	previousQuery = tuple.getString("stock_symbol");
	        	source.add(tuple);	    
	        	allSource.add(tuple);
	        }
	        similarity = new ArrayList<Double>();
	        while(iter.hasNext()){      
	        	tuple = (Tuple)iter.next();
	        	if(!tuple.getString("stock_symbol").equals(previousQuery)){
	        		source = new ArrayList<Tuple>();
	        		previousQuery = tuple.getString("stock_symbol");
	        	}
	        	
	        	while(iter.hasNext() && tuple.getString("stock_symbol").equals(previousQuery)){
	        		source.add(tuple);	        		
	        		allSource.add(tuple);
	        		previousQuery = tuple.getString("stock_symbol");
	        		tuple = (Tuple)iter.next();
	        	}
	//        	System.out.println("one loop："+source.size());
	            //这段代码用来把实际数值的y轴的最大值最小值求出来
	            double maxY = source.get(0).getDouble(yalabel),
	            		minY= source.get(0).getDouble(yalabel);
	            for(int i = 1; i < source.size()-NUM_CPOINTS; i ++){
	            	double temp = source.get(i).getDouble(yalabel);
	            	if(temp > maxY)
	            		maxY = temp;
	            	if(temp < minY)
	            		minY = temp;
	            }            
	            
	            for(int i = 0; i < source.size()-NUM_CPOINTS; i ++){
	            	try {
	    				ArrayList<Point2D> sourcePoints = getNormized(source, i, i + NUM_CPOINTS, minY, maxY, 800/(double)source.size());
	    				ArrayList<Point2D> p = getRegression(sourcePoints);
	    				similarity.add(computeSimilarity(p, search));
	    			} catch (ParseException e) {
	    				e.printStackTrace();
	    			}
	            }            
	        }
        }
        
        for(int index = 0; index < 10 ; index ++){
	        //以下代码将相似度最高的呈现出来
	        double min = Double.MAX_VALUE;
	        double max = Double.MIN_VALUE;
	        for(double d:similarity){
	        	if(d < min )
	        		min = d;
	        	if(d > max)
	        		max = d;
	        }
//	        System.out.println("min:"+min+"::"+"max:"+max);
	        int indexOfMin = similarity.indexOf(min);
	        if(indexOfMin == -1)
	        	continue;
	        Table tt = new Table();
	        String[] names = new String[table.getColumnCount()];
	        Class[] types = new Class[table.getColumnCount()];
	
	        for(int i = 0; i < table.getColumnCount() ; i ++){        	
	        	names[i] = table.getSchema().getColumnName(i);
	        	types[i] = table.getSchema().getColumnType(i);
	        	tt.addColumn(names[i], types[i]);
	        } 
	        if(queryContentString != null){
		        for(int i = indexOfMin ; i < indexOfMin+NUM_CPOINTS; i ++){
		        	tt.addTuple(source.get(i));
		        }
	        }else{
		        for(int i = indexOfMin ; i < indexOfMin+NUM_CPOINTS; i ++){
		        	tt.addTuple(allSource.get(i));
		        }
	        }
	        tables.add(tt);
	        similarity.remove(indexOfMin);
        }

        return tables;
//        ResultFrame f = new ResultFrame(t);
//        f.setSize(900, 700);
//        f.setVisible(true);        
	}
	
	/**
	 * 线性回归的结果
	 * 20140627：效果不好暂时不用
	 * @param sourcePoints
	 */
	private ArrayList<Point2D> getRegression(ArrayList<Point2D> sourcePoints) {
		return sourcePoints;
		
//		final PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
//		for(Point2D p: sourcePoints){
//			fitter.addObservedPoint(p.getX(), p.getY());
//		}
//		final double[] init = { 1, 1, 1 }; 
//		final double[] best = fitter.fit(new PolynomialFunction.Parametric(), init);
//		final PolynomialFunction fitted = new PolynomialFunction(best);
		
		
//		SimpleRegression regression = new SimpleRegression();
//		ArrayList<Point2D> result = new ArrayList<Point2D>();
//		for(Point2D p: sourcePoints){
//			regression.addData(p.getX(), p.getY());
//		}
//		for(Point2D p: sourcePoints){
//			System.out.println(p.getX()+":"+p.getY()+":"+regression.predict(p.getX()));
//			
//			result.add(new Point2D.Double(p.getX(),regression.predict(p.getX())));
//		}
//		System.out.println("---------");
//		return result;
	}

	/**
	 * 直接计算欧式距离
	 * @param sourcePoints
	 * @param search
	 * @return
	 */
	private double computeSimilarity(ArrayList<Point2D> sourcePoints,
			ArrayList<Point2D> search) {
		ArrayList<Double> r1 = computeArray(sourcePoints);
		ArrayList<Double> r2 = computeArray(search);
		double sum = 0;
		for(int i = 0; i < r1.size(); i ++){
			sum += (r1.get(i)-r2.get(i))*(r1.get(i)-r2.get(i));
		}
//		for(int i = 0; i < search.size(); i ++){
//			sum += 
//			(sourcePoints.get(i).getX()-search.get(i).getX())*(sourcePoints.get(i).getX()-search.get(i).getX())+
//			(sourcePoints.get(i).getY()-search.get(i).getY())*(sourcePoints.get(i).getY()-search.get(i).getY());
//		}
//		System.out.println("===");
		return Math.sqrt(sum);
	}

	private ArrayList<Double> computeArray(ArrayList<Point2D> sourcePoints) {
		ArrayList<Double> r = new ArrayList<Double>();
		//f1
		int index = sourcePoints.size()/NUM_CPOINTS;
		if(index < 2)
			index = 2;
		double d = Math.sqrt((sourcePoints.get(index).getX()-sourcePoints.get(0).getX())*
				(sourcePoints.get(index).getX()-sourcePoints.get(0).getX())+
				((sourcePoints.get(index).getY()+sourcePoints.get(index-1).getY()+sourcePoints.get(index+1).getY())/3-sourcePoints.get(0).getY())*
				((sourcePoints.get(index).getY()+sourcePoints.get(index-1).getY()+sourcePoints.get(index+1).getY())/3-sourcePoints.get(0).getY()));
		double f1 = (sourcePoints.get(index).getX()-sourcePoints.get(0).getX())/d;
		r.add(f1);
		//f2
		double f2 = (sourcePoints.get(index).getY()-sourcePoints.get(0).getY())/d;
		r.add(f2);
		double maxX=getMaxX(sourcePoints);
		double maxY=getMaxY(sourcePoints);
		double minX=getMinX(sourcePoints);
		double minY=getMinY(sourcePoints);
		double f3 = Math.sqrt((maxX-minX)*(maxX-minX) + (maxY-minY)*(maxY-minY));
		r.add(f3);
		double f4 = Math.atan((maxY-minY)/(maxX-minX));
		r.add(f4);
		//f5
		double f5 = Math.sqrt(
				(sourcePoints.get(sourcePoints.size()-1).getX()-sourcePoints.get(0).getX())*
				(sourcePoints.get(sourcePoints.size()-1).getX()-sourcePoints.get(0).getX())+
				(sourcePoints.get(sourcePoints.size()-1).getY()-sourcePoints.get(0).getY())*
				(sourcePoints.get(sourcePoints.size()-1).getY()-sourcePoints.get(0).getY()));
		r.add(f5);
		double f6 = (sourcePoints.get(sourcePoints.size()-1).getX()-sourcePoints.get(0).getX())/f5;
		r.add(f6);
		double f7 = (sourcePoints.get(sourcePoints.size()-1).getY()-sourcePoints.get(0).getY())/f5;
		r.add(f7);
//		double f8 = 0;
//		for(int i = 0; i < sourcePoints.size()-2; i ++){
//			f8 += Math.sqrt((sourcePoints.get(i+1).getX()-sourcePoints.get(i).getX())*
//			(sourcePoints.get(i+1).getX()-sourcePoints.get(i).getX())+
//			(sourcePoints.get(i+1).getY()-sourcePoints.get(i).getY())*
//			(sourcePoints.get(i+1).getY()-sourcePoints.get(i).getY()));
//		}
//		r.add(f8);
//		double f9 = 0,f10=0,f11=0;
//		for(int i = 1 ; i< sourcePoints.size()-2; i ++){
//			double dxk = sourcePoints.get(i+1).getX()-sourcePoints.get(i).getX();
//			double dyk = sourcePoints.get(i+1).getY()-sourcePoints.get(i).getY();
//			double dxkm1 = sourcePoints.get(i).getX()-sourcePoints.get(i-1).getX();
//			double dykm1 = sourcePoints.get(i).getY()-sourcePoints.get(i-1).getY();
//			double th = (dxk*dykm1-dxkm1*dyk)/(dxk*dxkm1-dyk*dykm1);
//			
//			f9+=Math.atan(th);
//			f10+=Math.atan(Math.abs(th));
//			f11 += Math.atan(th)*Math.atan(th);
//		}
//		r.add(f9);
//		r.add(f10);
//		r.add(f11);
//		for(double dd:r){
//			System.out.println("kkk:"+dd);
//		}
		return r;
		
	}

	private double getMaxX(ArrayList<Point2D> points) {
		double max = -1;
		for(Point2D p : points){
			if(p.getX() > max)
				max = p.getX();
		}
		return max;
	}
	private double getMaxY(ArrayList<Point2D> points) {
		double max = -1;
		for(Point2D p : points){
			if(p.getY() > max)
				max = p.getY();
		}
		return max;
	}
	private double getMinX(ArrayList<Point2D> points) {
		double min = Double.MAX_VALUE;
		for(Point2D p : points){
			if(p.getX() < min)
				min = p.getX();
		}
		return min;
	}
	private double getMinY(ArrayList<Point2D> points) {
		double min = Double.MAX_VALUE;
		for(Point2D p : points){
			if(p.getY() < min)
				min = p.getY();
		}
		return min;
	}
	
	/**
	 * 根据
	 * @param source tupleset
	 * @param i 开始
	 * @param j 结束点
	 * @param maxYAll 
	 * @param minYAll 
	 * unitWidth 一个时间单位(一天)的屏幕宽度
	 * @return
	 * @throws java.text.ParseException
	 */
	private ArrayList<Point2D> getNormized(ArrayList<Tuple> source, int i,
			int j, double minYAll, double maxYAll,double unitWidth) throws ParseException {
				
		double maxY = source.get(i).getDouble(yalabel);
		double minY = source.get(i).getDouble(yalabel);
		for(int index = i+1; index < j; index ++){
			if(source.get(index).getDouble(yalabel) > maxY)
				maxY = source.get(index).getDouble(yalabel);
			if(source.get(index).getDouble(yalabel) < minY)
				minY = source.get(index).getDouble(yalabel);
		}
		//计算当前数据对应的屏幕尺寸
		double screenX = unitWidth*NUM_CPOINTS;
		double screenY = 600*(maxY-minY)/(maxYAll-minYAll);
		double ratio = screenY/screenX;
		
		ArrayList<Point2D> result = new ArrayList<Point2D>();
		long maxT = df.parse(source.get(j-1).get("date").toString()).getTime();
		long minT = df.parse(source.get(i).get("date").toString()).getTime();
		for(int index = i; index < j; index ++){
			long t1 = df.parse(source.get(index).get("date").toString()).getTime();
			double ty = source.get(index).getDouble(yalabel);
			long t = t1-minT;
//			System.out.println(source.get(index).get("date")+":t1:"+t1);
			result.add(new Point2D.Double((double)t/(double)(maxT-minT), (double)(ty-minY)*ratio/(maxY-minY)));//600));//(double)(maxY-minY)));
//			System.out.println(new Point2D.Double((double)t/(double)(maxT-minT), (double)(ty-minY)*ratio/(maxYAll-minY)));//(double)(maxY-minY)));
		}
		return result;
	}

	/**
	 * 先使用平均采样，NUM_CPOINTS个采样点
	 * @param s
	 * @return
	 */
//	private ArrayList<Point2D> resample(ArrayList<Point2D> s){
//		ArrayList<Point2D> r = new ArrayList<Point2D>();
//		int interval = s.size()/NUM_CPOINTS;
//		if(interval == 0){
//			for(Point2D p : s)
//				r.add(p);
//		}else{
//			for(int i = 0; i < NUM_CPOINTS; i ++){
//				r.add(s.get(i*interval));
//			}
//		}
//		return r;
//		
//	}
	
	public Stroke getQuery() {
		return query;
	}

	public void setQuery(Stroke query) {
		this.query = query;
	}

	public int getQueryContentType() {
		return queryContentType;
	}

	public void setQueryContentType(int queryContentType, String queryContentString) {
		this.queryContentType = queryContentType;
		this.queryContentString = queryContentString;
	}

	public int getQueryType() {
		return queryType;
	}

	public void setQueryType(int queryType) {
		this.queryType = queryType;
	}
    
    
    
}
