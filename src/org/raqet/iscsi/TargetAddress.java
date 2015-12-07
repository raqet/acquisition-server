/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.iscsi;

import static com.javaforge.reifier.check.ArgumentChecker.argNotNull;
import static com.javaforge.reifier.check.ArgumentChecker.argNotNullAndNotEmpty;


public class TargetAddress {
    private final String _portalUrl;
    private final String _username;
    private final String _password;

    public TargetAddress(final String portalUrl, final String username, final String password) {
        argNotNullAndNotEmpty("portalUrl", portalUrl);
        argNotNull("username", username);
        argNotNull("password", password);
        _portalUrl = portalUrl;
        _username = username;
        _password = password;
    }

    public TargetAddress(final String portalUrlWithCredentials) {
        argNotNullAndNotEmpty("portalUrl", portalUrlWithCredentials);
        if (portalUrlWithCredentials.contains("@")) {
            final int atIndex = portalUrlWithCredentials.lastIndexOf('@');
            _portalUrl = portalUrlWithCredentials.substring(atIndex + 1);
            final int colonIndex = portalUrlWithCredentials.indexOf(':');
            _username = portalUrlWithCredentials.substring(0, colonIndex);
            _password = portalUrlWithCredentials.substring(colonIndex + 1, atIndex);
        }
        else {
            _portalUrl = portalUrlWithCredentials;
            _username = "";
            _password = "";
        }
    }

    public String getPortalUrl() {
        return _portalUrl;
    }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    @Override
    public String toString() {
        return _portalUrl;
    }


}
