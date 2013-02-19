/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing all DC inputs required for a submission, organized into pages
 *
 * @author Brian S. Hughes, based on work by Jenny Toves, OCLC
 * @version $Revision$
 */

public class DCInputSet
{
	/** name of the input set  */
	private String formName = null; 
	/** the list of properly initialized pages of the input set */
	private List<DCInput.Page> pages = null;
	
	/** constructor */
	public DCInputSet(String formName, List<DCInput.Page> pages)
	{
		this.formName = formName;
		this.pages = pages;
	}
	
	/**
	 * Return the name of the form that defines this input set
	 * @return formName 	the name of the form
	 */
	public String getFormName()
	{
		return formName;
	}
	
	/**
	 * Return the number of pages in this  input set
	 * @return number of pages
	 */
	public int getNumberPages()
	{
		return pages.size();
	}
	
    /**
     * Get all the rows for a page from the form definition
     *
     * @param  pageNum	desired page within set
     * @param  addTitleAlternative flag to add the additional title row
     * @param  addPublishedBefore  flag to add the additional published info
     *
     * @return  an array containing the page's displayable rows
     */
	
	public DCInput[] getPageRows(int pageNum, boolean addTitleAlternative,
		      					 boolean addPublishedBefore)
	{
		List<DCInput> filteredInputs = new ArrayList<DCInput>();
		if ( pageNum < pages.size() )
		{
			DCInput.Page page = pages.get(pageNum);
			DCInput[] inputs = page.getInputs();
			for(DCInput input : inputs)
			{
				if (doField(input, addTitleAlternative, addPublishedBefore))
				{
					filteredInputs.add(input);
				}
			}
		}

		// Convert list into an array
		DCInput[] inputArray = new DCInput[filteredInputs.size()];
		return filteredInputs.toArray(inputArray);
	}
	
    /**
	 * Gets the title for a page from the form definition
	 *
	 * @param  pageNum desired page within set
	 *
	 * @return The title or <code>null</code> if not set
	 */

	public String getPageTitle(int pageNum){
		if( pageNum < pages.size() )
		{
			DCInput.Page page = pages.get(pageNum);
			return page.getTitle();
		}
		return null;
	}

    /**
     * Does this set of inputs include an alternate title field?
     *
     * @return true if the current set has an alternate title field
     */
    public boolean isDefinedMultTitles()
    {
    	return isFieldPresent("title.alternative");
    }
    
    /**
     * Does this set of inputs include the previously published fields?
     *
     * @return true if the current set has all the prev. published fields
     */
    public boolean isDefinedPubBefore()
    {
    	return ( isFieldPresent("date.issued") && 
    			 isFieldPresent("identifier.citation") &&
				 isFieldPresent("publisher.null") );
    }
    
    /**
     * Does the current input set define the named field?
     * Scan through every field in every page of the input set
     *
     * @return true if the current set has the named field
     */
    public boolean isFieldPresent(String fieldName)
    {
    	for (int i = 0; i < pages.size(); i++)
    	{
    		DCInput.Page page = pages.get(i);
    		DCInput[] inputs = page.getInputs();
    		for (DCInput input : inputs)
    		{
    			String fullName = input.getElement() + "." +
				              	  input.getQualifier();
    			if (fullName.equals(fieldName))
    			{
    				return true;
    			}
    		}
	    }
    	return false;
    }
	
    private static boolean doField(DCInput dcf, boolean addTitleAlternative, 
		    					   boolean addPublishedBefore)
    {
    	String rowName = dcf.getElement() + "." + dcf.getQualifier();
    	if ( rowName.equals("title.alternative") && ! addTitleAlternative )
    	{
    		return false;
    	}
    	if (rowName.equals("date.issued") && ! addPublishedBefore )
    	{
    		return false;
    	}
    	if (rowName.equals("publisher.null") && ! addPublishedBefore )
    	{
    		return false;
    	}
    	if (rowName.equals("identifier.citation") && ! addPublishedBefore )
    	{
    		return false;
    	}

    	return true;
    }
}
