# YTCatcher

## A YouTube video downloader for Android devices

### Overview
This simple YouTube video downloader is made for a university project. It's completely written in Java for Android devices (I used Android Studio). With my little program now you don't even have to paste the URL of the video somewhere in order to get it! When launched or resumed, the system clipboard is checked to see what's in it: if there is a YouTube video URL, the program automatically detects the formats in which it is avaliable and enumerates them. You only have to choose the format you want and wait for the video to download.

### How it works
The program checks the clipboard and parse it with a Regex. If it matches, an AsyncTask is executed, in which the program downloads the page source file in order to get the desired informations about the video, such as the title or the various avaliable format. When the user choose the desired format, a new AsyncTask is launched: this time it opens an input stream directly with the desired video file and, passing through an output stream, saves it on a file.

### Author
*Castellini Jacopo*
