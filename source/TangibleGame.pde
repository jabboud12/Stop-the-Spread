void settings() {
  size(1200, 850, P3D);
  //fullScreen(P3D);
}

void setup() {
  //background(100);
  frameRate(50);
  //pattern = loadImage("stripes.jpg");
  ball = new MovingBall();
  plate = new Plate();
  generator = new CylinderGenerator();
  gameSurface = createGraphics(width, height-bottomTab, P3D);
  background = createGraphics(width, bottomTab, P2D);
  topView = createGraphics(topViewDim, topViewDim, P2D);
  scoreboard = createGraphics(topViewDim, topViewDim, P2D);
  barChart = createGraphics(width - 2*topViewDim - 6*padding, topViewDim, P2D);
  menu = createGraphics(width-100, height, P2D);
  drawMenu(menu);
  pauseQuit = createGraphics(100, height- barChart.height - padding, P2D);
  hs = new HScrollbar(3*topViewDim+50, height-20, 300, 20);

  mov = new Movie(this, "testvideo.avi");
  mov.loop();

  // source position is at cylinders.get(0)
  villain = loadShape("robotnik.obj");

  improc = new ImageProcessing();
  String[] s = {"Game"};
  PApplet.runSketch(s, improc);
}

void draw() {
  background.beginDraw();
  background.background(229, 228, 223);
  background.endDraw();
  image(background, 0, height-bottomTab);
  drawGame(gameSurface);
  drawButtons(pauseQuit);
  if (!paused) {
    drawTopView(topView);
    image(topView, padding, height-bottomTab+padding);
    drawScoreboard(scoreboard);
    image(scoreboard, topViewDim+ 2*padding, height-bottomTab+padding);
    drawBarChart(barChart);
    image(barChart, 2*topViewDim + 4*padding, height -bottomTab+padding);
  }
  image(gameSurface, 0, 0);
  image(pauseQuit, width -100, 0);
  if (paused) {
    image(menu, 0, 0);
  }
}

void drawMenu(PGraphics surface) {
  surface.beginDraw();
  font = createFont("Arial Bold", 15);
  if (paused) {
    surface.background(229, 228, 223);
    surface.noStroke();

    surface.fill(200);
    surface.stroke(50);
    surface.strokeWeight(3);
    surface.rect(width/2-275, height/2 -130, 550, 260, 15);
    surface.fill(0);
    surface.textFont(font);
    surface.text("-To play/pause/quit the game, use the upper right buttons", width/2-250, height/2-110);
    surface.text("-To place the villain and start the game, use Shift", width/2-250, height/2-90);
    surface.text("-To toggle debug mode, use Enter", width/2-250, height/2-70);
    surface.text("-To change the board rotation speed, use the mouse wheel", width/2-250, height/2-50);
    surface.text("-To restart the game, use the R key", width/2-250, height/2-30);
    surface.text("-To enable/disable the Kalman Filter tracking method, use T (we don't ", width/2-250, height/2 - 10);
    surface.text("  recommend using it as it reduces accuracy and doesn't improve", width/2-250, height/2 + 10);
    surface.text("  speed that much", width/2-250, height/2 + 30);
    surface.text("\nDon't worry, the game will keep a chart of your score after you restart", width/2-250, height/2 + 50);
    surface.text("\n\nGood luck fellow gamer, don't let Dr.Eggman spread his dirty virus", width/2-250, height/2 + 70);

    surface.noStroke();

    font = createFont("Arial Bold", 45);
    surface.textFont(font);
    surface.fill(50, 150, 255);
    surface.text("Stop the Spread!!", width/2-185, 150);
    surface.text("#StayAtHome", width/2-150, surface.height-200);
  }
  surface.endDraw();
}


