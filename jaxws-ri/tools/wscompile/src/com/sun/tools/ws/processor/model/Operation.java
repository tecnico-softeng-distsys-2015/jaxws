/*
 * $Id: Operation.java,v 1.2 2005-07-18 18:14:00 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
import com.sun.tools.ws.wsdl.document.soap.SOAPUse;

/**
 *
 * @author WS Development Team
 */
public class Operation extends ModelObject {

    public Operation() {}

    public Operation(Operation operation){
        this(operation._name);
        this._style = operation._style;
        this._use = operation._use;
    }
    public Operation(QName name) {
        _name = name;
        _uniqueName = name.getLocalPart();
        _faultNames = new HashSet();
        _faults = new HashSet();
    }

    public QName getName() {
        return _name;
    }

    public void setName(QName n) {
        _name = n;
    }

    public String getUniqueName() {
        return _uniqueName;
    }

    public void setUniqueName(String s) {
        _uniqueName = s;
    }

    public Request getRequest() {
        return _request;
    }

    public void setRequest(Request r) {
        _request = r;
    }

    public Response getResponse() {
        return _response;
    }

    public void setResponse(Response r) {
        _response = r;
    }

    public boolean isOverloaded() {
        return !_name.getLocalPart().equals(_uniqueName);
    }

    public void addFault(Fault f) {
        if (_faultNames.contains(f.getName())) {
            throw new ModelException("model.uniqueness");
        }
        _faultNames.add(f.getName());
        _faults.add(f);
    }

    public Iterator getFaults() {
        return _faults.iterator();
    }

    public Set<Fault> getFaultsSet() {
        return _faults;
    }

    /* serialization */
    public void setFaultsSet(Set s) {
        _faults = s;
        initializeFaultNames();
    }

    private void initializeFaultNames() {
        _faultNames = new HashSet();
        if (_faults != null) {
            for (Iterator iter = _faults.iterator(); iter.hasNext();) {
                Fault f = (Fault) iter.next();
                if (f.getName() != null && _faultNames.contains(f.getName())) {
                    throw new ModelException("model.uniqueness");
                }
                _faultNames.add(f.getName());
            }
        }
    }

    public Iterator getAllFaults() {
        Set allFaults = getAllFaultsSet();
        if (allFaults.size() == 0) {
            return null;
        }
        return allFaults.iterator();
    }

    public Set getAllFaultsSet() {
        Set transSet = new HashSet();
        transSet.addAll(_faults);
        Iterator iter = _faults.iterator();
        Fault fault;
        Set tmpSet;
        while (iter.hasNext()) {
            tmpSet = ((Fault)iter.next()).getAllFaultsSet();
            transSet.addAll(tmpSet);
        }
        return transSet;
    }

    public int getFaultCount() {
        return _faults.size();
    }

    public Set<Block> getAllFaultBlocks(){
        Set<Block> blocks = new HashSet<Block>();
        Iterator faults = _faults.iterator();
        while(faults.hasNext()){
            Fault f = (Fault)faults.next();
            blocks.add(f.getBlock());
        }
        return blocks;
    }

    public JavaMethod getJavaMethod() {
        return _javaMethod;
    }

    public void setJavaMethod(JavaMethod i) {
        _javaMethod = i;
    }

    public String getSOAPAction() {
        return _soapAction;
    }

    public void setSOAPAction(String s) {
        _soapAction = s;
    }

    public SOAPStyle getStyle() {
        return _style;
    }

    public void setStyle(SOAPStyle s) {
        _style = s;
    }

    public SOAPUse getUse() {
        return _use;
    }

    public void setUse(SOAPUse u) {
        _use = u;
    }

    public boolean isWrapped() {
        return _isWrapped;
    }

    public void setWrapped(boolean isWrapped) {
        _isWrapped = isWrapped;
    }


    public void accept(ModelVisitor visitor) throws Exception {
        visitor.visit(this);
    }



    @Persistent
    private boolean _isWrapped = true;
    private QName _name;
    private String _uniqueName;
    private Request _request;
    private Response _response;
    private JavaMethod _javaMethod;
    private String _soapAction;
    private SOAPStyle _style = SOAPStyle.DOCUMENT;
    private SOAPUse _use = SOAPUse.LITERAL;
    private Set _faultNames;
    private Set _faults;

}
