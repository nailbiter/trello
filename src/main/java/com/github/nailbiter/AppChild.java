package com.github.nailbiter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.nailbiter.util.TableBuilder;

public class AppChild extends App {
	public AppChild() {
		super();
	}
	protected void populateCommands(ArrayList<String> commands) {
		commands.clear();
		commands.add("makearchived");
		commands.add("addcard");
		commands.add("countcard");
		commands.add("getactions");
		commands.add("removecards");
		commands.add("movetoeasytasks");
	}
	public String movetoeasytasks(String rem) throws Exception {
		String oldlistid = ta_.findListByName(HABITBOARDID, "todo"),
				newlistid = ta_.findListByName(INBOXBOARDID, "sweet tasks");
		System.err.format("old=%s\nnew=%s\n", oldlistid,newlistid);
		JSONArray arr = ta_.getCardsInList(oldlistid);
		String cardid = arr.getJSONObject(arr.length()-1).getString("id");
		ta_.moveCard(cardid, newlistid,"top");
		
		return arr.getJSONObject(arr.length()-1).toString();
	}
	public String getactions(String rem) throws Exception {
		String listId = ta_.findListByName(HABITBOARDID, "TODO");
		JSONObject obj = null;
		if(!(rem==null || rem.isEmpty())) {
			obj = new JSONObject(rem);
		}
		final JSONObject filter = obj;
		JSONArray arr = ta_.getListActions(listId,obj);
		if(obj!=null) {
			ArrayList<JSONObject> coll = new ArrayList<JSONObject>();
			for(int i = 0; i < arr.length(); i++) {
				coll.add(arr.getJSONObject(i));
			}
			for(final String key:filter.keySet()) {
				coll.removeIf(new Predicate<JSONObject>() {
					@Override
					public boolean test(JSONObject t) {
						return !(t.getString(key).startsWith(filter.getString(key)));
					}
				});
			}
			arr = new JSONArray(coll);
		}
		return String.format("got: %s\n", arr.toString(2));
	}
	public String countcard(String rem) throws Exception {
		String listId = ta_.findListByName(HABITBOARDID, "TODO");
		JSONArray array = ta_.getCardsInList(listId);
		
		String regex = rem;
		
		int count = 0;
		Hashtable<String,Integer> counthash = new Hashtable<String,Integer>(); 
		for(Object o : array) {
			JSONObject obj = (JSONObject)o;
			String name = obj.getString("name");
			if(Pattern.matches(regex, name)) {
				if(!counthash.containsKey(name)) {
					counthash.put(name, 0);
				}
				counthash.put(name, counthash.get(name)+1);
				count++;
			}
		}
		
		TableBuilder tb = new TableBuilder();
		tb.addNewlineAndTokens("name", "count");
		for(String name : counthash.keySet()) {
			tb.newRow();
			tb.addToken(name);
			tb.addToken(counthash.get(name));
		}
		return String.format("%scounted %d (out of %d) cards with name \"%s\"",
				tb.toString(),
				count,array.length(),regex);
		}
	public String removecards(String rem) throws Exception {
		String[] split = rem.split(" ",2);
		String listId = ta_.findListByName(HABITBOARDID, "TODO");
		int count = (int)engine_.eval(split[0]);
		String name = split[1];
		JSONArray array = ta_.getCardsInList(listId);
		
		int i = 0;
		for(Object o : array) {
			JSONObject obj = (JSONObject)o;
			String cardName = obj.getString("name");
			if(cardName.equals(name)) {
				if(i >= count) break;
				ta_.removeCard(obj.getString("id"));
				System.out.format("removed %s\n", obj.getString("id"));
				i++;
			}
		}
		
		return String.format("removed \"%s\" %d times", name,i);
	}
	public String addcard(String rem) throws Exception {
		String[] split = rem.split(" ",2);
		String listId = ta_.findListByName(HABITBOARDID, "TODO");
		JSONObject obj = new JSONObject()
				.put("name", split[1])
				.put("count", (int)engine_.eval(split[0])),
				clone = new JSONObject(obj.toString());
		
		clone.remove("count");
		for(int i = 0, count = obj.getInt("count"); i < count;i++) {
			ta_.addCard(listId, clone);
		}
		
		return String.format("added \"%s\" %d times", obj.toString(),obj.getInt("count"));
	}
	public String makearchived(String rem) throws Exception {
		String listId = ta_.findListByName(HABITBOARDID, "todo");
		
		JSONObject res = ta_.addCard(listId, new JSONObject().put("name", rem));
		String[] split = res.getString("shortUrl").split("/");
		String id = split[4];
		System.err.format("id: %s\n", id);
		ta_.archiveCard(id);
		return String.format("%s", res.getString("shortUrl"));
	}
}
