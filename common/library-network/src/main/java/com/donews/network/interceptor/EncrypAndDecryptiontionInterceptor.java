/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.donews.network.interceptor;


import android.util.Log;

import com.donews.ads.mediation.v2.encrypt.DoNewsEncryptUtils;
import com.donews.network.BuildConfig;
import com.donews.network.model.HttpHeaders;
import com.donews.network.utils.HttpLog;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * <p>描述：加密解密的拦截器，针对传输层 </p>
 * 作者： lcl<br/>
 * AES :对此加密(加密内容)
 * RSA :非对此加密
 * <p>
 * https://lottery.
 */
public class EncrypAndDecryptiontionInterceptor implements Interceptor {

    /**
     * 密码偏移量
     */
    final String vector;
    /**
     * 随机生成动态密码
     */
    final String dynamicPassword;

    //需要拦截的URLs
    private static List<String> interceptorUrls = new ArrayList<String>() {
        {
            add(BuildConfig.LOGIN_URL + "upload"); //忽略掉上传文件接口
        }
    };

    public EncrypAndDecryptiontionInterceptor() {
        //生成偏移向量
        this.vector = DoNewsEncryptUtils.generateVector();
        //生成动态密码
        this.dynamicPassword = DoNewsEncryptUtils.generateDynamicPassword();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        MediaType mediaType = null;
        try {
            boolean isEncyrp = true;//是否需要加密
            //此链接需要加密
            for (int i = 0; i < interceptorUrls.size(); i++) {
                if(interceptorUrls.get(i).startsWith(chain.request().url().toString())){
                    isEncyrp = false;
                    break;
                }
            }
            if(isEncyrp) {
                String encHead = DoNewsEncryptUtils.generateEncryptHeader(dynamicPassword, vector);
                Log.v("http_encryp", "传输加密已生效:" + HttpHeaders.HEAD_KEY_ENCRYP_AND_DECRYP + "->" + encHead
                + "->" + chain.request().url().toString());
                builder.addHeader(HttpHeaders.HEAD_KEY_ENCRYP_AND_DECRYP,
                        encHead);
                String reqJson = parseParams(chain.request());
                if (reqJson != null && reqJson.length() > 0) {
                    mediaType = chain.request().body().contentType();
                    builder.method(
                            chain.request().method(),
                            RequestBody.create(mediaType,
                                    DoNewsEncryptUtils.encrypt(reqJson, dynamicPassword, vector)));
                }
                Response originalResponse = chain.proceed(builder.build());
                if (originalResponse.body() != null &&
                        originalResponse.body().contentLength() > 0) {
                    originalResponse = originalResponse.newBuilder()
                            .body(ResponseBody.create(
                                    DoNewsEncryptUtils.decrypt(
                                            originalResponse.body().bytes(), dynamicPassword, vector),
                                    mediaType))
                            .build();
                }
                return originalResponse;
            }else{
                //不需要加密。直接执行原始方法
                Log.v("http_encryp", "加密已经忽略了。不加密此请求");
                return chain.proceed(chain.request());
            }
        } catch (Exception e) {
            HttpLog.e(e);
            throw e;
        }
    }

    //检查是否需要加密
    private boolean checkIsEncryp(Request req) {
        return "get".equals(req.method().toLowerCase()) ||
                req.body().contentType().toString()
                        .startsWith(MediaType.get("application/json").toString());
    }

    //是否需要拦截的URL
    private boolean checkIsInterceptUrl(String reqUrl) {
        return true;
//        for (String interceptorUrl : interceptorUrls) {
//            if (reqUrl.startsWith(interceptorUrl)) {
//                return true;
//            }
//        }
//        return false;
    }

    //转换参数
    public String parseParams(Request request) throws IOException {
        try {
            RequestBody body = request.newBuilder().build().body();
            if (body == null) {
                return "";
            }
            Buffer requestbuffer = new Buffer();
            body.writeTo(requestbuffer);
            Charset charset = Charset.forName("UTF-8");
            MediaType contentType = body.contentType();
            if (contentType != null) {
                charset = contentType.charset(charset);
            }
            return requestbuffer.readString(charset);
        } catch (IOException e) {
            throw e;
        }
    }
}
