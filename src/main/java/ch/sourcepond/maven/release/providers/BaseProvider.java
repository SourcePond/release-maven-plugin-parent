package ch.sourcepond.maven.release.providers;

import static org.apache.commons.lang3.Validate.notNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.inject.Provider;

public abstract class BaseProvider<T> implements Provider<T> {
	protected final MavenComponentSingletons singletons;

	protected BaseProvider(final MavenComponentSingletons pSingletons) {
		singletons = pSingletons;
	}

	protected abstract T getDelegate();

	protected abstract Class<T> getDelegateType();

	@SuppressWarnings("unchecked")
	@Override
	public T get() {
		return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { getDelegateType() },
				new InvocationHandler() {

					@Override
					public Object invoke(final Object proxy, final Method method, final Object[] args)
							throws Throwable {
						notNull(getDelegate(), "Delegate object is not available!");
						return method.invoke(getDelegate(), args);
					}
				});
	}
}
