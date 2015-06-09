Xikolo Android App Signing
==========================

The Key store _xikolo.jks_ can be found in the root directory. Please use this Key store to sign Release APKs.

## Signing Process

If your build is stable and should be released

1. Open the app/build.gradle file
2. Increase the versionName semantically
3. Increase the versionCode by 1

Then in Android Studio

1. Go to Build -> Generate Signed APK...
2. Choose _app_ as Module
3. Click Next
4. Choose the _xikolo.jks_ file for the Key store Path
5. Enter Key store password
6. Choose the Key alias
7. Enter Key password
8. Click Next
9. Choose _app/build_ as APK Destination Folder
10. Choose Build Type _release_
11. Choose the right Flavor matching to the previous Key alias
12. Click Finish
