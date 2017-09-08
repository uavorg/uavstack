/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2017 UAVStack
 * ==
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
 * >>
 */

package com.creditease.uav.helpers.webhttp;

/**
 * 上传进度回调接口
 *
 * @author <a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since 1.0
 */
public interface UploadProgress {

    /**
     * 上传数据的回调函数调用
     * 
     * @param uploaded
     *            已经上传的字节数
     * @param total
     *            字节总数
     */
    void onUpload(long uploaded, long total);

    UploadProgress DEFAULT = new UploadProgress() {

        @Override
        public void onUpload(long uploaded, long total) {

        }
    };
}
