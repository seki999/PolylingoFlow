package com.polylingoflow.translate;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 使用REST API（例如，Google Translate, LibreTranslate）进行文本翻译的服务。
 * 使用OkHttp进行网络请求，使用Gson进行JSON解析。
 */
public class TranslatorService {

    private static final Logger log = LoggerFactory.getLogger(TranslatorService.class);

    // 配置一个带有超时设置的OkHttpClient，以提高健壮性
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    // LibreTranslate的公共API端点
    private static final String TRANSLATE_API_URL = "https://libretranslate.de/translate";

    /**
     * 用于解析LibreTranslate API响应的内部数据类。
     * 使用Java record可以简洁地定义一个不可变的数据载体。
     */
    private record TranslationResponse(String translatedText) {}

    /**
     * 异步翻译文本。
     *
     * @param text       要翻译的文本。
     * @param sourceLang 源语言代码（例如, "en"）。
     * @param targetLang 目标语言代码（例如, "zh"）。
     * @return 一个CompletableFuture，它将以翻译后的文本完成。如果发生错误，它将以异常完成。
     */
    public CompletableFuture<String> translateAsync(String text, String sourceLang, String targetLang) {
        CompletableFuture<String> future = new CompletableFuture<>();

        if (text == null || text.isBlank()) {
            log.warn("翻译文本为空，提前返回。");
            future.complete(""); // 对于空输入，直接返回空字符串
            return future;
        }

        // 为LibreTranslate API构建请求体
        RequestBody body = new FormBody.Builder()
                .add("q", text)
                .add("source", sourceLang)
                .add("target", targetLang)
                .add("format", "text") // 指定格式为纯文本
                .build();

        Request request = new Request.Builder()
                .url(TRANSLATE_API_URL)
                .post(body)
                .build();

        log.debug("向 {} 发送翻译请求...", TRANSLATE_API_URL);

        // 使用OkHttp的异步`enqueue`方法执行请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 网络层面的失败 (例如, 无法连接服务器)
                log.error("翻译请求失败: {}", e.getMessage());
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                // 使用try-with-resources确保响应体被关闭，避免资源泄露
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        // API返回了非200的状态码 (例如, 4xx, 5xx)
                        String errorBody = responseBody != null ? responseBody.string() : "无响应体";
                        IOException e = new IOException("服务器返回意外的响应码: " + response.code() + ", 响应体: " + errorBody);
                        log.error("翻译API返回错误: {}", e.getMessage());
                        future.completeExceptionally(e);
                        return;
                    }

                    if (responseBody == null) {
                        IOException e = new IOException("服务器返回了空的响应体");
                        log.error(e.getMessage());
                        future.completeExceptionally(e);
                        return;
                    }

                    // 解析JSON响应
                    String jsonString = responseBody.string();
                    TranslationResponse translationResponse = gson.fromJson(jsonString, TranslationResponse.class);

                    String translatedText = Objects.requireNonNull(translationResponse.translatedText, "解析出的翻译文本为null");
                    log.info("成功翻译文本。");
                    future.complete(translatedText);

                } catch (IOException | JsonSyntaxException | NullPointerException e) {
                    // 处理I/O错误、JSON解析错误或空指针
                    log.error("处理翻译响应时出错: {}", e.getMessage());
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }
}