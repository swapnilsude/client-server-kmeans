package project;

import java.io.IOException;
import java.net.*;

public class FinalClient {
	private static short ackNumber = 0x0;   //Initialing ACK number
	private static short noOfVectors = 50;   // Number of Data Vectors
	////////////////////////////////////////////enter the file location here\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	private static String fileLocation ="C:\\Users\\hshar\\OneDrive\\Desktop\\data01.txt";
	
	public static void main(String[] args) throws IOException {

		byte[] receiveData = new byte[17];   // byte array for the received packet to hold the expected byte sequence
		DatagramSocket clientSocket = new DatagramSocket();	// DatagramPacket object for socket open to send and receive UDP segments at client     /////////////////////////////////SocketException
		InetAddress IPAddress = InetAddress.getByName("127.0.0.1"); //IP address of the server                          /////////////////////////////////UnknownHostException
		int serverPortNumber = 9880;  //port number of the server
		System.out.println("Number of data vectors per DATA paket: "+noOfVectors);
		while(true) {    //while loop for sending all data packets
		
			int timeoutTime =1000;//Initial timeout time for each DATA packet to be sent
			int intArray []= ReadTextEncodeByte.getDocToArray((ackNumber)*noOfVectors,(ackNumber)*noOfVectors+noOfVectors,fileLocation);//docToIntArray((ackNumber)*noOfVectors,(ackNumber)*noOfVectors+noOfVectors);	//docToArray method to get array of data vectors in integer form
			if (intArray==null) {
				//System.out.println("Reached end of the array");
				break;  //Terminating the while loop after reaching the end of Data document array
			}
			byte dataPacket[]=ReadTextEncodeByte.getPacketStructure(intArray,noOfVectors,ackNumber);  //packetStructure Method to get byte encoded DATA packets with the header         /////IOException,,,,,from the method
	
			DatagramPacket sendPacket = new DatagramPacket(dataPacket, dataPacket.length, IPAddress, serverPortNumber); //	DatagramPacket object for the UDP packet to be transmitted
			
			while(true) {  //while loop for retransmitting the timeout packet
				
				if(timeoutTime==8000) {
					System.out.println("Communication failure");
					System.exit(0);		//exit the program when 4th timeout break and declare communication failure 
				}
				
				clientSocket.send(sendPacket);   //sending data
				System.out.println("sending "+ackNumber+ " DATA packet");
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);		//DatagramPacket object for the received UDP packet
				//System.out.println("Receiving DACK from the server...");
				clientSocket.setSoTimeout(timeoutTime);      //setting socket timeout
				try{
					clientSocket.receive(receivePacket);	//receiving DACK
					int sequenceNumber = (int)((0xff & dataPacket[1]) << 8  |	(0xff & dataPacket[2]) << 0 );  //sequence number
					ackNumber=(short)((0xff & receiveData[1]) << 8  |	(0xff & receiveData[2]) << 0 );		//ACK number 
					if(receiveData[0]!=0x1 || sequenceNumber!=ackNumber) {
						System.out.println("	Incorrct DACK received");
						throw new IllegalArgumentException(); //Throwing an exception when invalid data packet is received
					}
				}
				catch(Exception e) {		//catching the timeout and the invalid DACK exceptions
					System.out.println("Client DACK timeout!");
					timeoutTime=timeoutTime*2;             //if timeout double the timeout time
					continue; //resuming the code to the start so as to retransmit the DATA packet again
				}
				short sequenceNumber = (short)((0xff & dataPacket[1]) << 8  |	(0xff & dataPacket[2]) << 0 );  //sequence number
				ackNumber=(short)((0xff & receiveData[1]) << 8  |	(0xff & receiveData[2]) << 0 );		//ACK number 
	
				if(receiveData[0]==0x1 && sequenceNumber==ackNumber) {		//checking for correct packet type and has same sequence number and ack number 
					System.out.println("	DACK "+ackNumber+ " received, correct");
					ackNumber++;		//if received correctly incrementing the ACK number for sending the next packet
					break;		//terminating the retransmission loop as correct DACK is received 
				}
			}
		}
		
