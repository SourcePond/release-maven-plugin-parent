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
package ch.sourcepond.maven.release.scm;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.lib.Ref;

public interface SCMRepository {

	ProposedTag fromRef(Ref gitTag) throws SCMException;

	void errorIfNotClean() throws SCMException;

	boolean hasLocalTag(String tag) throws SCMException;

	void revertChanges(Collection<File> changedFiles) throws SCMException;

	Collection<Long> getRemoteBuildNumbers(String artifactId, String versionWithoutBuildNumber)
			throws SCMException;

	Collection<ProposedTag> tagsForVersion(String module, String versionWithoutBuildNumber) throws SCMException;

	ProposedTagsBuilder newProposedTagsBuilder() throws SCMException;

	void checkValidRefName(String releaseVersion) throws SCMException;

	boolean hasChangedSince(String modulePath, List<String> childModules, Collection<ProposedTag> tags)
			throws SCMException;

	void pushChanges(Collection<File> changedFiles) throws SCMException;
}