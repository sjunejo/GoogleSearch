package com.sjunejo.googlesearch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.sjunejo.googlesearch.data.SQLiteHelper;
import com.sjunejo.googlesearch.data.SearchResult;

/**
 * Main activity from which google searches can be executed.
 * @author Sadruddin
 *
 */
public class MainActivity extends Activity implements OnClickListener {

	// Components of the activity 
	Button btnSearch;
	EditText etSearch;
	ListView lvResults;
	
	// Allows for the display of search results in a list 
	ResultsAdapter resultsAdapter;
	
	// Keys needed in order to use the Custom Google Search API
	private static final String API_KEY = "AIzaSyDHFYZPjIH3mH9FRyZqXKLwDtQ6cfABMN0";
	private static final String CX_KEY = "013036536707430787589:_pqjad5hr1a";
	
	// Responsible for database connection as well as CRUD operations
	private SQLiteHelper databaseHandler;
	
	// Checks for internet connection before querying data
	private ConnectionDetector connectionDetector;
	
	/**
	 * Run when the app starts up
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Google Custom Search");
		setContentView(R.layout.activity_main);
		initialise();
		
	}

	// Initialising components found in activity, as well as database handler
	private void initialise(){
		etSearch = (EditText) findViewById(R.id.etSearch);
		btnSearch = (Button) findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(this);
		lvResults = (ListView) findViewById(R.id.lvResults);
		
		resultsAdapter = new ResultsAdapter();
		lvResults.setAdapter(resultsAdapter);
		databaseHandler = new SQLiteHelper(this);
		connectionDetector = new ConnectionDetector(this);
		
	}

	/**
	 * This method is run when the user presses a button.
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch(id){
			// When the search button is pressed
			case R.id.btnSearch:
				
				// NOTE LOWER CASE
				String searchTerm = etSearch.getText().toString().toLowerCase();
				
				btnSearch.setEnabled(false);
				// Validation (prevention of empty string querying)
				if (searchTerm.length() > 0){
					
					// Need to remove whitespace
					searchTerm = formatSearchTerm(searchTerm);
					
					/* Checks if the search term has previously been queried
					 * Using search term data stored in the database.
					 * If there's a match, data will be fetched from the database 
					 * instead of another Google search.
					 */
					if (databaseHandler.storedInDatabase(searchTerm)){
						Toast.makeText(this, "Match found in database. Retrieving data...", Toast.LENGTH_SHORT).show();
						ArrayList<SearchResult> searchResults = fetchDataFromStorage(searchTerm);
						btnSearch.setEnabled(true);
						displayResults(searchResults);
					}
					else {
						// No match was found, hence Google search needed
						
						// Check for connection to internet before querying Google.
						if (connectionDetector.isConnectedToInternet()){
							Toast.makeText(this, "Search query not stored in database - performing asynchronous Google search...", Toast.LENGTH_SHORT).show();
							executeGoogleSearch(searchTerm);
						}
						else { // No connection available
							Toast.makeText(this, "Need to search Google, but no internet connection available!", Toast.LENGTH_SHORT).show();
							btnSearch.setEnabled(true);
						}
					}
				}
				else {
					// Display validation error
					Toast.makeText(this, "No text entered!", Toast.LENGTH_SHORT).show();
					//////////////////////////////
					// BUG FIX
					btnSearch.setEnabled(true);
					//////////////////////////////
				}
				break;
		} // End of switch statements
		
	} // End of onClick() method
	
	/**
	 * Removes whitespace. (could possibly require additional formatting)
	 * @param searchTerm
	 * @return formatted seach term
	 */
	private String formatSearchTerm(String searchTerm){
		String formattedSearchTerm = searchTerm.replaceAll("\\s","%20");
		return formattedSearchTerm;
	}
	
	/**
	 * Checks the SQL database for the search results based on the search term
	 * @param searchTerm the search query
	 */
	private ArrayList<SearchResult> fetchDataFromStorage(String searchTerm){
		ArrayList<SearchResult> storedSearchResults = databaseHandler.getStoredSearchResults(searchTerm);
		return storedSearchResults;
	}
	
	/**
	 * Performs a Google search using the Custom Google Search API,
	 * and then adds the search results to the SQLite database.
	 * @param searchTerm
	 */
	private void executeGoogleSearch(String searchTerm){
		new GoogleSearchTask().execute(searchTerm);
	}
	
	/**
	 * After a Google search is completed, the search term and results data
	 * are added to the database so that unnecessary Google searches can 
	 * be avoided.
	 * @param searchTerm
	 */
	private void addSearchResultsToDatabase(String searchTerm, ArrayList<SearchResult> searchResults){
		btnSearch.setEnabled(true);
		// First of all the search term has to be added to the table
		databaseHandler.insertSearchTerm(searchTerm);
		databaseHandler.insertSearchResults(searchResults, searchTerm);
		displayResults(searchResults);
	}
	
	/**
	 * Displays search results
	 */
	private void displayResults(ArrayList<SearchResult> searchResults){
		resultsAdapter.updateSearchResults(searchResults);
	}
	
	/**
	 * Performs Google search asynchronously so as not
	 * to disrupt the UI thread.
	 * @author Sadruddin
	 *
	 */
	 private class GoogleSearchTask extends AsyncTask<String, Integer, ArrayList<SearchResult>> {
		 
		 private String searchTerm;
		 private ArrayList<SearchResult> searchResults;
		 
		 /**
		  * Main processing of async task
		  */
	     protected ArrayList<SearchResult> doInBackground(String... string) {
	      
	    	 searchTerm = string[0];
	    	 searchResults = new ArrayList<SearchResult>();
	    	 
		     try {
		    	 // Prepares URL for custom Google search
		        URL url = new URL("https://www.googleapis.com/customsearch/v1?key=" 
		        	+ API_KEY +"&cx=" + CX_KEY + "&q="+searchTerm+"&alt=json");

		         // Prepare connection and GET request
		         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		         connection.setRequestMethod("GET");
		         connection.setRequestProperty("Accept", "application/json");
		              
		         // Receive JSON data through GET request
		         BufferedReader br = new BufferedReader(new InputStreamReader(
		                      (connection.getInputStream())));

		         StringBuilder builder = new StringBuilder();
		         String line;
		         while ((line = br.readLine()) != null) {
		             builder.append(line);
		         }
		         String output = builder.toString();
		              
		         connection.disconnect();
		         searchResults = parseJSON(output);

		     } catch (Exception e) {
		    	 e.printStackTrace();
		     }
		        
		     return searchResults;
	     } // End of doInBackground() method
	     
	     /**
	      * Parses JSON Output from Google custom search 
	      * @param output the JSON data in string format
	      * @return a list of search results
	      */
	     private ArrayList<SearchResult> parseJSON(String output){
	    	 
	    	 ArrayList<SearchResult> results = new ArrayList<SearchResult>();
	    	 
	    	 try {
                 JSONObject jsonObject = new JSONObject(output);
                 
                 JSONArray array = jsonObject.getJSONArray("items");
                 
                 for (int i = 0; i < array.length(); i++) {
	                    JSONObject object = array.getJSONObject(i);
	                    SearchResult searchResult = new SearchResult(object.getString("title"), 
	                    		object.getString("link"), object.getString("snippet"));
	                    results.add(searchResult);
	                   
	                  }
                
               } catch (Exception e) {
                 e.printStackTrace();
               }
	    	 
	    	 return results;
	     } // End of parseJSON() method

	     /**
	      * Run after doInBackground() processing is completed.
	      */
	     protected void onPostExecute(ArrayList<SearchResult> results) {
	    	MainActivity.this.addSearchResultsToDatabase(searchTerm, results);
	     }
	     
	 } // End of GoogleSearchTask class definition
	 
} // End of MainActivity class definition