		byte reqPacket[]= {0x2}; // packet type 02h
		DatagramPacket sendPacket = new DatagramPacket(reqPacket, reqPacket.length, IPAddress, serverPortNumber); //	DatagramPacket object for the UDP packet to be transmitted
		int timeoutTime =1000;  // setting timeout time
		
		while(true) {  //while loop for retransmitting the timeout packet
			
			if(timeoutTime==8000) {
				System.out.println("communication failure");
				System.exit(0);		//exit the program when 4th timeout break and declare communication failure  
			}
			
			clientSocket.send(sendPacket);   //sending data
			System.out.println("Sending REQ packet");
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);		//DatagramPacket object for the received UDP packet
			//System.out.println("Receiving RACK from the server...");
			clientSocket.setSoTimeout(timeoutTime);      //setting socket timeout
			try{
				clientSocket.receive(receivePacket);	//receiving RACK
				if(receiveData[0]!=0x03) {
					System.out.println("	Incorrct RACK received");
					throw new IllegalArgumentException(); //when packet is received but is invalid throwing an illegal argument exception
				}
			}
			catch(Exception e) {   //catching timeout and invalid RACK 
				System.out.println("Client RACK timeout!");
				timeoutTime=timeoutTime*2;             //if timeout double the timeout time
				continue; //resuming the code to the start so as to retransmit the DATA packet again
			}
			//System.out.println(Arrays.toString(receiveData));
			if(receiveData[0]==0x03) {
				System.out.println("	RACK received");
				break; //RACK is received correctly. Hence breaking the while loop
			}
		}
		int count=1;   //counter to send back a CACK packet if needed
		int count1=1;  // counter to check for duplicate CLUS packet
		while(true) {
			timeoutTime=30000;	//timeout time for CLUS
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);		//DatagramPacket object for the received UDP packet
			clientSocket.setSoTimeout(timeoutTime);      //setting socket timeout
			try {
				clientSocket.receive(receivePacket);	//listening for correct DACK if incorrect DACK is received
			}
			catch(Exception e){		//catching the socket timeout
				System.out.println("CLUS Timeout");
				if(count==2) {		//displaying the error for second timeout
					System.out.println("	No dupicate CLUS received");
				}
				System.exit(0);  //exit the program when timeout is 30sec
			}
			if(receiveData[0]==0x4) {
				if(count1==1) {		//Executing this block for 1st correctly received CLUS
					float[] clusVector = byteEncodedToVectors(receiveData);		//Method to convert byte encoded CLUS to floating point 2d vector
					System.out.println("	CLUS Rxed");	
					System.out.println("Centroids for two Clusters are\n"+		//Displaying the floating point CLUS vector on screen
					"m1= ("+clusVector[0]+","+clusVector[1]+")\n"+
					"m2= ("+clusVector[2]+","+clusVector[3]+")\n"	);
					count1++; // increase counter1
				}
				byte cackPacket[]= {0x5};// packet type 05h
				sendPacket = new DatagramPacket(cackPacket, cackPacket.length, IPAddress, serverPortNumber); //	DatagramPacket object for the UDP packet to be transmitted
				clientSocket.send(sendPacket);   //sending CACK
				System.out.println("Sending CACK");
			}
			else {
				System.out.println("	received invalid CLUS packet");		//if invalid packet is received
			}
			if(count==2) {
				break;		//terminating the while loop after receiving any duplicate packet
			}
			count++;	//increase the counter 
		}
		clientSocket.close();
	}//main()
	
	public static float[] byteEncodedToVectors(byte[] receiveData) {
	float[]clusVector = new float[4];		//storing the received last cluster centroid vectors
	int j=0;								//index counter
	for(int i=1;i<receiveData.length;i=i+4) {		//reverse conversion 4 bytes to floating point data vectors
		clusVector[j] = (float)((0xff & receiveData[i]) << 24  |  (0xff & receiveData[i+1]) << 16  |	(0xff & receiveData[i+2]) << 8   |	(0xff & receiveData[i+3]) << 0 )/100;
		j++;
	}
	return clusVector;		//returning the decoded floating point data vectors	
}//byteEncodedToVectors

}//class ClientSide

