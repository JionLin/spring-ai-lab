## 1. 閰嶇疆涓庡鎴风楠ㄦ灦



- [x] 1.1 鏂板 `@ConfigurationProperties`锛堝缓璁墠缂€ `lab.external.post`锛夋弿杩板 profile锛歚base-url`銆乣path`銆佸彲閫?API Key 澶村悕涓庡€肩粦瀹氾紱鎻愪緵 `application.yml` 绀轰緥鍧楋紙鍊间负鍗犱綅绗︽垨绌猴級

- [x] 1.2 鏂板 `ExternalJsonPostClient`锛堟垨绛変环鍛藉悕锛夋帴鍙ｄ笌 **RestClient** 瀹炵幇锛歅OST銆乣application/json`銆佽繛鎺?璇昏秴鏃躲€佹寜 profile 鎷?URL銆?01/403/5xx/瓒呮椂鏄犲皠涓哄彈妫€澶辫触鎴栫粺涓€澶辫触绫诲瀷

- [x] 1.3 娉ㄥ唽閰嶇疆鎵弿锛坄@EnableConfigurationProperties` 鎴?`@Configuration` 鍐?`@Bean`锛?



## 2. Tool Bean 涓庡璇濆叆鍙?



- [x] 2.1 灏嗗師 `WeatherTools` 閲嶆瀯涓?`@Component`锛堟垨鎷嗗垎涓哄涓?Bean锛夛紝娉ㄥ叆 `ExternalJsonPostClient` 涓庨厤缃睘鎬э紱鑷冲皯 **涓や釜** `@Tool` 鏂规硶锛屽搴斾袱涓笉鍚?JSON 璇锋眰浣?DTO 涓庝袱涓?profile

- [x] 2.2 淇敼 `ChatController`锛氭瀯閫犲櫒娉ㄥ叆 Tool Bean锛沗ChatClient.tools(...)` 浣跨敤 Bean锛?*鍒犻櫎** `new WeatherTools()`

- [x] 2.3 涓烘瘡涓?`@Tool` 瀹炵幇鍝嶅簲瑙ｆ瀽 + **鎽樿/鍏抽敭瀛楁**杩斿洖锛涘け璐ヨ矾寰勮繑鍥?**鐭敊璇枃妗?*锛堟棤鍫嗘爤銆佹棤瀵嗛挜锛?



## 3. 娴嬭瘯



- [x] 3.1 娣诲姞鍑虹珯 Mock锛?*MockWebServer** 鎴?`MockRestServiceServer`锛夛細瑕嗙洊鎴愬姛 JSON銆?xx銆佽秴鏃舵垨杩炴帴澶辫触涔嬩竴锛涙柇瑷€ Tool 鎴?Client 琛屼负涓庨敊璇憳瑕佺瓥鐣?

- [ ] 3.2 `mvn test` 鍦?JDK 17 涓嬮€氳繃锛堣鍦ㄦ湰鍦?JDK 17 鎵ц `mvn test` 鍚庡嬀閫夛級



## 4. 鏂囨。锛堝彲閫変絾鎺ㄨ崘锛?



- [x] 4.1 鏇存柊 `docs/技术栈说明.md`锛氬鍔犮€孴ool + 缁熶竴 POST JSON 瀹㈡埛绔€嶅皬鑺傘€侀厤缃墠缂€璇存槑銆佷笌 `tech-onboarding-docs` 瑙勬牸涓嶅啿绐佺殑绠€杩?



## 5. 瑙勬牸楠屾敹



- [x] 5.1 瀵圭収 `openspec/changes/tools-post-json-external-api/specs/ai-tools-http-post-json/spec.md` 鍏ㄦ潯鑷閫氳繃




