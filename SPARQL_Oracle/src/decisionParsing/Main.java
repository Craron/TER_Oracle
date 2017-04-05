package decisionParsing;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Main {

	public static void main(String[] args){
		List<Integer> lMin = new ArrayList<Integer>();
		
		System.out.println("begin");
		SPARQLParser sp1 = new SPARQLParser();
		List<QueryEstimation> listQuery;
		listQuery = sp1.parseRegLineare("queryAndTimer");
		sp1.printAllARFF(0);
		
		//calculate estimationTime
		
		try (BufferedReader br = new BufferedReader(new FileReader("ARFF/Instance_0_queryAndTimertreshold_0.0.arff")))
		{
			String line;
			int enteteEnd =0;
			try {
				while (enteteEnd<30 &&(line = br.readLine()) != null ) {
					System.out.println(line);
					++enteteEnd;
				}
				for(QueryEstimation q : listQuery)
				{
					q.estimate(br.readLine());
				}
			} catch (IOException e) {
				System.out.println("Error: unknow error while reading the file");
			}
		}
		catch(IOException e){
			System.out.println("Error: can't open the input file");
		}
		
		Collections.sort(listQuery);
		//System.out.println(listQuery.toString());
		for(int i=0;i<100;++i)
		{
			System.out.println(listQuery.get(i).toString());
		}
		
		System.out.println("end!");
		
		for(double threshold = 50;threshold<=50;threshold+=1) // initial 0.5
		{
			SPARQLParser sp = new SPARQLParser();
			
			sp.parse("queryAndTimer", threshold);
			sp.printAllARFF(threshold);
			
			lMin.add(sp.prepare("queryAndTimer", "trainFile/QueryTime"+threshold+".txt",threshold, 436));
			sp = new SPARQLParser();
			sp.parse("trainFile/QueryTime"+threshold+".txt", threshold);
			sp.printTrainARFF(threshold);
			
			DataSource source;
			Instances testSet;
			Instances trainingSet;
			
			try 
			{
//				source = new DataSource("ARFF/Instance_0_queryAndTimer.arff");
//				trainingSet = source.getDataSet();
//				testSet = source.getDataSet();
//				classifierTree cl = new classifierTree(trainingSet, testSet);
//				cl.classified();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("Fin du parsing");
		}
		
		System.out.println("\n\nBorne minimal pour population potentielle : \n"+lMin);
		
	}
}