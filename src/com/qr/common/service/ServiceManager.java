package com.qr.common.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;

public class ServiceManager {

	private static final String APP_NAME = "jsf_app1";
	static Properties jndiProperties = new Properties();
	private static Map<Class<?>, String> serviceNames = new HashMap<Class<?>, String>(0);
	private static boolean initialized = false;
	
	private ServiceManager() {
		
	}

	static {
		init();
	}

	public static void init() {
		try {
			InputStream inputStream = ServiceManager.class.getClassLoader().getResourceAsStream("jndi.properties");
			jndiProperties = new Properties();
			jndiProperties.load(inputStream);
			initialized = true;
			
			inputStream.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static Object getService(Class<?> clazz) {
		if (!initialized) {
			init();
		}
		return getServiceByJndiName(getGlobalJndiName(clazz));
	}

	private static Object getServiceByJndiName(String serviceName) {
		try {
			return lookupRemoteService(serviceName);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return null;
	}

	private static String getGlobalJndiName(Class<?> clazz) {
		String jndiName = serviceNames.get(clazz);

		if (jndiName == null) {
			if (clazz.getSimpleName().endsWith("Remote")) {
				jndiName = "java:global/" + APP_NAME + "/" + clazz.getSimpleName().substring(0, (clazz.getSimpleName().lastIndexOf("Remote"))) + "!" + clazz.getName();
			} else if (clazz.getSimpleName().endsWith("Local")) {
				jndiName = "java:global/" + APP_NAME + "/" + clazz.getSimpleName().substring(0, (clazz.getSimpleName().lastIndexOf("Local"))) + "!" + clazz.getName();
			}

			serviceNames.put(clazz, jndiName);
		}

		return jndiName;
	}

	private static Object lookupRemoteService(String remoteServiceName) throws Exception {
		return getInitialContext().lookup(remoteServiceName);
	}

	public static InitialContext getInitialContext() throws Exception {
		return new InitialContext(jndiProperties);
	}
}
