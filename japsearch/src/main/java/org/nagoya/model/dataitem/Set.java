package org.nagoya.model.dataitem;


import io.vavr.control.Option;

public class Set extends MovieDataItem {

	public static Option<Set> of(String s)
	{
		return (s == null)? Option.none() : Option.of(new Set(s));
	}

	private String set;

	public String getSet() {
		return set;
	}

	public void setSet(String set) {
		this.set = sanitizeString(set);
	}

	public Set(String set) {
		setSet(set);
	}

	@Override
	public String toString() {
		return "Set [set=\"" + set + "\"" + dataItemSourceToString() + "]";
	}

	@Override
	public String toXML() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set() {
		set = "";
	}

}
