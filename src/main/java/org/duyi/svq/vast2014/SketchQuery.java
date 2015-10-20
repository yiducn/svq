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
	private static final int NUM_CPOINTS = 15;//360;//5;

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

		ArrayList<ArrayList<Tuple>> match = new ArrayList<ArrayList<Tuple>>();
		ArrayList<ArrayList<Tuple>> allMatchs = new ArrayList<ArrayList<Tuple>>();

		//若选中某股票则执行if,若选择全部股票all则执行else
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
			for(int i = 0 ; i < source.size() - NUM_CPOINTS ; i++){
				for(int j = NUM_CPOINTS ; j < 1.2 * NUM_CPOINTS && i + j < source.size() ; j++){
					try {
						ArrayList<Point2D> sourcePoints = getNormized(source,
								i, i + j , minY, maxY, 800/(double)source.size());
						ArrayList<Point2D> p = getRegression(sourcePoints);
						similarity.add(computeSimilarity(p, search));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ArrayList<Tuple> a = new ArrayList<Tuple>();
					for(int k = i ; k < i + j ; k++){
						a.add(source.get(k));
					}
					//match的大小应该和similarity的大小是一样的，similarity的值对应match的序列
					match.add(a);
				}
			}

//          for(int i = 0; i < source.size()-NUM_CPOINTS; i ++){
//          	try {
//          		//sourcePoints是时间和股价的归一结果,归一化的时间点和股价
//  				ArrayList<Point2D> sourcePoints = getNormized(source,
//  						i, i + NUM_CPOINTS, minY, maxY, 800/(double)source.size());
//  				//数组sourcePoints的X,Y的归一化结果
//  				ArrayList<Point2D> p = getRegression(sourcePoints);
//  				similarity.add(computeSimilarity(p, search));
//  			} catch (ParseException e) {
//  				e.printStackTrace();
//  			}
//          }
		}else{
			Tuple tuple = null;
			String previousQuery = "";//上一个检索的股票
			//第一支股票的名称
			if(iter.hasNext()){
				tuple = (Tuple)iter.next();
				previousQuery = tuple.getString("stock_symbol");
				source.add(tuple);
//	        	allSource.add(tuple);
			}
//	        System.out.println("source的大小：" + source.size());
//	        System.out.println("allsource的大小：" + allSource.size());
			similarity = new ArrayList<Double>();
			while(iter.hasNext()){
				tuple = (Tuple)iter.next();
				//如果本条数据和上一条数据不属同一支股票，则将股票跟踪信号更新
				if(!tuple.getString("stock_symbol").equals(previousQuery)){
					source = new ArrayList<Tuple>();
					previousQuery = tuple.getString("stock_symbol");
				}
				//将本支股票的数据分别存入source和allSource（存储所有股票数据）
				while(iter.hasNext() && tuple.getString("stock_symbol").equals(previousQuery)){
					source.add(tuple);
//	        		allSource.add(tuple);
					previousQuery = tuple.getString("stock_symbol");
					tuple = (Tuple)iter.next();
				}

//	        	System.out.println("one loop："+source.size());
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
				for(int i = 0 ; i < source.size() - NUM_CPOINTS ; i++){
					for(int j = NUM_CPOINTS ; j < 1.2 * NUM_CPOINTS && i + j < source.size() ; j++){
						try {
							ArrayList<Point2D> sourcePoints = getNormized(source,
									i, i + j, minY, maxY, 800/(double)source.size());
							ArrayList<Point2D> p = getRegression(sourcePoints);
							similarity.add(computeSimilarity(p, search));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						ArrayList<Tuple> a = new ArrayList<Tuple>();
						for(int k = i ; k < i + j ; k++){
							a.add(source.get(k));
						}
//	            		match.add(a);
						allMatchs.add(a);
					}
				}
//	            for(int i = 0; i < source.size()-NUM_CPOINTS; i ++){
//	            	try {
//	    				ArrayList<Point2D> sourcePoints = getNormized(source,
//	    						i, i + NUM_CPOINTS, minY, maxY, 800/(double)source.size());
//	    				ArrayList<Point2D> p = getRegression(sourcePoints);
//	    				similarity.add(computeSimilarity(p, search));
//	    			} catch (ParseException e) {
//	    				e.printStackTrace();
//	    			}
//	            }
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
//	        System.out.println("最小相似度：" + similarity.get(indexOfMin));
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
//	        System.out.println("source: "+source.size() + "   " + "allSource: " + allSource.size()
//	        		+ "   " + "indexOfMin: " + indexOfMin);
			if(queryContentString != null){
//		        for(int i = indexOfMin ; i < indexOfMin+NUM_CPOINTS; i ++){
//		        	tt.addTuple(source.get(i));
//		        }
				ArrayList<Tuple> a = new ArrayList<Tuple>();
				a = match.get(indexOfMin);
				for(Tuple d : a ){
					tt.addTuple(d);
				}
			}else{
//		        for(int i = indexOfMin ; i < indexOfMin+NUM_CPOINTS; i ++){
//		        	tt.addTuple(allSource.get(i));
//		        }
				ArrayList<Tuple> a = new ArrayList<Tuple>();
				a = allMatchs.get(indexOfMin);
				for(Tuple d : a ){
					tt.addTuple(d);
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
		double maxX = getMaxX(sourcePoints);
		double maxY = getMaxY(sourcePoints);
		double minX = getMinX(sourcePoints);
		double minY = getMinY(sourcePoints);
		ArrayList<Point2D> result = new ArrayList<Point2D>();
		for(Point2D p : sourcePoints){
			result.add(new Point2D.Double((double)(p.getX()-minX)/(double)(maxX-minX),
					(double)(p.getY()-minY)/(maxY-minY)));
		}

		return result;

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
	 * 计算曲线距离
	 * @param sourcePoints
	 * @param search
	 * @return
	 */
	private double computeSimilarity(ArrayList<Point2D> sourcePoints,
									 ArrayList<Point2D> search) {
		ArrayList<Double> r1 = computeArray2(sourcePoints);//待搜索序列
		ArrayList<Double> r2 = computeArray2(search);//目标序列
//		for(double d: r1){
//			System.out.print(d+" ");
//		}
//		System.out.println("end r1");
//		for(double d: r2){
//			System.out.print(d+" ");
//		}
//		System.out.println("end r2");

//		System.out.print("r1的大小：" + r1.size() + "       " + "r2的大小：" + r2.size() + "      ");

		double D1,D2,D3;
		double sum = 0;
		double[][] distance;//DTW距离矩阵
		double[][] disE;//累计距离矩阵

		distance = new double[r1.size()][r2.size()];
		disE = new double[r1.size()][r2.size()];

		for(int i = 0; i < r1.size(); i ++){
			for(int j = 0;j < r2.size();j++) {
				disE[i][j] = Double.MAX_VALUE;
				double a = r1.get(i) - r2.get(j);
				distance[i][j] = a*a; //Math.abs(a);
			}
		}
		disE[0][0] = distance[0][0];
		for(int i = 1; i < r1.size(); i ++){
			for(int j = 0;j < r2.size();j++) {
				if(i > 0 ){
					D1 = disE[i-1][j];
				}else{
					D1 = Double.MAX_VALUE;
				}
				if (j>0) {
					D2 = disE[i][j-1];
				}else {
					D2 = Double.MAX_VALUE;
				}
				if(i > 0 && j > 0){
					D3 = disE[i-1][j-1];
				}else {
					D3 = disE[0][0];
				}
				disE[i][j] = min(D1,D2,D3) + distance[i][j];
			}
		}
		sum = disE[r1.size()-1][r2.size()-1];
//		System.out.println("相似度"+sum);
		return sum;
	}
	private double min(double x,double y,double z) {
		double a = (x<=y)? x:y;
		return (a<=z)? a:z;
	}

	private ArrayList<Double> computeArray2(ArrayList<Point2D> sourcePoints) {
		ArrayList<Double> result = new ArrayList<Double>();
		for(Point2D p : sourcePoints){
			result.add(p.getY());
		}
		return result;
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
		for(int index = i + 1; index < j; index ++){
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