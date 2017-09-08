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

package com.creditease.uav.feature.upgrade.exception;

public class UpgradeException extends Exception {

    private static final long serialVersionUID = -9130037644997916270L;

    public UpgradeException() {
        super();
    }

    public UpgradeException(String message) {
        super(message);
    }

    public UpgradeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpgradeException(Throwable cause) {
        super(cause);
    }
}
