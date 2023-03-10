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

package com.donews.network.request;

import android.content.Context;
import android.text.TextUtils;

import com.donews.network.BuildConfig;
import com.donews.network.EasyHttp;
import com.donews.network.api.ApiService;
import com.donews.network.cache.RxCache;
import com.donews.network.cache.converter.IDiskConverter;
import com.donews.network.cache.model.CacheMode;
import com.donews.network.https.HttpsUtils;
import com.donews.network.interceptor.BaseDynamicInterceptor;
import com.donews.network.interceptor.CacheInterceptor;
import com.donews.network.interceptor.CacheInterceptorOffline;
import com.donews.network.interceptor.EncrypAndDecryptiontionInterceptor;
import com.donews.network.interceptor.HeadersInterceptor;
import com.donews.network.interceptor.LoginHeaderInterceptor;
import com.donews.network.interceptor.NoCacheInterceptor;
import com.donews.network.model.HttpHeaders;
import com.donews.network.model.HttpParams;
import com.donews.network.utils.HttpLog;
import com.donews.network.utils.RxUtil;
import com.donews.network.utils.Utils;
import com.donews.utilslibrary.utils.DeviceUtils;
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor;

import java.io.File;
import java.io.InputStream;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import okhttp3.Cache;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

import static com.donews.network.EasyHttp.getRetrofitBuilder;
import static com.donews.network.EasyHttp.getRxCache;

/**
 * <p>??????????????????????????????</p>
 * ????????? created by honeylife<br>
 * ????????? 2017/4/28 17:19 <br>
 * ????????? v1.0<br>
 */
@SuppressWarnings(value = {"unchecked", "deprecation"})
public abstract class BaseRequest<R extends BaseRequest> {
    protected Cache cache = null;
    protected CacheMode cacheMode = CacheMode.NO_CACHE;                    //???????????????
    protected long cacheTime = -1;                                         //????????????
    protected String cacheKey;                                             //??????Key
    protected IDiskConverter diskConverter;                                //??????Rxcache???????????????
    protected String baseUrl;                                              //BaseUrl
    protected String url;                                                  //??????url
    protected long readTimeOut;                                            //?????????
    protected long writeTimeOut;                                           //?????????
    protected long connectTimeout;                                         //????????????
    protected int retryCount;                                              //??????????????????3???
    protected int retryDelay;                                              //??????xxms??????
    protected int retryIncreaseDelay;                                      //????????????
    protected boolean isSyncRequest;                                       //?????????????????????
    protected List<Cookie> cookies = new ArrayList<>();                    //?????????????????????Cookie
    protected final List<Interceptor> networkInterceptors = new ArrayList<>();
    protected HttpHeaders headers = new HttpHeaders();                     //?????????header
    protected HttpParams params = new HttpParams();                        //?????????param
    protected Retrofit retrofit;
    protected RxCache rxCache;                                             //rxcache??????
    protected ApiService apiManager;                                       //????????????api??????
    protected OkHttpClient okHttpClient;
    protected Context context;
    private boolean sign = false;                                          //??????????????????
    private boolean timeStamp = false;                                     //???????????????????????????
    private boolean accessToken = false;                                   //??????????????????token
    protected HttpUrl httpUrl;
    protected Proxy proxy;
    protected HttpsUtils.SSLParams sslParams;
    protected HostnameVerifier hostnameVerifier;
    protected List<Converter.Factory> converterFactories = new ArrayList<>();
    protected List<CallAdapter.Factory> adapterFactories = new ArrayList<>();
    protected final List<Interceptor> interceptors = new ArrayList<>();
    protected boolean isShowToast = true;  // ????????????????????????toast


    public BaseRequest(String url) {
        this.url = url;
        context = EasyHttp.getContext();
        EasyHttp config = EasyHttp.getInstance();
        this.baseUrl = config.getBaseUrl();
        if (!TextUtils.isEmpty(this.baseUrl)) {
            httpUrl = HttpUrl.parse(baseUrl);
        }
        if (baseUrl == null && url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
            httpUrl = HttpUrl.parse(url);
            baseUrl = httpUrl.url().getProtocol() + "://" + httpUrl.url().getHost() + "/";
        }
        cacheMode = config.getCacheMode();                                //??????????????????
        cacheTime = config.getCacheTime();                                //????????????
        retryCount = config.getRetryCount();                              //??????????????????
        retryDelay = config.getRetryDelay();                              //??????????????????
        retryIncreaseDelay = config.getRetryIncreaseDelay();              //????????????????????????
        //Okhttp  cache
        cache = config.getHttpCache();
        //???????????? Accept-Language
        String acceptLanguage = HttpHeaders.getAcceptLanguage();
        if (!TextUtils.isEmpty(acceptLanguage))
            headers(HttpHeaders.HEAD_KEY_ACCEPT_LANGUAGE, acceptLanguage);
        headers("chl", DeviceUtils.getChannelName());
        //???????????? User-Agent
        String userAgent = HttpHeaders.getUserAgent();
        if (!TextUtils.isEmpty(userAgent)) headers(HttpHeaders.HEAD_KEY_USER_AGENT, userAgent);
        //????????????????????????
        if (config.getCommonParams() != null) params.put(config.getCommonParams());
        if (config.getCommonHeaders() != null) headers.put(config.getCommonHeaders());
    }

