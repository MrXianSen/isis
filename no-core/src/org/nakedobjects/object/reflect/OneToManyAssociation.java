package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;


public class OneToManyAssociation extends NakedObjectAssociation {
    private final OneToManyPeer reflectiveAdapter;

    public OneToManyAssociation(String className, String methodName, NakedObjectSpecification type, OneToManyPeer association) {
        super(methodName, type, new MemberIdentifier(className, methodName));
        this.reflectiveAdapter = association;
    }

    public void clearAssociation(NakedObject inObject, NakedObject associate) {
        if (associate == null) {
            throw new IllegalArgumentException("element should not be null");
        }
        reflectiveAdapter.removeAssociation(getIdentifier(), inObject, associate);
    }

    public void clearCollection(NakedObject inObject) {
        reflectiveAdapter.removeAllAssociations(getIdentifier(), inObject);
    }

    public Naked get(NakedObject fromObject) {
        return reflectiveAdapter.getAssociations(getIdentifier(), fromObject);
    }

    public Object getExtension(Class cls) {
        return reflectiveAdapter.getExtension(null);
    }

    public Hint getHint(NakedObject object) {
        return getHint(object, null, true);
    }

    public Hint getHint(NakedObject container, NakedObject element, boolean add) {
        if (hasHint()) {
            return reflectiveAdapter.getHint(getIdentifier(), container, element, add);
        } else {
            return new DefaultHint();
        }

    }

    protected String getLabel(NakedObject object) {
        Hint about = getHint(object);

        return getLabel(about);
    }

    public boolean hasHint() {
        return reflectiveAdapter.hasHint();
    }

    public void initAssociation(NakedObject inObject, NakedObject associate) {
        reflectiveAdapter.initAssociation(getIdentifier(), inObject, associate);
    }

    public void initOneToManyAssociation(NakedObject inObject, NakedObject[] instances) {
        reflectiveAdapter.initOneToManyAssociation(getIdentifier(), inObject, instances);
    }

    public boolean isCollection() {
        return true;
    }

    public boolean isDerived() {
        return reflectiveAdapter.isDerived();
    }

    public boolean isEmpty(NakedObject inObject) {
        return reflectiveAdapter.isEmpty(getIdentifier(), inObject);
    }

    public boolean isPart() {
        return true;
    }

    public void setAssociation(NakedObject inObject, NakedObject associate) {
        if (associate == null) {
            throw new IllegalArgumentException("Can't use null to add an item to a collection");
        }
        reflectiveAdapter.addAssociation(getIdentifier(), inObject, associate);
    }

    public String toString() {
        return "OneToManyAssociation [" + super.toString() + ",type="
                + (getSpecification() == null ? "unknown" : getSpecification().getShortName()) + "]";
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
