/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.wing.element;


import org.dspace.app.xmlui.wing.AttributeMap;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingContext;
import org.dspace.app.xmlui.wing.WingException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * A class representing all the containers that contain unformatted text, such
 * as head, label, help, value, etc...
 * 
 * This class may not be instantiated on it's own instead you must use one of
 * the extending classes listed above. This abstract class implements the
 * methods common to each of those elements.
 * 
 * @author Scott Phillips
 */

public abstract class TextContainer extends Container
{

    /**
     * Construct a new text container.
     * 
     * This method doesn't do anything but because the inheriting abstract class
     * mandates a constructor for this class to compile it must ensure that the
     * parent constructor is called. Just as implementors of this class must
     * ensure that this constructor is called, thus is the chain of life. :)
     * 
     * @param context
     *            (Required) The context this element is contained in.
     * @throws org.dspace.app.xmlui.wing.WingException passed through.
     */
    protected TextContainer(WingContext context) throws WingException
    {
        super(context);
    }

    /**
     * Add character content to container.
     * 
     * @param characters
     *            (Required) Direct content or a dictionary tag to be inserted
     *            into the element.
     * @throws org.dspace.app.xmlui.wing.WingException passed through.
     */
    public void addContent(String characters) throws WingException
    {
        Data data = new Data(context, characters);
        contents.add(data);
    }
    
    /**
     * Add integer content to container.
     * 
     * @param integer
     *            (Required) Add the integer into the element's container.
     * @throws org.dspace.app.xmlui.wing.WingException passed through.
     */
    public void addContent(int integer) throws WingException
    {
        Data data = new Data(context, String.valueOf(integer));
        contents.add(data);
    }
    
    /**
     * Add translated content to container.
     * 
     * @param message
     *            (Required) A key into the i18n catalogue for translation into
     *            the user's preferred language.
     * @throws org.dspace.app.xmlui.wing.WingException passed through.
     */
    public void addContent(Message message) throws WingException
    {
        Data data = new Data(context, message);
        contents.add(data);
    }

    public void addContent(Message message, final boolean para) throws WingException {
        Data data = new Data(context, message){

            @Override
            public void toSAX(ContentHandler contentHandler, LexicalHandler lexicalHandler, NamespaceSupport namespaces) throws SAXException {

                if(para){
                    startElement(contentHandler, namespaces, Para.E_PARA, null);
                }

                super.toSAX(contentHandler, lexicalHandler, namespaces);

                if(para){
                    endElement(contentHandler, namespaces, Para.E_PARA);
                }
            }
        };
        contents.add(data);
    }

    public void addHtmlContent(String characters) throws WingException {
        SimpleHTMLFragment data = new SimpleHTMLFragment(context, false, characters);

        contents.add(data);
    }

	public void addInfo(final String label, String characters) throws WingException {
		contents.add(new Data(context, characters){
			@Override
			public void toSAX(ContentHandler contentHandler, LexicalHandler lexicalHandler, NamespaceSupport namespaces) throws SAXException {
				AttributeMap attributes = new AttributeMap();
				attributes.put("label", label);
				startElement(contentHandler, namespaces, "info", attributes);
				super.toSAX(contentHandler, lexicalHandler, namespaces);
				endElement(contentHandler, namespaces, "info");
			}
		});
	}
}
