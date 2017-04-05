package decisionParsing;

import org.apache.jena.graph.Triple;

public class TripleHeuristic implements Comparable<TripleHeuristic>{
	


	private Triple n;
	private boolean s;
	private boolean p;
	private boolean o;
	
	// constructor
	public TripleHeuristic(Triple n)
	{
		this.n = n;
		this.s = n.getSubject().isConcrete() || n.getSubject().isURI() ;
		this.p = n.getPredicate().isConcrete() || n.getPredicate().isURI();
		this.o = n.getObject().isConcrete() || n.getObject().isURI();
	}
	
	
	// getters & setters
	
	public Triple getN() {
		return n;
	}

	public void setN(Triple n) {
		this.n = n;
	}

	public boolean isS() {
		return s;
	}

	public void setS(boolean s) {
		this.s = s;
	}

	public boolean isP() {
		return p;
	}

	public void setP(boolean p) {
		this.p = p;
	}

	public boolean isO() {
		return o;
	}

	public void setO(boolean o) {
		this.o = o;
	}


	public int compareTo(TripleHeuristic arg0) 
	{
		int comparaison = 0;
		int sumThis = (this.s?1:0) + (this.o?1:0) + (this.p?1:0);
		int sumArg  = (arg0.s?1:0) + (arg0.o?1:0) + (arg0.p?1:0);
				
		if(sumThis<sumArg) comparaison++;
		else
		{
			if(sumThis<sumArg) comparaison--;
			else
				// see https://openproceedings.org/2012/conf/edbt/TsialiamanisSFCB12.pdf
				// point 4.
				
			{	// checking which triple have the lower selectivity pattern
				if(!(this.s = arg0.s)) return this.s?comparaison++:comparaison--;
				if(!(this.p = arg0.p)) return this.p?comparaison++:comparaison--;
				if(!(this.o = arg0.o)) return this.o?comparaison++:comparaison--;
				comparaison = 0; //default return, they have same selectivity pattern
			}
		}
		return comparaison;
	
	}
	
	@Override
	public String toString() {
		return "TripleHeuristic [s=" + (s?1:0) + ", p=" + (p?1:0) + ", o=" + (o?1:0)
				+ "]";
	}

	
}
