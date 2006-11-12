package org.codejive.web.weblog;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.codejive.common.CodejiveException;

public class PeriodIterator {
	private Integer key;
	private Collection<Dated> items;
	
	private PeriodIterator(Integer _key) {
		key = _key;
		items = new LinkedList<Dated>();
	}
	
	public PeriodIterator(Collection<Dated> _items) {
		items = _items;
	}
	
	public SortedMap<Integer, PeriodIterator> getYears() throws CodejiveException {
		return getItemsByDatePart(Calendar.YEAR);
	}
	
	public SortedMap<Integer, PeriodIterator> getMonths() throws CodejiveException {
		return getItemsByDatePart(Calendar.MONTH);
	}
	
	public SortedMap<Integer, PeriodIterator> getDays() throws CodejiveException {
		return getItemsByDatePart(Calendar.DAY_OF_MONTH);
	}
	
	private SortedMap<Integer, PeriodIterator> getItemsByDatePart(int datePart) throws CodejiveException {
		Calendar cal = Calendar.getInstance();
		SortedMap<Integer, PeriodIterator> result = new TreeMap<Integer, PeriodIterator>();
		for (Dated d : items) {
			Date date = d.getDate();
			cal.setTime(date);
			Integer key = new Integer(cal.get(datePart));
			PeriodIterator pi = result.get(key);
			if (pi == null) {
				pi = new PeriodIterator(key);
				result.put(key, pi);
			}
			pi.items.add(d);
		}
		return result;
	}

	public Integer getKey() {
		return key;
	}
	
	public Collection<Dated> getItems() {
		return Collections.unmodifiableCollection(items);
	}
}
