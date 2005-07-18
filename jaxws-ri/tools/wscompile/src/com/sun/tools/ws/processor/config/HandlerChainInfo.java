/*
 * $Id: HandlerChainInfo.java,v 1.2 2005-07-18 18:13:55 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author WS Development Team
 */
public class HandlerChainInfo {

    public HandlerChainInfo() {
        handlers = new ArrayList();
        roles = new HashSet();
    }

    public void add(HandlerInfo i) {
        handlers.add(i);
    }

    public Iterator getHandlers() {
        return handlers.iterator();
    }

    public int getHandlersCount() {
        return handlers.size();
    }

    /* serialization */
    public List getHandlersList() {
        return handlers;
    }

    /* serialization */
    public void setHandlersList(List l) {
        handlers = l;
    }

    public void addRole(String s) {
        roles.add(s);
    }

    public Set getRoles() {
        return roles;
    }

    /* serialization */
    public void setRoles(Set s) {
        roles = s;
    }

    private List handlers;
    private Set roles;
}
