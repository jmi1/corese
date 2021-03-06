//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.24 at 10:15:40 AM CEST 
//


package fr.inria.edelweiss.rif.xml.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://www.w3.org/2007/rif#}IRIMETA" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2007/rif#}declare" maxOccurs="unbounded"/>
 *         &lt;element name="formula">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;group ref="{http://www.w3.org/2007/rif#}CLAUSE"/>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "id",
    "meta",
    "declare",
    "formula"
})
@XmlRootElement(name = "Forall")
public class Forall {

    protected Id id;
    protected Meta meta;
    @XmlElement(required = true)
    protected List<Declare> declare;
    @XmlElement(required = true)
    protected Forall.Formula formula;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Id }
     *     
     */
    public Id getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Id }
     *     
     */
    public void setId(Id value) {
        this.id = value;
    }

    /**
     * Gets the value of the meta property.
     * 
     * @return
     *     possible object is
     *     {@link Meta }
     *     
     */
    public Meta getMeta() {
        return meta;
    }

    /**
     * Sets the value of the meta property.
     * 
     * @param value
     *     allowed object is
     *     {@link Meta }
     *     
     */
    public void setMeta(Meta value) {
        this.meta = value;
    }

    /**
     * Gets the value of the declare property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the declare property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeclare().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Declare }
     * 
     * 
     */
    public List<Declare> getDeclare() {
        if (declare == null) {
            declare = new ArrayList<Declare>();
        }
        return this.declare;
    }

    /**
     * Gets the value of the formula property.
     * 
     * @return
     *     possible object is
     *     {@link Forall.Formula }
     *     
     */
    public Forall.Formula getFormula() {
        return formula;
    }

    /**
     * Sets the value of the formula property.
     * 
     * @param value
     *     allowed object is
     *     {@link Forall.Formula }
     *     
     */
    public void setFormula(Forall.Formula value) {
        this.formula = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;group ref="{http://www.w3.org/2007/rif#}CLAUSE"/>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "implies",
        "atom",
        "equal",
        "member",
        "subclass",
        "frame"
    })
    public static class Formula {

        @XmlElement(name = "Implies")
        protected Implies implies;
        @XmlElement(name = "Atom")
        protected Atom atom;
        @XmlElement(name = "Equal")
        protected Equal equal;
        @XmlElement(name = "Member")
        protected Member member;
        @XmlElement(name = "Subclass")
        protected Subclass subclass;
        @XmlElement(name = "Frame")
        protected Frame frame;

        /**
         * Gets the value of the implies property.
         * 
         * @return
         *     possible object is
         *     {@link Implies }
         *     
         */
        public Implies getImplies() {
            return implies;
        }

        /**
         * Sets the value of the implies property.
         * 
         * @param value
         *     allowed object is
         *     {@link Implies }
         *     
         */
        public void setImplies(Implies value) {
            this.implies = value;
        }

        /**
         * Gets the value of the atom property.
         * 
         * @return
         *     possible object is
         *     {@link Atom }
         *     
         */
        public Atom getAtom() {
            return atom;
        }

        /**
         * Sets the value of the atom property.
         * 
         * @param value
         *     allowed object is
         *     {@link Atom }
         *     
         */
        public void setAtom(Atom value) {
            this.atom = value;
        }

        /**
         * Gets the value of the equal property.
         * 
         * @return
         *     possible object is
         *     {@link Equal }
         *     
         */
        public Equal getEqual() {
            return equal;
        }

        /**
         * Sets the value of the equal property.
         * 
         * @param value
         *     allowed object is
         *     {@link Equal }
         *     
         */
        public void setEqual(Equal value) {
            this.equal = value;
        }

        /**
         * Gets the value of the member property.
         * 
         * @return
         *     possible object is
         *     {@link Member }
         *     
         */
        public Member getMember() {
            return member;
        }

        /**
         * Sets the value of the member property.
         * 
         * @param value
         *     allowed object is
         *     {@link Member }
         *     
         */
        public void setMember(Member value) {
            this.member = value;
        }

        /**
         * Gets the value of the subclass property.
         * 
         * @return
         *     possible object is
         *     {@link Subclass }
         *     
         */
        public Subclass getSubclass() {
            return subclass;
        }

        /**
         * Sets the value of the subclass property.
         * 
         * @param value
         *     allowed object is
         *     {@link Subclass }
         *     
         */
        public void setSubclass(Subclass value) {
            this.subclass = value;
        }

        /**
         * Gets the value of the frame property.
         * 
         * @return
         *     possible object is
         *     {@link Frame }
         *     
         */
        public Frame getFrame() {
            return frame;
        }

        /**
         * Sets the value of the frame property.
         * 
         * @param value
         *     allowed object is
         *     {@link Frame }
         *     
         */
        public void setFrame(Frame value) {
            this.frame = value;
        }
        
        /** XML syntactic tree -> AST. Clause within the Forall quantifier
         * CLAUSE ::= Implies | ATOMIC */
        public fr.inria.edelweiss.rif.api.IClause XML2AST() {
        	if(this.implies != null) {
        		// Clause is an Implies (entailment)
        		return this.implies.XMl2AST() ;
        	} else {
        		// Clause is an ATOMIC (Atom, Equal, Member, Subclass, Frame)
        		// ATOMIC ::= (Atom | Equal | Member | Subclass | Frame)
        		if(this.atom != null) {
        			// ATOMIC is an Atom
        			return null ; // TODO
        		} else if(this.equal != null) {
        			// ATOMIC is a "=" binary operator
        			return this.equal.XML2AST() ;
        		} else if(this.member != null) {
        			// ATOMIC is a "#" binary operator 
        			return this.member.XML2AST() ;
        		} else if(this.subclass != null) {
        			// ATOMIC is a "##" binary operator
        			return this.subclass.XML2AST() ;
        		} else if(this.frame !=  null) {
        			// ATOMIC is a Frame
        			return this.frame.XML2AST() ;
        		}
         	}
        	return null ; // Something is wrong if we get there. Likely the XML doesn't validate against XSD.
        }

    }
    /** XML syntactic tree -> AST. Universally quantified formula
     * RULE ::= IRIMETA? 'Forall' Var+ '(' CLAUSE ')' */
    public fr.inria.edelweiss.rif.ast.Forall XMl2AST() {
    	Vector<fr.inria.edelweiss.rif.ast.Var> universalVars = new Vector<fr.inria.edelweiss.rif.ast.Var>() ;
    	for(Declare dec : this.declare)
    		universalVars.add(dec.XML2AST()) ;
    	return fr.inria.edelweiss.rif.ast.Forall.create(universalVars, this.formula.XML2AST()) ;
    }

}
