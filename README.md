# UICrawler
UICralwer is a framework that explores UIs automatically using DFS(Depth-first-search). It dumps XML and image files into sdcard.


UICrawler is based on UIAutomator 2.0 API, which is orginally used for Android testing framework.

## Usage
1. Create a text file named "app_list" that contains all the apps you want to do UI-crawling.

The format should be one app name per line.

2. Push file to /sdcard/ :

adb push app_list /sdcard/


3. Run this UICrawler by run "Main"


