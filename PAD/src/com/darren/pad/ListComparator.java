package com.darren.pad;

import java.util.Comparator;

public class ListComparator implements Comparator<App> {

	@Override
	public int compare(App app1, App app2) {
		return app2.getTimeCount() - app1.getTimeCount();
	}


}
