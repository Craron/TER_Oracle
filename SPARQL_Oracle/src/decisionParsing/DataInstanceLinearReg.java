package decisionParsing;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpWalker;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class used as a structure to hold a WEKA library instance
 * More precisely, an instance of this class contains all the entries of a log file
 * as a Weka "Instances" vector 
 * @see <a href="https://weka.wikispaces.com/Use+WEKA+in+your+Java+code>WEKA Java lib</a>
 */
public class DataInstanceLinearReg extends DataInstance{
	Instances data;
	int nbEntries;
	String inputName;
	String fedType;
	FastVector atts;
	FastVector attVals;
	
	/**
	 * DataInstance constructor
	 * Fill the FastVectors with out features
	 * @param inptName	 the name of the log file from which we construct the instance
	 */
	public DataInstanceLinearReg(String inptName){
		nbEntries = 0;
		inputName = nameWithoutPath(inptName);
		atts = new FastVector();
		atts.addElement(new Attribute("isASK"));
		atts.addElement(new Attribute("isSELECT"));
		atts.addElement(new Attribute("isCONSTRUCT"));
		atts.addElement(new Attribute("valLIMIT"));
		atts.addElement(new Attribute("valOFFSET"));
		atts.addElement(new Attribute("hasVarPref"));
		atts.addElement(new Attribute("usePrefix"));
		atts.addElement(new Attribute("nbGROUPBY"));
		atts.addElement(new Attribute("nbUNION"));
		atts.addElement(new Attribute("nbFILTER"));
		atts.addElement(new Attribute("nbORDERBY"));
		atts.addElement(new Attribute("nbBGP"));
		atts.addElement(new Attribute("nbTRIPLE"));
		atts.addElement(new Attribute("nbSERVICE"));
		atts.addElement(new Attribute("levenshtein"));
		atts.addElement(new Attribute("nbOperator"));
		atts.addElement(new Attribute("selectivity"));
		
		atts.addElement(new Attribute("nbVariable"));
		atts.addElement(new Attribute("nbConcrete"));
		atts.addElement(new Attribute("nbPredicatGeneric"));
		atts.addElement(new Attribute("nbVariableLiee"));
		
		atts.addElement(new Attribute("ratioVariable"));
		atts.addElement(new Attribute("ratioConcrete"));
		atts.addElement(new Attribute("ratioGeneric"));
		atts.addElement(new Attribute("ratioVLiee"));
		
		
		

		atts.addElement(new Attribute("Class"));
		data = new Instances("MyRelation", atts, 0);
		data.setClassIndex(data.numAttributes() - 1);
	}

	/**
	 * Add a new entry to the features vector
	 * @param query the query where we want to extract features
	 * @param isFederated the type of query (single/fedx/anapsid)
	 * @see SPARQLVisitor
	 * @see <a href="https://jena.apache.org> Jena Java library doc</a> for more informations
	 */
	public void addData(String query, double timeExec){
		Query q = QueryFactory.create(query);
		double[] vals = new double[data.numAttributes()];
		//passing to the visitor all features already extracted from the query
		SPARQLVisitor v = new SPARQLVisitor(q.isAskType(),
											q.isSelectType(), 
  				 							q.isConstructType(),
  				 							q.hasLimit()?q.getLimit():-1,
  				 							q.hasOffset()?q.getOffset():-1,
  				 						   (q.getPrefixMapping().getNsPrefixMap().size()>0)
  				 						   );

		Op op = Algebra.compile(q);
		OpWalker.walk(op,v); // call the walker on the visitor
		// copy every visitors features values on the double vector
		vals[0] = v.isASK()?1:0;
		vals[1] = v.isSELECT()?1:0;
		vals[2] = v.isCONSTRUCT()?1:0;
		vals[3] = v.getValLIMIT();
		vals[4] = v.getValOFFSET();
		vals[5] = v.hasVarPref()?1:0;
		vals[6] = v.usePrefix()?1:0;
		vals[7] = v.getNbGROUPBY();
		vals[8] = v.getNbUNION();
		vals[9] = v.getNbFILTER();
		vals[10] = v.getNbORDERBY();
		vals[11] = v.getNbBGP();
		vals[12] = v.getNbTRIPLE();
		vals[13] = v.getNbSERVICE();
		vals[14] = v.getLevenshtein();
		vals[15] = v.getNbOperator();
		vals[16] = v.getSelectivity();
		
		vals[17] = v.getNbVariable();
		vals[18] = v.getNbConcrete();
		vals[19] = v.getNbPredicatGenerique();
		vals[20] = v.getNbVariablePortee();
				
		vals[21] = (v.getNbVariable()*100/(v.getNbTRIPLE()*3));
		vals[22] = (v.getNbConcrete()*100/(v.getNbTRIPLE()*3));
		vals[23] = (v.getNbPredicatGenerique()*100/(v.getNbTRIPLE()*3));
		vals[24] = (v.getNbVariablePortee()*100/(v.getNbTRIPLE()*3));
		
		vals[25] = timeExec;
		data.add(new Instance(1.0, vals)); // add the new entry to the WEKA Instances vector
		
		
		
		++nbEntries;
	}
	
	/**
	 * Same that addData(String, String) but this time adding directly an existing Instance
	 * This adding method is used to create mixed Instance from existing federated or single ones
	 * @param inst The existing Instance to add
	 * @param isFederated the Intance type
	 */
	public void addData(Instance inst,String isFederated){
		if(fedType == null)
			fedType = isFederated;
		else{
			fedType = fedType.equals(isFederated)?fedType:"mixed";
		}
		data.add(inst);
		++nbEntries;
	}

	
	/**
	 * Gather the name of a file from his path (removing everything before the last /)
	 * @param inptName the input path
	 * @return the output name
	 */
	private String nameWithoutPath(String inptName) {
		String[] spliter;
		spliter = inptName.split("/");
		spliter = spliter[spliter.length-1].split("\\.");
		return spliter[0];
	}
	
	public Instance getInstance(int ind){
		return data.instance(ind);
	}
	
	public Instances getData(){
		return data;
	}
	
	public String getFedType(){
		return fedType;
	}
	
	public String getInputName(){
		return inputName;
	}
	
	public int getNbEntries(){
		return nbEntries;
	}
}