void drawBarChart(PGraphics surface) {
  surface.beginDraw();
  if (!paused) {
    surface.background(225, 220, 159);
    secs += 1;
    if (secs>=50) {
      secs=0;
      points.add((int)(score/10));//right rounding for negative numbers??
    }
    surface.fill(255);
    surface.stroke(225, 220, 159);
    float squareDim = (1+hs.getPos());
    for (int i = 0; i<points.size(); ++i) {
      for (int j= 0; j<abs(points.get(i)); ++j) {
        int k = (points.get(i)>0) ? -1 : 1;
        surface.rect(i*5*squareDim, (topViewDim)/2 +7.5*j*k, 5*squareDim, 7.5);
        surface.rect(i*5*squareDim, (topViewDim)/2 +7.5*j*k, 5*squareDim, 7.5);
      }
    }
  }
  hs.update();
  hs.display();
  surface.endDraw();
}

void drawButtons(PGraphics surface) {
  surface.beginDraw();
  surface.background(229, 228, 223);
  surface.noStroke();
  surface.fill(150);
  if (mouseX <= width -55 && mouseX >= width -85 && mouseY >= 15 && mouseY <= 45) {
    font = createFont("Arial Bold", 15);
    surface.textFont(font);
    surface.fill(0);
    if (!paused) {
      surface.text("Pause", surface.width-90, 60);
    } else {
      surface.text("Play", surface.width-85, 60);
    }
    surface.fill(100);
  }
  surface.circle(surface.width-70, 30, 30);
  surface.fill(255);
  if (!paused) {
    surface.rect(surface.width -75, 21, 4, 18);
    surface.rect(surface.width -69, 21, 4, 18);
  } else {
    surface.triangle(surface.width-75, 21, surface.width-75, 39, surface.width-60, 30);
  }

  surface.fill(229, 228, 223);
  surface.noStroke();
  //surface.rect(width-46, 46, 60, 30);

  surface.fill(150);
  if (mouseX <= width -15 && mouseX >= width -45 && mouseY >= 15 && mouseY <= 45) {
    font = createFont("Arial Bold", 15);
    surface.textFont(font);
    surface.fill(0);
    surface.text("Quit", surface.width-45, 60);
    surface.fill(100);
  }
  surface.circle(surface.width-30, 30, 30);
  surface.stroke(255);
  surface.strokeWeight(3);
  surface.line(surface.width-38, 21, surface.width-22, 39);
  surface.line(surface.width-38, 39, surface.width-22, 21);

  surface.endDraw();
}


void drawScoreboard(PGraphics surface) {
  surface.beginDraw();
  if (!paused) {
    surface.background(187);
    surface.text("Score :\n " + score(), padding, 2*padding );
    String velocity = String.format("%.1f", ball.velocity.mag());
    surface.text("\n\n\nVelocity : \n" + velocity, padding, 2*padding + 22);
    String lastHit = String.format("%.2f", lastHit());
    surface.text("\n\n\nLast score :\n" + lastHit, padding, 2*padding + 88);
  }
  surface.endDraw();
}
void drawTopView(PGraphics surface) {
  surface.beginDraw();
  if (!paused) {
    surface.background(255, 0, 0);
    surface.fill(11, 79, 107);

    surface.rect(0, 0, topViewDim, topViewDim);
    surface.noFill();
    generator.drawPoints(surface);
    ball.drawBall(surface);
  }
  surface.endDraw();
}

void increaseScore(float x) { 
  lastHit = x*10;
  score += lastHit;
}
void decreaseScore() {
  score -= 10;
}
float score() {
  return score;
};
float lastHit() {
  return lastHit;
}

void drawGame(PGraphics surface) {
  surface.beginDraw();
  if (!paused) {
    surface.noStroke();
    mx = mouseX - width/2.0;
    my = mouseY - (height-bottomTab)/2.0;
    surface.background(229, 228, 223);

    surface.pushMatrix();
    surface.translate(width/2, (height-bottomTab)/2);
    if (!shiftMode) 
      regularMode();
    else 
    shiftMode();
    surface.popMatrix();
  }

  if (easterEgg) {
    surface.colorMode(HSB, height, height, height);  
    surface.noStroke();
    //surface.background(0);
    int whichBar = mouseX / barWidth;
    if (whichBar != lastBar) {
      int barX = whichBar * barWidth;
      surface.fill(mouseY, height, height);
      surface.rect(barX, 0, barWidth, height);
      lastBar = whichBar;
    }
    surface.colorMode(RGB, 255);
  }

  surface.endDraw();
}


