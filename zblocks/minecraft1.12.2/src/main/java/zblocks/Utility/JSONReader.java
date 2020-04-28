package zblocks.Utility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import zblocks.libs.org.json.simple.JSONObject;
import zblocks.libs.org.json.simple.parser.JSONParser;
import zblocks.libs.org.json.simple.parser.ParseException;

public class JSONReader {
	private ArrayList<String> sounds = new ArrayList<String>();

	public JSONReader(String file) {
		// JSON parser object to parse read file
		try {
			JSONParser jsonParser = new JSONParser();
			ClassLoader loader = JSONReader.class.getClassLoader();
			InputStreamReader is = new InputStreamReader(loader.getResourceAsStream("assets/zblock/sounds.json"));
			Object obj = jsonParser.parse(is);
			JSONObject sounds = (JSONObject) obj;
			for (Object jobj : sounds.entrySet()) {
				this.sounds.add(jobj.toString().split("=")[0]);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> getSounds() {
		return sounds;
	}
}
