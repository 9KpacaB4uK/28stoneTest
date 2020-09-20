import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/*
	1. Using https://restcountries.eu create a text data file in JSON format, containing only the
	following data about countries in the European Union: name, capital, currencies, population,
	area.
	2. Read and process this file with your java program to get the following results:
		a. top 10 countries with the biggest population
		b. top 10 countries with the biggest area
		c. top 10 countries with the biggest population density (people / square km)
	3a. Add the ability to consume the data about countries directly from the restcountries.eu and
	process it (keep the code that processes data from the text file as well!)
	3b. Convert your application to Spring Boot application and expose results via REST API
	4. Add unit tests
*/
public class MainClass {
	static ArrayList<Object> listOfEU = new ArrayList<Object>();

	public static void main(String[] args) throws IOException, IOException, JSONException {
		int input = -1;
		JSONArray jArray = readJsonFromUrl("https://restcountries.eu/rest/v2/all");
		System.out.println("INSTRUCTION:\n1 - Create file with EU counties");
		while(input!=0) {
			input = getUserInput();
			if(input == 1) {
				createAndWriteToFile(getEUData(jArray),"JSONformat_EUcountries.txt");
				JSONArray jsonArrayFile =  readJsonFromFile("JSONformat_EUcountries.txt");
				System.out.println("INSTRUCTION:\n"
						+ "2 -top 10 countries with the biggest population\n"
						+ "3 -top 10 countries with the biggest area\n"
						+ "4 - top 10 countries with the biggest population density\n"
						+ "0 - Exit"
						);
				while(input!=0) {
					input = getUserInput();
					if(input < 5 && input >1) {
						switch(input) {
						case(2):
							System.out.println("top 10 countries with the biggest population");
							getTopPopulation(10,jsonArrayFile);
						break;
						case(3):
							System.out.println("top 10 countries with the biggest area");
							getTopArea(10,jsonArrayFile);
						break;
						case(4):
							System.out.println("top 10 countries with the biggest population density");
							getTopPopulationDensity(10,jsonArrayFile);
						break;
						}		
					}else {
						System.out.println("Warning-0: use only menu numbers!");
					}
					System.out.println("INSTRUCTION:\n"
							+ "2 -top 10 countries with the biggest population\n"
							+ "3 -top 10 countries with the biggest area\n"
							+ "4 - top 10 countries with the biggest population density\n"
							+ "0 - Exit"
							);
				}
				break;
			}else {
				System.out.println("Warning-1: At first create file (Press 1)");
			}		 
		}
		System.out.println("Bye! :)");
	}
	
	//get input from keyboard and check it for errors
	public static int getUserInput() {
        boolean flag;
        String n;
        do
	        {
	        Scanner sc = new Scanner(System.in);
	        System.out.print("Enter:");
	        n = sc.next();
		        try
		        {
		            Integer.parseInt(n);
		            flag=false;
		        }
		        catch(NumberFormatException e)
		        {
		            System.out.println("Warning-0: use only menu numbers!");
		            flag=true;
		        }
        }while(flag);
        return  Integer.parseInt(n);
	}
	
    // Function to sort by second column 
    public static String[][] sortbyColumn(String[][] dataArray) 
    { 
    	Arrays.sort( dataArray, new Comparator<String[]>(){

    	    @Override
    	    public int compare(final String[] first, final String[] second){

    	        return Double.valueOf(second[1]).compareTo(
    	            Double.valueOf(first[1])
    	        );
    	    }
    	});
		return dataArray;
    } 
    
    //get data and push it to array and 
	public static String[][] getArrayOfData(JSONArray jArray,String firstCol,String secondCol,int topOf) throws JSONException {
		int yLength = 2;
		int xLength =  jArray.length();
		if(topOf == -1)//-1 if get all elements
			topOf = jArray.length();
		String dataArray[][] = new String[xLength][yLength];
		for(int i = 0; i<xLength;i++) {
			for(int j = 0;j<yLength;j++) {
				switch(j) {
				case(0):
					dataArray[i][j] =  (String) jArray.getJSONObject(i).get(firstCol).toString();					
					break;
				case(1):
					dataArray[i][j] =  (String) jArray.getJSONObject(i).get(secondCol).toString();					
					break;
				}
			}
		}
		return dataArray;
	}
	
	//output two dimensional array and indicate count of rows
	public static void outputConsoleArray(String[][] dataArray,int topOf) {
		for(int i = 0; i<topOf;i++) {
			System.out.print(i+1+") ");
			for(int j = 0;j<dataArray[0].length;j++) {
				 System.out.print(dataArray[i][j]+"\n");
			}
		};
	}
	
