package com.teaminversion.envisionbuddy;

import java.util.List;

public class Response{
	private String next;
	private Cursors cursors;
	private Object previous;
	private List<ResultsItem> results;

	public String getNext(){
		return next;
	}

	public Cursors getCursors(){
		return cursors;
	}

	public Object getPrevious(){
		return previous;
	}

	public List<ResultsItem> getResults(){
		return results;
	}
}