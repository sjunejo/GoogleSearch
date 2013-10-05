package com.sjunejo.googlesearch;

import java.util.ArrayList;
import com.sjunejo.googlesearch.data.SearchResult;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Class responsible for displaying the search results
 * in a list.
 * @author Sadruddin
 *
 */
public class ResultsAdapter extends BaseAdapter {

     private ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();  
     
     public ResultsAdapter(){
    	 
     }
    
     @Override
     public int getCount() {
        return searchResults.size();
     }

     @Override
     public SearchResult getItem(int position) {
        return searchResults.get(position);
     }

     @Override
     public long getItemId(int position) {
        return position;
     }

     /**
      * Handles display of a single search result
      */
     @Override
     public View getView(int position, View convertView,  final ViewGroup parent) {                                          
        LinearLayout view;
        if (convertView == null) {      
          LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
          view = (LinearLayout) layoutInflater.inflate(
                    R.layout.results_adapter, parent, false);

        } else {
           view = (LinearLayout) convertView;
        }

        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);                        
        TextView tvURL = (TextView) view.findViewById(R.id.tvURL);                        
        TextView tvSnippet = (TextView) view.findViewById(R.id.tvSnippet);                           

        String title = searchResults.get(position).getTitle();
        tvTitle.setText(title);
        final String url = searchResults.get(position).getURL();
        tvURL.setText(url);
        String snippet = searchResults.get(position).getSnippet();
        tvSnippet.setText(snippet);
        
        // Navigate to website when clicked
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				parent.getContext().startActivity(i);
            }
        });
        
        return view;
     } // End of getView() method

     /**
      * Updates the list when a new set of search results appears
      * @param sResults latest batch of search results
      */
     public void updateSearchResults(ArrayList<SearchResult> sResults) {
        searchResults = sResults;
        notifyDataSetChanged();
     }

} // End of ResultsAdapter class definition
