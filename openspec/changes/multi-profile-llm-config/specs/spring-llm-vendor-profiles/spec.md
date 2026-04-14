## ADDED Requirements

### Requirement: 鎸夊巶鍟嗘媶鍒嗙殑 profile 閰嶇疆鏂囦欢瀛樺湪

浠撳簱 SHALL 鍦?`src/main/resources/` 涓嬫彁渚涜嚦灏戜袱涓?Spring Boot 閰嶇疆鏂囦欢锛屾枃浠跺悕鍒嗗埆瀵瑰簲 **`deepseek`** 涓?**`qwen`** 鍘傚晢缁村害锛堢害瀹氾細`application-deepseek.yml` 涓?`application-qwen.yml`锛屾垨鍚岀瓑璇箟鐨?`.yaml` 鎵╁睍鍚嶄簩閫変竴涓斿叏浠撳簱涓€鑷达級锛岀敤浜庢壙杞?**浜掓枼** 鐨?`spring.ai.openai` 閰嶇疆鐗囨锛坄base-url`銆乣chat.options.model` 鍙婁笌瀵嗛挜鐩稿叧鐨勫崰浣嶅睘鎬э級锛屼笖 SHALL NOT 鍦ㄥ悓涓€娈佃惤閲嶅瀹氫箟鍐茬獊鐨?`spring.ai.openai.chat.options.model` 浜庢牴 `application.yml` 涓?profile 鏂囦欢瀵艰嚧鍚堝苟姝т箟锛堟牴鏂囦欢搴旂Щ闄ゆ垨涓嶅啀鍖呭惈瀹屾暣鍘傚晢鍧楋級銆?

#### Scenario: 鍙彂鐜?profile 鏂囦欢

- **WHEN** 璐＄尞鑰呮祻瑙?`src/main/resources/`
- **THEN** 鍏?SHALL 鑳芥壘鍒颁笂杩颁袱涓巶鍟?profile 鏂囦欢涔嬩竴瀵癸紝涓旀瘡涓枃浠?SHALL 鍖呭惈閿墠缂€ `spring.ai.openai` 涓嬬殑杩炴帴涓庢ā鍨嬮厤缃?

### Requirement: 鏍归厤缃粎淇濈暀鍏变韩椤瑰苟澹版槑婵€娲绘柟寮?

鏍?`application.yml` SHALL 淇濈暀涓庡巶鍟嗘棤鍏崇殑鍏变韩閰嶇疆锛堣嚦灏戝寘鍚?`spring.application.name`锛夛紱瀵逛簬 LLM锛孲HALL 閫氳繃娉ㄩ噴鎴栧睘鎬ц鏄庡浣曡缃?**`spring.profiles.active`** / 鐜鍙橀噺 **`SPRING_PROFILES_ACTIVE`** 浠ラ€夋嫨 `deepseek` 鎴?`qwen`锛涜嫢椤圭洰閫夋嫨鎻愪緵榛樿婵€娲?profile锛孲HALL 鍦ㄦ枃妗ｄ腑璇存槑璇ラ粯璁よ涓恒€?

#### Scenario: 婵€娲昏鏄庡彲璇?

- **WHEN** 闃呰鑰呮墦寮€鏍?`application.yml` 鎴?`docs/技术栈说明.md`
- **THEN** 鍏?SHALL 鑳借幏寰椾竴鍙ヤ互涓婂叧浜庛€屽浣曞惎鐢?deepseek / qwen profile銆嶇殑璇存槑

### Requirement: 瀵嗛挜涓嶅緱纭紪鐮佷簬璧勬簮鏂囦欢

