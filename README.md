Versioning with Git
===================

As I was working with a team on a project using Git, I decided to somehow automate labeling builds like `projectname-1.0.3.516`. With SVN this is easily achieved as each revision has its ordinal number, but in Git a revision is instead indentified by a SHA hash, and branching is used commonly and extensively, which further complicates things. So I searched around on StackOverflow and composed the following workflow:

* Any "milestone" version changes (i.e. first 2 or 3 numbers) are set manually via tags on branch `master`.
* Each commit subsequent to the tag adds to the build number. (Only for tags on `master`.)
* If building from a separate branch, its name should be included in the version.

> Example:  
> On `master`, the last tag is named `v1.0` and 3 commits have been made since that tag. You are working on a branch `feature` (beginning from the last commit in `master`) and have made 2 commits to it. Full version number for your build becomes `feature-1.0.5`. If you switch back to `master`, full version number will become `1.0.3`.

This is partially achieved by command `git describe`. But as I am usually working on a Java project in Eclipse, I want an Ant build script; from Ant I can call custom java code, but the current version of JGit doesn't support `describe`. I also find it comfortable to manage the repository via Eclipse's egit, so I don't need to actually install Git and, on Windows, have it in my `PATH` environment variable, which means that I can't call `<exec executable="git"/>` from the Ant script.

Having considered all that, I created this custom Ant task.

Using JGitVersion
=================

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
