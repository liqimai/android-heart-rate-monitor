Heart Rate Monitor
==========================

Android heart rate monitor

## Introduction

Android based heart rate monitor which uses the camera and its flash to determine the users heart rate in beats per minute.

Origionally Created by Justin Wetherell. Here is his orginal code:
* Google: http://code.google.com/p/android-heart-rate-monitor
* Github: http://github.com/phishman3579/android-heart-rate-monitor
* LinkedIn: http://www.linkedin.com/in/phishman3579
* E-mail: phishman3579@gmail.com
* Twitter: http://twitter.com/phishman3579

Modified by Li Qimai, Dou Quan and Ba Meng.
* We applyed another algorithm, a new UI and refactoring the codes.
* This app looks much better now
* a more concrete performance
* give the result with a much more short delay. Only 3-5 seconds to get result. The Origial app needs dozens of seconds.

## Details
The App uses the PreviewCallback mechanism to grab the latest image from the preview frame. It then processes the YUV420SP data and pulls out all the green pixel values.

It uses data smoothing in a Integer array to figure out the average pixel pixel value in the image. Once it figures out the average it determines a heart beat when the average green pixel value in the latest image is greater than the smoothed average.

This app will record the time spend by recent 6 beats. Then display the Heart Rate on-line.

## How To

All you have to do is open the Heart App and then hold the tip of your index finger over the camera lens of your phone. The entire camera preview image should be red with a lighter area where the tip of your finger is touching. Do not press too hard or you will cut off circulation which will result in an inaccurate reading.  

After a second or two, you should see the heart icon start to flash big when it senses a heart beat. After 2-3 seconds it will compute your heart rate and display your hear rate. It'll take between five and ten seconds to get an accurate heart rate.
