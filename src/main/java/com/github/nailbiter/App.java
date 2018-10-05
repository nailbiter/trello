package com.github.nailbiter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.json.JSONArray;
import org.json.JSONException;
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
	private static final String PROMPT = "trello> ";
	private static final String BOARDID = "kDCITi9O";
	static JSONObject secret = new JSONObject();
	private static TrelloImpl trelloApi;
	private static Board board;
	private static TList list;
	private static TrelloAssistant ta_;
	private static String resFolder;
    public static void main( String[] args ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        System.out.println( "Hello World!" );
        Getopt g = new Getopt("testprog", args, "s:m:r:i");
		String testfile = null, methodToCall = null;
		int c = 0;
		while ((c = g.getopt()) != -1) {
			if(c=='s'){
				testfile= g.getOptarg();
				System.out.format("secret file: %s\n",testfile);
				secret = getJSONObject(testfile);
			} else if(c=='m') {
				methodToCall = g.getOptarg();
				System.out.format("method to call: %s\n",methodToCall);
			} else if(c=='r') {
				resFolder = g.getOptarg();
				System.out.format("resFolder: %s\n",resFolder);
			}else if(c=='i') {
				System.out.format("interactive mode\n");
				(new App()).startInteraction();
				return;
			}
		}

		System.out.println(secret.toString(2));
		
		trelloApi = new TrelloImpl(secret.getString("trellokey"), 
				secret.getString("trellotoken"), 
				new ApacheHttpClient());
		ta_ = new TrelloAssistant(secret.getString("trellokey"), 
				secret.getString("trellotoken"));
		board = trelloApi.getBoard(BOARDID);
		System.out.println(String.format("board is named: %s", board.getName()));
    	System.out.println("lists:");
		List<TList> lists = board.fetchLists();
		for(TList l:lists) {
			System.out.println(String.format("\tlist %s with id=%s", l.getName(),l.getId()));
			if(l.getName().equals("PENDING")) {
				list = l;
				break;
			}
		}
		System.out.println(String.format("list is named: %s", list.getName()));
		
		App.class.getDeclaredMethod(methodToCall).invoke(new App());
    }
    
    private void startInteraction() {
    	ArrayList<String> commands = new ArrayList<String>();
		PopulateCommands(commands);
		commands.add("exit");
		commands.add("help");
		System.out.format("commands: %s\n", commands.toString());
		
		Completer completer = new StringsCompleter(commands);
        LineReader reader = LineReaderBuilder.builder().completer(completer).build();
        String line = null;
        
        while (true) {
            line = null;
            try {
                line = reader.readLine(PROMPT).trim();
                if(line.equals("exit")) {
                	return;
                }else if(line.equals("help")){
//                	return;
                	System.out.format("commands: %s\n", commands.toString());
                } else {
                	String[] split = line.split(" ",2);
                	String methodToCall = split[0];
                	String reply = (String)App.class.getDeclaredMethod(methodToCall,String.class).invoke(this,split[1]);
                	System.out.format("%s\n", reply);
                }
            }
            catch(Exception e) {
            	e.printStackTrace();
            }
        }
	}

	private static void PopulateCommands(ArrayList<String> commands) {
		commands.clear();
		commands.add("makearchived");
	}
	public static String makearchived(String rem) throws Exception {
		String listId = ta_.findListByName(BOARDID, "todo");
		
		JSONObject res = ta_.addCard(listId, new JSONObject().put("name", rem));
		String[] split = res.getString("shortUrl").split("/");
		String id = split[4];
		System.err.format("id: %s\n", id);
		ta_.archiveCard(id);
		return String.format("%s", res.getString("url"));
	}
	public App() {
		ta_ = new TrelloAssistant(secret.getString("trellokey"), 
				secret.getString("trellotoken"));
	}

	static public void putlabel() throws ClientProtocolException, IOException {
    	JSONArray cards = ta_.getCardsInList(list.getId());
    	for(Object o:cards) {
    		JSONObject obj = (JSONObject)o;
    		System.out.println(String.format("going to put label for card %s", obj.getString("name")));
    		ta_.setLabel(obj.getString("id"),"green");
    	}
    }
    static public void readlabels() throws ClientProtocolException, IOException {
    	Map<String, String> labels = board.getLabelNames();
		for(String key:labels.keySet()) {
			System.out.println(String.format("\t\tlabel: %s -- %s", key,labels.get(key)));
		}
    }
    static public void readcard() throws ClientProtocolException, IOException {
    	JSONArray cards = ta_.getCardsInList(list.getId());
    	System.out.println("here go the cards");
    	System.out.println(cards.length());
    	for(Object o:cards) {
    		JSONObject obj = (JSONObject)o;
    		System.out.println(String.format("\t%s", obj.toString()));
    	}
    }
    static public void writecard() throws ClientProtocolException, IOException {
    	System.out.println("write card");
    	JSONArray cards = ta_.getCardsInList(list.getId());
    	System.out.println(cards.length());
    	for(Object o:cards) {
    		JSONObject obj = (JSONObject)o;
    		if(obj.getString("name").equals("java test")) {
    			System.out.println(String.format("here with id %s", obj.getString("id")));
    			ta_.setCardDuedone(obj.getString("id"), true);
    			break;
    		}
    	}
    }
    static public void makecard2() throws Exception {
    	String listid =  ta_.findListByName(board.getId(),"TODO");
    	ta_.addCard(listid, new JSONObject()
    			.put("name", "testname")
    			.put("due", new Date()));
    }
    static public void makecard() {
		System.out.println("\t\there");
		Card card = new Card();
		card.setName("java test");
		Calendar calendar = Calendar.getInstance(); 
		calendar.add(Calendar.HOUR, 4);  
		Date due = calendar.getTime();
		System.out.println(String.format("date: %s", due.toString()));
		card.setDue(due);
		list.createCard(card);
    }
    static void uploadsmalltasklist() throws Exception {
    	String listid =  ta_.findListByName(board.getId(),"TODO");
    	System.out.format("id :%s\n", listid);
    	try (BufferedReader br = new BufferedReader(new FileReader(resFolder+"smalltodo.txt"))) {
    	    String line;
    	    while ((line = br.readLine()) != null) {
    	    	ta_.addCard(listid, new JSONObject().put("name", line));
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
