/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.maven.release.providers;

import static org.apache.commons.lang3.Validate.notNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
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
						try {
							return method.invoke(getDelegate(), args);
						} catch (final InvocationTargetException e) {
							throw e.getTargetException();
						}
					}
				});
	}
}
