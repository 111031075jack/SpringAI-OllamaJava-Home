package ollama.generate;

import ollama.generate.QueryExecutor.QueryCallback;

public class QueryExecutorTest {

	public static void main(String[] args) {
		// 基本財經參數
		String modelName = "qwen3:0.6b";
		String symbol = "2330";
		
		//QueryExecutor 參數
		String prompt = TwseDataDownload.getStringDataWithPrompt(symbol);
		String fullPrompt = prompt + "請問台積電現在適合買進嗎";
		// CallBack 參數
		QueryCallback callback = new QueryCallback() {
			
			@Override
			public void onResponseChar(char ch) {
				System.out.print(ch);
				
			}
			
			@Override
			public void onHttpError(int code) {
				System.err.println("\nHttp 請求失敗, HTTP 狀態碼: " + code);
				
			}
			
			@Override
			public void onError(String message) {
				System.err.println("\n執行錯誤: " + message);
				
			}
			
			@Override
			public void complete() {
				System.out.println("\n查詢完成");
				
			}
		};
		
		QueryExecutor executor = new QueryExecutor();
		executor.execute(modelName, fullPrompt, callback);
		
		
	}
	
}
