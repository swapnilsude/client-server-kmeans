package project;


import java.util.Random;

public class ComputeKmeans {

public static float[] [] getCentroid (float [][] xyCoordinates)
{
	int randomIndexone=0; 
	int randomIndextwo=0;
	
		// generating two different random indexes of xyCoordinates
	while(randomIndexone==randomIndextwo) {
		randomIndexone= new Random().nextInt(xyCoordinates.length);
		randomIndextwo= new Random().nextInt(xyCoordinates.length);
	}
		
	float[] [] RandomCentroid= {{xyCoordinates[randomIndexone][0],xyCoordinates[randomIndexone][1]},{xyCoordinates[randomIndextwo][0],xyCoordinates[randomIndextwo][1]}};
   // Creating 2d array to store 2 different centroids

      	
    double distance1[]=new double[xyCoordinates.length];     // distance from centroid 1 to all xycoordinates
    double distance2[] = new double [xyCoordinates.length];  //distance from centroid 2 to all xycoordinates
    
    while (true){
    	for(int i=0;i<xyCoordinates.length;i++) {
    		// calculating distance from centroid 1 to all xycoordinates
    		distance1[i]=Math.sqrt((Math.pow((RandomCentroid[0][0]-xyCoordinates[i][0]), 2))+ Math.pow((RandomCentroid[0][1]-xyCoordinates[i][1]),2));           
    		// claculating distance from centroid 2 to all xycoordinates
    		distance2[i]=Math.sqrt((Math.pow((RandomCentroid[1][0]-xyCoordinates[i][0]), 2))+ Math.pow((RandomCentroid[1][1]-xyCoordinates[i][1]),2));          
    	}
  
    	int compare[]=new int[xyCoordinates.length]; // this array will store comparison data 0->cluster1, 1->cluster2
    	int clusterOneSize=0;
    	int clustertwoSize=0;
    	
    	// storing 0 in compare in case of cluster 1 and 1 in case of cluster 2.
    	for(int k=0;k<xyCoordinates.length;k++) {
    		if(distance1[k]<=distance2[k]) {
    			compare[k]=0;
    			clusterOneSize++;   
    		}
    		else {
    			compare[k]=1;
    			clustertwoSize++;
    		}
    	}
    	
    	float cluster1[][]= new float[clusterOneSize][2];  // initializing cluster 1 array
    	float cluster2[][]=new float[clustertwoSize][2];   // initializing cluster 2 array
    	int rowsClusterOne=0;
    	int rowsClusterTwo=0;
    	
    	// separating cluster1 & cluster 2 coordinates from xyCoordinates with the help of data at compare array
    	for(int l=0;l<xyCoordinates.length;l++) {
    		if(compare[l]==0) {
    			cluster1[rowsClusterOne][0]=xyCoordinates[l][0];
    			cluster1[rowsClusterOne][1]= xyCoordinates[l][1];
    			rowsClusterOne++;
    		}
    		else {
    			cluster2[rowsClusterTwo][0]=xyCoordinates[l][0];
    	        cluster2[rowsClusterTwo][1]= xyCoordinates[l][1];
    	        rowsClusterTwo++;
    		}
    	}
    
    	float newCentroid[][]=new float[2][2]; // initializing new centroid
    	
    	//Calculating new centroids for cluster1 & cluster 2
    	for(int i=0;i<rowsClusterOne;i++)	{
    		newCentroid[0][0]=(newCentroid[0][0]+cluster1[i][0]);
    		newCentroid[0][1]=(newCentroid[0][1]+cluster1[i][1]);
    	}
    	newCentroid[0][0]=newCentroid[0][0]/rowsClusterOne;
    	newCentroid[0][1]=newCentroid[0][1]/rowsClusterOne;
    	for(int i=0;i<rowsClusterTwo;i++)	{
    		newCentroid[1][0]=(newCentroid[1][0]+cluster2[i][0]);
    		newCentroid[1][1]=(newCentroid[1][1]+cluster2[i][1]);
    	}
    	newCentroid[1][0]=newCentroid[1][0]/rowsClusterTwo;
    	newCentroid[1][1]=newCentroid[1][1]/rowsClusterTwo; 	
          
    	//calculating distance between old centroid and new centroid
    	double	distanceone=Math.sqrt((Math.pow((RandomCentroid[0][0]-newCentroid[0][0]), 2))+ Math.pow((RandomCentroid[0][1]-newCentroid[0][1]),2));  	
	   	double	distancetwo=Math.sqrt((Math.pow((RandomCentroid[1][0]-newCentroid[1][0]), 2))+ Math.pow((RandomCentroid[1][1]-newCentroid[1][1]),2));  	
				
		double e=0.00001;	// convergence constant
		//System.out.println(Arrays.deepToString(newCentroid));
		
		//Checking whether cluster centroids stop changing or not
       	if(((distanceone+distancetwo)<e||newCentroid[0][0]==RandomCentroid[0][0]&&newCentroid[0][1]==RandomCentroid[0][1]&&newCentroid[1][0]==RandomCentroid[1][0]&&newCentroid[1][1]==RandomCentroid[1][1]))
         	{
       		return newCentroid; // returning value of centroid if cluster centroids stop changing
      	}
       	else {
       		RandomCentroid[0][0]=newCentroid[0][0];
       		RandomCentroid[0][1]=newCentroid[0][1];
       		RandomCentroid[1][0]=newCentroid[1][0];
       		RandomCentroid[1][1]=newCentroid[1][1];
       	}
    }
    		
    		
    		
      	}
	
}
	

