package ollama.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ollama.chat.QueryChatExecutor.QueryCallback;

/**
 * 利用 QueryChatExecutor 完成本範例
 *  */
public class OllamaChatCLI {
	
	// 是否已完成
	private static boolean isComplete = false;
	 
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
		QueryChatExecutor executor = new QueryChatExecutor();
		
		// 選擇模型(1:llama3.1:8b, 2:qwen3:4b, 3:qwen3:0.6b)
		String[] modelNames = {"llama3.1:8b", "qwen3:4b", "qwen3:0.6b", "martain7r/finance-llama-8b:fp16"};
		System.out.print("請選擇模型(0:llama3.1:8b, 1:qwen3:4b, 2:qwen3:0.6b, 3:martain7r/finance-llama-8b:fp16) => ");
		
		int modelIndex = scanner.nextInt();
		String modelName = modelNames[modelIndex];
		scanner.nextLine();
		// 建立對話訊息列表(messages)
		List<Map<String, String>> messages = new ArrayList<>();
		
		System.out.println("對話開始, 輸入 q/quit 結束.");
		
		
		// 利用 QueryChatExecutor 與 AI 持續對話
		while(true) {
			isComplete = false;
			System.out.print("\n你說: ");
			String userInput = scanner.nextLine();
			
			if(userInput.equalsIgnoreCase("q") || userInput.equalsIgnoreCase("quit")){
				System.out.println("對話結束");
				break;
			}
			
			// 加入用戶文字到對話歷史中
			Map<String, String> userMessage = new HashMap<>();
			userMessage.put("role", "user");
			userMessage.put("content", userInput);
			messages.add(userMessage);
			
			System.out.print("Ollama 回覆: ");
			
			// 實現 QueryChatExecutor.QueryCallback
			QueryCallback queryCallback = new QueryCallback() {
				
				@Override
				public void onresponseChar(char ch) {
					// 逐字回覆
					System.out.print(ch);
					
				}
				
				@Override
				public void onHttpError(int statusCode) {
					System.err.println("\nHTTP 請求失敗, HTTP 狀態碼: " + statusCode);	
					
				}
				
				@Override
				public void onError(String message) {
					System.err.println("\n執行錯誤: " + message);
					
				}
				
				@Override
				public void onComplete() {
					isComplete = true;
					System.out.println("\n查詢完成");
					
				}
			};
			
			// 呼叫 QueryChatExecutor
			executor.execute(modelName, messages, queryCallback);
			
			// 等待 isComplete = true
			// 每隔一秒鐘檢查一次
			while(!isComplete) {
				try {
					Thread.sleep(1000);
				}catch (Exception e) {
					
				}
			}
			
		}
		
		
		scanner.close();
		
	}
	
}