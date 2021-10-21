package scc.srv;

import scc.srv.Resources.ControlResource;
import scc.srv.Resources.MediaResource;
import scc.srv.Resources.MessageResource;
import scc.srv.Resources.UserResource;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public MainApplication() {
		resources.add(ControlResource.class);
//		resources.add(MediaResource.class);
		resources.add(UserResource.class);
		resources.add(MessageResource.class);
		singletons.add( new MediaResource());
		//singletons.add( new UserResource());
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
