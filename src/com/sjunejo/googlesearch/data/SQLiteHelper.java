package com.sjunejo.googlesearch.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Database handler responsible for maintaining a 
 * connection to the SQLite database as well as performing
 * CRUD (Create, Read, Update, Delete) Operations.
 * @author Sadruddin
 *
 */
public class SQLiteHelper extends SQLiteOpenHelper {
	
	 /**
	  * CUSTOM SEARCH TABLE 
	  */
	  private static final String DATABASE_NAME = "customsearch.db";
	  private static final int DATABASE_VERSION = 1;
	  
	  /**
	   * SEARCHTERMS table:
	   * Holds search terms that have previously been used in the program
	   */
	  public static final String TABLE_SEARCHTERMS = "search_terms";
	  public static final String SEARCHTERMS_COLUMN_ID = "searchterm_id";
	  public static final String SEARCHTERMS_COLUMN_SEARCHTERM = "searchterm";
	
	  /**
	   * RESULTS table
	   * Holds results specific to search terms
	   */
	  public static final String TABLE_RESULTS = "search_results";
	  public static final String RESULTS_COLUMN_ID = "result_id";
	  public static final String RESULTS_COLUMN_SEARCHTERM = "search_term";
	  public static final String RESULTS_COLUMN_TITLE = "result_title";
	  public static final String RESULTS_COLUMN_SNIPPET = "result_snippet";
	  public static final String RESULTS_COLUMN_URL = "result_url";
	  
	  /**
	   * TABLE CREATION QUERIES
	   * Responsible for creating the two tables for use in the database.
	   */
	  private static final String QUERY_CREATE_TABLE_SEARCHTERMS = "CREATE TABLE " + TABLE_SEARCHTERMS 
			  + "(" + SEARCHTERMS_COLUMN_ID + " INTEGER PRIMARY KEY, " + SEARCHTERMS_COLUMN_SEARCHTERM 
			  + " VARCHAR(100)" + ")";
	  // Foreign key used for data consistency
	  private static final String QUERY_CREATE_TABLE_RESULTS = "CREATE TABLE " + TABLE_RESULTS 
			  + "(" + RESULTS_COLUMN_ID + " INTEGER PRIMARY KEY, " + RESULTS_COLUMN_SEARCHTERM + " VARCHAR(100), "
			  + RESULTS_COLUMN_TITLE + " VARCHAR(100), " + RESULTS_COLUMN_URL + " VARCHAR(200)," 
			  + RESULTS_COLUMN_SNIPPET + " VARCHAR(200), "
			  + " FOREIGN KEY (" + RESULTS_COLUMN_SEARCHTERM + ") REFERENCES " + TABLE_SEARCHTERMS 
			  + "(" + SEARCHTERMS_COLUMN_SEARCHTERM + ")"+ " ON DELETE RESTRICT ON UPDATE CASCADE " + ")";
	  
	  /**
	   * Constructor method
	   * @param context the current context
	   */
	  public SQLiteHelper(Context context) {
		    super(context, DATABASE_NAME, null, DATABASE_VERSION);
		  }

	/**
	 * Runs when the database is FIRST CREATED.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(QUERY_CREATE_TABLE_SEARCHTERMS);
		db.execSQL(QUERY_CREATE_TABLE_RESULTS);
		
	}

	/**
	 * Handles what occurs when the database is upgraded.
	 * In this case, it drops all data currently stored
	 * and creates a new table from scratch.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		clearDatabase(db);
	}

	/**
	 * Insert a single search term into the search terms table.
	 * @param searchTerm
	 */
	public void insertSearchTerm(String searchTerm){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(SEARCHTERMS_COLUMN_SEARCHTERM, searchTerm);
		db.insert(TABLE_SEARCHTERMS, null, cv);
		db.close();
	}
	
	/**
	 * Checks if a search term entered from the main activity
	 * has already been used previously.
	 * @param searchTerm the search term
	 * @return true if the search term has been previously used,
	 * false if it hasn't.
	 */
	public boolean storedInDatabase(String searchTerm){
		String queryCheckForSearchTerm = "SELECT COUNT(*) FROM " + SQLiteHelper.TABLE_SEARCHTERMS 
				+ " WHERE " + SQLiteHelper.SEARCHTERMS_COLUMN_SEARCHTERM 
				+ " LIKE '" + searchTerm + "'";
	
		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor cursor = db.rawQuery(queryCheckForSearchTerm, null);
		cursor.moveToFirst();
		
		int count = cursor.getInt(0);
		
		cursor.close();
		db.close();
		
		// If count is anything besides 0 that means a match has been found.
		if (count > 0)
			return true;
		else
			return false; // No match found, so must execute Google search
	} // End of storedInDatabase
	
	/**
	 * This method executed AFTER a new search term is inserted into the database.
	 * @param searchTerm
	 */
	public void insertSearchResults(ArrayList<SearchResult> searchResults, String searchTerm){
		/* So now we have an array of search results.
		 * Lets add the search results to the database!
		 * But how can we do this efficiently? 
		 * Transactions may be helpful here.
		 */
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		
		ContentValues cv;
		for (SearchResult searchResult: searchResults){
			cv = new ContentValues();
			cv.put(RESULTS_COLUMN_SEARCHTERM, searchTerm);
			cv.put(RESULTS_COLUMN_TITLE, searchResult.getTitle());
			cv.put(RESULTS_COLUMN_URL, searchResult.getURL());
			cv.put(RESULTS_COLUMN_SNIPPET, searchResult.getSnippet());
			db.insert(TABLE_RESULTS, null, cv);
		}

		db.setTransactionSuccessful();
		db.endTransaction();
		
		db.close();
	} // End of insertSearchResults method
	
	/**
	 * Gets results already stored in SQLite database.
	 * @param searchTerm
	 * @return list of search results
	 */
	public ArrayList<SearchResult> getStoredSearchResults(String searchTerm){
		String queryCheckForSearchTerm = "SELECT * FROM " + TABLE_RESULTS
				+ " WHERE " + RESULTS_COLUMN_SEARCHTERM
				+ " LIKE '" + searchTerm + "'";
		
		ArrayList<SearchResult> storedSearchResults = new ArrayList<SearchResult>();
		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor cursor = db.rawQuery(queryCheckForSearchTerm, null);
		cursor.moveToFirst();
	
		do {
			// Retrieves data from database
			storedSearchResults.add(new SearchResult(cursor.getString(2), 
					cursor.getString(3), cursor.getString(4)));
		}while (cursor.moveToNext());
		cursor.close();
		db.close();
		
		return storedSearchResults;
	} // End of getCachedResults class definition
	
	/**
	 * The database may get cluttered after a while, 
	 * so in order to clear the 'cache' this method can be run.
	 */
	public void clearDatabase(SQLiteDatabase db){
		// Drop older tables 
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESULTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCHTERMS);
				
		// Create tables again
		onCreate(db);
	}

} // End of SQLiteHelper class definition
