package hunternif.jgitversion;

import java.io.File;

public class TestGit {
	public static void main(String[] args) {
		try {
			JGitVersionTask task = new JGitVersionTask();
			task.setMasterBranch("master");
			System.out.println(task.getProjectVersion(new File(".")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
