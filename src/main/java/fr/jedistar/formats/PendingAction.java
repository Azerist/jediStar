package fr.jedistar.formats;

import java.awt.List;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;

import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.Reaction;

public class PendingAction {

	//The user that's expected to do the action
	private User user;
	
	//The method that should be called
	private String methodName;
	
	//The object on which this method should be called
	private Object object;
	
	//The arguments for the method call
	private Object[] args;
	
	//The message on which the action is expected to be made
	private Message message;
	
	//The time at which this action expires
	private Calendar expiration;

	/**
	 * 
	 * @param user
	 * @param method : A method that should return a String and take a Reaction as first argument
	 * @param object
	 * @param args
	 * @param message
	 * @param expiration : the time in minutes in which this action will expire
	 */
	public PendingAction(User user, String methodName, Object object, Message message, Integer expiration,Object... args) {

		this.user = user;
		this.methodName = methodName;
		this.object = object;
		this.args = args;
		this.message = message;

		Calendar cal = Calendar.getInstance();

		cal.add(Calendar.MINUTE, expiration);

		this.expiration = cal;
	}
	
	public boolean isExpired() {
		return expiration.compareTo(Calendar.getInstance()) < 0;
	}
	
	
	public String doAction(Reaction reaction) {
		try {
			ArrayList<Class<?>> paramsTypes = new ArrayList<Class<?>>();
			
			paramsTypes.add(Reaction.class);
			
			for(Object arg : args) {
				paramsTypes.add(arg.getClass());
			}
			
			Class<?>[] classesArray = (Class<?>[]) paramsTypes.toArray();
			
			Method method = object.getClass().getMethod(methodName, classesArray);
			
			if(method == null) {
				return "A problem happened while executing this action";
			}
			
			return (String) method.invoke(object, reaction, args);
		}
		catch(Exception e) {
			e.printStackTrace();
			return "A problem happened while executing this action";
		}
	}

	public User getUser() {
		return user;
	}

	public Message getMessage() {
		return message;
	}
	
}
