package org.strongpoint.sdfcli.plugin.services.session;

public class AuthorizationSessionData {
	
	private AuthorizationSessionData session;
	
	public AuthorizationSessionData getSession() {
		if (session == null) {
			session = new AuthorizationSessionData();
		}
		return session;
	}

}
