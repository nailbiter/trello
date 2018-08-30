package com.github.nailbiter;

import java.io.FileReader;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.impl.TrelloImpl;
import com.julienvey.trello.impl.http.ApacheHttpClient;

import gnu.getopt.Getopt;

/**
 * Hello world!
 *
 */
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
		List<TList> lists = board.fetchLists();
    }
    static JSONObject getJSONObject(String fname) {
    	FileReader fr = null;
		JSONObject res = null;
//		System.out.println(String.format("%s.get(%s,%s)", StorageManager.class.getName(),name,register));
		try {
//			System.out.println("StorageManager got "+name);
//			String fname = LocalUtil.getJarFolder()+name+".json";
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
