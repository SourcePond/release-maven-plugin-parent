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
package ch.sourcepond.maven.release.reactor;

import ch.sourcepond.maven.release.commons.PluginException;

@SuppressWarnings("serial")
public class ReactorException extends PluginException {

	public ReactorException(final Throwable cause, final String message, final Object... args) {
		super(cause, message, args);
	}

	public ReactorException(final String message, final Object... args) {
		super(message, args);
	}

	@Override
	public ReactorException add(final String format, final Object... args) {
		super.add(format, args);
		return this;
	}

}
