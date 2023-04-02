/*
 * Copyright (C) 2022-2023 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.browservio.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class DownloaderThread extends HandlerThread {
    public static final int TYPE_SUCCESS = 2;
    public static final int TYPE_FAILED = 3;

    public final static String MSG_RESPONSE = "response";

    private Handler mCallerHandler;

    public DownloaderThread(String name) {
        super(name);
    }

    public void setCallerHandler(Handler callerHandler) {
        mCallerHandler = callerHandler;
    }

    public void startDownload(String url) {
        Message message = mCallerHandler.obtainMessage();
        Bundle bundle = new Bundle();

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler(getLooper()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                if (response == null) {
                    bundle.putString(MSG_RESPONSE, CommonUtils.EMPTY_STRING);
                    message.what = TYPE_FAILED;
                } else {
                    bundle.putString(MSG_RESPONSE, new String(response));
                    message.what = TYPE_SUCCESS;
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                if (errorResponse == null) {
                    bundle.putString(MSG_RESPONSE, CommonUtils.EMPTY_STRING);
                } else {
                    bundle.putString(MSG_RESPONSE, new String(errorResponse));
                }
                message.what = TYPE_FAILED;
            }

            @Override
            public void onFinish() {
                message.setData(bundle);
                mCallerHandler.sendMessage(message);
            }
        });
    }
}
