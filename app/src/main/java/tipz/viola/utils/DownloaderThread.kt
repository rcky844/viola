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
package tipz.viola.utils

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header

class DownloaderThread(name: String?) : HandlerThread(name) {
    private var mCallerHandler: Handler? = null
    fun setCallerHandler(callerHandler: Handler?) {
        mCallerHandler = callerHandler
    }

    fun startDownload(url: String?) {
        val message = mCallerHandler!!.obtainMessage()
        val bundle = Bundle()
        val client = AsyncHttpClient()
        client[url, object : AsyncHttpResponseHandler(looper) {
            override fun onSuccess(statusCode: Int, headers: Array<Header>, response: ByteArray?) {
                if (response == null) {
                    bundle.putString(MSG_RESPONSE, CommonUtils.EMPTY_STRING)
                    message.what = TYPE_FAILED
                } else {
                    bundle.putString(MSG_RESPONSE, String(response))
                    message.what = TYPE_SUCCESS
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header>,
                errorResponse: ByteArray?,
                e: Throwable
            ) {
                if (errorResponse == null) {
                    bundle.putString(MSG_RESPONSE, CommonUtils.EMPTY_STRING)
                } else {
                    bundle.putString(MSG_RESPONSE, String(errorResponse))
                }
                message.what = TYPE_FAILED
            }

            override fun onFinish() {
                message.data = bundle
                mCallerHandler!!.sendMessage(message)
            }
        }]
    }

    companion object {
        const val TYPE_SUCCESS = 2
        const val TYPE_FAILED = 3
        const val MSG_RESPONSE = "response"
    }
}