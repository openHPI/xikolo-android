Xikolo Android App
==================

[![Build Status](https://travis-ci.org/openHPI/xikolo-android.svg?branch=master)](https://travis-ci.org/openHPI/xikolo-android)

The official Android App for [openHPI](https://open.hpi.de/), [openSAP](https://open.sap.com/), [mooc.house](https://mooc.house/) and [OpenWHO](https://openwho.org/).

## Setup

Clone git repository:

```
git clone --recursive https://github.com/openHPI/xikolo-android
```

### Development

Please use the [Android Studio](https://developer.android.com/sdk/) IDE, since we rely on the Gradle build system.

### Release Builds

Signing credentials are protected with [`git-crypt`](https://github.com/AGWA/git-crypt/). To compile release builds, [install](https://www.agwa.name/projects/git-crypt/) `git-crypt`:
```
brew install git-crypt
```
And unlock the encrypted files:
```
git-crypt unlock /path/to/xikolo-android.key
```
The keyfile is managed by the openHPI team and should never be made public or added to the repository.

## Version History

Please push a git [tag](https://github.com/openHPI/xikolo-android/tags) for every released build.

## Google Play Links

- openHPI on [Google Play](https://play.google.com/store/apps/details?id=de.xikolo.openhpi)
- openSAP on [Google Play](https://play.google.com/store/apps/details?id=de.xikolo.opensap)
- mooc.house on [Google Play](https://play.google.com/store/apps/details?id=de.xikolo.moochouse)
- OpenWHO on [Google Play](https://play.google.com/store/apps/details?id=de.xikolo.openwho)

## Contributing

Please feel free to help us with the ongoing development of this app. See open [issues](https://github.com/openHPI/xikolo-android/issues) to get some inspiration and open a Merge Request as soon as you are ready to go.
