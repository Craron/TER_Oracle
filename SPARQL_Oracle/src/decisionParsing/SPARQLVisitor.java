package decisionParsing;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLabel;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpList;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProcedure;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
/**
 * An overrode OpVisitor to parse SPARQL algebra and extract features
 * This visitor come from JENA library. Please refer to JAVA visitor mechanisms for more details.
 * @see <a href="https://weka.wikispaces.com/Use+WEKA+in+your+Java+code>WEKA Java lib</a>
 * @see <a href="https://sourcemaking.com/design_patterns/visitor/java/1>Visitor Design Pattern</a> for a Java visitor example
 */
public class SPARQLVisitor implements OpVisitor {
	
	private boolean isASK; 
	private boolean isSELECT; 
	private boolean isCONSTRUCT;
	private double valLIMIT;
	private double valOFFSET;
	private boolean hasVarPref;
	private boolean usePrefix;
	private int nbGROUPBY;
	private int nbAND;
	private int nbUNION;
	private int nbFILTER;
	private int nbOPTIONAL;
	private int nbORDERBY;
	private int nbREGEX;
	private int nbBGP;
	private int nbTRIPLE;
	private int nbSERVICE;
	private int levenshtein;
	private int nbOperator;
	private String isFederated;
	private int selectivity; // if unSelectivity > 0 then selectivity = -1
	private int unSelectivity;
	
	private int nbVariable;
	private int nbVariableLiee;
	private int nbConcrete;
	private int nbPredicatGenerique;
	
	public SPARQLVisitor(boolean ask, boolean sel, boolean cons, double limit, double offset, boolean pref){
		isASK = ask;
		isSELECT = sel;
		isCONSTRUCT = cons;
		valLIMIT = limit;
		valOFFSET = offset;
		hasVarPref = true;
		usePrefix = pref;
		nbGROUPBY = 0;
		nbAND = 0;
		nbUNION = 0;
		nbFILTER = 0;
		nbOPTIONAL = 0;
		nbORDERBY = 0;
		nbREGEX = 0;
		nbBGP = 0;
		nbTRIPLE = 0;
		nbSERVICE = 0;
		levenshtein = 0;
		nbOperator = 0;
		selectivity = 0;
		unSelectivity = 0;
		
		nbVariable = 0;
		nbVariableLiee = 0;
		nbConcrete = 0;
		nbPredicatGenerique = 0;
	}

	
	/*
	 * to delete
	 */
	public void visit(OpBGP arg0) {		
		nbBGP += 1;
		nbTRIPLE += arg0.getPattern().size();
		List<Triple> tps = arg0.getPattern().getList();

		List<String> listVariable = new ArrayList<String>();
		List<TripleHeuristic> listNode = new ArrayList<>();
		for(Triple t : tps) // see (4.) https://openproceedings.org/2012/conf/edbt/TsialiamanisSFCB12.pdf
		{
			TripleHeuristic nh = new TripleHeuristic(t);
			if(t.getPredicate().toString().equals("http://www.w3.org/2000/01/rdf-schema#label") ||
					t.getPredicate().toString().equals("http://www.w3.org/2000/01/rdf-schema#type")) // TODO read it in a file
			{
				++nbPredicatGenerique;
			}
			listNode.add(nh);
		}
		
		Collections.sort(listNode);
		
		for(TripleHeuristic th : listNode)
		{
			if(!th.isO())
			{
				String objet = th.getN().getObject().getName();
				if(listVariable.contains(objet)) 
				{
					th.setO(true);
					++nbVariableLiee;
				}
				else 
				{
					listVariable.add(objet);
					++nbVariable;
				}
			}
			else ++nbConcrete ; 
			if(!th.isP())
			{
				String predicat = "";
				try{
				predicat = th.getN().getPredicate().getName();
				}
				catch(Exception e)
				{ // URI are catch here
					th.setP(true);
					++nbConcrete;
				}
				
				if(listVariable.contains(predicat)) 
				{
					th.setP(true);
					++nbVariableLiee;
				}
				else 
				{
					listVariable.add(predicat);
					++nbVariable;
				}
			}
			else ++nbConcrete ; 
			if(!th.isS())
			{
				
				String sujet = th.getN().getSubject().getName();
				if(listVariable.contains(sujet)) 
				{
					th.setS(true);
					++nbVariableLiee;
				}
				else 
				{
					listVariable.add(sujet);
					++nbVariable;
				}
			}
			else ++nbConcrete ; 
			
			
			
			
			
		}
		Collections.sort(listNode);
		
		
		// checking for selectivity
		
		/**/
		TripleHeuristic t = listNode.get(0);
		if((t.isO() && t.isP()) || (t.isO() && t.isS()) || ( t.isP()&&t.isS()) ) 
			{
				if(selectivity>=0) 
					{
						++selectivity;
					}
			}
		else 
			{
				selectivity = 0;
			}
		
		// checking for levenshtein
		int nbTps = tps.size();
		if(nbTps > 1){
			for(int i = 0; i < nbTps ; ++ i){
				for(int j = i+1; j < nbTps ; ++ j){
					levenshtein += 0;
				}
			}
		}
		++nbOperator;
	}

	
	public void visit(OpQuadPattern arg0) {
		System.out.println(arg0.toString());
		++nbOperator;
	}


