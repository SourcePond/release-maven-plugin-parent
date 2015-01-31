package e2e;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListTagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static scaffolding.GitMatchers.hasTag;

public class JGitDiscoveryTest {
    Repository repo;

    @Before public void locateRepo() throws IOException {
        repo = new FileRepository(findSubFolder(".git"));
    }

    @After public void closeRepo() {
        repo.close();
    }

    @Test public void showMeTheLog() throws IOException, GitAPIException {
        Git git = new Git(repo);
        Iterable<RevCommit> log = git.log().call();
        for (RevCommit revCommit : log)
            System.out.println(revCommit.getFullMessage().trim());
    }

    @Test
    public void hasTagOrHasNot() throws GitAPIException {
        Git git = new Git(repo);
        ListTagCommand listTagCommand = git.tagList();
        for (Ref ref : listTagCommand.call()) {
            System.out.println("ref = " + ref.getName());
        }
        assertThat(git, hasTag("tag-for-jgit-discovery-test"));
        assertThat(git, not(hasTag("some-non-existent-tag")));
    }

    @Test public void name() throws IOException, GitAPIException {
        ObjectId head = repo.resolve("HEAD^{tree}");
        ObjectId oldHead = repo.resolve("HEAD^^{tree}");

        System.out.println("Diff between: " + oldHead + " and " + head);

        ObjectReader reader = repo.newObjectReader();

        CanonicalTreeParser prevParser = new CanonicalTreeParser();
        prevParser.reset(reader, oldHead);

        CanonicalTreeParser headParser = new CanonicalTreeParser();
        headParser.reset(reader, head);

        List<DiffEntry> diffs = new Git(repo).diff()
                .setNewTree(headParser)
                .setOldTree(prevParser)
                .call();

        for (DiffEntry entry : diffs)
            System.out.println(entry);
    }

    private File findSubFolder(String subFolder) {
        return findFolder(new File(".").getAbsoluteFile(), subFolder);
    }

    private File findFolder(File folder, String subFolder) {
        File candidateFolder = new File(folder, subFolder);
        return candidateFolder.exists() && candidateFolder.isDirectory() ? candidateFolder : findFolder(folder.getParentFile(), subFolder);
    }
}