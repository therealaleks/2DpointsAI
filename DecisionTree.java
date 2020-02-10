//Alex Liu 260867551

import java.io.Serializable;
import java.util.ArrayList;
import java.text.*;
import java.lang.Math;

public class DecisionTree implements Serializable {

	DTNode rootDTNode;
	int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split
	//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
	public static final long serialVersionUID = 343L;
	public DecisionTree(ArrayList<Datum> datalist , int min) {
		minSizeDatalist = min;
		rootDTNode = (new DTNode()).fillDTNode(datalist);
	}

	class DTNode implements Serializable{
		//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
		public static final long serialVersionUID = 438L;
		boolean leaf;
		int label = -1;      // only defined if node is a leaf
		int attribute; // only defined if node is not a leaf
		double threshold;  // only defined if node is not a leaf



		DTNode left, right; //the left and right child of a particular node. (null if leaf)

		DTNode() {
			leaf = true;
			threshold = Double.MAX_VALUE;
		}



		// this method takes in a datalist (ArrayList of type datum) and a minSizeInClassification (int) and returns
		// the calling DTNode object as the root of a decision tree trained using the datapoints present in the
		// datalist variable
		// Also, KEEP IN MIND that the left and right child of the node correspond to "less than" and "greater than or equal to" threshold
		DTNode fillDTNode(ArrayList<Datum> datalist) {

			//YOUR CODE HERE
			if (datalist.size() >= minSizeDatalist) {
				
				int prev = 0;
				boolean first = true;
				boolean same = true;
				for (Datum i : datalist) {
					if(first) {
						prev = i.y;
						first = false;
					}else {
						same = (i.y == prev);
						if (!same) break;
						prev = i.y;
					}
				}
				
				if(same) {
					this.leaf = true;
					this.label = findMajority(datalist);
					return this;
				}else {
					
					ArrayList<Datum> dat1 = new ArrayList<Datum>();
					ArrayList<Datum> dat2 = new ArrayList<Datum>();
					
					double bestAvgE = Double.MAX_VALUE;
					int bestAtt = -1;
					double best_thre = -1;
					
					for ( int i = 0 ; i < datalist.get(0).x.length ; i ++ ) {
						for (Datum z : datalist) {
							
							ArrayList<Datum> sp1 = new ArrayList<Datum>();
							ArrayList<Datum> sp2 = new ArrayList<Datum>();
							
							for (Datum v : datalist) {
								if (v.x[i] < z.x[i]) {
									sp1.add(v);
								}else if (v.x[i] >= z.x[i]) {
									sp2.add(v);
								}
							}
							
							double w1 = (double) sp1.size() / (double) datalist.size();
							double w2 = (double) sp2.size() / (double) datalist.size();
							
							Double currentAvg = (w1*calcEntropy(sp1)) + (w2*calcEntropy(sp2));
							
							if (bestAvgE > currentAvg) {
								bestAvgE = currentAvg;
								bestAtt = i;
								best_thre = z.x[i];
								
								dat1 = sp1;
								dat2 = sp2;
							}
						}
					}
					
					if (!(dat1.size() > 0 )){
						this.leaf = true;
						this.label = findMajority(datalist);
						return this;
					}
					
				
					this.attribute = bestAtt;
					this.threshold = best_thre;
					this.leaf = false;
					
					this.left = (new DTNode()).fillDTNode(dat1);
					this.right = (new DTNode()).fillDTNode(dat2);
					
	
					return this;
				}
			}else {
			
				this.leaf = true;
				this.label = findMajority(datalist);
				return this;
				
			}
		}



		//This is a helper method. Given a datalist, this method returns the label that has the most
		// occurences. In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
		int findMajority(ArrayList<Datum> datalist)
		{
			int l = datalist.get(0).x.length;
			int [] votes = new int[l];

			//loop through the data and count the occurrences of datapoints of each label
			for (Datum data : datalist)
			{
				votes[data.y]+=1;
			}
			int max = -1;
			int max_index = -1;
			//find the label with the max occurrences
			for (int i = 0 ; i < l ;i++)
			{
				if (max<votes[i])
				{
					max = votes[i];
					max_index = i;
				}
			}
			return max_index;
		}




