package ollama.chat;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ModelTags {

	private static final String MODEL_API_TAGS = "http://localhost:11434/api/tags";
	OkHttpClient client = new OkHttpClient();
	Gson gson = new Gson();
	public String[] choose() {
		
		String[] defaultModels = {
			"llama3.1:8b", "qwen3:4b", "qwen3:0.6b", "martain7r/finance-llama-8b:fp16"
        };
		
		Request request = new Request.Builder()
				.url(MODEL_API_TAGS)
				.get()
				.build();
		
		try(Response response = client.newCall(request).execute()){
			if(!response.isSuccessful()) {
				System.err.println("無法從 Ollama 取得模型列表。HTTP 狀態碼: " + response.code());
				return defaultModels;
			}
			
			String jsonResponse = response.body().string();
			
			JsonObject obj = gson.fromJson(jsonResponse, JsonObject.class);
			
			JsonArray modelsArray = obj.getAsJsonArray("models");
			
			List<String> modelNames = new ArrayList<>();
			
			for(int i=0;i<modelsArray.size();i++) {
				JsonObject modelObj = modelsArray.get(i).getAsJsonObject();
				String name = modelObj.get("name").getAsString();
				
				if(name!=null && !name.isEmpty()) {
					modelNames.add(name);
				}
				if (modelNames.isEmpty()) {
	                System.err.println("Ollama API 回傳的模型列表為空。");
	               
				}
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return defaultModels;
		
	}
	
}
