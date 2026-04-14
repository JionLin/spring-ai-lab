## Why

褰撳墠 `spring.ai.openai` 涓庡崟涓€鍘傚晢锛圖eepSeek锛夊啓鍦ㄥ悓涓€ `application.yml` 涓紝鍒囨崲鑷抽樋閲屼簯閫氫箟绛?**OpenAI 鍏煎** 缃戝叧鏃堕渶鎵嬪伐鏀瑰悓涓€鏂囦欢锛屾槗璇敼銆侀毦瀵规瘮銆傞渶瑕侀€氳繃 **Spring Boot 澶?profile 鍒嗘枃浠?* 鍥哄寲鍚勫巶鍟嗙殑 `base-url` / `model` 缁勫悎锛屽苟鐢ㄧ幆澧冨彉閲忔敞鍏ュ瘑閽ワ紝闄嶄綆鍒囨崲鎴愭湰涓庢彁浜ら闄┿€?

## What Changes

- 灏?**澶фā鍨嬬浉鍏?* 閰嶇疆浠庢牴 `application.yml` 鎷嗗嚭锛屾柊澧炶嚦灏戜袱涓?profile 涓撶敤鏂囦欢锛堜緥濡?**`deepseek`**銆?*`qwen`**锛夛紝鍚勮嚜鍖呭惈瀹屾暣鐨?`spring.ai.openai.*` 鐗囨锛堜粛浣跨敤鐜版湁 `spring-ai-starter-model-openai`锛屼笉寮哄埗鏂板 Maven 渚濊禆锛夈€?
- 鏍?`application.yml` 淇濈暀搴旂敤鍏叡椤癸紙濡?`spring.application.name`銆乣lab.external.post` 绛夛級锛屽苟 **鏂囨。鍖?* 濡備綍閫氳繃 `spring.profiles.active` / `SPRING_PROFILES_ACTIVE` 閫夋嫨鍘傚晢 profile锛涘彲閫夋彁渚涢粯璁ゆ縺娲?profile 鐨勭ず渚嬶紙**BREAKING**锛氳嫢榛樿浠庛€屼粎鏍?yml銆嶆敼涓恒€屽繀椤婚€?profile銆嶏紝鏈湴鍚姩鏂瑰紡浼氬彉鍖栵紝椤诲湪 `design.md` 涓庢枃妗ｄ腑鍐欐槑锛夈€?
- **娴嬭瘯**锛歚test` profile 涓嬩笂涓嬫枃鍔犺浇 SHALL 浠嶆垚鍔燂紱閫氳繃 `application-test.yml` 涓?鎴?`@ActiveProfiles` 缁勫悎淇濊瘉 CI 涓嶄緷璧栫湡瀹炲缃戝瘑閽ャ€?
- **鏂囨。**锛氭洿鏂?`docs/技术栈说明.md` 璇存槑澶?profile 涓庡垏鎹㈡柟寮忥紙涓?`tech-onboarding-docs` 涓嶅啿绐佺殑澧為噺鎻忚堪锛夈€?

## Capabilities

### New Capabilities

- `spring-llm-vendor-profiles`锛氬畾涔夊熀浜?Spring profile 鎷嗗垎 OpenAI 鍏煎澶фā鍨嬮厤缃殑鏂囦欢甯冨眬銆佹縺娲绘柟寮忎笌瀵嗛挜绾︽潫銆?

### Modified Capabilities

- 锛堟棤锛氫笉淇敼 `openspec/specs/tech-onboarding-docs/spec.md` 涓棦鏈?Requirement 鏂囨湰锛涙枃妗ｅ閲忕敱鏈彉鏇?`tasks.md` 璺熻釜銆傦級

## Impact

- **閰嶇疆**锛氭柊澧?`application-<profile>.yml`锛堟垨 `.yaml`锛夛紝璋冩暣鏍?`application.yml`銆乣application-test.yml`銆?
- **婧愮爜**锛氶€氬父 **鏃犻渶** 淇敼 `ChatController` / `ChatClient`锛堜粛娉ㄥ叆鍚屼竴 `ChatModel` Bean锛夛紱鑻ユ祴璇曠被闇€鏄惧紡 profile 鍒欏皬骞呰皟鏁存祴璇曟敞瑙ｆ垨閰嶇疆銆?
- **渚濊禆**锛氭棤寮哄埗 `pom.xml` 鍙樻洿銆?

