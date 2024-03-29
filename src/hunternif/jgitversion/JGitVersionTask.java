package hunternif.jgitversion;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.gitective.core.CommitFinder;
import org.gitective.core.CommitUtils;
import org.gitective.core.filter.commit.CommitCountFilter;

public class JGitVersionTask extends Task {
	private String dir;
	private String masterBranch;
	private String property;
	private boolean tagonly;
	
	public void setDir(String dir) {
		this.dir = dir;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public void setMasterBranch(String masterBranch) {
		this.masterBranch = masterBranch;
	}

	
	public void setTagonly(boolean tagonly) {
		this.tagonly = tagonly;
	}
	
	@Override
	public void execute() throws BuildException {
		try {
			String version = getProjectVersion(new File(dir));
			Project project = getProject();
			if (project != null) {
				project.setProperty(property, version);
			}
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}
	
	public String getProjectVersion(File repoDir) throws IOException, GitAPIException {
		Git git = Git.open(repoDir);
		Repository repo = git.getRepository();
		
		// Find base commit between current branch and "main":
		String branch = repo.getBranch();
		RevCommit base = CommitUtils.getBase(repo, this.masterBranch, branch);
		CommitCountFilter count = new CommitCountFilter();
		CommitFinder finder = new CommitFinder(repo).setFilter(count);
		finder.findBetween(branch, base);
		long commitsSinceBase = count.getCount();
		
		// Find tags in "master" before base commit:
		RevWalk rw = new RevWalk(repo);
		rw.markStart(base);
		rw.setRetainBody(false);
		Ref master = repo.getRef(this.masterBranch);
		List<Ref> masterAsList = Arrays.asList(master);
		List<Ref> tags = git.tagList().call();
		Map<RevCommit, Ref> masterTags = new HashMap<RevCommit, Ref>();
		for (Ref tag : tags) {
			tag = repo.peel(tag);
			ObjectId commitID = tag.getPeeledObjectId();
			if (commitID == null) continue;
			RevCommit commit = rw.parseCommit(commitID);
			// Only remember tags reachable from "master":
			if (!RevWalkUtils.findBranchesReachableFrom(commit, rw, masterAsList).isEmpty()) {
				masterTags.put(commit, tag);
			}
		}
		
		// Find the shortest distance in commits between base tag in "master":
		long commitsBetweenBaseAndTag = Long.MAX_VALUE;
		String tagName = "";
		for (RevCommit tagCommit : masterTags.keySet()) {
			count.reset();
			finder.findBetween(base, tagCommit);
			if (count.getCount() < commitsBetweenBaseAndTag) {
				commitsBetweenBaseAndTag = count.getCount();
				tagName = masterTags.get(tagCommit).getName();
			}
		}
		if (commitsBetweenBaseAndTag == Long.MAX_VALUE) {
			// If no tag, get total number of commits:
			commitsBetweenBaseAndTag = repo.getRefDatabase().getRefs("").size();
		}
		long commitsSinceLastMasterTag = commitsSinceBase + commitsBetweenBaseAndTag;
		
		// Construct version string:
		String version = branch.equals(this.masterBranch) ? "" : (branch + "-");
		if (tagName.startsWith("refs/tags/")) {
			tagName = tagName.substring("refs/tags/".length());
		}
		// v1.1 -> 1.1
		if (tagName.matches("v\\d+.*")) {
			tagName = tagName.substring(1);
		}
		if (tagName.isEmpty()) {
			version = "0";
		}
		version += tagName + ((!tagonly) ? "." + commitsSinceLastMasterTag : "");
		
		return version;
	}
}
