/*
 The contents of this file are subject to the terms
 of the Common Development and Distribution License
 (the "License").  You may not use this file except
 in compliance with the License.
 
 You can obtain a copy of the license at
 https://jwsdp.dev.java.net/CDDLv1.0.html
 See the License for the specific language governing
 permissions and limitations under the License.
 
 When distributing Covered Code, include this CDDL
 HEADER in each file and include the License file at
 https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 add the following below this CDDL HEADER, with the
 fields enclosed by brackets "[]" replaced with your
 own identifying information: Portions Copyright [yyyy]
 [name of copyright owner]
*/
/*
 $Id: MapRequiredException.java,v 1.1.2.1 2006-08-18 21:56:12 arungupta Exp $

 Copyright (c) 2006 Sun Microsystems, Inc.
 All rights reserved.
*/

package com.sun.xml.ws.addressing;

import javax.xml.ws.WebServiceException;
import javax.xml.namespace.QName;

/**
 * @author Arun Gupta
 */
public class MapRequiredException extends WebServiceException {
    QName name;

    public MapRequiredException(QName name) {
        this.name = name;
    }

    public QName getMapQName() {
        return name;
    }
}
