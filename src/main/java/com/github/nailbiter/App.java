package com.github.nailbiter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.http.client.ClientProtocolException;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TrelloAssistant;
import com.github.nailbiter.util.Util;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.TList;
import com.julienvey.trello.impl.TrelloImpl;
import com.julienvey.trello.impl.http.ApacheHttpClient;

import gnu.getopt.Getopt;

public abstract class App 
{
	protected static final String PROMPT = "trello> ";
	protected static final String HABITBOARDID = "kDCITi9O";
	protected static final String INBOXBOARDID = "foFETfOx";
	static JSONObject secret = new JSONObject();
//	protected static TrelloImpl trelloApi;
	protected static Board board;
	protected static TList list;
	protected static TrelloAssistant ta_;
	protected static String resFolder;
	protected ScriptEngine engine_;
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
				secret = Util.getJSONObject(testfile);
			} else if(c=='m') {
				methodToCall = g.getOptarg();
				System.out.format("method to call: %s\n",methodToCall);
			} else if(c=='r') {
				resFolder = g.getOptarg();
				System.out.format("resFolder: %s\n",resFolder);
			}else if(c=='i') {
				System.out.format("interactive mode\n");
				(new AppChild()).startInteraction();
				return;
			}
		}

		System.out.println(secret.toString(2));
		
		TrelloImpl trelloApi = new TrelloImpl(secret.getString("trellokey"), 
				secret.getString("trellotoken"), 
				new ApacheHttpClient());
		ta_ = new TrelloAssistant(secret.getString("trellokey"), 
				secret.getString("trellotoken"));
		board = trelloApi.getBoard(HABITBOARDID);
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
		
		App.class.getDeclaredMethod(methodToCall).invoke(new AppChild());
    }
    
    protected void startInteraction() {
    	ArrayList<String> commands = new ArrayList<String>();
		populateCommands(commands);
		commands.add("exit");
		commands.add("help");
		System.out.format("commands: %s\n", commands.toString());
		
		Completer completer = new StringsCompleter(commands);
        LineReader reader = LineReaderBuilder.builder().completer(completer).build();
        
        for (String line=null;;) {
            line = null;
            try {
                line = reader.readLine(PROMPT).trim();
                if(line.equals("exit")) {
                	return;
                }else if(line.equals("help")){
                	System.out.format("commands: %s\n", commands.toString());
                } else {
                	String[] split = line.split(" ",2);
                	String methodToCall = split[0];
                		String reply = (String)this.getClass().getDeclaredMethod(methodToCall,String.class)
                				.invoke(this,(split.length>1)?split[1]:null);
                    	System.out.format("%s\n", reply);
                }
            }
            catch(Exception e) {
            	e.printStackTrace();
            }
        }
	}
    abstract protected void populateCommands(ArrayList<String> commands);

	static void makeCardWithCheckList() throws Exception{
    	String listid =  ta_.findListByName(HABITBOARDID,"PENDING");
    	JSONObject res = ta_.addCard(listid, new JSONObject()
    			.put("name", "testcard")
    			.put("checklist", new JSONArray()
    					.put("checkname")
    					.put("one")
    					.put("two")
    					.put("three")));
    	System.out.format("hi there with!\n %s\n",res.toString(2)); 
    }
	
	public App() {
		ta_ = new TrelloAssistant(secret.getString("trellokey"), 
				secret.getString("trellotoken"));
		ScriptEngineManager mgr = new ScriptEngineManager();
	    engine_ = mgr.getEngineByName("JavaScript");
	}

	static public void putlabel() throws Exception {
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
    static public void readcard() throws Exception {
    	JSONArray cards = ta_.getCardsInList(list.getId());
    	System.out.println("here go the cards");
    	System.out.println(cards.length());
    	for(Object o:cards) {
    		JSONObject obj = (JSONObject)o;
    		System.out.println(String.format("\t%s", obj.toString()));
    	}
    }
    static public void writecard() throws Exception {
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
    	String listid =  ta_.findListByName(HABITBOARDID,"TODO");
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
    	String listid =  ta_.findListByName(HABITBOARDID,"TODO");
    	System.out.format("id :%s\n", listid);
    	try (BufferedReader br = new BufferedReader(new FileReader(resFolder+"smalltodo.txt"))) {
    	    String line;
    	    while ((line = br.readLine()) != null) {
    	    	ta_.addCard(listid, new JSONObject().put("name", line));
    	    }
    	}
    }
}
