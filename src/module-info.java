/**
 * 
 */
/**
 * @author osboxes
 *
 */
module practica2 {
	exports recipes_service.data;
	exports recipes_service.test;
	exports recipes_service.activity_simulation;
	exports recipes_service.tsae.sessions;
	exports recipes_service.communication;
	exports util;
	exports lsim.element.coordinator;
	exports lsim.element.recipes_service;
	exports recipes_service.tsae.data_structures;
	exports lsim.element.evaluator;
	exports communication;
	exports recipes_service;

	requires DSLabStorage;
	requires java.desktop;
	requires lsim.commons;
	requires lsim.library;
}