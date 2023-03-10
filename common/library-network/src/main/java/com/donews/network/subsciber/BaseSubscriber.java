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

package com.donews.network.subsciber;

import android.content.Context;
import android.widget.Toast;

import com.donews.network.exception.ApiException;
import com.donews.network.utils.HttpLog;

import java.lang.ref.WeakReference;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;

import static com.donews.network.utils.Utils.isNetworkAvailable;

/**
 * <p>描述：订阅的基类</p>
 * 1.可以防止内存泄露。<br>
 * 2.在onStart()没有网络时直接onCompleted();<br>
 * 3.统一处理了异常<br>
 * 作者： created by honeylife<br>
 * 日期： 2016/12/20 10:35<br>
 * 版本： v2.0<br>
 */
public abstract class BaseSubscriber<T> extends DisposableObserver<T> {
    public WeakReference<Context> contextWeakReference;

    private boolean isShowToast;

    public BaseSubscriber() {
    }

    @Override
    protected void onStart() {
        HttpLog.e("-->http is onStart");
        if (contextWeakReference != null && contextWeakReference.get() != null && !isNetworkAvailable(contextWeakReference.get())) {
            //Toast.makeText(context, "无网络，读取缓存数据", Toast.LENGTH_SHORT).show();
            onComplete();
        }
    }


    public BaseSubscriber(Context context) {
        this(context, true);
    }

    public BaseSubscriber(Context context, boolean isShowToast) {
        if (context != null) {
            contextWeakReference = new WeakReference<>(context);
        }
        this.isShowToast = isShowToast;
    }

    @Override
    public void onNext(@NonNull T t) {
        HttpLog.e("-->http is onNext");
    }

    @Override
    public final void onError(java.lang.Throwable e) {
        HttpLog.e("-->http is onError");

        HttpLog.e("--->" + e.getMessage());
        HttpLog.d("--->" + Thread.currentThread().getName());
        if (e instanceof ApiException) {
            HttpLog.e("--> e instanceof ApiException err:" + e);
            HttpLog.e("--> e instanceof ApiException err:" + ((ApiException) e).getCode());
            if (((ApiException) e).getCode() != ApiException.ERROR.NULLPOINTER_EXCEPTION) {
                if (((ApiException) e).getCode() < ApiException.NO_CODE && isShowToast) {
                    Toast.makeText(contextWeakReference.get(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                onError((ApiException) e);
            } else {
                onCompleteOk();
            }
        } else {
            HttpLog.e("--> e !instanceof ApiException err:" + e);
            onError(ApiException.handleException(e));
        }
    }

    @Override
    public void onComplete() {
        HttpLog.e("-->http is onComplete");
    }


    public abstract void onError(ApiException e);

    public void onCompleteOk() {
    }

}