// ----------------------------------------------------------------------------


// REGULAR MODE
void regularMode() {

  setLight(gameSurface);

  gameSurface.pushMatrix();

  if (vectRot.x != 0 && vectRot.y != 0) {
    rotationX = -vectRot.x + PI;
    rotationZ = -vectRot.y;
  }


  gameSurface.rotateX(rotationX);
  gameSurface.rotateZ(rotationZ);

  plate.draw(gameSurface);

  if (drawAxis)
    plate.drawAxis(gameSurface);  

  ball.update();
  ball.draw(gameSurface);
  ball.checkCollision(plate);
  ball.checkCollision(generator.cylinders);

  gameSurface.rotateX(PI/2);

  generator.draw(gameSurface);
  generator.update(plate, ball);

  gameSurface.popMatrix();
  if (drawAxis) {
    drawInfo(gameSurface, rotationX, rotationZ, speed);
  }
}


// SHIFT MODE
void shiftMode() {
  gameSurface.lights();
  gameSurface.fill(126);

  gameSurface.rect(-plate.x/2, -plate.z/2, plate.x, plate.z);

  // Draw the cylinders
  generator.drawShitMode(gameSurface);


  // Draw the ball
  gameSurface.translate(ball.location.x, -ball.location.z);
  gameSurface.sphere(ball.ball_radius);
}





// ----EVENT HANDLERS ----------------------------------------------------


void mousePressed() {

  if (shiftMode) {
    CylinderGenerator newGen = new CylinderGenerator(mx, my);
    if (!newGen.isInside(ball) && plate.isInside(newGen))
      generator = newGen;
  }

  if (mouseX <= width -55 && mouseX >= width -85 && mouseY >= 15 && mouseY <= 45) {
    paused = !paused;
  }

  if (mouseX <= width -15 && mouseX >= width -45 && mouseY >= 15 && mouseY <= 45) {
    exit();
  }
}


//  This method is used to rotate the board following the mouse's movements when dragged
void mouseDragged() {

  float angle_limit = PI/3;
  //fixme : ask TA
  if (mouseY<height-bottomTab) {
    rotationX -= speed * (mouseY - pmouseY) / height;
    rotationZ += speed * (mouseX - pmouseX) / width;
  }

  if (rotationZ > angle_limit)
    rotationZ = angle_limit;
  if (rotationZ < -angle_limit)
    rotationZ = - angle_limit;
  if (rotationX  > angle_limit)
    rotationX = angle_limit;
  if (rotationX < -angle_limit)
    rotationX = - angle_limit;
}

//  This method increments/decrements the rotation's speed of the board with the scrolling of the mouse
void mouseWheel(MouseEvent e) {
  float speed_threshold_min = 0.2;
  float speed_threshold_max = 10;
  float increment_coef = 0.05;
  float ev = e.getCount();

  if (speed < -ev * increment_coef + speed_threshold_min) 
    speed = speed_threshold_min;
  else if (speed > - ev * increment_coef + speed_threshold_max) 
    speed = speed_threshold_max;
  else
    speed += ev * increment_coef;
}




void keyPressed() {
  if (keyCode == ENTER) {
    drawAxis = !drawAxis;
    change_ball_color = !change_ball_color;
  }  

  if (keyCode == SHIFT) 
    shiftMode = true;

  if (key == 'R' || key == 'r')
    reset();

  if (key == 'T' || key == 't') {
    tracking = !tracking;
  }

  if (key == 'E' || key == 'e') {
    easterEgg = !easterEgg;
  }
}

void keyReleased() {
  if (keyCode == SHIFT) 
    shiftMode = false;
}