    public HttpParams getParams() {
        return this.params;
    }

    public R readTimeOut(long readTimeOut) {
        this.readTimeOut = readTimeOut;
        return (R) this;
    }

    public R writeTimeOut(long writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
        return (R) this;
    }

    public R connectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
        return (R) this;
    }

    public R okCache(Cache cache) {
        this.cache = cache;
        return (R) this;
    }

    public R cacheMode(CacheMode cacheMode) {
        this.cacheMode = cacheMode;
        return (R) this;
    }

    public R isShowToast(boolean isShowToast) {
        this.isShowToast = isShowToast;
        return (R) this;
    }

    public R cacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
        return (R) this;
    }

    public R cacheTime(long cacheTime) {
        if (cacheTime <= -1) cacheTime = EasyHttp.DEFAULT_CACHE_NEVER_EXPIRE;
        this.cacheTime = cacheTime;
        return (R) this;
    }

    public R baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        if (!TextUtils.isEmpty(this.baseUrl))
            httpUrl = HttpUrl.parse(baseUrl);
        return (R) this;
    }

    public R retryCount(int retryCount) {
        if (retryCount < 0) throw new IllegalArgumentException("retryCount must > 0");
        this.retryCount = retryCount;
        return (R) this;
    }

    public R retryDelay(int retryDelay) {
        if (retryDelay < 0) throw new IllegalArgumentException("retryDelay must > 0");
        this.retryDelay = retryDelay;
        return (R) this;
    }

    public R retryIncreaseDelay(int retryIncreaseDelay) {
        if (retryIncreaseDelay < 0)
            throw new IllegalArgumentException("retryIncreaseDelay must > 0");
        this.retryIncreaseDelay = retryIncreaseDelay;
        return (R) this;
    }

    public R addInterceptor(Interceptor interceptor) {
        interceptors.add(Utils.checkNotNull(interceptor, "interceptor == null"));
        return (R) this;
    }

    public R addNetworkInterceptor(Interceptor interceptor) {
        networkInterceptors.add(Utils.checkNotNull(interceptor, "interceptor == null"));
        return (R) this;
    }

    public R addCookie(String name, String value) {
        Cookie.Builder builder = new Cookie.Builder();
        Cookie cookie = builder.name(name).value(value).domain(httpUrl.host()).build();
        this.cookies.add(cookie);
        return (R) this;
    }

    public R addCookie(Cookie cookie) {
        this.cookies.add(cookie);
        return (R) this;
    }

    public R addCookies(List<Cookie> cookies) {
        this.cookies.addAll(cookies);
        return (R) this;
    }

    /**
     * ??????Converter.Factory,??????GsonConverterFactory.create()
     */
    public R addConverterFactory(Converter.Factory factory) {
        converterFactories.add(factory);
        return (R) this;
    }

    /**
     * ??????CallAdapter.Factory,??????RxJavaCallAdapterFactory.create()
     */
    public R addCallAdapterFactory(CallAdapter.Factory factory) {
        adapterFactories.add(factory);
        return (R) this;
    }

    /**
     * ????????????
     */
    public R okproxy(Proxy proxy) {
        this.proxy = proxy;
        return (R) this;
    }

    /**
     * ????????????????????????
     */
    public R cacheDiskConverter(IDiskConverter converter) {
        this.diskConverter = Utils.checkNotNull(converter, "converter == null");
        return (R) this;
    }

    /**
     * https?????????????????????
     */
    public R hostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return (R) this;
    }

    /**
     * https????????????????????????
     */
    public R certificates(InputStream... certificates) {
        this.sslParams = HttpsUtils.getSslSocketFactory(null, null, certificates);
        return (R) this;
    }

    /**
     * https??????????????????
     */
    public R certificates(InputStream bksFile, String password, InputStream... certificates) {
        this.sslParams = HttpsUtils.getSslSocketFactory(bksFile, password, certificates);
        return (R) this;
    }

    /**
     * ???????????????
     */
    public R headers(HttpHeaders headers) {
        this.headers.put(headers);
        return (R) this;
    }

    /**
     * ???????????????
     */
    public R headers(String key, String value) {
        headers.put(key, value);
        return (R) this;
    }

    /**
     * ???????????????
     */
    public R removeHeader(String key) {
        headers.remove(key);
        return (R) this;
    }

    /**
     * ?????????????????????
     */
    public R removeAllHeaders() {
        headers.clear();
        return (R) this;
    }

    /**
     * ????????????
     */
    public R params(HttpParams params) {
        this.params.put(params);
        return (R) this;
    }

    public R params(String key, String value) {
        params.put(key, value);
        return (R) this;
    }

    public R params(Map<String, String> keyValues) {
        params.put(keyValues);
        return (R) this;
    }

    public R removeParam(String key) {
        params.remove(key);
        return (R) this;
    }

    public R removeAllParams() {
        params.clear();
        return (R) this;
    }

    public R sign(boolean sign) {
        this.sign = sign;
        return (R) this;
    }

    public R timeStamp(boolean timeStamp) {
        this.timeStamp = timeStamp;
        return (R) this;
    }

    public R accessToken(boolean accessToken) {
        this.accessToken = accessToken;
        return (R) this;
    }

    public R syncRequest(boolean syncRequest) {
        this.isSyncRequest = syncRequest;
        return (R) this;
    }

    /**
     * ???????????????key???
     */
    public void removeCache(String key) {
        getRxCache().remove(key).compose(RxUtil.<Boolean>io_main())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        HttpLog.i("removeCache success!!!");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        HttpLog.i("removeCache err!!!" + throwable);
                    }
                });
    }

    /**
     * ?????????????????????????????????????????????OkClient
     */
    private OkHttpClient.Builder generateOkClient() {
        if (readTimeOut <= 0 && writeTimeOut <= 0 && connectTimeout <= 0 && sslParams == null
                && cookies.size() == 0 && hostnameVerifier == null && proxy == null && headers.isEmpty()) {
            OkHttpClient.Builder builder = EasyHttp.getOkHttpClientBuilder();
            for (Interceptor interceptor : builder.interceptors()) {
                if (interceptor instanceof BaseDynamicInterceptor) {
                    ((BaseDynamicInterceptor) interceptor).sign(sign).timeStamp(timeStamp).accessToken(accessToken);
                }
            }
            return builder;
        } else {
            final OkHttpClient.Builder newClientBuilder = EasyHttp.getOkHttpClient().newBuilder();
            if (readTimeOut > 0)
                newClientBuilder.readTimeout(readTimeOut, TimeUnit.MILLISECONDS);
            if (writeTimeOut > 0)
                newClientBuilder.writeTimeout(writeTimeOut, TimeUnit.MILLISECONDS);
            if (connectTimeout > 0)
                newClientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
            if (hostnameVerifier != null) newClientBuilder.hostnameVerifier(hostnameVerifier);
            if (sslParams != null)
                newClientBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
            if (proxy != null) newClientBuilder.proxy(proxy);
            if (cookies.size() > 0) EasyHttp.getCookieJar().addCookies(cookies);

            //???????????????????????????(???????????????????????????????????????????????????????????????)
//            newClientBuilder.addInterceptor(new EncrypAndDecryptiontionInterceptor());
            //?????????  ????????????????????????????????????????????????????????????
            newClientBuilder.addInterceptor(new HeadersInterceptor(headers));
            //?????????  ????????????????????????????????????????????????????????????
            newClientBuilder.addInterceptor(new LoginHeaderInterceptor(headers));

//            if (BuildConfig.DEBUG) {
//                //??????????????????
//                newClientBuilder.addInterceptor(new OkHttpProfilerInterceptor());
//            }

            for (Interceptor interceptor : interceptors) {
                if (interceptor instanceof BaseDynamicInterceptor) {
                    ((BaseDynamicInterceptor) interceptor).sign(sign).timeStamp(timeStamp).accessToken(accessToken);
                }
                newClientBuilder.addInterceptor(interceptor);
            }
            for (Interceptor interceptor : newClientBuilder.interceptors()) {
                if (interceptor instanceof BaseDynamicInterceptor) {
                    ((BaseDynamicInterceptor) interceptor).sign(sign).timeStamp(timeStamp).accessToken(accessToken);
                }
            }
            if (networkInterceptors.size() > 0) {
                for (Interceptor interceptor : networkInterceptors) {
                    newClientBuilder.addNetworkInterceptor(interceptor);
                }
            }
            return newClientBuilder;
        }
    }

    /**
     * ?????????????????????????????????????????????Retrofit
     */
    private Retrofit.Builder generateRetrofit() {
        if (converterFactories.isEmpty() && adapterFactories.isEmpty()) {
            Retrofit.Builder builder = getRetrofitBuilder();
            if (!TextUtils.isEmpty(baseUrl)) {
                builder.baseUrl(baseUrl);
            }
            return builder;
        } else {
            final Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
            if (!TextUtils.isEmpty(baseUrl)) retrofitBuilder.baseUrl(baseUrl);
            if (!converterFactories.isEmpty()) {
                for (Converter.Factory converterFactory : converterFactories) {
                    retrofitBuilder.addConverterFactory(converterFactory);
                }
            } else {
                //?????????????????????????????????
                Retrofit.Builder newBuilder = EasyHttp.getRetrofitBuilder();
                if (!TextUtils.isEmpty(baseUrl)) {
                    newBuilder.baseUrl(baseUrl);
                }
                List<Converter.Factory> listConverterFactory = newBuilder.build().converterFactories();
                for (Converter.Factory factory : listConverterFactory) {
                    retrofitBuilder.addConverterFactory(factory);
                }
            }
            if (!adapterFactories.isEmpty()) {
                for (CallAdapter.Factory adapterFactory : adapterFactories) {
                    retrofitBuilder.addCallAdapterFactory(adapterFactory);
                }
            } else {
                //?????????????????????????????????
                Retrofit.Builder newBuilder = EasyHttp.getRetrofitBuilder();
                List<CallAdapter.Factory> listAdapterFactory = newBuilder.baseUrl(baseUrl).build().callAdapterFactories();
                for (CallAdapter.Factory factory : listAdapterFactory) {
                    retrofitBuilder.addCallAdapterFactory(factory);
                }
            }
            return retrofitBuilder;
        }
    }

    /**
     * ?????????????????????????????????????????????RxCache???Cache
     */
    private RxCache.Builder generateRxCache() {
        final RxCache.Builder rxCacheBuilder = EasyHttp.getRxCacheBuilder();
        switch (cacheMode) {
            case NO_CACHE://???????????????
                final NoCacheInterceptor NOCACHEINTERCEPTOR = new NoCacheInterceptor();
                interceptors.add(NOCACHEINTERCEPTOR);
                networkInterceptors.add(NOCACHEINTERCEPTOR);
                break;
            case DEFAULT://??????Okhttp?????????
                if (this.cache == null) {
                    File cacheDirectory = EasyHttp.getCacheDirectory();
                    if (cacheDirectory == null) {
                        cacheDirectory = new File(EasyHttp.getContext().getCacheDir(), "okhttp-cache");
                    } else {
                        if (cacheDirectory.isDirectory() && !cacheDirectory.exists()) {
                            cacheDirectory.mkdirs();
                        }
                    }
                    this.cache = new Cache(cacheDirectory, Math.max(5 * 1024 * 1024, EasyHttp.getCacheMaxSize()));
                }
                String cacheControlValue = String.format("max-age=%d", Math.max(-1, cacheTime));
                final CacheInterceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new CacheInterceptor(EasyHttp.getContext(), cacheControlValue);
                final CacheInterceptorOffline REWRITE_CACHE_CONTROL_INTERCEPTOR_OFFLINE = new CacheInterceptorOffline(EasyHttp.getContext(), cacheControlValue);
                networkInterceptors.add(REWRITE_CACHE_CONTROL_INTERCEPTOR);
                networkInterceptors.add(REWRITE_CACHE_CONTROL_INTERCEPTOR_OFFLINE);
                interceptors.add(REWRITE_CACHE_CONTROL_INTERCEPTOR_OFFLINE);
                break;
            case FIRSTREMOTE:
            case FIRSTCACHE:
            case ONLYREMOTE:
            case ONLYCACHE:
            case CACHEANDREMOTE:
            case CACHEANDREMOTEDISTINCT:
                interceptors.add(new NoCacheInterceptor());
                if (diskConverter == null) {
                    final RxCache.Builder tempRxCacheBuilder = rxCacheBuilder;
                    tempRxCacheBuilder.cachekey(Utils.checkNotNull(cacheKey, "cacheKey == null"))
                            .cacheTime(cacheTime);
                    return tempRxCacheBuilder;
                } else {
                    final RxCache.Builder cacheBuilder = getRxCache().newBuilder();
                    cacheBuilder.diskConverter(diskConverter)
                            .cachekey(Utils.checkNotNull(cacheKey, "cacheKey == null"))
                            .cacheTime(cacheTime);
                    return cacheBuilder;
                }
        }
        return rxCacheBuilder;
    }

    protected R build() {
        final RxCache.Builder rxCacheBuilder = generateRxCache();
        OkHttpClient.Builder okHttpClientBuilder = generateOkClient();
        if (cacheMode == CacheMode.DEFAULT) {//okhttp??????
            okHttpClientBuilder.cache(cache);
        }
        final Retrofit.Builder retrofitBuilder = generateRetrofit();
        okHttpClient = okHttpClientBuilder.build();
        retrofitBuilder.client(okHttpClient);
        retrofit = retrofitBuilder.build();
        rxCache = rxCacheBuilder.build();
        apiManager = retrofit.create(ApiService.class);
        return (R) this;
    }

    protected abstract Observable<ResponseBody> generateRequest();
}
