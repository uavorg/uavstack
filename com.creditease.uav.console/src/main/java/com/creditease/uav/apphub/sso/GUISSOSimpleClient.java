package com.creditease.uav.apphub.sso;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class GUISSOSimpleClient extends GUISSOClient {
	
	@SuppressWarnings("unused")
    private GUISSOSimpleClient() {

    }

    protected GUISSOSimpleClient(HttpServletRequest request) {
        super(request);
    }

	@Override
	protected Map<String, String> getUserByLoginImpl(String loginId, String loginPwd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, String>> getUserByQuery(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getEmailListByQuery(String email) {
		// TODO Auto-generated method stub
		return null;
	}

}
