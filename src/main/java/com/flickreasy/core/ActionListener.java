package com.flickreasy.core;

import java.util.ArrayList;
import java.util.List;

public class ActionListener {

	private List<Action> actionList = new ArrayList<Action>();
	
	public void add(Action action) {
		actionList.add(action);
	}
	
	public void remove(Action action) {
		actionList.remove(action);
	}
	
	public void doExecute() {
		for (Action action : actionList) {
			action.execute();
		}
	}
	
}
