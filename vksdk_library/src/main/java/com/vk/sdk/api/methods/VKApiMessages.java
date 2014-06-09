package com.vk.sdk.api.methods;

import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKParser;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKApiChat;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;

import org.json.JSONObject;

public class VKApiMessages extends VKApiBase {
    public VKRequest getChat(VKParameters params) {
        return prepareRequest("getChat", params, VKRequest.HttpMethod.GET, new VKParser() {
            @Override
            public Object createModel(JSONObject object) {
                return new VKList<VKApiChat>(object, VKApiChat.class);
            }
        });
    }
}
