# WordPress-Utils-Android

Collection of utility methods for Android and WordPress.

## Use the library in your project

* In your `build.gradle`:
```groovy
repositories {
    maven { url "https://a8c-libs.s3.amazonaws.com/android" }
}

dependencies {
    implementation 'org.wordpress:utils:2.0.0'
}
```

## Publishing a new version

In the following cases, CircleCI will publish a new version with the following format to our remote Maven repo:

* For each commit in an open PR: `<PR-number>-<commit full SHA1>`
* Each time a PR is merged to `develop`: `develop-<commit full SHA1>`
* Each time a new tag is created: `{tag-name}`

## Apps and libraries using WordPress-Utils-Android:

- [WordPress for Android][2]
- [FluxC][3]

## License
Dual licensed under MIT, and GPL.

[1]: https://github.com/wordpress-mobile/WordPress-Utils-Android/blob/a9fbe8e6597d44055ec2180dbf45aecbfc332a20/WordPressUtils/build.gradle#L37
[2]: https://github.com/wordpress-mobile/WordPress-Android
[3]: https://github.com/wordpress-mobile/WordPress-FluxC-Android
