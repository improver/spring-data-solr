/*
 * Copyright 2012 - 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.solr.core.query.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.Field;
import org.springframework.data.solr.core.query.SimpleField;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.util.ObjectUtils;

/**
 * Base implementation of page holding solr response entities.
 * 
 * @author Christoph Strobl
 * 
 */
public class SolrResultPage<T> extends PageImpl<T> implements FacetPage<T>, HighlightPage<T> {

	private static final long serialVersionUID = -4199560685036530258L;

	private Map<PageKey, Page<FacetFieldEntry>> facetResultPages = new LinkedHashMap<PageKey, Page<FacetFieldEntry>>(1);
	private Page<FacetQueryEntry> facetQueryResult;
	private List<HighlightEntry<T>> highlighted;

	public SolrResultPage(List<T> content) {
		super(content);
	}

	public SolrResultPage(List<T> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	@Override
	public final Page<FacetFieldEntry> getFacetResultPage(String fieldname) {
		Page<FacetFieldEntry> page = this.facetResultPages.get(new StringPageKey(fieldname));
		return page != null ? page : new PageImpl<FacetFieldEntry>(Collections.<FacetFieldEntry> emptyList());
	}

	@Override
	public final Page<FacetFieldEntry> getFacetResultPage(Field field) {
		return this.getFacetResultPage(field.getName());
	}

	public final void addFacetResultPage(Page<FacetFieldEntry> page, Field field) {
		this.facetResultPages.put(new StringPageKey(field.getName()), page);
	}

	public void addAllFacetFieldResultPages(Map<Field, Page<FacetFieldEntry>> pageMap) {
		for (Map.Entry<Field, Page<FacetFieldEntry>> entry : pageMap.entrySet()) {
			addFacetResultPage(entry.getValue(), entry.getKey());
		}
	}

	@Override
	public Collection<Page<FacetFieldEntry>> getFacetResultPages() {
		return Collections.unmodifiableCollection(this.facetResultPages.values());
	}

	public final void setFacetQueryResultPage(List<FacetQueryEntry> facetQueryResult) {
		this.facetQueryResult = new PageImpl<FacetQueryEntry>(facetQueryResult);
	}

	@Override
	public Page<FacetQueryEntry> getFacetQueryResult() {
		return this.facetQueryResult != null ? this.facetQueryResult : new PageImpl<FacetQueryEntry>(
				Collections.<FacetQueryEntry> emptyList());
	}

	@Override
	public Collection<Field> getFacetFields() {
		if (this.facetResultPages.isEmpty()) {
			return Collections.emptyList();
		}
		List<Field> fields = new ArrayList<Field>(this.facetResultPages.size());
		for (PageKey pageKey : this.facetResultPages.keySet()) {
			fields.add(new SimpleField(pageKey.getKey().toString()));
		}
		return fields;
	}

	@Override
	public Collection<Page<? extends FacetEntry>> getAllFacets() {
		List<Page<? extends FacetEntry>> entries = new ArrayList<Page<? extends FacetEntry>>(
				this.facetResultPages.size() + 1);
		entries.addAll(this.facetResultPages.values());
		entries.add(this.facetQueryResult);
		return entries;
	}

	@Override
	public List<HighlightEntry<T>> getHighlighted() {
		return this.highlighted != null ? this.highlighted : Collections.<HighlightEntry<T>> emptyList();
	}

	public void setHighlighted(List<HighlightEntry<T>> highlighted) {
		this.highlighted = highlighted;
	}

	@Override
	public List<Highlight> getHighlights(T entity) {
		if (entity != null && this.highlighted != null) {
			for (HighlightEntry<T> highlightEntry : this.highlighted) {
				if (highlightEntry != null && ObjectUtils.nullSafeEquals(highlightEntry.getEntity(), entity)) {
					return highlightEntry.getHighlights();
				}
			}
		}
		return Collections.emptyList();
	}

}