		// This method takes in a datapoint (excluding the label) in the form of an array of type double (Datum.x) and
		// returns its corresponding label, as determined by the decision tree
		int classifyAtNode(double[] xQuery) {
			//YOUR CODE HERE
			if (this.leaf == true) {
				return this.label;
			}else {
				if(xQuery[this.attribute] < this.threshold) {
					
					return this.left.classifyAtNode(xQuery);
					
				}else if(xQuery[this.attribute] >= this.threshold) {
					
					return this.right.classifyAtNode(xQuery);
					
				}
			}
			return -1; //dummy code.  Update while completing the assignment.
		}


		//given another DTNode object, this method checks if the tree rooted at the calling DTNode is equal to the tree rooted
		//at DTNode object passed as the parameter
		public boolean equals(Object dt2)
		{
			if (dt2 == null) return false;
			
			else if (dt2 instanceof DTNode) {
				boolean a = true;
				boolean b = true;
				DTNode bob = (DTNode)dt2;
				if(bob.leaf == true && this.leaf == true){
					return bob.label == this.label;
				}else {
					if(bob.leaf == false && this.leaf == false && bob.attribute == this.attribute && bob.threshold == this.threshold) {
						
						if((bob.left == null && this.left == null) || (bob.right == null && this.right == null )) {
								
							if(bob.left != null) {
								a = this.left.equals(bob.left);
							}
							
							if(bob.right != null) {
								b = this.right.equals(bob.right);		
							}
							
						}else if ((bob.left != null && this.left != null) && (bob.right != null && this.right != null )) {
								
							a = this.left.equals(bob.left);
							b = this.right.equals(bob.right);
								
						}else return false;
						
					}else return false;
				}
				
				return (a && b);
			}
			
			return false;
		}
	}



	//Given a dataset, this retuns the entropy of the dataset
	double calcEntropy(ArrayList<Datum> datalist)
	{
		double entropy = 0;
		double px = 0;
		float [] counter= new float[2];
		if (datalist.size()==0)
			return 0;
		double num0 = 0.00000001,num1 = 0.000000001;

		//calculates the number of points belonging to each of the labels
		for (Datum d : datalist)
		{
			counter[d.y]+=1;
		}
		//calculates the entropy using the formula specified in the document
		for (int i = 0 ; i< counter.length ; i++)
		{
			if (counter[i]>0)
			{
				px = counter[i]/datalist.size();
				entropy -= (px*Math.log(px)/Math.log(2));
			}
		}

		return entropy;
	}


	// given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object
	int classify(double[] xQuery ) {
		DTNode node = this.rootDTNode;
		return node.classifyAtNode( xQuery );
	}

    // Checks the performance of a DecisionTree on a dataset
    //  This method is provided in case you would like to compare your
    //results with the reference values provided in the PDF in the Data
    //section of the PDF

    String checkPerformance( ArrayList<Datum> datalist)
	{
		DecimalFormat df = new DecimalFormat("0.000");
		float total = datalist.size();
		float count = 0;

		for (int s = 0 ; s < datalist.size() ; s++) {
			double[] x = datalist.get(s).x;
			int result = datalist.get(s).y;
			if (classify(x) != result) {
				count = count + 1;
			}
		}

		return df.format((count/total));
	}


	//Given two DecisionTree objects, this method checks if both the trees are equal by
	//calling onto the DTNode.equals() method
	public static boolean equals(DecisionTree dt1,  DecisionTree dt2)
	{
		boolean flag = true;
		flag = dt1.rootDTNode.equals(dt2.rootDTNode);
		return flag;
	}

}
