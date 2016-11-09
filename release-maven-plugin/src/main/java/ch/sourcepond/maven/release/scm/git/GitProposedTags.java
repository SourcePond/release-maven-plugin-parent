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
package ch.sourcepond.maven.release.scm.git;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;

import java.util.Iterator;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

import ch.sourcepond.maven.release.commons.Version;
import ch.sourcepond.maven.release.scm.ProposedTag;
import ch.sourcepond.maven.release.scm.ProposedTags;
import ch.sourcepond.maven.release.scm.SCMException;


final class GitProposedTags implements ProposedTags {
	static final String KEY_FORMAT = "%s/%s";
	private final Log log;
	private final Map<String, ProposedTag> proposedTags;

	GitProposedTags(final Log pLog, final Map<String, ProposedTag> proposedTags) {
		this.log = pLog;
		this.proposedTags = proposedTags;
	}

	@Override
	public void tag() throws SCMException {
		for (final ProposedTag tag : proposedTags.values()) {
			tag.tagAndPush();
		}
	}

	@Override
	public ProposedTag getTag(final String tag, final Version version) throws SCMException {
		final String key = toKey(tag, version);
		final ProposedTag proposedTag = proposedTags.get(key);
		if (proposedTag == null) {
			throw new SCMException("No proposed tag registered for %s", key);
		}
		return proposedTag;
	}

	static String toKey(final String tag, final Version version) {
		return format(KEY_FORMAT, tag, version.getReleaseVersion());
	}

	@Override
	public Iterator<ProposedTag> iterator() {
		return unmodifiableCollection(proposedTags.values()).iterator();
	}

	@Override
	public void undoTag() {
		for (final ProposedTag tag : proposedTags.values()) {
			try {
				tag.delete();
			} catch (final SCMException e) {
				log.warn(e);
			}
		}
	}
}