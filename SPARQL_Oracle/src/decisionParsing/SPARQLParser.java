package decisionParsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import weka.core.converters.ArffSaver;

/**
 * The class the user will interact with.
 * <p>SPARQLParser contains every methods to parse a log file and generate the ARFF associated file</p>
 * <p>Only logs from <a href="lsq.aksw.org/sparql">Virtuoso SPARQL Query Editor</a> are supported</p>
 * @see DataInstance
 */
public class SPARQLParser {
	private ArrayList<DataInstance> datas; // All parsed files as DataInstance
	private String destinationFolder = "ARFF/"; // ARFF generated files destination folder
	// TODO allow user to choose the destination folder
	private int nbAnapsidEntries; // the total number of ANAPSID parsed queries
	private int nbFedxEntries; // the total number of FedX parsed queries
	private int nbSingleEntries; // the total number of ANAPSID parsed queries
	
	private int nbMineEntries; // the total number of MINE parsed queries <- oracle project 2017
	
	/**
	 * SPARLParser constructor
	 */
	public SPARQLParser(){
		datas = new ArrayList<DataInstance>();
		nbAnapsidEntries = 0;
		nbFedxEntries = 0;
		nbSingleEntries = 0;
		setNbMineEntries(0);
	}
	
	/**
	 * The only parsing method visible by user.
	 * <p> This method calls the right parsing strategy according the the logType entered by user
	 * @param inputPath The path of the file to parse (has to be a query-engine log file)
	 * @param logType The type of the input log file (has to be single, fedx or anapsid)
	 */
	public void parse(String inputPath, double threshold){
		//TODO use Strategy Design Pattern instead of 2 parsing methods
		System.out.println("Parsing : " + inputPath);
		try (BufferedReader br = new BufferedReader(new FileReader(inputPath)))
		{
			parseOracle(br,inputPath,threshold);
		}
		catch(IOException e){
			System.out.println("Error: can't open the input file");
		}
	}
	/**
	 * Method parsing ..
	 * 
	 * @warning file must respect the following synthax: {y.xxx} theQuery
	 * 			where y is the execution time of the query (in second) and x (3 compulsory)
	 * @param br buffer of on filename
	 * @param fileName name of the file which is read
	 */
	private void parseOracle(BufferedReader br, String fileName, double threshold)
	{
		String line;
		DataInstance di;
		double timeExecution;
		try {
			di = new DataInstance(fileName); // see DataInstance class
			while ((line = br.readLine()) != null) {
				timeExecution = getTimerExecution(line);
				line = line.substring(getBeginQuery(line), line.length());
				di.addData(line, (threshold>timeExecution)); // tps exec < threshold
			}
			datas.add(di);
			nbSingleEntries += di.getNbEntries();
		} catch (IOException e) {
			System.out.println("Error: unknow error while reading the file");
		}
	}
	
	
	public List<QueryEstimation> parseRegLineare(String fileName)
	{
		List<QueryEstimation> list = new ArrayList<QueryEstimation>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName)))
		{
			String line;
			DataInstanceLinearReg di;
			double timeExecution;
			try {
				di = new DataInstanceLinearReg(fileName); // see DataInstance class
				while ((line = br.readLine()) != null) {
					timeExecution = getTimerExecution(line);
					line = line.substring(getBeginQuery(line), line.length());
					list.add(new QueryEstimation(line,timeExecution));
					di.addData(line, timeExecution); 
				}
				datas.add(di);
				nbSingleEntries += di.getNbEntries();
			} catch (IOException e) {
				System.out.println("Error: unknow error while reading the file");
			}
		}
		catch(IOException e){
			System.out.println("Error: can't open the input file");
		}
		return list;
	}
	

	
	/**
	 * Print the ARFF file from the datas vector on the chose index to the provided file path
	 * @param ouputPath The output file path
	 * @param index The index of the DataInstance to print
	 * @see <a href="https://weka.wikispaces.com/Use+WEKA+in+your+Java+code>WEKA Java lib</a>
	 */
	public void printARFF(String ouputPath, int index){
		ArffSaver saver = new ArffSaver();
		saver.setInstances(datas.get(index).getData());
		try {
			saver.setFile(new File(ouputPath)); // ARFF/test.arff
			saver.writeBatch();
		} catch (IOException e) {
			System.out.println("Can't read the outputFile");
		} 
	}
	
	/**
	 * Same than printARFF but for every datas vector instances (one ARFF file per DataInstance)
	 * <p>The name is automatically built according to the current instance type</p>
	 */
	public void printAllARFF(double treshold){
		ArffSaver saver = new ArffSaver();
		StringBuilder outputName;
		int instanceIndex = 0; // used to ensure that files names are unique
		for(DataInstance di : datas){
			// output file name building
			outputName = new StringBuilder();
			outputName.append(destinationFolder);
			outputName.append("Instance_");
			outputName.append(instanceIndex);
			outputName.append("_");
			outputName.append(di.getInputName());
			outputName.append("treshold_"+treshold);
			outputName.append(".arff");
			
			saver.setInstances(di.getData());
			try {
				saver.setFile(new File(outputName.toString())); //ARFF/test.arff
				saver.writeBatch();
			} catch (IOException e) {
				System.out.println("Can't read the outputFile");
			}
			++instanceIndex;
		}
	}
	
	public void printTrainARFF(double threshold)
	{
		ArffSaver saver = new ArffSaver();
		StringBuilder outputName;
		for(DataInstance di : datas){
			System.out.println("test");
			// output file name building
			outputName = new StringBuilder();
			outputName.append("trainFile/");
			outputName.append("train");
			outputName.append("_");
			outputName.append("treshold_"+threshold);
			outputName.append(".arff");
			
			saver.setInstances(di.getData());
			try {
				saver.setFile(new File(outputName.toString())); //ARFF/test.arff
				saver.writeBatch();
			} catch (IOException e) {
				System.out.println("Can't read the outputFile");
			}
		}
	}
	
	/**
	 * Same that print all ARFF but only for DataInstance containing both single and federated queries
	 */
	public void printAllMixedARFF(){
		ArffSaver saver = new ArffSaver();
		StringBuilder outputName;
		int instanceIndex = 0;
		for(DataInstance di : datas){
				outputName = new StringBuilder();
				outputName.append(destinationFolder);
				outputName.append("Instance_");
				outputName.append(instanceIndex);
				outputName.append("_");
				outputName.append(di.getInputName());
				outputName.append("_");
				outputName.append(di.getFedType());
				outputName.append(".arff");
				
				saver.setInstances(di.getData());
				try {
					saver.setFile(new File(outputName.toString())); //ARFF/test.arff
					saver.writeBatch();
				} catch (IOException e) {
					System.out.println("Can't read the outputFile");
				}
				++instanceIndex;
		}
	}
	
	
	/**
	 * Generate a DataInstance containing the number of each data type according to parameters
	 * The generated instance is pushed at the end of the 'datas' vector
	 * Selected entries are the X first in the 'datas' vector (starting at indexe 0)
	 * ARFF generated by this methods are the ones used in WEKA for experimentations
	 * @param nbFedX The number of FedX entries
	 * @param nbAnapsid The number of anapsid entries
	 * @param nbSingle The number of Single entries
	 */
	public void genMixed(int nbSingle){

		int currIndexDatas = 0;
		int currIndexData = 0;
		int remainingDatas;
		DataInstance currentDi;
		DataInstance mixedDi = new DataInstance(nbSingle + "OfSingle");
		
		
		// takes the nbSingle first Single queries of 'datas' vector and put them in mixedDi
		currIndexDatas = 0;
		while(nbSingle > 0){
				currentDi = datas.get(currIndexDatas);
				remainingDatas = currentDi.getNbEntries();
				currIndexData = 0;
				while(remainingDatas > 0 && nbSingle > 0){
					mixedDi.addData(currentDi.getInstance(currIndexData),"mixed");
					currIndexData += 1;
					remainingDatas -= 1;
					nbSingle -= 1;
				}
			currIndexDatas += 1;
		}
		datas.add(mixedDi); // add mixedDi to datas
	}
	
	public DataInstance getDataInstance(int index){
		return datas.get(index);
	}
	
	public int getTotalEntries(){
		return nbAnapsidEntries + nbFedxEntries + nbSingleEntries;
	}
	
	public int getNbFedex(){
		return nbFedxEntries;
	}
	
	public int getNbAnapsid(){
		return nbAnapsidEntries;
	}
	
	public int getNbSingle(){
		return nbSingleEntries;
	}

	public int getNbMineEntries() {
		return nbMineEntries;
	}

	public void setNbMineEntries(int nbMineEntries) {
		this.nbMineEntries = nbMineEntries;
	}
	
	
	/**
	* retourne le temps d'execution d'une query sous le format Double.
	* @param s la string contenant le temps d'execution et la query
	* @warning la string doit suivre le format suivant : " " + time + " " + query
	* @param queryStartAt iterateur du debut de la query
	* @return le temps d'execution d ela query
	*/
	public static double getTimerExecution(String s)
	{
		String timer = "";
		boolean timerFound =true;
		for (int i = 1; timerFound ;++i ) {
			if( s.charAt(i) == ' ')
			{
				timerFound =false;
			}
			else
			{
				timer += s.charAt(i);
			}
		}
		return Double.parseDouble(timer);
	}

	/**
	* retourne l'iterateur de debut de la query
	* @param s la string contenant le temps d'execution et la query
	* @warning la string doit suivre le format suivant : " " + time + " " + query
	* @return l'iterateur(int) du debut de la query
	*/
	public static int getBeginQuery(String s)
	{
		int queryStartAt = 0;
		boolean timerFound =true;
		for (int i = 1; timerFound ;++i ) {
			if( s.charAt(i) == ' ') 
			{
				++queryStartAt;
				timerFound =false;
			}
			else
			{
				++queryStartAt;
			}
		}
		return queryStartAt;
	}
	
	
	
	
	public int prepare(String inputPath, String outputPath, double threshold, int limit) {
		// TODO Auto-generated method stub
		double timeExecution;
		int upperQuery = 0; // upper than threshold
		int underQuery = 0;
		int min = 0;
		String line;
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(inputPath));
			while ((line = br.readLine()) != null) {
				timeExecution = getTimerExecution(line);
				if (threshold>timeExecution)
					++underQuery;
				else
					++upperQuery;
			}
			
		System.out.println("under : " + underQuery);
		System.out.println("upper : " + upperQuery + "\n");
		
		
		br = new BufferedReader(new FileReader(inputPath));
	    PrintWriter writer = new PrintWriter(outputPath, "UTF-8");
	    min = min(underQuery,upperQuery);
	    int underQueriesWrite = min(limit,min);
	    int upperQueriesWrite = min(limit,min);
	    
	    System.out.println(underQueriesWrite +"__&__"+ upperQueriesWrite);
	    
	    
	    while((upperQueriesWrite + underQueriesWrite) != 0)
	    {
	    	
	    	line = br.readLine();
	    	if(getTimerExecution(line)>threshold && upperQueriesWrite>0){
	    		writer.println(line);
	    		--upperQueriesWrite;
	    	}
	    	if(getTimerExecution(line)<=threshold && underQueriesWrite>0){
	    		writer.println(line);
	    		--underQueriesWrite;
	    	}
	    }
	    
	    writer.close();

			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
		return min;
	}
	
	
	public int min(int a, int b)
	{
		if(a<b) return a;
		else return b;
	}
}