鎵€鏈夋彁浜ゅ埌浠撳簱鐨?YAML SHALL NOT 鍖呭惈鐪熷疄澶фā鍨?API Key銆丏ashScope 瀵嗛挜鎴栦笌鐢熶骇绛変环鐨勬槑鏂?token锛汼HALL 浣跨敤鍗犱綅绗︽垨鐜鍙橀噺寮曠敤锛堜緥濡?`${SPRING_AI_OPENAI_API_KEY}`銆乣${DASHSCOPE_API_KEY}` 绛夌敱瀹炵幇閫夊畾鐨勪竴绉嶆垨涓ょ锛岄』鍦?`docs/技术栈说明.md` 涓垪鍑猴級銆?

#### Scenario: 鏃犳槑鏂囧瘑閽?

- **WHEN** 瀹℃煡鑰呭 `src/main/resources/**/*.yml` 鎵ц甯歌瀵嗛挜褰㈡€佹绱?
- **THEN** 鍏?SHALL NOT 鍙戠幇鍙鐢ㄧ殑 `sk-` 闀夸覆绛夌湡瀹炲嚟鎹紙娴嬭瘯涓撶敤 dummy 鍊艰嫢瀛樺湪锛孲HALL 浠呭嚭鐜板湪 `application-test.yml` 鎴栨祴璇曡祫婧愪腑涓旀槑鏄句负鍋囧瘑閽ワ級

### Requirement: qwen profile 浣跨敤 DashScope OpenAI 鍏煎绔偣褰㈡€?

鍦?**`qwen`** profile 瀵瑰簲鏂囦欢涓紝`spring.ai.openai.base-url` SHALL 璁剧疆涓洪樋閲屼簯 Model Studio 鏂囨。鎵€鎻忚堪鐨?**OpenAI 鍏煎妯″紡** 鍩哄湴鍧€褰㈡€佷箣涓€锛堥』鍖呭惈 `compatible-mode` 璺緞娈靛強鐗堟湰鍚庣紑濡?`/v1`锛屽叿浣撳湴鍩?URL 浠ュ疄鐜颁负鍑嗕笖涓庢敞閲婁腑鐨勫畼鏂硅鏄庝竴鑷达級锛沗spring.ai.openai.chat.options.model` SHALL 璁剧疆涓洪€氫箟绯诲垪鍦ㄥ吋瀹规帴鍙ｄ笅鍙敤鐨勬ā鍨嬫爣璇嗕箣涓€锛屽苟 SHALL 鍦ㄦ梺娉ㄤ腑鎻愮ず缁存姢鑰呬互鎺у埗鍙版ā鍨嬪垪琛ㄤ负鍑嗐€?

#### Scenario: 鍏煎妯″紡鍙鲸璇?

- **WHEN** 瀹℃煡鑰呮墦寮€ `application-qwen.yml`
- **THEN** 鍏?SHALL 鐪嬪埌 `base-url` 鎸囧悜 DashScope **compatible-mode** 褰㈡€侊紝涓?SHALL 鐪嬪埌妯″瀷鍚嶄笌娉ㄩ噴鎻愮ず

### Requirement: 娴嬭瘯涓婁笅鏂囧湪 test profile 涓嬩粛鍙惎鍔?

鍦?**`test`** profile锛堜緥濡?`application-test.yml`锛変笅锛屽簲鐢ㄤ笂涓嬫枃鍔犺浇娴嬭瘯 SHALL 鏃犻渶璁块棶鐪熷疄澶栫綉鍗冲彲閫氳繃锛涘叾 SHALL 閫氳繃 `spring.profiles.include`銆侀噸澶嶆渶灏?`spring.ai.openai.api-key` 鍗犱綅銆佹垨 `@ActiveProfiles` 缁勫悎涔嬩竴锛屼繚璇?`ChatModel` 鑷姩閰嶇疆鎵€闇€灞炴€у畬鏁淬€?

#### Scenario: 娴嬭瘯缁胯壊

- **WHEN** 鍦?JDK 17 鐜涓嬫墽琛?`mvn test` 涓旈粯璁ゆ縺娲?`test` 鐩稿叧 profile
- **THEN** 鏋勫缓 SHALL 涓嶅洜缂哄け `spring.ai.openai.api-key` 鎴?`base-url` 瀵艰嚧涓婁笅鏂囧惎鍔ㄥけ璐?

