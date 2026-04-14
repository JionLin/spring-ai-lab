## 1. Profile 閰嶇疆鎷嗗垎



- [x] 1.1 鏂板 `application-deepseek.yml`锛氬啓鍏?DeepSeek 鐨?`spring.ai.openai.base-url`銆乣chat.options.model`銆乣api-key` 鐜鍙橀噺鍗犱綅

- [x] 1.2 鏂板 `application-qwen.yml`锛氬啓鍏?DashScope **OpenAI 鍏煎** `base-url`锛堝惈 `compatible-mode/v1`锛夈€乣chat.options.model`锛堢ず渚嬬敤 `qwen-plus` 鎴栨枃妗ｆ帹鑽愬€煎苟鍔犳敞閲娿€屼互鎺у埗鍙颁负鍑嗐€嶏級銆乣api-key` 浣跨敤 `${DASHSCOPE_API_KEY:}` 鎴栫瓑浠峰崰浣?

- [x] 1.3 璋冩暣鏍?`application.yml`锛氱Щ闄や笌鍘傚晢缁戝畾鐨?`spring.ai.openai` 鏁村潡鑷?profile 鏂囦欢锛涗繚鐣?`lab.*` 绛夊叡浜」锛涘鍔?`spring.profiles.active` 榛樿鍊兼垨娓呮櫚娉ㄩ噴璇存槑 `SPRING_PROFILES_ACTIVE`



## 2. 娴嬭瘯涓庨粯璁よ涓?



- [x] 2.1 鏇存柊 `application-test.yml`锛堝強蹇呰鏃?`SpringAiLabApplicationTests` 鐨?`@ActiveProfiles`锛夛細淇濊瘉 `test` 涓嬭兘鍔犺浇瀹屾暣 LLM 鍗犱綅閰嶇疆锛堜緥濡?`spring.profiles.include: deepseek`锛?

- [ ] 2.2 鍦?JDK 17 涓嬫墽琛?`mvn test` 骞朵慨澶嶅洜閰嶇疆鎷嗗垎瀵艰嚧鐨勫惎鍔ㄩ棶棰橈紙褰撳墠 CI/鏈満涓?JDK 8 鏃舵棤娉曢獙璇侊紝璇锋湰鍦?JDK 17 閫氳繃鍚庡嬀閫夛級



## 3. 鏂囨。



- [x] 3.1 鏇存柊 `docs/技术栈说明.md`锛氭柊澧炪€屽ぇ妯″瀷澶?profile銆嶅皬鑺傦紝鍒楀嚭 `deepseek` / `qwen`銆佺幆澧冨彉閲忋€佺ず渚嬪惎鍔ㄦ柟寮?



## 4. 瑙勬牸楠屾敹



- [x] 4.1 瀵圭収 `openspec/changes/multi-profile-llm-config/specs/spring-llm-vendor-profiles/spec.md` 鍏ㄦ潯鑷閫氳繃




