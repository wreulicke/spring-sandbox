package com.github.wreulicke.transactionaggregation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TestService {
	
	Logger log = LoggerFactory.getLogger(TestService.class);
	
	public void execute(Model model) {
		log.info("executing with {}", model);
		List<ModelUpdateEvent> resource = (List<ModelUpdateEvent>) TransactionSynchronizationManager.getResource(this);
		if (resource == null) {
			ArrayList<ModelUpdateEvent> models = new ArrayList<>();
			models.add(new ModelUpdateEvent(model));
			TransactionSynchronizationManager.bindResource(this, models);
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
				
				@Override
				public void afterCommit() {
					models.forEach(model -> log.info("found model: {}", model));
				}
				
				@Override
				public void beforeCompletion() {
					TransactionSynchronizationManager.unbindResource(TestService.this);
				}
			});
		} else {
			resource.add(new ModelUpdateEvent(model));
		}
		
	}
	
	public static class Model {
		
		private final String name;
		
		public Model(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return "Model{" +
				"name='" + name + '\'' +
				'}';
		}
	}
	
	public static class ModelUpdateEvent {
		
		private final Model model;
		
		public ModelUpdateEvent(Model model) {
			this.model = model;
		}
		
		@Override
		public String toString() {
			return "ModelUpdateEvent{" +
				"model=" + model +
				'}';
		}
	}
	
}