	public void visit(OpQuadBlock arg0) {
		System.out.println(arg0.toString());
		++nbOperator;
	}

	public void visit(OpTriple arg0) {
		System.out.println(arg0.toString());
		++nbOperator;
	}


	public void visit(OpQuad arg0) {
		System.out.println(arg0.toString());
		++nbOperator;
	}


	public void visit(OpPath arg0) {
		++nbOperator;
	}


	public void visit(OpTable arg0) {
		++nbOperator;
	}

	
	public void visit(OpNull arg0) {
		++nbOperator;
	}

	
	public void visit(OpProcedure arg0) {
		++nbOperator;
	}

	
	public void visit(OpPropFunc arg0) {
		++nbOperator;
	}

	
	public void visit(OpFilter arg0) {
		nbFILTER += 1;
		++nbOperator;
	}

	
	public void visit(OpGraph arg0) {
		++nbOperator;
	}

	
	public void visit(OpService arg0) {
		nbSERVICE += 1;
		++nbOperator;
	}

	
	public void visit(OpDatasetNames arg0) {
		++nbOperator;
	}

	
	public void visit(OpLabel arg0) {
		++nbOperator;
	}

	
	public void visit(OpAssign arg0) {
		++nbOperator;
	}

	
	public void visit(OpExtend arg0) {
		++nbOperator;
	}

	
	public void visit(OpJoin arg0) {
		++nbOperator;
	}

	
	public void visit(OpLeftJoin arg0) {
		++nbOperator;
	}

	
	public void visit(OpUnion arg0) {
		nbUNION += 1;
		++nbOperator;
	}

	
	public void visit(OpDiff arg0) {
		++nbOperator;
	}

	
	public void visit(OpMinus arg0) {
		++nbOperator;
	}

	
	public void visit(OpConditional arg0) {
		++nbOperator;
	}

	
	public void visit(OpSequence arg0) {
		++nbOperator;
	}

	
	public void visit(OpDisjunction arg0) {
		++nbOperator;
	}

	
	public void visit(OpExt arg0) {
		++nbOperator;
	}

	
	public void visit(OpList arg0) {
		++nbOperator;
	}

	
	public void visit(OpOrder arg0) {
		nbORDERBY += 1;
		++nbOperator;
	}

	
	public void visit(OpProject arg0) {
		ArrayList<Var> v = (ArrayList<Var>)arg0.getVars();
		if(v.size() > 1){
			if(v.get(0).toString().length() > 2 && v.get(1).toString().length() > 2){
				String pref = "";
				if(v.get(0).toString().substring(0,3).equals(v.get(1).toString().substring(0,3))){
					pref = v.get(0).toString().substring(0,3);
					for (Var str : v){
						if(str.toString().length()>2){
							if(!str.toString().substring(0, 3).equals(pref)){
								hasVarPref = false;
							}
						}else hasVarPref = false;
					}
				}else hasVarPref = false;
			}else hasVarPref = false;
		}else hasVarPref = false;
		++nbOperator;
	}

	
	public void visit(OpReduced arg0) {
		++nbOperator;
	}

	
	public void visit(OpDistinct arg0) {
		++nbOperator;
	}

	
	public void visit(OpSlice arg0) {
		++nbOperator;
	}

	
	public void visit(OpGroup arg0) {
		nbGROUPBY += 1;
		++nbOperator;
	}

	
	public void visit(OpTopN arg0) {
		++nbOperator;
	}

