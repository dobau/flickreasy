package com.flickreasy.core;

import java.util.Observer;

public class UploadListener {

	private FireObservable onSuccess = new FireObservable();
	private FireObservable onFailure = new FireObservable();
	private FireObservable onComplete = new FireObservable();

	public void addOnSuccess(Observer observer) {
		onSuccess.addObserver(observer);
	}

	public void addOnFailure(Observer observer) {
		onFailure.addObserver(observer);
	}

	public void addOnComplete(Observer observer) {
		onComplete.addObserver(observer);
	}

	public void fireOnSuccess(Object obj) {
		onSuccess.fire(obj);
	}

	public void fireOnFailure(Object obj) {
		onFailure.fire(obj);
	}

	public void fireOnComplete(Object obj) {
		onComplete.fire(obj);
	}

}
