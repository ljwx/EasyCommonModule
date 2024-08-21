package com.ljwx.baseutils

import org.json.JSONObject

object JsonObjectUtils {

    fun default() {
        val params = JSONObject()
        params.put("type", "SIDEBAR_GET_BUTTON_RECT")
        val payload = JSONObject()
        payload.put("id", "1")
        params.put("payload", payload)
    }

    /**
     * 数组不转字符串
     */
    fun keepArray() {
        /**
         * //设置侧边栏按钮
         * val params = com.google.gson.JsonObject()
         * val payload = JsonObject()
         * val buttonArray = JsonArray()
         * val gson = Gson()
         * buttons.forEach {
         *     buttonArray.add(gson.toJsonTree(it).asJsonObject)
         * }
         * payload.add("buttons", buttonArray)
         * params.addProperty("type", "SIDEBAR_SET_BUTTONS")
         * params.add("payload", payload)
         */
    }

}