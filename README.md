# GobbleImage
 **An intelligent Android app that helps to enrich creativity, improve drawing skills, increase focus and at the same time have lots of fun !**
 
GobbleImage uses Image classification to continuously classify whatever is drawn on the screen. Inference is performed using the TensorFlow Lite Java API. GobbleImage images in real-time, displaying the top 3 most probable classifications.

This App uses MobileNetV1 (float) "mobilenet_v1_1.0_224" model. MobileNet architecture model was chosen for increased speed. When benchmark_model tool is used against both Inception v3 and MobileNetv1 it will indicate 11.42B as FLOPs estimate for Inception v3 based retrained model which means about 11 B FLOPS required to make an inference. Whereas running benchmark tool for MobileNet model based retrained model  would indicate FLOPS estimate of about 1.14 B about 10 times faster. Generally modern smartphones run 10 B FLOPS.

*This app can further be gamified and used to improve hand-writing of children after increased training and accuracy of model.*

![**Demo video**](https://github.com/gobbleminds/GobbleImage/blob/master/demo/GobbleImage.gif)
