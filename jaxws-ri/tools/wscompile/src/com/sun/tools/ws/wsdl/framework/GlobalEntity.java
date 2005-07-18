/*
 * $Id: GlobalEntity.java,v 1.2 2005-07-18 18:14:20 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * An entity that can be defined in a target namespace.
 *
 * @author WS Development Team
 */
public abstract class GlobalEntity extends Entity implements GloballyKnown {

    public GlobalEntity(Defining defining) {
        _defining = defining;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public abstract Kind getKind();

    public Defining getDefining() {
        return _defining;
    }

    private Defining _defining;
    private String _name;
}
