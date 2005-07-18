/*
 * $Id: Identifiable.java,v 1.2 2005-07-18 18:14:21 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * An interface implemented by entities which have an ID.
 *
 * @author WS Development Team
 */
public interface Identifiable extends Elemental {
    public String getID();
}
