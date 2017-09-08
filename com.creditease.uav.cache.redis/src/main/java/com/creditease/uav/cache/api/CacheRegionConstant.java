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

package com.creditease.uav.cache.api;

/**
 * Created with IntelliJ IDEA. User: ghost Date: 14-3-20 Time: 上午11:42 To change this template use File | Settings |
 * File Templates.
 */
public interface CacheRegionConstant {

    public static final String SERVER_URL = "serverUrl"; // serverUrl域
    public static final String WEB_ROOT_PATH = "webRootPath"; // Web根磁盘目录域
    public static final String ACCESS_TOKEN_REGION = "accessToken"; // 微信AccessToken域
    public static final String COOKIES_REGION = "cookies"; // cookies域
    public static final String TOKEN_REGION = "token"; // cookies域
    public static final String LOGIN_REGION = "login"; // cookies域
    public static final String FETCH_USERS_REGION = "fetchUsers"; // fetchUsersRegion

    public static final String IMAGE_TEXT_ENTITY_REGION = "imageTextEntityRegion"; // imageTextEntityRegion

    public static final String CACHE_EXPIRE = "EXPIRE"; // 缓存超时设置
    public static final String SESSION_CONTEXT_VALUE = "EXPIRE"; // 缓存超时设置
    public static final String SESSION_CONTEXT_OPERATE = "EXPIRE"; // 缓存超时设置

    public static final String PROMOTION_DICT_REGION = "promotionDictRegion"; // 推广类型

    public static final String JSAPI_TICKET_REGION = "jsApiTicket"; // jsApiTicket

    // JOB机制处理完存执域
    public static final String CUSTOMER_REGION = "CUSTOMER_REGION";

    // 原有系统域
    public static String INTERMGMT_INTERFACE = "InterMgmt:InterfaceDefinition:"; // 接口

    // 预热缓存域
    public static final String PUBLIC_USER_REGION = "publicUser"; // 微信公众账号域
    public static String KEY_WORD = "KEY_WORD"; // 关键字
    public static String TENCENT_TEMPLATE = "TENCENT_TEMPLATE"; // 腾讯模板
    public static String ASYNC_DEFAULT_RESP_TEXT = "DEFAULT_ASYNC_RESP_TEXT"; // 默认返回话术异步

    // 前台缓存域
    public static String WEB_GUIDE_MENU = "WEB_GUIDE_MENU"; // 引导菜单
    public static String WEB_USER_DKF = "WEB_USER_DKF"; // 多客服

    // 模板KEY
    public final static String tempKey_text = "wxt.text"; // 文本
    public final static String tempKey_outcus = "wxt.outcus"; // 多客服
    public final static String tempKey_click_image = "wxt.click.image"; // 图文开始
    public final static String tempKey_tclick_image_item = "wxt.tclick.image.item"; // 图文body
    public final static String tempKey_tclick_image_end = "wxt.tclick.image.end"; // 图文结束
    public final static String tempKey_asyncrespKey_in_job = "async.resp.in.job"; // 作业提交中
    public final static String tempKey_verifyCust_def_resp = "verifyCust.def.resp"; // 客户验证默认返回话术

    // 微信菜单点击类型预热域
    public final static String WEIXIN_CLICK_MENU = "weixinClickMenu";

    public static final String VALIDATE_CODE_REGION = "validatecodeRegion"; // validatecodeRegion

    // 系统内部工号
    public static final String SYSTEM_USER_NO = "1";

    public static final String USEROPENID_REGION = "USEROPENID_REGION";
}
