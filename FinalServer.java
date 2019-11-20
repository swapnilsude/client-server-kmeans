package project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

public class FinalServer {

	public static void main(String[] args) throws IOException {
		
		int serverPortNumber = 9880	;			//server port number 
		DatagramSocket serverSocket = new DatagramSocket(serverPortNumber);		// DatagramPacket object to open socket to send and receive UDP segments at server
		byte[] receiveData = new byte[1024];		//byte array to store incoming packet 
		
		while(true) {	//keeping the server running
			
			short seqNum=0;	//initial sequence number s set to 0
			byte[] sequenceNumber=new byte[2];				//storing byte encoded sequence number
		        
		    ArrayList<Float> floatValuesArrayList=new ArrayList<Float>(); //Arraylist to store incoming DATA packet variable part

		    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);		//DatagramPacket object for the received UDP packet

			while(true) {		//loop for receiving DATA packets and sending DACK packets
				
				sequenceNumber[0]=(byte)(seqNum >> 8);		//byte encoding sequence number
				sequenceNumber[1]=(byte)(seqNum >> 0);		//byte encoding sequence number
				
				//System.out.println("rx");
				serverSocket.receive(receivePacket);		//receiving
						
				byte[] dAck= new byte[3];					//initializing DACK packet
				
				if(receiveData[0]==0x0 && receiveData[1]==sequenceNumber[0] && receiveData[2]==sequenceNumber[1] ) {   //when packet type is correct and is in sequence
					for(int i=5;i<receivePacket.getLength();i=i+4) {		//decoding and adding floating point values to Arraylist
						int byteToInt = (int)((0xff & receiveData[i]) << 24  |  (0xff & receiveData[i+1]) << 16  |	(0xff & receiveData[i+2]) << 8   |	(0xff & receiveData[i+3]) << 0 );
						floatValuesArrayList.add((float)(byteToInt)/100);
					}
					dAck[0]=0x1; 							//
					dAck[1]=receiveData[1];					//creating DACK packet when a valid DATA packet is received 	
					dAck[2]=receiveData[2];					//
					System.out.println("DATA "+seqNum+ " received");
				}
				else if(receiveData[0]==0x2) {
					System.out.println("REQ received");
					break;									//breaking while loop on receiving the REQ packet
				}
				else { 										//When DATA packet is invalid
					seqNum--;								//changing sequence number to last correctly received packet
					dAck[0]=0x1;						//
					dAck[1]=(byte)(seqNum >> 8);		//creating DACK packet when a invalid DATA packet is received 
					dAck[2]=(byte)(seqNum >> 0);		//
					System.out.println("Invalid DATA received");
				}
				//System.out.println(Arrays.toString(dAck));
				DatagramPacket dACKPacket = new DatagramPacket(dAck,dAck.length,receivePacket.getAddress(),receivePacket.getPort());  //DatagramPacket object for the UDP DATA packet to be transmitted  // fetching IP address and port from the received packets
		        System.out.println("	Sending "+seqNum+" DACKto the client.....");//
		        serverSocket.send(dACKPacket); 		//sending
		        seqNum++;   						//incrementing the sequence number to next expected packet sequence 
				}
			
		    float [] [] floatValuesArray = new float[floatValuesArrayList.size()/2][2];				//initializing array to store 2d floating type data vectors
			for(int i=0;i<floatValuesArrayList.size()/2;i++) {										//Arraylist to 2d floating point vector array
				for(int j=0;j<2;j++) {
					floatValuesArray[i][j]=floatValuesArrayList.get(i+i+j);
				}	
			}
			//System.out.println(Arrays.deepToString(floatValuesArray));		
			
			byte[] rAck= new byte[1];
			rAck[0]=0x3;
			
			while(true) {
				
				DatagramPacket rACKPacket = new DatagramPacket(rAck,rAck.length,receivePacket.getAddress(),receivePacket.getPort());//DatagramPacket object for the UDP RACK packet to be transmitted
	            System.out.println("	Sending the RACKto the client.....");//
	            serverSocket.send(rACKPacket); 		//sending
	            byte req[]= new byte[1];			//initialing duplicate REQ byte packet
	            DatagramPacket waitingREQ = new DatagramPacket(req, req.length);		//DatagramPacket object for the received UDP req packet
	            serverSocket.setSoTimeout(3000);	//waiting for duplicate REQ packet for retransmitting RACK
	            try{
	    			serverSocket.receive(waitingREQ);		
	    			System.out.println("REQ received again");
	    		}
	    		catch(SocketTimeoutException e)	{
	    			System.out.println("	no duplicate REQ received..continue....");
	    			break;							//when no duplicate packet received break loop
	    		}	
			}
			
			
			float [] []centroid=ComputeKmeans.getCentroid(floatValuesArray);	//method to calculate K-means clustering and obtaining floating point last cluster centroid vectors m1 and m2 
			
		    byte[] clusPacketFormation = new byte[4];							//initializing byte array to store one centroid vector converted from integer to 4 bytes
		    byte[] clusPacket = new byte[17];									//initializing CLUS packet
		    clusPacket[0]=0x04;													//packet type 04h
		    int[] centroidInt = new int[centroid.length*2];						//float to integer
		    //multiplying by 100
		    for( int j=0;j<centroid.length;j++) {
		    	for(int k=0;k<2;k++) {
		    		centroidInt[j*2+k]=(int)(centroid[j][k]*100);
		    	}
		    }
		    for(int l=0;l<centroidInt.length;l++) {								//integer to byte i.e. byte encoding
		    	clusPacketFormation=new byte[] {
		    			(byte)((centroidInt[l] >> 24) & 0xff), (byte)((centroidInt[l] >> 16) & 0xff),(byte)((centroidInt[l] >> 8) & 0xff),(byte)((centroidInt[l] >> 0) & 0xff)};
		    	for(int j=0;j<4;j++) {
		    		clusPacket[j+(4*l)+1] = clusPacketFormation[j];
		    	}
		    }
		    //System.out.println(Arrays.toString(clusPacket));
	    	DatagramPacket CLUS = new DatagramPacket(clusPacket, clusPacket.length, receivePacket.getAddress(), receivePacket.getPort());		//DatagramPacket object for the UDP CLUS packet to be transmitted  // fetching IP address and port from the received packets

	    	int cackTimeout=1000;		//initial timeout for cack
		    while(true) {
		    	if(cackTimeout==8000) {
					System.out.println("Communication failure");
					break;				//when 4th timeout break and declare communication failure
		    	}
	    		serverSocket.send(CLUS);
	    		System.out.println("	Sending CLUS packet");
	    		byte cACk[]= new byte[1];	//initializing CLUS packet
	    		DatagramPacket CACK = new DatagramPacket(cACk, cACk.length);		//DatagramPacket object for the received UDP ACK packet
	    		//System.out.println("receiving CACK from client");
	    		serverSocket.setSoTimeout(cackTimeout);		//setting socket timeout
	    		try {
	    			serverSocket.receive(CACK);				//receiving
	    			if(cACk[0]==0x5) {						
	    				System.out.println("Cack received");	
	    				break;		//breaking loop after valid CACK is received
	    			}
	    			if(cACk[0]!=0x5) {
	    				System.out.println("Invalid Cack received");
						throw new IllegalArgumentException(); //Throwing an exception when invalid data packet is received
	    			}
			    }
			    catch(Exception e) {
			    	System.out.println("Cack Timeout");
			    	cackTimeout=cackTimeout*2;		//doubling the CACK timeout time
			    }
		    }
		    
    		serverSocket.setSoTimeout(300000); 		//increasing the timeout time of server socket for receiving the next request from he client
		}
	}	
}	
