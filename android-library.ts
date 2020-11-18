import {fail, danger} from "danger";

// We want each PR towards develop or trunk of our Android libraries to bump
// the library version. This check makes sure of it.
export default async () => {
  const baseBranch = danger.github.pr.base.ref
  if (baseBranch != "develop" && baseBranch != "trunk") { return }

  // Android projects have more than one build.gradle file, to be able to run
  // this check on any library, lets's just look into _all_ the build.gradle
  // files.
  const modified = danger.git.modified_files
    .filter((path: string) => path.endsWith('build.gradle'))

  for (let file of modified) {
    const diff = await danger.git.diffForFile(file)

    // We could be more refiened and actually pares the .before and .after
    // contents to extract the versionName value and do a SemVer check on it to
    // make sure it's appropriate.
    //
    // The '"' is a way to make user the actual versionName definition change,
    // not a line using versionName.
    const matchFound = diff.after.includes('versionName "')
    if (matchFound) { return }
  }

  // If we're here, no match was found: fail the build.
  fail(`Please update the library version before merging into \`${baseBranch}\``)
}
