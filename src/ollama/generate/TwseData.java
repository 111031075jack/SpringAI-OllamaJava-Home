package ollama.generate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TwseData {
	// 定義 ollama web api url
	private static final String GENERATE_WEB_API = "http://localhost:11434/api/generate";
	
	
	// 定義媒體(MediaType)型別為 json
	private static final MediaType JSON = MediaType.get("application/json;charset=utf-8");
	
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
		
		// 選擇模型(0: qwen3:0.6b, 1: phi3:latest, 2: qwen2.5:0.5b )
		String[] modelNames = {"qwen3:0.6b", "phi3:latest", "qwen2.5:0.5b"};
		System.out.print("模型選擇(0: qwen3:0.6b, 1: phi3:latest, 2: qwen2.5:0.5b) => ");
		int modelIndex = scanner.nextInt();
		String modelName = modelNames[modelIndex];
		
		/*
		String finaceDate = """
				有一檔股票財金資訊如下: 證券代號=2330 證券名稱=台積電 收盤價=1460.00 殖利率(%)=1.16 股利年度=113 本益比=25.94 股價淨值比=8.26 財報年/季=114/2
				""";
		*/
		
		System.out.print("請輸入股票代號(例如: 2330) => ");
		String symbol = scanner.next();
		String finaceDate = TwseDataDownload.getStringDataWithPrompt(symbol);
		
		// 問題文字
		System.out.println("請輸入問題(不要有空格) => ");
		String question = scanner.next();
		
		// question 前面要加上 finaceData <= prompt(提示語, 給 AI 的說明書, 讓 AI 更具有充分資料解決問題)
		String prompt = finaceDate + "請問: " + question; 
		// 消除換行符號
		prompt = prompt.replaceAll("\n", "");
		
		// 是否支援 stream
		Boolean supportSTREAM = true;
		
		//---------------------------------------------------------------------------------
		// 1. 建立 JSON 請求內容
		//---------------------------------------------------------------------------------
		String jsonBody = """
				{
					"model":"%s",
					"prompt":"%s",
					"stream":%b
				}
				""";
		jsonBody = String.format(jsonBody, modelName, prompt, supportSTREAM);
		System.out.printf("要發送的 JSON: %n%s%n", jsonBody);
		
		//---------------------------------------------------------------------------------
		// 2. 建立 OkHttpClient 實例(加入 Timeout)
		//---------------------------------------------------------------------------------
		OkHttpClient client = new OkHttpClient.Builder()
				.connectTimeout(60, TimeUnit.SECONDS)
				.readTimeout(60, TimeUnit.SECONDS)
				.writeTimeout(60, TimeUnit.SECONDS)
				.build();
		
		
		//---------------------------------------------------------------------------------
		// 3. 建立 RequestBody (將 JSON 字串包裝成請求主體)
		//---------------------------------------------------------------------------------
		RequestBody body = RequestBody.create(jsonBody, JSON);
		
		
		//---------------------------------------------------------------------------------
		// 4. 建立 Request 物件
		//---------------------------------------------------------------------------------
		Request request = new Request.Builder()
				.url(GENERATE_WEB_API)
				.post(body)
				.build();
		
		
		//---------------------------------------------------------------------------------
		// 5. 同步發送請求並取得回應
		//---------------------------------------------------------------------------------
		try(Response response = client.newCall(request).execute()){
			
			// 檢查回應是否成功 ?
			if(!response.isSuccessful()) {
				System.out.printf("請求失敗, HTTP 狀態碼: %n%s%n", response.code());
				return;
			}
			
			// 取得回應內容
			if(supportSTREAM) { // "stream":true
				try(InputStream is = response.body().byteStream(); // byte 單位
					InputStreamReader isr = new InputStreamReader(is, "UTF-8"); // char 單位
					BufferedReader reader = new BufferedReader(isr)){ // 可逐行讀取
				
					String line = null;
					Gson gson = new Gson();
					while((line = reader.readLine()) != null) {
						// System.out.println(line); // 逐行顯示
						// {"model":"qwen3:0.6b","created_at":"2025-11-07T05:54:34.9213502Z","response":"、","done":false}
						JsonObject obj = gson.fromJson(line, JsonObject.class);
						if(obj.get("response") == null) {
							continue;
						}
						String responseContent = obj.get("response").getAsString();
						System.out.print(responseContent);
					}
				}
				
			}else { // "stream":false
				String responseBody = response.body().string();
				System.out.printf("%n 回應碼: %s%n", response.code());
				System.out.printf("%n 完整回應: %s%n", responseBody);
			}
			scanner.close();
		}
	
	}
}
