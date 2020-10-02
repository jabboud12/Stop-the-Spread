PImage toHueMap(PImage img, float threshold1, float threshold2) {
  PImage result = createImage(img.width, img.height, HSB);
  result = img.copy();


  for (int i = 0; i < img.width * img.height; i++) {
    if (!(hue(img.pixels[i]) >= threshold1 && hue(img.pixels[i]) <= threshold2)) {
      result.pixels[i] = 0;
    } else {
      result.pixels[i] = color(255);
    }
  }
  return result;
}

PImage thresholdHSB(PImage img, int minH, int maxH, int minS, int maxS, int minB, int maxB) {
  PImage result = createImage(img.width, img.height, HSB);
  result = img.copy();



  for (int i = 0; i < img.width * img.height; i++) {
    if (!(hue(img.pixels[i]) >= minH && hue(img.pixels[i]) <= maxH && saturation(img.pixels[i]) >= minS && saturation(img.pixels[i]) <= maxS 
      && brightness(img.pixels[i]) >= minB && brightness(img.pixels[i]) <= maxB)) {
      result.pixels[i]=0;
    } else {
      result.pixels[i] = color(255);
    }
  }

  return result;
}

// Not used in our program, but could be useful to detect two different objects
PImage doubleThresholdHSB(PImage img, int minH0, int maxH0, int minH1, int maxH1, int minS, int maxS, int minB, int maxB) {
  PImage result = createImage(img.width, img.height, HSB);
  result = img.copy();

  for (int i = 0; i < img.width * img.height; i++) {
    if (!(((hue(img.pixels[i]) >= minH0 && hue(img.pixels[i]) <= maxH0) || (hue(img.pixels[i]) >= minH1 && hue(img.pixels[i]) <= maxH1)) && saturation(img.pixels[i]) >= minS && saturation(img.pixels[i]) <= maxS 
      && brightness(img.pixels[i]) >= minB && brightness(img.pixels[i]) <= maxB)) {
      result.pixels[i]=0;
    } else {
      //result.pixels[i] = color(255);
    }
  }

  return result;
}

PImage addImages(PImage img0, PImage img1, int brightnessThreshold ) {
  PImage result = createImage(img.width, img.height, HSB);
  for (int i = 0; i < img0.width * img0.height; i++) {
    if (brightness(img0.pixels[i]) >brightnessThreshold && brightness(img1.pixels[i]) <=brightnessThreshold) {
      //result.pixels[i] = img0.pixels[i];
      result.pixels[i] = color(200, 150, 0);
    } else if (brightness(img0.pixels[i]) <=brightnessThreshold && brightness(img1.pixels[i]) >brightnessThreshold) {
      //result.pixels[i] = img1.pixels[i];
      result.pixels[i] = color(0, 80, 80);
    } else if (brightness(img0.pixels[i]) >brightnessThreshold && brightness(img1.pixels[i]) >brightnessThreshold) {
      //result.pixels[i] = img0.pixels[i];
      result.pixels[i] = color(200, 150, 0);
    } else {
      result.pixels[i]= color(0);
    }
  }

  return result;
}


PImage threshold(PImage img, int threshold) {
  // create a new, initially transparent, 'result' image
  PImage result = createImage(img.width, img.height, RGB);

  for (int i = 0; i < img.width * img.height; i++) {
    // do something with the pixel img.pixels[i]
    if ((brightness(img.pixels[i])> threshold)) {
      result.pixels[i] = img.pixels[i];
    } else {
      result.pixels[i] = 0;
    }
  }
  return result;
}

boolean imagesEqual(PImage img1, PImage img2) {
  if (img1.width != img2.width || img1.height != img2.height)
    return false;
  for (int i = 0; i < img1.width*img1.height; i++)
    //assuming that all the three channels have the same value
    if (red(img1.pixels[i]) != red(img2.pixels[i]))
      return false;
  return true;
}

PImage convolute(PImage img) {
  float[][] kernel1 = { 
    { 0, 0, 0 }, 
    { 0, 2, 0 }, 
    { 0, 0, 0 }};

  float[][] kernel2 = { 
    { 0, 1, 0 }, 
    { 1, 0, 1 }, 
    { 0, 1, 0 }};

  float[][] gaussianKernel = { 
    { 9, 12, 9 }, 
    { 12, 15, 12 }, 
    { 9, 12, 9 }};

  float normFactor = 99.f;
  // create a greyscale image (type: ALPHA) for output
  PImage result = createImage(img.width, img.height, ALPHA);
  // kernel size N = 3
  for (int x = 1; x<img.width-1; ++x) {
    for (int y = 1; y<img.height-1; ++y) {
      result.pixels[y * img.width + x] = 0;
      for (int i=-1; i<2; ++i) {
        for (int j=-1; j<2; ++j) {
          result.pixels[y * img.width + x] += brightness(img.pixels[(y+j) * img.width + x + i]) * gaussianKernel[j+1][i+1];
        }
      }
      result.pixels[y * img.width + x] /= normFactor;
      result.pixels[y * img.width + x] = color((int)(result.pixels[y * img.width + x]));
    }
  }
  result.updatePixels();
  return result;
}

PImage scharr(PImage img) {
  float[][] vKernel = {
    { 3, 0, -3 }, 
    { 10, 0, -10 }, 
    { 3, 0, -3 } };

  float[][] hKernel = {
    { 3, 10, 3 }, 
    { 0, 0, 0 }, 
    { -3, -10, -3 } };

  PImage result = createImage(img.width, img.height, ALPHA);
  // clear the image
  for (int i = 0; i < img.width * img.height; i++) {
    result.pixels[i] = color(0);
  }
  float max=0;
  float[] buffer = new float[img.width * img.height];
  // *************************************
  // Implement here the double convolution
  // *************************************
  float normFactor = 1.f;
  for (int y = 1; y < img.height - 1; y++) { // Skip top and bottom edges
    for (int x = 1; x < img.width - 1; x++) { // Skip left and right
      int sum_h = 0;
      int sum_v = 0;
      for (int i=-1; i<2; ++i) {
        for (int j=-1; j<2; ++j) {
          sum_h += brightness(img.pixels[(y+j) * img.width + x + i]) * hKernel[j+1][i+1];
          sum_v += brightness(img.pixels[(y+j) * img.width + x + i]) * vKernel[j+1][i+1];
        }
      }
      result.pixels[y * img.width + x] /= normFactor;
      result.pixels[y * img.width + x] = color((int)(result.pixels[y * img.width + x]));
      float  sum=sqrt(pow(sum_h, 2) + pow(sum_v, 2));
      if (sum> max )
        max = sum;
      buffer[y * img.width + x] = sum;
    }
  }

  for (int y = 1; y < img.height - 1; y++) { // Skip top and bottom edges
    for (int x = 1; x < img.width - 1; x++) { // Skip left and right
      int val=(int) ((buffer[y * img.width + x] / max)*255);
      result.pixels[y * img.width + x]=color(val);
    }
  }
  return result;
}
