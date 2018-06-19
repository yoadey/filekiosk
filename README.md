# filekiosk

This is a small project, to enable a simple kiosk screen on small devices, e.g. raspberry pi.
It scans a folder on a regular basis to check, for new files and shows all files in this folder in a sequential order.
The name of the images should have the following format: [index]-[duration].png, e.g. the image 1-10.png would
be the first image and displayed 10 seconds.<br/>
The application is always shown on the first monitor, two monitors are not supported.<br/>
However, if the application is launched on multiple computer, the images are synchronized.<br/>
To have multiple screens synchronized, the first image is loaded 12:00pm and the image next to load is
calculated depending on how many images with given time intervals are loaded.
