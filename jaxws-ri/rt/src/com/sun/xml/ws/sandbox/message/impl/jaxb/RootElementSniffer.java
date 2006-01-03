/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.sandbox.message.impl.jaxb;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Sniffs the root element name and its attributes.
 *
 * @author Kohsuke Kawaguchi
 */
class RootElementSniffer extends DefaultHandler {
    String nsUri = "##error";
    String localName = "##error";

    public void startElement(String uri, String localName, String qName, Attributes a) throws SAXException {
        this.nsUri = uri;
        this.localName = localName;
        checkAttributes(a);
        // no need to parse any further.
        throw aSAXException;
    }

    protected void checkAttributes(Attributes a) {
    }

    private static final SAXException aSAXException = new SAXException();
}
