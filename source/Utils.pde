void drawInfo(PGraphics surface, float rotationX , float rotationZ , float speed) {
  surface.noLights();
  surface.translate(-width/2, -(height-bottomTab)/2);
  surface.textSize(20);
  surface.fill(255, 0, 0);
  surface.text("RotationX : "+rotationX, 50, 50);
  surface.fill(0, 0, 255);
  surface.text("RotationZ : "+rotationZ, 50, 50 + 22);
  surface.fill(0);
  surface.text("Speed : "+speed, 50, 50 + 44);
  String s = tracking ? "On" : "Off";
  surface.text("Kalman filter : " + s, 50, 50 + 66);
  
}

void setLight(PGraphics surface) {
  surface.ambientLight(150, 150, 150);
  surface.directionalLight(128, 128, 128, 1, 1, -1);
  surface.lightFalloff(1, 0, 0);
  surface.lightSpecular(0, 0, 0);
  surface.pointLight(51, 102, 126, 35, 40, 36);
  //surface.pointLight(255, 255, 255, 1, 1, -1);
}

static float vect_norm(float ... x) {
  float sum = 0;
  for (int i = 0; i < x.length; ++i) {
    sum += x[i] * x[i];
  }
  return sum;
}

void reset() {
  ball = new MovingBall();
  generator = new CylinderGenerator();
  rotationX = 0;
  rotationZ = 0;
  speed = 1;
  score = 0;
}


   
   
