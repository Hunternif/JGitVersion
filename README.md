# Versioning with git

As I was working with a team on a project using git, I decided to somehow automate labeling builds like `projectname-1.0.3.516`. With SVN this is easily achieved as each revision has its ordinal number. I searched around on StackOverflow and composed the following workflow:

* Any "milestone" version changes (i.e. first 2 or 3 numbers) are set manually via tags on branch `master`.
* Each commit subsequent to the tag adds to the build number. (Only for tags on `master`.)
* If building from a separate branch, its name should be included in the version.

## Example:

On `master`, the last tag is `v1.0` and there are 3 commits since that tag. You are working on a branch `feature` and have made 2 commits to it. Full version number for your build becomes `test-1.0.5`. If you switch back to `master`, full version number will become `1.0.3`.

This is partially achieved by command `git describe`. But as I am usually working on a Java project in Eclipse on Windows, I want an Ant build script. I also find it comfortable to manage the repository via Eclipse's egit, so I don't need to actually install git and have it in my `PATH` environment variable, which means that I can't call `<exec executable="git"/>' from the Ant script.

Having considered all that, I made this task for Ant.

# Using JGitVersion

Build the jar from sources and include it along with `JGit` and `gitective` in your project. Example of your Ant target:

```xml
<taskdef name="jgitversion" classname="hunternif.jgitversion.JGitVersionTask">
    <classpath>
        <pathelement path="path/to/jgitversion.jar"/>
        <pathelement path="path/to/gitective.jar"/>
        <pathelement path="path/to/jgit.jar"/>
    </classpath>
</taskdef>

<target name="build-release">
		<jgitversion dir="." property="build.version"/>
		<echo message="${build.version}" />
		...
</target>
```
