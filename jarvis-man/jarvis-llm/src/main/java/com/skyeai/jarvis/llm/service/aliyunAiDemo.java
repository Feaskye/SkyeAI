package com.skyeai.jarvis.llm.service;


import java.util.Arrays;
import java.lang.System;
import java.util.Collections;
import java.util.HashMap;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;


public class aliyunAiDemo {

    public static class Main {
        //  若使用新加坡地域的模型，请释放下列注释
        //  static {Constants.baseHttpApiUrl="https://dashscope-intl.aliyuncs.com/api/v1";}
        public static GenerationResult callWithMessage() throws ApiException, NoApiKeyException, InputRequiredException {
            Generation gen = new Generation();
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("You are a helpful assistant.")
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content("你是谁？")
                    .build();
            GenerationParam param = GenerationParam.builder()
                    // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：.apiKey("sk-xxx")
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    // 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                    .model("qwen-max")
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();
            return gen.call(param);
        }
        public static void main(String[] args) {
            try {
                GenerationResult result = callWithMessage();
                System.out.println(result.getOutput().getChoices().get(0).getMessage().getContent());
            } catch (ApiException | NoApiKeyException | InputRequiredException e) {
                System.err.println("错误信息："+e.getMessage());
                System.out.println("请参考文档：https://help.aliyun.com/zh/model-studio/developer-reference/error-code");
            }
            System.exit(0);
        }



        public static void simpleMultiModalImageCall()
                throws ApiException, NoApiKeyException, UploadFileException {
            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("base64_image", "xx"),
                            Collections.singletonMap("text", "图中描绘的是什么景象?"))).build();
            MultiModalConversationParam param = MultiModalConversationParam.builder()

                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .model("qwen3-vl-plus")  // 此处以qwen3-vl-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
                    .messages(Arrays.asList(userMessage))
                    .build();
            MultiModalConversationResult result = conv.call(param);
            System.out.println(result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text"));
            System.out.println(result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text"));
        }


        public static void simpleMultiModalVoiceCall(String localPath)
                throws ApiException, NoApiKeyException, UploadFileException {
            String filePath = "file://"+localPath;
            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                    .content(Arrays.asList(new HashMap<String, Object>()
                                           {{
                                               put("video", filePath);// fps参数控制视频抽帧数量，表示每隔1/fps 秒抽取一帧
                                               put("fps", 2);
                                           }},
                            new HashMap<String, Object>(){{put("text", "这段视频描绘的是什么景象？");}})).build();
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    // 各地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .model("qwen3-vl-plus")
                    .messages(Arrays.asList(userMessage))
                    .build();
            MultiModalConversationResult result = conv.call(param);
            System.out.println("输出结果为：\n" + result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text"));
        }
    }
}