	//get top population
	public static void getTopPopulation(int countTop,JSONArray jArray) throws JSONException {
		outputConsoleArray(
				sortbyColumn(
						getArrayOfData(jArray,"name","population",countTop)),
				countTop);
	}
	
	//get top of area
	public static void getTopArea(int countTop,JSONArray jArray) throws JSONException {
		outputConsoleArray(
				sortbyColumn(
						getArrayOfData(jArray,"name","area",countTop)),
				countTop);
	}
	
	//get top of population density
	public static void getTopPopulationDensity(int countTop,JSONArray jArray) throws JSONException {
		String[][] data = getArrayOfData(jArray,"population","area",-1);
		for(int i = 0; i<data.length;i++) {
			for(int j = data[0].length-1;j>=0;j--) {
					switch(j) {
					case(1):
						data[i][j] = String.valueOf(Float.parseFloat(data[i][j-1].toString())/Integer.parseInt(data[i][j].toString()));
						break;
					case(0):
						if( Integer.parseInt(jArray.getJSONObject(i).get("population").toString()) == Integer.parseInt(data[i][j])) {
							data[i][j] =  (String) jArray.getJSONObject(i).get("name").toString();	
						}
						break;
					}
			}
		};
		sortbyColumn(data);
		outputConsoleArray(data,countTop);
	}
	
	//create file and write to it
	public static void createAndWriteToFile(String textLine,String fileName) {
	    try {
	        FileWriter myWriter = new FileWriter(""+fileName);
	        myWriter.write(textLine);
	        myWriter.close();
	        System.out.println("Successfully wrote to the file.");
	      } catch (IOException e) {
	        System.out.println("An error occurred.");
	        e.printStackTrace();
	      }
	}
	
	//get data from web and found from it only EU
	public static  String getEUData(JSONArray jArray) throws NumberFormatException, JSONException {
		String name,capital;
		ArrayList<String> listOfCurrencies = new ArrayList<String>();
		int population;
		float area=0;
		String jsonEUText="[";
		
		for (int i = 0; i < jArray.length(); i++) {
			for(int j = 0;j <jArray.getJSONObject(i).getJSONArray("regionalBlocs").length();j++) {
				if(jArray.getJSONObject(i).getJSONArray("regionalBlocs").getJSONObject(j).get
						("name").toString().equalsIgnoreCase("European Union")) {
					listOfCurrencies.clear();
					
					name = (String) jArray.getJSONObject(i).get("name");
					capital = (String) jArray.getJSONObject(i).get("capital");
					for(int k = 0;k <jArray.getJSONObject(i).getJSONArray("currencies").length();k++) {	
						listOfCurrencies.add(jArray.getJSONObject(i).getJSONArray("currencies").getJSONObject(k).get("name").toString());
					}
					population = (int) jArray.getJSONObject(i).get("population");
					
					if(jArray.getJSONObject(i).get("area").equals(null)) {
						area = -1;
					}
					else {
						area = Float.parseFloat(jArray.getJSONObject(i).get("area").toString());
					}
					JSONObject currencies = new JSONObject();
					for(int k=0;k<listOfCurrencies.size();k++) {
						currencies.put(""+k,listOfCurrencies.get(k));
					}
					jsonEUText = jsonEUText + new JSONObject().put("id",i).put("name",name).put("capital", capital).put("population", population).put("area", area).put("currencies",currencies)+",";
				}
				
			}
		}
		jsonEUText = jsonEUText.substring(0, jsonEUText.length()-1);
		jsonEUText = jsonEUText + "]";
		
		return jsonEUText;
	}
	
	// get character by character
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	//get data from web
	public static JSONArray readJsonFromUrl(String url) throws IOException, JSONException {//
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("US-ASCII")));
			String jsonText = readAll(rd);

			JSONArray jArray = (JSONArray) new JSONTokener(jsonText).nextValue();
			return jArray;

		} finally {
			is.close();
		}
	}
	
	//get data from file
	public static JSONArray readJsonFromFile(String fileName) throws IOException, JSONException {//
		BufferedReader objReader = null;
		try {
			   objReader = new BufferedReader(new FileReader(fileName));
			   String jsonText = readAll(objReader);
			   
				JSONArray jArray = (JSONArray) new JSONTokener(jsonText).nextValue();
				return jArray;
		}
		finally {
			
		}
	}

}
