package com.github.nailbiter;

import java.io.FileReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.Label;
import com.julienvey.trello.domain.TList;
import com.julienvey.trello.impl.TrelloImpl;
import com.julienvey.trello.impl.http.ApacheHttpClient;

import gnu.getopt.Getopt;

public class App 
{
	static JSONObject secret = new JSONObject();
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        Getopt g = new Getopt("testprog", args, "s:");
		String testfile = null;
		int c = 0;
		while ((c = g.getopt()) != -1) {
			if(c=='s')
			{
				testfile= g.getOptarg();
				System.out.format("secret file: %s\n",testfile);
			}
		}
		secret = getJSONObject(testfile);
		System.out.println(secret.toString(2));
		
		Trello trelloApi = new TrelloImpl(secret.getString("trellokey"), 
				secret.getString("trellotoken"), 
				new ApacheHttpClient());
		Board board = trelloApi.getBoard("foFETfOx");
		System.out.println(String.format("board is named: %s", board.getName()));
		System.out.println("lists:");
		List<TList> lists = board.fetchLists();
		for(TList list:lists) {
			System.out.println(String.format("\tlist %s with id=%s", list.getName(),list.getId()));
			if(list.getName().equals("TOSHI")) {
				System.out.println("\t\there");
				Card card = new Card();
				card.setName("java test");
				Calendar calendar = Calendar.getInstance(); 
				calendar.add(Calendar.HOUR, 4);  
				Date due = calendar.getTime();
				System.out.println(String.format("date: %s", due.toString()));
				card.setDue(due);
				list.createCard(card);
//				card.setClosed(true);
				
				
				Map<String, String> labels = board.getLabelNames();
				for(String key:labels.keySet()) {
					System.out.println(String.format("\t\tlabel: %s -- %s", key,labels.get(key)));
				}
//				Label label = new Label();
//				label.setName("testlabel");
//				card.setLabels(Arrays.asList(label));
//				card.isClosed()
			}
		}
    }
    static JSONObject getJSONObject(String fname) {
    	FileReader fr = null;
		JSONObject res = null;
		try {
			System.out.println("storageManager gonna open: "+fname);
			fr = new FileReader(fname);
			StringBuilder sb = new StringBuilder();
            int character;
            while ((character = fr.read()) != -1) {
            		sb.append((char)character);
                //System.out.print((char) character);
            }
            System.out.println("found "+sb.toString());
			fr.close();
			res = (JSONObject) (new JSONTokener(sb.toString())).nextValue();
		}
		catch(Exception e) {
			System.out.println("found nothing");
			res = new JSONObject();
		}
		return res;
    }
}
