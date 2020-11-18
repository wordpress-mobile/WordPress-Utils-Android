import {fail, danger} from "danger";

export default async () => {
  if (danger.github.pr.base.ref != "develop") { return }

  const modified_build_gradles = danger.git.modified_files.filter((path: string) => path.endsWith('build.gradle'))

  if (modified_build_gradles.length == 0) {
    fail("Please update the library version before merging into `develop`")
  }

  // TODO: need to support multiple files
  var matchFound = false
  for (let file of modified_build_gradles) {
    const diff = await danger.git.diffForFile(file)

    // We could be more refiened and actually pares the .before and .after
    // contents to extract the versionName value and do a SemVer check on it to
    // make sure it's appropriate
    matchFound = diff.after.includes('versionName "')
    if (matchFound) { break }
  }

  if (matchFound == false) {
    fail("Please update the library version before merging into `develop`")
  }
}
