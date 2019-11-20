package project;



import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadTextEncodeByte {

	public static int [] getDocToArray	(int startVector, int lastVector,String fileLocation)throws IOException
	{
		BufferedReader docBuff; //create BufferededReader to read 
		String sRead = "";				//variable to store string line by line after reading from BufferedReader 
		String sReadNew ="";			//variable to store the necessary string
		int lineNumber = 0 ;
		
			docBuff = new BufferedReader(new FileReader(fileLocation));	//create and use a FileReader wrapped in a BufferedReader
			while ((sRead = docBuff.readLine()) !=null) {		//using readLine() from BufferedReader to read a line from text
				lineNumber++; 		//counting the total number of lines
				if(lineNumber>startVector && lineNumber<=lastVector)		//reading and adding particular lines from the data file 
					sReadNew = sReadNew + sRead; 							//and storing it to string sNew
			}
			docBuff.close();
		
		if(sReadNew=="") {
			return null;			//when the data vector points are over return NULL
		}
		if(lastVector>lineNumber) {
			lastVector = lineNumber;			//changing the lastVector to be read when the last line i.e. last data point in doc file 
		}
		int docToArray[] = new int[(lastVector-startVector)*2];		//size of array which is difference b/w last vector and start vector multiplied by 2 (as there are two points in each vector) 
		
		int i=0;		//initializer for array Index
		Pattern checkRegex = Pattern.compile("[-]?[0-9]*\\.?[0-9]+");   // regex expression pattern to be matched
		Matcher regexMatcher = checkRegex.matcher(sReadNew);			// matching the pattern from string sReadNew
		
		while(regexMatcher.find()) {					//finding the match and then storing them into array
			docToArray[i]= (int)((Float.valueOf(regexMatcher.group().trim()))*100);		//converting them from floating point values to integer
			i++;	//incrementing the array index
		}
		return docToArray;			//returning the array containing data vectors in integer format
	}
////////////////////////////////////////////
	public static byte[] getPacketStructure(int docToArray[],short noOfVectors, short ackNumber) throws IOException {
	
		byte[] byteHeader = new byte[5];			//Fixed length part of he DATA packet
				
		byteHeader[0]=0x0;							//packet type 00h
		byteHeader[1]=(byte)(ackNumber >> 8);		//sequence number
		byteHeader[2]=(byte)(ackNumber >> 0);		//sequence number
		byteHeader[3]=(byte)(noOfVectors >> 8);		//no of data vectors
		byteHeader[4]=(byte)(noOfVectors >> 0);		//no of data vectors
	
		byte[] byteVariable = new byte[4];			//storing one 4 byte integer data point into 4 Bytes 
		byte[] byteVariableTotal = new byte[docToArray.length*4];	// storing variable part of DATA packet i.e. byte encoded data vectors
		//System.out.println(docToArray.length);
		for(int i=0;i<docToArray.length;i++)		//loop to encode integer to byte  
		{
			byteVariable=new byte[] {
		        (byte)((docToArray[i] >> 24) & 0xff), (byte)((docToArray[i] >> 16) & 0xff),(byte)((docToArray[i] >> 8) & 0xff),(byte)((docToArray[i] >> 0) & 0xff)};
			for(int j=0;j<4;j++) {
				byteVariableTotal[j+(4*i)] = byteVariable[j];
			}
		}
				
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );					//concating the fixed and variable part to form DATA segment 
		outputStream.write( byteHeader ); ///////////////////////////////////////////////IOException
		outputStream.write( byteVariableTotal ); ///////////////////////////////////////////////IOException
		byte[] bytePacket = outputStream.toByteArray( );
		//System.out.println(Arrays.toString(bytePacket));
		return bytePacket;	//returning the DATA packet
	}//packetStructure()
}//class ReadTextEncodeByte