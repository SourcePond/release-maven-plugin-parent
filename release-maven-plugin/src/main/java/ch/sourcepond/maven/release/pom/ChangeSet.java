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
package ch.sourcepond.maven.release.pom;

/**
 * Represents a set of changed files. This interface extends
 * {@link AutoCloseable}; when {@link #close()} is called all changed files will
 * be reverted.
 * 
 *
 */
public interface ChangeSet extends AutoCloseable {

	/**
	 * Sets the exception to be thrown when {@link #close()} is called. If an
	 * exception is set and the revert of the changed files fails, the revert
	 * exception will only be logged. This is to keep the original exception so
	 * the root cause isn't lost. If no exception is set and the revert
	 * operation fails, the revert exception will be caused to be thrown because
	 * that is the root problem
	 * 
	 * @param message
	 *            Error message to be set
	 * @param failure
	 *            Exception which shall be caused to be thrown when
	 *            {@link #close()} is called.
	 */
	void setFailure(String message, Exception failure);

	@Override
	void close() throws ChangeSetCloseException;
}
