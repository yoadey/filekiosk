# filekiosk

This is a small project, to enable a simple kiosk screen on small devices, e.g. raspberry pi.
It scans a folder on a regular basis to check, for new files and shows all files in this folder in a sequential order.
The name of the images should have the following format: index-duration.png, e.g. the image 1-10.png would
be the first image and displayed 10 seconds.<br/>
To display the images, open with a browser the following URL, preferable in fullscreen mode: TODO<br/>
To have multiple screens synchronized, the first image is loaded 12:00pm and the image next to load is
calculated depending on how many images with given time intervals are loaded.
