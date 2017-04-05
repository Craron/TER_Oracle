package decisionParsing;

import java.util.ArrayList;
import java.util.Arrays;

public class QueryEstimation implements Comparable<QueryEstimation>{
	String query;
	double realTimeExecution;
	double estimateTimeExecution;
	
	public QueryEstimation(String q, double t)
	{
		query = q;
		realTimeExecution = t;
		estimateTimeExecution = 0;
	}

	
	
	
	// getter & setter
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public double getRealTimeExecution() {
		return realTimeExecution;
	}

	public void setRealTimeExecution(double realTimeExecution) {
		this.realTimeExecution = realTimeExecution;
	}

	public double getEstimateTimeExecution() {
		return estimateTimeExecution;
	}

	public void setEstimateTimeExecution(double estimateTimeExecution) {
		this.estimateTimeExecution = estimateTimeExecution;
	}


	@Override
	public int compareTo(QueryEstimation arg0) {
		return (int) (arg0.getEstimateTimeExecution() - this.estimateTimeExecution);
	}




	public void estimate(String readLine) {
		
		double timeEstimate =0;
		int lastVirguleWasAt = -1;
		ArrayList<String> l = new ArrayList<String>();
		String[] tab = readLine.split(",");
		for(int i = 0; i<readLine.length();++i)
		{
			if(readLine.charAt(i) == ',')
				{
					l.add(readLine.substring(lastVirguleWasAt+1, i));
					lastVirguleWasAt =i;
				}
			
		}
		l = new ArrayList<String>(Arrays.asList(tab));
		
		timeEstimate = 0.0592 * Double.parseDouble(l.get(3)) +
					   130.1823 * Double.parseDouble(l.get(5)) +
					   79.8366 * Double.parseDouble(l.get(6)) +
					   44.9704 * Double.parseDouble(l.get(8)) +
					   100.3302 * Double.parseDouble(l.get(9)) +
					   -177.0657 * Double.parseDouble(l.get(16)) +
					   -159.0821 * Double.parseDouble(l.get(19)) +
					   6.8378 * Double.parseDouble(l.get(22)) +
					   32.2326 * Double.parseDouble(l.get(23)) +
					   -16.917  * Double.parseDouble(l.get(24)) +
					   -379.0345;
				
		setEstimateTimeExecution(timeEstimate);
	}
	
	public String toString()
	{
		return "Estimate at : " +estimateTimeExecution+ "| real Tiame : " + realTimeExecution;
	}
}