	public boolean isASK() {
		return isASK;
	}

	public boolean isSELECT() {
		return isSELECT;
	}

	public boolean isCONSTRUCT() {
		return isCONSTRUCT;
	}
	
	public boolean hasVarPref() {
		return hasVarPref;
	}
	
	public boolean usePrefix() {
		return usePrefix;
	}

	public int getNbAND() {
		return nbAND;
	}

	public int getNbUNION() {
		return nbUNION;
	}

	public int getNbFILTER() {
		return nbFILTER;
	}

	public int getNbOPTIONAL() {
		return nbOPTIONAL;
	}

	public int getNbORDERBY() {
		return nbORDERBY;
	}
	
	public double getValLIMIT() {
		return valLIMIT;
	}

	public double getValOFFSET() {
		return valOFFSET;
	}

	public int getNbREGEX() {
		return nbREGEX;
	}

	public int getNbBGP() {
		return nbBGP;
	}

	public int getNbTRIPLE() {
		return nbTRIPLE;
	}
	
	public int getNbSERVICE() {
		return nbSERVICE;
	}
	
	public int getNbGROUPBY() {
		return nbGROUPBY;
	}
	
	public String isFederated() {
		return isFederated;
	}
	
	public int getLevenshtein() {
		return levenshtein;
	}
	
	public int getNbOperator() {
		return nbOperator;
	}
	
	@Override
	public String toString() {
		return "SPARQLVisitor [isASK=" + isASK + ", isSELECT=" + isSELECT + ", isCONSTRUCT=" + isCONSTRUCT
				+ ", valLIMIT=" + valLIMIT + ", valOFFSET=" + valOFFSET + ", hasVarPref=" + hasVarPref + ", usePrefix="
				+ usePrefix + ", nbGROUPBY=" + nbGROUPBY + ", nbAND=" + nbAND + ", nbUNION=" + nbUNION + ", nbFILTER="
				+ nbFILTER + ", nbOPTIONAL=" + nbOPTIONAL + ", nbORDERBY=" + nbORDERBY + ", nbREGEX=" + nbREGEX
				+ ", nbBGP=" + nbBGP + ", nbTRIPLE=" + nbTRIPLE + ", nbSERVICE=" + nbSERVICE + ", levenshtein="
				+ levenshtein + ", nbOperator=" + nbOperator + ", isFederated=" + isFederated + "]";
	}


	public int getSelectivity() {
		return selectivity;
	}


	public void setSelectivity(int unselectivity) {
		this.unSelectivity = unselectivity;
	}
	
	public int getUnSelectivity() {
		return unSelectivity;
	}


	public void setUnSelectivity(int unselectivity) {
		this.unSelectivity = unselectivity;
	}


	public int getNbVariable() {
		return nbVariable;
	}


	public void setNbVariable(int nbVariable) {
		this.nbVariable = nbVariable;
	}


	public int getNbVariablePortee() {
		return nbVariableLiee;
	}


	public void setNbVariablePortee(int nbVariablePortee) {
		this.nbVariableLiee = nbVariablePortee;
	}


	public int getNbConcrete() {
		return nbConcrete;
	}


	public void setNbConcrete(int nbConcrete) {
		this.nbConcrete = nbConcrete;
	}


	public int getNbPredicatGenerique() {
		return nbPredicatGenerique;
	}


	public void setNbPredicatGenerique(int nbPredicatGenerique) {
		this.nbPredicatGenerique = nbPredicatGenerique;
	}
	
	
}
