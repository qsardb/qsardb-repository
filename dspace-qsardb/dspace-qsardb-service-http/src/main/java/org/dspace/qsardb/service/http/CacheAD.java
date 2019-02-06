/*
 *  Copyright (c) 2019 University of Tartu
 */
package org.dspace.qsardb.service.http;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.dspace.qsardb.service.ApplicabilityDomain;
import org.qsardb.model.Model;

class CacheAD {

	private final Cache<String, ApplicabilityDomain> cache;

	public CacheAD(long maxSize) {
		cache = CacheBuilder.newBuilder().maximumSize(maxSize).build();
	}

	public ApplicabilityDomain get(String handle, Model model) {
		String key = handle + "/" + model.getId();
		ApplicabilityDomain ad = cache.getIfPresent(key);
		return ad != null ? ad : load(key, model);
	}

	private ApplicabilityDomain load(String key, final Model model) {
		try {
			return cache.get(key, new Callable<ApplicabilityDomain>() {
				@Override
				public ApplicabilityDomain call() {
					return new ApplicabilityDomain(model);
				}
			});
		} catch (ExecutionException ex) {
			throw new IllegalArgumentException("Can't load AD for: "+key, ex);
		}
	}

}
