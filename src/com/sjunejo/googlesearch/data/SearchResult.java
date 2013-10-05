package com.sjunejo.googlesearch.data;

/**
 * Contains data for each specific search result. In particular:
 * (1) The search term associated with each search result
 * (2) The result's webpage title
 * (3) The URL of the result webpage
 * (4) The text snippet of the webpage
 * Includes constructor as well as basic accessor methods.
 * @author Sadruddin
 *
 */
public class SearchResult {
	
	private String title;
	private String url;
	private String snippet;
	
	public SearchResult(String title, String url, String snippet){
		this.title = title;
		this.url = url;
		this.snippet = snippet;
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public String getURL(){
		return this.url;
	}
	
	public String getSnippet(){
		return this.snippet;
	}

}
