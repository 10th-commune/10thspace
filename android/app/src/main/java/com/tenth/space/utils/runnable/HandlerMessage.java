/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tenth.space.utils.runnable;

public class HandlerMessage {
	public static final int MSG_PREPARE = 0;
	public static final int MSG_SUCCESS = 1;
	public static final int MSG_CANCEL = 2;



	// error
	public static final int MSG_FAILURE = 3;
	public static final int MSG_FAILURE_NETWORK = 4;
	public static final int MSG_AUTH_ERROR = 5;
	public static final int MSG_400 = 6;
	public static final int MSG_404 = 7;
	public static final int MSG_FILE_NOT_FOUND = 8;
	public static final int MSG_INVALID_ADDRESS = 9;

	public static final int MSG_PASSWORD_WRONG = 11;
	public static final int MSG_ADDRESS_NOT_MONITOR = 12;
	public static final int MSG_SUCCESS_FROM_CACHE = 13;

	public static final int MSG_DOWLOAD = 30;
	public static final int MSG_CHECK_STATE = 31;

	public static final int SDK_PAY_FLAG = 40;


	public static final int MSG_CREATEID_SUCCESS = 50;
	public static final int MSG_CREATEID_FAILURE = 51;
	public static final int MSG_IMPORTID_SUCCESS = 52;
	public static final int MSG_IMPORTID_FAILURE = 53;
	public static final int MSG_REFERRAL_DECODE_SUCCESS = 54;
	public static final int MSG_REFERRAL_DECODE_FAILURE = 55;
}
