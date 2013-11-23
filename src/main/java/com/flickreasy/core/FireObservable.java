package com.flickreasy.core;

import java.util.Observable;

public class FireObservable extends Observable {

	public void fire(Object obj) {
		this.setChanged();
		this.notifyObservers(obj);
	}
	
}