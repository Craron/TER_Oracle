package decisionParsing;

import java.awt.BorderLayout;

import weka.core.Instances;
import weka.core.Option;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

public class classifierTree {

	private Instances train;
	private Instances test;
	
	public classifierTree(Instances trainingSet,Instances testSet)
	{
		this.test = testSet;
		this.train= trainingSet;
	}
	
	public void classified() throws Exception
	{

		 Remove rm = new Remove();
		 rm.setAttributeIndices("1");  // remove 1st attribute
		 // classifier
		 J48 j48 = new J48();
		 j48.setUnpruned(true);        // using an unpruned J48
		 // meta-classifier
		 FilteredClassifier fc = new FilteredClassifier();
		 fc.setFilter(rm);
		 fc.setClassifier(j48);
		 /*
		 fc.setOptions(weka.core.Utils.splitOptions("")); // <==== a remplir
		 java.util.Enumeration<Option> st = fc.listOptions();
		 for(;st!=null;st.nextElement())
		 {
			 System.out.println(st.toString());
			 System.out.println(((weka.core.Option) st).description());
		 }
		 */
		 // train and make predictions
		 test.setClass(test.attribute("Class"));
		 train.setClass(train.attribute("Class"));
		 fc.buildClassifier(train);
		 
		 /*
		 System.out.println(fc.graph());
		 for (int i = 0; i < 10 ; i++) //test.numInstances()
		 {
		   double pred = fc.classifyInstance(test.instance(i));
		   System.out.print("ID: " + test.instance(i).value(0));
		   System.out.print(", actual: " + test.classAttribute().value((int) test.instance(i).classValue()));
		   System.out.println(", predicted: " + test.classAttribute().value((int) pred));
		 }
		 */
		 
		 
		// display classifier
		 /*
	     final javax.swing.JFrame jf = 
	       new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
	     jf.setSize(500,400);
	     jf.getContentPane().setLayout(new BorderLayout());
	     TreeVisualizer tv = new TreeVisualizer(null,
	         fc.graph(),
	         new PlaceNode2());
	     jf.getContentPane().add(tv, BorderLayout.CENTER);
	     jf.addWindowListener(new java.awt.event.WindowAdapter() {
	       public void windowClosing(java.awt.event.WindowEvent e) {
	         jf.dispose();
	       }
	     });

	     jf.setVisible(true);
	     tv.fitToScreen();
	     */
	}

}