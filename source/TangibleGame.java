import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.ArrayList; 
import java.util.List; 
import java.util.TreeSet; 
import processing.video.*; 
import gab.opencv.*; 
import java.util.Arrays; 
import processing.video.*; 
import java.util.ArrayList; 
import java.util.List; 
import java.util.TreeSet; 
import java.util.Collections; 
import java.util.Collections; 
import java.util.Comparator; 
import java.util.List; 
import java.util.ArrayList; 
import java.util.Map; 
import java.util.List; 
import processing.core.PVector; 
import org.opencv.core.Mat; 
import org.opencv.core.CvType; 
import org.opencv.core.Core; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class TangibleGame extends PApplet {

public void settings() {
  size(1200, 850, P3D);
  //fullScreen(P3D);
}

public void setup() {
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

public void draw() {
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

public void drawMenu(PGraphics surface) {
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


public void drawBarChart(PGraphics surface) {
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
        surface.rect(i*5*squareDim, (topViewDim)/2 +7.5f*j*k, 5*squareDim, 7.5f);
        surface.rect(i*5*squareDim, (topViewDim)/2 +7.5f*j*k, 5*squareDim, 7.5f);
      }
    }
  }
  hs.update();
  hs.display();
  surface.endDraw();
}

public void drawButtons(PGraphics surface) {
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


public void drawScoreboard(PGraphics surface) {
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
public void drawTopView(PGraphics surface) {
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

public void increaseScore(float x) { 
  lastHit = x*10;
  score += lastHit;
}
public void decreaseScore() {
  score -= 10;
}
public float score() {
  return score;
};
public float lastHit() {
  return lastHit;
}

public void drawGame(PGraphics surface) {
  surface.beginDraw();
  if (!paused) {
    surface.noStroke();
    mx = mouseX - width/2.0f;
    my = mouseY - (height-bottomTab)/2.0f;
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
public void regularMode() {

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
public void shiftMode() {
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


public void mousePressed() {

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
public void mouseDragged() {

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
public void mouseWheel(MouseEvent e) {
  float speed_threshold_min = 0.2f;
  float speed_threshold_max = 10;
  float increment_coef = 0.05f;
  float ev = e.getCount();

  if (speed < -ev * increment_coef + speed_threshold_min) 
    speed = speed_threshold_min;
  else if (speed > - ev * increment_coef + speed_threshold_max) 
    speed = speed_threshold_max;
  else
    speed += ev * increment_coef;
}




public void keyPressed() {
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

public void keyReleased() {
  if (keyCode == SHIFT) 
    shiftMode = false;
}





public PImage findConnectedComponents(PImage input, boolean onlyBiggest) {

  // First pass: labels the pixels and stores labels' equivalences
  //PImage res = input.copy();
  int [] labels = new int [input.width*input.height];
  List<TreeSet<Integer>> labelsEquivalences = new ArrayList<TreeSet<Integer>>();
  TreeSet<Integer> s = new TreeSet<Integer>();
  s.add(0);
  labelsEquivalences.add(s);
  int threshold = 100;
  int currentLabel = 1;



  for (int y = 1; y < input.height-1; ++y) {
    for (int x = 1; x < input.width-1; ++x) {
      labels[y* input.width + x] = 0;


      if (brightness(input.pixels[y* input.width + x])  > threshold) {
        TreeSet<Integer> neighbors = new TreeSet<Integer>();

        for (int i=-1; i<2; ++i) {
          for (int j = -1; j<2; ++j) {
            if (brightness(input.pixels[(y+j) * input.width + x + i ])>=threshold && labels[(y+j) * input.width + x + i]!=0) {
              neighbors.add(labels[((y+j)* input.width + x + i)]);
            }
          }
        }

        if (neighbors.isEmpty()) {
          TreeSet<Integer> set = new TreeSet<Integer>();
          set.add(currentLabel);
          labelsEquivalences.add(set);
          labels[y* input.width + x] = currentLabel;
          currentLabel += 1;
        } else {

          labels[y* input.width + x] = neighbors.first();
          TreeSet<Integer> tree = new TreeSet<Integer>();
          for (int label : neighbors ) {
            tree.addAll(labelsEquivalences.get(label));
          }
          for (int label : tree) {
            TreeSet<Integer> t = labelsEquivalences.get(label);
            t.addAll(tree);
            labelsEquivalences.set(label, t);
          }
        }
      }
    }
  }


  // Second pass: re-labels the pixels by their equivalent class
  //if onlyBiggest==true, counts the number of pixels for each label

  int labelCount[] = new int[currentLabel+1];

  for (int x = 0; x < input.width; ++x) {
    for (int y = 0; y < input.height; ++y) {

      int label = labelsEquivalences.get(labels[y* input.width + x]).first();
      labels[y* input.width + x] = label;
      if (onlyBiggest)
        labelCount[label] = labelCount[label]+1;
    }
  }


  // if onlyBiggest==false, output an image with each blob colored in one uniform color
  // if onlyBiggest==true, output an image with the biggest blob in white and others in black
  int maxLabel = 1;
  if (onlyBiggest) {  
    for (int i = 1; i<labelCount.length; ++i) { //we don't take i = 0 because it is the background
      //println(labelCount[i]);
      if (labelCount[i]>labelCount[maxLabel]) maxLabel = i;
    }
  }

  if (!onlyBiggest) {
    for (int y = 0; y < input.height; ++y) {
      for (int x = 0; x < input.width; ++x) {
        input.pixels[y*input.width+x] = labels[y*input.width+x];
        if (input.pixels[y*input.width+x]%7 == 1)  input.pixels[y*input.width+x] = color(123);
        if (input.pixels[y*input.width+x]%7 == 2)  input.pixels[y*input.width+x] = color(255, 0, 0);
        if (input.pixels[y*input.width+x]%7 == 3)  input.pixels[y*input.width+x] = color(0, 255, 0);
        if (input.pixels[y*input.width+x]%7 == 4)  input.pixels[y*input.width+x] = color(0, 0, 255);

        if (input.pixels[y*input.width+x]%7 == 5)  input.pixels[y*input.width+x] = color(255, 255, 0);
        if (input.pixels[y*input.width+x]%7 == 6)  input.pixels[y*input.width+x] = color(0, 255, 255);
        if (input.pixels[y*input.width+x]%7 == 7)  input.pixels[y*input.width+x] = color(255, 0, 255);
      }
    }
  }

  return input;
}


class Cylinder {

  //cylinders
  float cylinderBaseSize = 20;
  float cylinderHeight = 30;
  int cylinderResolution = 40;
  PShape openCylinder = new PShape();
  PShape cylinderBases = new PShape();


  float x, y;

  Cylinder(float x_, float y_) {
    x = x_ ; 
    y = y_ ;
    cylinderSetup();
  }

  public Cylinder copy() {
    return new Cylinder(x, y);
  }

  public boolean isInside(Cylinder o) {
    float norm = vect_norm(x - o.x, y - o.y);
    if (norm < 4 * cylinderBaseSize * cylinderBaseSize)
      return true;
    return false;
  }

  public boolean isInside(MovingBall ball) {
    float norm = vect_norm(ball.location.x - x, - ball.location.z - y);
    if (norm < (ball.ball_radius + cylinderBaseSize) * (ball.ball_radius + cylinderBaseSize))
      return true;
    return false;
  }


  public void draw(PGraphics surface) {
    noStroke();
    float red = random(255);
    float green = random(255);
    float blue = random(255);
    fill(red, green, blue);
    surface.shape(openCylinder, x, y);
    surface.shape(cylinderBases, x, y);
  }

  public void drawShitMode(PGraphics surface) {
    surface.shape(cylinderBases, x, y);
  }

  public float x() {
    return x;
  }
  public float y() {
    return y;
  }




  public void cylinderSetup() {
    ////setup for cylinder
    float angle;
    float[] x_ = new float[cylinderResolution + 1];
    float[] y_ = new float[cylinderResolution + 1];

    //get the x and y position on a circle for all the sides
    for (int i = 0; i < x_.length; i++) {
      angle = (TWO_PI / cylinderResolution) * i;
      x_[i] = sin(angle) * cylinderBaseSize;
      y_[i] = cos(angle) * cylinderBaseSize;
    }
    

    openCylinder = createShape();
    openCylinder.beginShape(QUAD_STRIP);

    //draw the border of the cylinder
    
    for (int i = 0; i < x_.length; i++) {
      openCylinder.vertex(x_[i], y_[i], 0);
      openCylinder.vertex(x_[i], y_[i], cylinderHeight);
    }
    openCylinder.endShape();

    // Closing the cylinder
    cylinderBases = createShape();
    cylinderBases.beginShape(TRIANGLE_FAN);
    
    for (int i=0; i< x_.length-1; ++i) {
      //create top surface for cylinder
      cylinderBases.vertex(0, 0, cylinderHeight);
      cylinderBases.vertex(x_[i], y_[i], cylinderHeight);
      cylinderBases.vertex(x_[i+1], y_[i+1], cylinderHeight);

      //create bottom surface for cylinder
      cylinderBases.vertex(0, 0, 0);
      cylinderBases.vertex(x_[i], y_[i], 0);
      cylinderBases.vertex(x_[i+1], y_[i+1], 0);

      cylinderBases.vertex(0, 0, 0);
    }
    cylinderBases.endShape();
    ////end setup for cylinder
  }
}
class CylinderGenerator {

  ArrayList<Cylinder> cylinders;

  //FIXME : put it in setup instead of here
  //float theta;


  CylinderGenerator() {
    cylinders = new ArrayList();
  }

  CylinderGenerator(float x, float y) {
    cylinders = new ArrayList();
    cylinders.add( new Cylinder(x, y) );
  }

  float oldAngle=0;
  public void draw(PGraphics surface) {

    // TODO : draw the source on top of cylinders.get(0)

    for ( Cylinder c : cylinders)
      c.draw(surface);


    if (cylinders.size() >0) {
      float x = cylinders.get(0).x;
      float y = cylinders.get(0).y;
      float x0 = ball.location.x;
      float y0 = ball.location.y;
      //theta = acos((ball.location.z - y)/ sqrt((x-x0)*(x-x0) + (y-y0)*(y-y0)));

      //villain.rotateY( theta-oldAngle);
      //oldAngle = theta;

      surface.pushMatrix();
      surface.rotateX(PI/2);
      surface.rotateY(PI);

      surface.translate(-x, 30/*cylinder height*/, y);
      surface.scale(25);       
      surface.shape(villain, 0, 0);
      surface.popMatrix();
    }
  }

  public void drawPoints(PGraphics surface) {
    float cylinderDim = 20*topViewDim*2/300;
    surface.noStroke();
    for ( Cylinder c : cylinders) {

      //fix values
      surface.fill(255);
      surface.ellipse((c.x()+150)/300*topViewDim, (c.y()+150)/300*topViewDim, cylinderDim, cylinderDim);
      //surface.noFill();
    }
    if (cylinders.size() >0) {
      Cylinder villain = cylinders.get(0);
      surface.fill(255, 0, 0);
      surface.ellipse((villain.x()+150)/300*topViewDim, (villain.y()+150)/300*topViewDim, cylinderDim, cylinderDim);
      surface.noFill();
    }
  }



  public void drawShitMode(PGraphics surface) {
    for ( Cylinder c : cylinders)
      c.drawShitMode(surface);
  }


  public boolean isInside(MovingBall ball) {
    return cylinders.get(0).isInside(ball);
  }

  public void update( Plate plate, MovingBall ball ) {



    if ( cylinders.isEmpty()  )
      return ;


    for ( int i=0; i< cylinders.size(); i++) {
      if ( cylinders.get(i).isInside(ball) ) {
        if (i == 0) {
          // Yeah you killed the SOURCE !!
          cylinders = new ArrayList();
          return;
        }
        cylinders.remove(i);
        i = i-1;
      }
    }

    if (frameCount % 50 != 0 )
      return ;


    int numAttempts = 100;

    for (int i=0; i< numAttempts; i++) {

      // Pick a cylinder and its center.
      int index = PApplet.parseInt(random(cylinders.size()));
      Cylinder new_cylinder = cylinders.get(index).copy();


      // Try to add an adjacent cylinder.
      float angle = random(TWO_PI);
      new_cylinder.x += sin(angle) * 2 * new_cylinder.cylinderBaseSize;
      new_cylinder.y += cos(angle) * 2 * new_cylinder.cylinderBaseSize;


      boolean noCylinderCollision = true;
      for (Cylinder c1 : cylinders)
        if (c1.isInside(new_cylinder)) noCylinderCollision = false;



      // TODO : i dont understant why it doesn t work wtith isInside uncommented ?
      if (plate.isInside(new_cylinder) /* && new_cylinder.isInside(ball)*/ && noCylinderCollision) {
        cylinders.add(new_cylinder);
        decreaseScore();
        break;
      }
    }
  }
}
public PImage toHueMap(PImage img, float threshold1, float threshold2) {
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

public PImage thresholdHSB(PImage img, int minH, int maxH, int minS, int maxS, int minB, int maxB) {
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
public PImage doubleThresholdHSB(PImage img, int minH0, int maxH0, int minH1, int maxH1, int minS, int maxS, int minB, int maxB) {
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

public PImage addImages(PImage img0, PImage img1, int brightnessThreshold ) {
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


public PImage threshold(PImage img, int threshold) {
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

public boolean imagesEqual(PImage img1, PImage img2) {
  if (img1.width != img2.width || img1.height != img2.height)
    return false;
  for (int i = 0; i < img1.width*img1.height; i++)
    //assuming that all the three channels have the same value
    if (red(img1.pixels[i]) != red(img2.pixels[i]))
      return false;
  return true;
}

public PImage convolute(PImage img) {
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

public PImage scharr(PImage img) {
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
ArrayList <ParticleSystem> particles;



class ParticleSystem {
  ArrayList<Particle> particles ;

  PVector origin;
  float particleRadius;
  int particleColor;

  ParticleSystem(PVector origin) {
    this.origin = origin.copy();
    particleColor = color(random(100, 255), random(100, 255), random(100, 255));
    particles = new ArrayList();
    particleRadius = random(5, 10);
    particles.add(new Particle(origin, particleColor, particleRadius));
  }

  public void addParticle() {
    if (particles.size() < 100)
      particles.add(new Particle(origin, particleColor, particleRadius));
    //System.out.println(particles.size());
  }

  public void run() {
    //println(particles.size());
    for (int i = 0; i< particles.size(); ++i) {
      if (particles.get(i).isDead()) {
        particles.remove(i);
      } else {
        particles.get(i).run();
      }
    }
  }
}

class Particle {
  PVector center;
  float radius;

  float speed;
  float angle;
  int c;

  Particle(PVector center, int c, float radius) {
    this.center = center.copy();
    this.radius = radius;
    speed = random(1);
    angle = random(0, 2*PI);
    this.c = c;
  }

  public void run() {
    update();
    display();
  }

  public void update() {
    radius -=0.1f;
    center.x += sin(angle)*speed;
    center.y += tan(angle)*speed;
    center.z += cos(angle)*speed;
  }

  public void display() {
    if (!isDead()) {
      background(123);
      strokeWeight(radius);
      stroke(c);
      //ellipse(center.x, center.y, radius, radius);
      point(center.x, center.y, center.z);
      //noFill();
    }
  }

  public boolean isDead() { 
    return radius<=0;
  }
}
class HScrollbar {
  float barWidth;  //Bar's width in pixels
  float barHeight; //Bar's height in pixels
  float xPosition;  //Bar's x position in pixels
  float yPosition;  //Bar's y position in pixels

  float sliderPosition, newSliderPosition;    //Position of slider
  float sliderPositionMin, sliderPositionMax; //Max and min values of slider

  boolean mouseOver;  //Is the mouse over the slider?
  boolean locked;     //Is the mouse clicking and dragging the slider now?

  /**
   * @brief Creates a new horizontal scrollbar
   * 
   * @param x The x position of the top left corner of the bar in pixels
   * @param y The y position of the top left corner of the bar in pixels
   * @param w The width of the bar in pixels
   * @param h The height of the bar in pixels
   */
  HScrollbar (float x, float y, float w, float h) {
    barWidth = w;
    barHeight = h;
    xPosition = x;
    yPosition = y;

    sliderPosition = xPosition + barWidth/2 - barHeight/2;
    newSliderPosition = sliderPosition;

    sliderPositionMin = xPosition;
    sliderPositionMax = xPosition + barWidth - barHeight;
  }
  HScrollbar (float x, float y, float w, float h, float sliderPosition) {
    barWidth = w;
    barHeight = h;
    xPosition = x;
    yPosition = y;

    //sliderPosition = xPosition + barWidth/2 - barHeight/2;
    //newSliderPosition = sliderPosition/255 + barWidth/2 - barHeight/2;
    newSliderPosition = sliderPosition*(barWidth - barHeight)/255 + xPosition ;




    sliderPositionMin = xPosition;
    sliderPositionMax = xPosition + barWidth - barHeight;
  }

  /**
   * @brief Updates the state of the scrollbar according to the mouse movement
   */
  public void update() {
    if (isMouseOver()) {
      mouseOver = true;
    } else {
      mouseOver = false;
    }
    if (mousePressed && mouseOver) {
      locked = true;
    }
    if (!mousePressed) {
      locked = false;
    }
    if (locked) {
      newSliderPosition = constrain(mouseX - barHeight/2, sliderPositionMin, sliderPositionMax);
    }
    if (abs(newSliderPosition - sliderPosition) > 1) {
      sliderPosition = sliderPosition + (newSliderPosition - sliderPosition);
    }
  }

  /**
   * @brief Clamps the value into the interval
   * 
   * @param val The value to be clamped
   * @param minVal Smallest value possible
   * @param maxVal Largest value possible
   * 
   * @return val clamped into the interval [minVal, maxVal]
   */
  public float constrain(float val, float minVal, float maxVal) {
    return min(max(val, minVal), maxVal);
  }

  /**
   * @brief Gets whether the mouse is hovering the scrollbar
   *
   * @return Whether the mouse is hovering the scrollbar
   */
  public boolean isMouseOver() {
    if (mouseX > xPosition && mouseX < xPosition+barWidth &&
      mouseY > yPosition && mouseY < yPosition+barHeight) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * @brief Draws the scrollbar in its current state
   */
  public void display() {
      barChart.noStroke();
      barChart.fill(204);
      barChart.rect(xPosition - barChart.width/2, barChart.height - barHeight, barWidth, barHeight);
      if (mouseOver || locked) {
        barChart.fill(0);
      } else {
        barChart.fill(102);
      }
      barChart.rect(sliderPosition - barChart.width/2, barChart.height - barHeight, barHeight, barHeight);
  }

  /**
   * @brief Gets the slider position
   * 
   * @return The slider position in the interval [0,1] corresponding to [leftmost position, rightmost position]
   */
  public float getPos() {
    return (sliderPosition - xPosition)/(barWidth - barHeight);
  }
}
 //<>//



PGraphics gameSurface;
PGraphics background;
PGraphics topView;
PGraphics scoreboard;
PGraphics barChart;
PGraphics pauseQuit;
PGraphics menu;
HScrollbar hs;
ArrayList<Integer> points = new ArrayList<Integer>();

PImage pattern; 

int padding = 5;
int bottomTab = 200;
int topViewDim = bottomTab-2*padding;

// simulation preferences
private boolean drawAxis = false;
private boolean change_ball_color = false;
private boolean shiftMode = false;

//score
private float score = 0;
private float lastHit = 0;
private int secs = 0;


//translated mouse coordinates
float mx, my;

// rotation settings
float rotationX = 0;
float rotationZ = 0;
float speed = 1;

// MODEL
CylinderGenerator generator;
MovingBall ball;
Plate plate;

Movie mov;

PImage img, board1, board2, board3, board4, nao, nao_blob, imgCheck;
final int Width = 1500;
final int Height = 500;
List<PImage> images;
int imageIndex;

boolean twoBlobsMode = false; // Enable and disable blob detection mode for an item on the board
boolean help = true; // Enable and disable help mode
boolean cameraMode = false; //fixme correct resizing on camera mode

// Maium number of lines allowed for the edges of the board
final int nBestLinesPaddingThreshold = 3; 

// The structure of titles is: Original image -- Detection mode || Edge detection ... (each || ... || is an image displayed by order)
String [] titles = { "Board 1 -- Corner detection || Edge detection || HSB thresholding", 
  "Board 2 -- Corner detection || Edge detection || HSB thresholding", 
  "Board 3 -- Corner detection || Edge detection || HSB thresholding", 
  "Board 4 -- Corner detection || Edge detection || HSB thresholding", 
  "Nao -- Corner detection || Edge detection || HSB thresholding", 
  "Nao Blob -- Corner detection || Edge detection || HSB thresholding", 
  "Help", 
  "Two Blobs Mode ON -- Corner detection || Edge detection 1st Blob ||  Edge detection 2nd Blob ||" +
  " || HSB thresholding both blobs || HSB thresholding 1st blob || HSB thresholding 2nd blob", 
  "Camera Mode -- Corner detection || Edge detection || HSB thresholding"};

// If board corners are not detected, try to lower these two doubles below by 0.05
double widthFactor = 0.9f;
double heightFactor = 0.9f;

PVector vectRot = new PVector(0, 0, 0);
List<PVector> quad = new ArrayList<PVector>();
List<PVector> corners = new ArrayList<PVector>();

List<PVector> hough;

Capture cam;

OpenCV opencv;

TwoDThreeD rot;

ImageProcessing improc;

KalmanFilter2D corner1;
KalmanFilter2D corner2;
KalmanFilter2D corner3;
KalmanFilter2D corner4;
Boolean tracking = false;

int barWidth = 20;
int lastBar = -1;
Boolean easterEgg = false;

// Parameters for HSB thresholding
int minH = 30;   //47
int maxH = 142; //142
int minS = 33;  //68
int maxS = 255; //255
int minB = 28;   //28
int maxB = 255; //168

// pre-compute the sin and cos values
float[] tabSin; 
float[] tabCos;
float ang;
float inverseR;

int incr = 0;
int total_incr = 0;

Boolean paused = true;

PFont font;
PShape villain;

class ImageProcessing extends PApplet {
  public void settings() {
    size(640, 360);
  }

  public void setup() {
    // pre-compute the sin and cos values
    tabSin = new float[phiDim]; 
    tabCos = new float[phiDim];
    ang = 0;
    inverseR = 1.f / discretizationStepsR;
    for (int accPhi = 0; accPhi < phiDim; ang += discretizationStepsPhi, accPhi++) {
      // we can also pre-multiply by (1/discretizationStepsR) since we need it in the Hough loop 
      tabSin[accPhi] = (float) (Math.sin(ang) * inverseR);
      tabCos[accPhi] = (float) (Math.cos(ang) * inverseR);
    }

    opencv = new OpenCV(this, 100, 100);
    rot = new TwoDThreeD(width, height, 0);

    corner1 = new KalmanFilter2D(2, 1);
    corner2 = new KalmanFilter2D(2, 1);
    corner3 = new KalmanFilter2D(2, 1);
    corner4 = new KalmanFilter2D(2, 1);
  }

  public void draw() {
    if (!paused) {
      ++total_incr;
      background(200);
      try {
        if (mov.available() == true) {
          mov.read();
        }
        img = mov.get();
        img.resize(width, height);
        image(img, 0, 0);

        if (!tracking || (tracking && (frameCount % 2 == 0 || frameCount == 1))) {

          img = thresholdHSB(img, minH, maxH, minS, maxS, minB, maxB);  // color thresholding
          img = convolute(img);       // gaussian blur  

          img = findConnectedComponents(img, true);                  // blob detection
          img = scharr(img);                                        // edge detection
          img = threshold(img, 100);                               // Suppression of pixels with low brightness


          int nBestLines = 1;
          //hough = hough(img, 1);

          QuadGraph q = new QuadGraph();
          do {
            hough = hough(img, 4*nBestLines);
            quad = q.findBestQuad(hough, img.width, img.height, (int)((img.height*0.5f)*(img.width*0.5f)), (int)((img.height*0.3f)*(img.width*0.3f)), false);
            corners = new ArrayList(quad);

            ++nBestLines;
          } while (quad.isEmpty() && nBestLines < nBestLinesPaddingThreshold);
          if (!quad.isEmpty()) {
            corners = new ArrayList(quad);
            ++incr;
          }
        } else if (corners.size() == 4) {
          corners.set(0, corner1.predict_and_correct(corners.get(0)));
          corners.set(1, corner2.predict_and_correct(corners.get(1)));
          corners.set(2, corner3.predict_and_correct(corners.get(2)));
          corners.set(3, corner4.predict_and_correct(corners.get(3)));
          ++incr;
        }

        draw_lines(hough, img.width, img.height);          // Hough transform (+ draw lines on canvas)

        stroke(0);
        //draw corner circles
        if (corners.size() != 0) {
          for (int i = 0; i<corners.size(); ++i) {
            fill(color((i%4) *255, (i-1%4) *255, (i-2%4) *255, 100));      
            circle(corners.get(i).x, corners.get(i).y, 20);
          }
          for (PVector corner : corners) {
            corner.z = 1;
          }
          vectRot = rot.get3DRotations(corners);
        }
        println("Board detecting accuracy: " + incr * 100.0f / total_incr);
      } 
      catch (Exception e ) {
        e.printStackTrace();
        exit();//remove
      }
    }
  }


  public void draw_lines(List<PVector> lines, int imageWidth, int imageHeight) {
    float x0 = 0;
    float y1 = 0;
    float x2 = imageWidth;
    float y3 = imageWidth;
    for (int idx = 0; idx < lines.size(); idx++) {
      PVector line=lines.get(idx);
      float r = line.x;
      float phi = line.y;
      //println("r = " + r + "phi = " + phi);
      // Cartesian equation of a line: y = ax + b
      // in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
      // => y = 0 : x = r / cos(phi)
      // => x = 0 : y = r / sin(phi)
      // compute the intersection of this line with the 4 borders of
      // the image

      float y0 = (int) (r / sin(phi));
      float x1 = (int) (r / cos(phi));
      float y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
      float x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));

      //println("x0 = " + x0 + " y0 = " + y0);
      //println("x1 = " + x1 + " y1 = " + y1);
      //println("x2 = " + x2 + " y2 = " + y2);
      //println("x3 = " + x3 + " y3 = " + y3);

      // Finally, plot the lines
      stroke(204, 102, 0);
      if (y0 > 0) {
        if (x1 > 0)
          line(x0, y0, x1, y1);
        else if (y2 > 0)
          line(x0, y0, x2, y2);
        else
          line(x0, y0, x3, y3);
      } else {
        if (x1 > 0) {
          if (y2 > 0)
            line(x1, y1, x2, y2);
          else 
          line(x1, y1, x3, y3);
        } else
          line(x2, y2, x3, y3);
      }
    }
  }
}
class KalmanFilter2D {
  float q = 1; // process variance
  float r = 2.0f; // estimate of measurement variance, change to see effect
  PVector vhat = new PVector(0, 0); // a posteriori estimate of x
  PVector vhatminus; // a priori estimate of x
  float p = 1.0f; // a posteriori error estimate
  float pminus; // a priori error estimate
  float kG = 0.0f; // kalman gain

  KalmanFilter2D() {
  }

  KalmanFilter2D(float q, float r) {
    q(q);
    r(r);
  }

  public void q(float q) {
    this.q = q;
  }

  public void r(float r) {
    this.r = r;
  }

  public PVector vhat() {
    return this.vhat;
  }

  public void predict() {
    vhatminus = vhat.copy();
    pminus = p + q;
  }

  public PVector correct(PVector v) {
    kG = pminus / (pminus + r);
    vhat.x = vhatminus.x +kG * (v.x - vhatminus.x);
    vhat.y = vhatminus.y +kG * (v.y - vhatminus.y);
    p = (1 - kG) * pminus;
    return vhat;
  }

  public PVector predict_and_correct(PVector v) {
    predict();
    return correct(v);
  }
}







float discretizationStepsPhi = 0.06f; 
float discretizationStepsR = 2.8f;
// dimensions of the accumulator
int phiDim = (int) (Math.PI / discretizationStepsPhi +1);
//The max radius is the image diagonal, but it can be also negative


public List<PVector> hough(PImage edgeImg, int n) throws Exception {


  //int minVotes = Math.min(edgeImg.width/4, edgeImg.height/4);
  //int regionSize = Math.min(edgeImg.width/30, edgeImg.height/30);
  int minVotes = 20;
  int regionSize = 1;


  int rDim = (int) ((sqrt(edgeImg.width*edgeImg.width +
    edgeImg.height*edgeImg.height) * 2) / discretizationStepsR +1);

  // our accumulator
  int[] accumulator = new int[phiDim * rDim];


  // Fill the accumulator: on edge points (ie, white pixels of the edge
  // image), store all possible (r, phi) pairs describing lines going
  // through the point.
  for (int y = 0; y < edgeImg.height; y++) {
    for (int x = 0; x < edgeImg.width; x++) {
      // Are we on an edge?
      if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
        // ...determine here all the lines (r, phi) passing through
        // pixel (x,y), convert (r,phi) to coordinates in the
        // accumulator, and increment accordingly the accumulator.
        // Be careful: r may be negative, so you may want to center onto
        // the accumulator: r += rDim / 2

        for (int phiStep = 0; phiStep < phiDim; phiStep += 1) {

          double phi = phiStep * discretizationStepsPhi ;
          if (phi < 0 || phi > Math.PI) throw new Exception("phi : "+phi+" not part of [0,PI]");

          //double r = x * Math.cos(phi) + y * Math.sin(phi) ;
          //r *= 1./discretizationStepsR;
          double r = x * tabCos[phiStep] + y * tabSin[phiStep] ;
          r += rDim / 2.f; // center r
          if (r >= rDim || r < 0 ) throw new Exception("r : "+r+" not part of [0,"+rDim+"]");

          accumulator[(int)(phiStep * rDim + r)] += 1.f;
        }
      }
    }
  }


  // ------- DEBUG --> DISPLAY ACCUMULATOR

  //PImage houghImg = createImage(rDim, phiDim, ALPHA);
  //for (int i = 0; i < accumulator.length; i++) {
  //  houghImg.pixels[i] = color(min(255, accumulator[i]));
  //}
  //// You may want to resize the accumulator to make it easier to see:
  //houghImg.resize(400, 400);
  //houghImg.updatePixels();
  //image(houghImg , edgeImg.width , 0 );

  // -------


  ArrayList<PVector> lines=new ArrayList<PVector>();
  List<Integer> bestCandidates = new ArrayList<Integer>();

  for (int idx = 0; idx < accumulator.length; idx++) {
    if (accumulator[idx] > minVotes ) {
      bestCandidates.add(idx);
    }
  }
  //println("bestCandidates.size() - pre = " + bestCandidates.size());
  Collections.sort(bestCandidates, new HoughComparator(accumulator));
  if (bestCandidates.size()>n)
    bestCandidates = bestCandidates.subList(0, n);
  //println("bestCandidates.size() - post = " + bestCandidates.size());





  for (int idx : bestCandidates) {
    // Computes back the (r, phi) polar coordinates:
    int accPhi = (int) (idx / (rDim));
    int accR = idx - (accPhi) * (rDim);
    float r = (accR - (rDim) * 0.5f) * discretizationStepsR;
    float phi = accPhi * discretizationStepsPhi;
    if (checkMaxNeighbor(accumulator, idx, regionSize, phiDim)) {
      lines.add(new PVector(r, phi));
    }
  }
  //println("lines size = " + lines.size());

  return lines;
}

public boolean checkMaxNeighbor(int[] accumulator, int idx, int regionSize, int phiDim) {
  int potentialMax = accumulator[idx];
  for (int i = -regionSize; i<=regionSize; ++i) {
    for (int j = -regionSize; j<=regionSize; ++j) {
      if ((idx +i + j*phiDim)>=0 && (idx +i + j*phiDim) < accumulator.length && accumulator[idx+i+j*phiDim ] > potentialMax)
        return false;
    }
  }
  return true;
}


class HoughComparator implements java.util.Comparator<Integer> { 
  int[] accumulator;

  public HoughComparator(int[] accumulator) {
    this.accumulator = accumulator;
  }

  @Override
    public int compare(Integer l1, Integer l2) { 
    if (accumulator[l1] > accumulator[l2] || (accumulator[l1] == accumulator[l2] && l1 < l2)) 
      return -1; 
    return 1;
  }
}
// Physical constants
final float GRAVITY = 9.81f;
final float mu = 0.01f;
final float elasticity = 0.75f;


class MovingBall{

  PVector location;
  PVector velocity;
  PVector gravityForce;


  // Ball colors
  private float red, blue, green;
  private float redInit = 0xf1;
  private float greenInit = 0xd6;
  private float blueInit = 0xe7;
  boolean change = false;

  //score
  boolean collision = false;



  // Ball characteristics
  float mass; 
  float ball_radius;

  //Ball rotation
  float prevX =0;
  float prevZ =0;


  PShape globe;


  // ---- CONSTRUCTOR ------------------------------------------------------
  MovingBall() {
    //pattern = loadImage("stripes.jpg");
    pattern = loadImage("green_mask.jpg");
    pattern.resize(100,100);
    location = new PVector(0, 0, 0);
    velocity = new PVector(0, 0, 0);
    gravityForce = new PVector(0, 0, 0);
    mass = 100;
    ball_radius = 15;
    red = redInit;
    green = greenInit;
    blue = blueInit;

    globe = createShape(SPHERE, ball_radius);
    globe.setStroke(false);
    globe.setTexture(pattern);
  }



  // ---- DRAW -------------------------------------------------------------
  public void draw(PGraphics surface) {
    surface.fill(red, green, blue);
    surface.pushMatrix();
    surface.translate(location.x, location.y - ball_radius - 2.5f, -location.z);
    surface.shape(globe);
    //gameSurface.sphere(ball_radius);
    surface.popMatrix();
  }


  public void drawBall(PGraphics surface) {
    surface.fill(10, 0, 211);
    surface.stroke(255, 0, 0);
    surface.ellipse((location.x+150)/300*190, (-location.z+150)/300*190, ball_radius/300*190*2, ball_radius/300*190*2);
    surface.noStroke();
    surface.noFill();
  }


  // ---- UPDATE -----------------------------------------------------------
  public void update() {
    // GravityForce
    gravityForce.x = sin(rotationZ) * GRAVITY;
    gravityForce.z = sin(rotationX) * GRAVITY;

    // FrictionForce
    float normalForce = 1;
    float frictionMagnitude = normalForce * mu;
    PVector friction = velocity.copy();
    friction.mult(-1);
    friction.normalize();
    friction.mult(frictionMagnitude);

    velocity.add(gravityForce.add(friction).div(mass));

    // UpdateLocation
    location.add(velocity);

    //Rotate ball
    globe.rotateX((location.z-prevZ)/ball_radius);
    prevZ = location.z;
    globe.rotate((location.x-prevX)/ball_radius, 0, 0, 1);
    prevX = location.x;
  }








  // ---- CHECK EDGES ------------------------------------------------------
  public void checkCollision(Plate plate) {
    if (location.x < -plate.x/2) {
      velocity.x = -(velocity.x * elasticity);
      location.x = -plate.x/2;
      change = true;
    }

    if (location.x > plate.x/2) {
      velocity.x = -(velocity.x * elasticity);
      location.x = plate.x/2;
      change = true;
    }

    if (location.z < -plate.z/2) {
      velocity.z = -(velocity.z * elasticity); 
      location.z = -plate.z/2;
      change = true;
    }

    if (location.z > plate.z/2) {
      velocity.z = -(velocity.z * elasticity); 
      location.z = plate.z/2;
      change = true;
    }

    if (change_ball_color) {
      if (change) {
        change = false;
        red = random(255);
        green = random(255);
        blue = random(255);
      }
    } else {
      red = redInit;
      green = greenInit;
      blue = blueInit;
    }
  }
  
  public float score(){return score;}


  // TODO : fixme !
  public void checkCollision(ArrayList<Cylinder> cylinders) {
    for ( Cylinder c : cylinders) {
      if ( c.isInside(this) ) { 
        PVector n = new PVector(c.x - location.x, 0, -c.y - location.z).normalize(); //fixme
        n = n.mult(2*velocity.dot(n));
        velocity.sub(n);
        increaseScore(velocity.mag());
      }
    }
  }
}
class Plate {

// plane settings


float x = 300;
float y = 5;
float z = 300;


public void draw(PGraphics surface){
    surface.noStroke();
    surface.fill(0xC0, 0xFF, 0xEE);
    surface.box(x, y, z);
}


public boolean isInside(float mx , float my){
    return (mx > -x/2) && (mx < x/2) && (my > -z/2) && (my < z/2);
}


public boolean isInside(Cylinder c ){
    return !((c.x - c.cylinderBaseSize < -x/2) ||
           (c.x + c.cylinderBaseSize > x/2)  || 
           (c.y - c.cylinderBaseSize < -z/2) || 
           (c.y + c.cylinderBaseSize > z/2));
}

public boolean isInside(CylinderGenerator c ){
    return isInside(c.cylinders.get(0));
}

public void drawAxis(PGraphics surface) {
  float a = 300;  

  surface.textSize(30);
  surface.fill(0);
  surface.strokeWeight(2);

  // Y axis
  surface.stroke(0, 255, 0);
  surface.line(0, -a, 0, 0, a, 0);
  surface.text("Y", 0, a, 0);

  // X axis
  surface.stroke(255, 0, 0);
  surface.line(-a, 0, 0, a, 0, 0);
  surface.text("X", a, 0, 0);

  // Z axis
  surface.stroke(0, 0, 255);
  surface.line(0, 0, -a, 0, 0, a);
  surface.text("Z", 0, 0, a);

  surface.noStroke();
}


}







class QuadGraph {

  boolean verbose=false;

  List<int[]> cycles = new ArrayList<int[]>();
  int[][] graph;

  public List<PVector> findBestQuad(List<PVector> lines, int width, int height, int max_quad_area, int min_quad_area, boolean verbose) {
    this.verbose=verbose;
    build(lines, width, height);
    findCycles(verbose);
    ArrayList<PVector> bestQuad=new ArrayList<PVector>();
    float bestQuadArea=0;
    for (int [] cy : cycles) {
      ArrayList<PVector> quad= new ArrayList<PVector>();
      PVector l1 = lines.get(cy[0]);
      PVector l2 = lines.get(cy[1]);
      PVector l3 = lines.get(cy[2]);
      PVector l4 = lines.get(cy[3]);


      quad.add(intersection(l1, l2));
      quad.add(intersection(l2, l3));
      quad.add(intersection(l3, l4));
      quad.add(intersection(l4, l1));
      quad=sortCorners(quad);

      PVector c1 = quad.get(0);
      PVector c2 = quad.get(1);
      PVector c3 = quad.get(2);
      PVector c4 = quad.get(3);

      if (isConvex(c1, c2, c3, c4) && 
        nonFlatQuad(c1, c2, c3, c4)) {
        float quadArea=validArea(c1, c2, c3, c4, max_quad_area, min_quad_area);
        if (quadArea>0 && quadArea>bestQuadArea) {
          bestQuadArea=quadArea;
          bestQuad=quad;
        }
      }
    }
    if (bestQuadArea>0)
      return bestQuad;
    else
      return new ArrayList<PVector>();
  }  


  public void build(List<PVector> lines, int width, int height) {

    int n = lines.size();

    // The maximum possible number of edges is n * (n - 1)/2
    graph = new int[n * (n - 1)/2][2];

    int idx =0;

    for (int i = 0; i < lines.size(); i++) {
      for (int j = i + 1; j < lines.size(); j++) {
        if (intersect(lines.get(i), lines.get(j), width, height)) {

          graph[idx][0]=i;
          graph[idx][1]=j;
          idx++;
        }
      }
    }
  }

  /** Returns true if polar lines 1 and 2 intersect 
   * inside an area of size (width, height)
   */
  public boolean intersect(PVector line1, PVector line2, int width, int height) {

    double sin_t1 = Math.sin(line1.y);
    double sin_t2 = Math.sin(line2.y);
    double cos_t1 = Math.cos(line1.y);
    double cos_t2 = Math.cos(line2.y);
    float r1 = line1.x;
    float r2 = line2.x;

    double denom = cos_t2 * sin_t1 - cos_t1 * sin_t2;

    int x = (int) ((r2 * sin_t1 - r1 * sin_t2) / denom);
    int y = (int) ((-r2 * cos_t1 + r1 * cos_t2) / denom);

    if (0 <= x && 0 <= y && width >= x && height >= y)
      return true;
    else
      return false;
  }

  public PVector intersection(PVector line1, PVector line2) {

    double sin_t1 = Math.sin(line1.y);
    double sin_t2 = Math.sin(line2.y);
    double cos_t1 = Math.cos(line1.y);
    double cos_t2 = Math.cos(line2.y);
    float r1 = line1.x;
    float r2 = line2.x;

    double denom = cos_t2 * sin_t1 - cos_t1 * sin_t2;

    int x = (int) ((r2 * sin_t1 - r1 * sin_t2) / denom);
    int y = (int) ((-r2 * cos_t1 + r1 * cos_t2) / denom);

    return new PVector(x, y);
  }

  public void findCycles(boolean verbose) {
    cycles.clear();
    for (int i = 0; i < graph.length; i++) {
      for (int j = 0; j < graph[i].length; j++) {
        findNewCycles(new int[] {graph[i][j]});
      }
    }
    if (verbose) {
      for (int[] cy : cycles) {
        String s = "" + cy[0];
        for (int i = 1; i < cy.length; i++) {
          s += "," + cy[i];
        }
        System.out.println(s);
      }
    }
  }

  public void findNewCycles(int[] path)
  {
    int n = path[0];
    int x;
    int[] sub = new int[path.length + 1];

    for (int i = 0; i < graph.length; i++)
      for (int y = 0; y <= 1; y++)
        if (graph[i][y] == n)
          //  edge refers to our current node
        {
          x = graph[i][(y + 1) % 2];
          if (!visited(x, path))
            //  neighbor node not on path yet
          {
            sub[0] = x;
            System.arraycopy(path, 0, sub, 1, path.length);
            //  explore extended path
            findNewCycles(sub);
          } else if ((path.length == 4) && (x == path[path.length - 1]))
            //  cycle found
          {
            int[] p = normalize(path);
            int[] inv = invert(p);
            if (isNew(p) && isNew(inv))
            {
              cycles.add(p);
            }
          }
        }
  }

  //  Check if both arrays have same lengths and contents
  public Boolean equals(int[] a, int[] b)
  {
    Boolean ret = (a[0] == b[0]) && (a.length == b.length);

    for (int i = 1; ret && (i < a.length); i++)
    {
      if (a[i] != b[i])
      {
        ret = false;
      }
    }

    return ret;
  }

  //  Create a path array with reversed order
  public int[] invert(int[] path)
  {
    int[] p = new int[path.length];

    for (int i = 0; i < path.length; i++)
    {
      p[i] = path[path.length - 1 - i];
    }

    return normalize(p);
  }

  //  Rotate cycle path such that it begins with the smallest node
  public int[] normalize(int[] path)
  {
    int[] p = new int[path.length];
    int x = smallest(path);
    int n;

    System.arraycopy(path, 0, p, 0, path.length);

    while (p[0] != x)
    {
      n = p[0];
      System.arraycopy(p, 1, p, 0, p.length - 1);
      p[p.length - 1] = n;
    }

    return p;
  }

  //  Compare path against known cycles,
  //  return true iff path is not a known cycle
  public Boolean isNew(int[] path)
  {
    Boolean ret = true;

    for (int[] p : cycles)
    {
      if (equals(p, path))
      {
        ret = false;
        break;
      }
    }

    return ret;
  }

  //  Return the int of the array which is the smallest
  public int smallest(int[] path)
  {
    int min = path[0];

    for (int p : path)
    {
      if (p < min)
      {
        min = p;
      }
    }

    return min;
  }

  //  Check if vertex n is contained in path
  public Boolean visited(int n, int[] path)
  {
    Boolean ret = false;

    for (int p : path)
    {
      if (p == n)
      {
        ret = true;
        break;
      }
    }

    return ret;
  }



  /** Check if a quad is convex or not.
   * 
   * Algo: take two adjacent edges and compute their cross-product. 
   * The sign of the z-component of all the cross-products is the 
   * same for a convex polygon.
   * 
   * See http://debian.fmi.uni-sofia.bg/~sergei/cgsr/docs/clockwise.htm
   * for justification.
   * 
   * @param c1
   */
  public boolean isConvex(PVector c1, PVector c2, PVector c3, PVector c4) {

    PVector v21= PVector.sub(c1, c2);
    PVector v32= PVector.sub(c2, c3);
    PVector v43= PVector.sub(c3, c4);
    PVector v14= PVector.sub(c4, c1);

    float i1=v21.cross(v32).z;
    float i2=v32.cross(v43).z;
    float i3=v43.cross(v14).z;
    float i4=v14.cross(v21).z;

    if (   (i1>0 && i2>0 && i3>0 && i4>0) 
      || (i1<0 && i2<0 && i3<0 && i4<0))
      return true;
    else if (verbose)
      System.out.println("Eliminating non-convex quad");
    return false;
  }

  /** Compute the area of a quad, and check it lays within a specific range
   */
  public float validArea(PVector c1, PVector c2, PVector c3, PVector c4, float max_area, float min_area) {

    float i1=c1.cross(c2).z;
    float i2=c2.cross(c3).z;
    float i3=c3.cross(c4).z;
    float i4=c4.cross(c1).z;

    float area = Math.abs(0.5f * (i1 + i2 + i3 + i4));


    if (area < max_area && area > min_area) {
      return area;
    }
    return 0;
  }

  /** Compute the (cosine) of the four angles of the quad, and check they are all large enough
   * (the quad representing our board should be close to a rectangle)
   */
  public boolean nonFlatQuad(PVector c1, PVector c2, PVector c3, PVector c4) {

    float min_cos = 0.5f;

    PVector v21= PVector.sub(c1, c2);
    PVector v32= PVector.sub(c2, c3);
    PVector v43= PVector.sub(c3, c4);
    PVector v14= PVector.sub(c4, c1);

    float cos1=Math.abs(v21.dot(v32) / (v21.mag() * v32.mag()));
    float cos2=Math.abs(v32.dot(v43) / (v32.mag() * v43.mag()));
    float cos3=Math.abs(v43.dot(v14) / (v43.mag() * v14.mag()));
    float cos4=Math.abs(v14.dot(v21) / (v14.mag() * v21.mag()));

    if (cos1 < min_cos && cos2 < min_cos && cos3 < min_cos && cos4 < min_cos)
      return true;
    else {
      if (verbose)
        System.out.println("Flat quad");
      return false;
    }
  }


  public ArrayList<PVector> sortCorners(ArrayList<PVector> quad) {

    // 1 - Sort corners so that they are ordered clockwise
    PVector a = quad.get(0);
    PVector b = quad.get(2);

    PVector center = new PVector((a.x+b.x)/2, (a.y+b.y)/2);

    Collections.sort(quad, new CWComparator(center));



    // 2 - Sort by upper left most corner
    PVector origin = new PVector(0, 0);
    float distToOrigin = 1000;

    for (PVector p : quad) {
      if (p.dist(origin) < distToOrigin) distToOrigin = p.dist(origin);
    }

    while (quad.get(0).dist(origin) != distToOrigin)
      Collections.rotate(quad, 1);

    return quad;
  }
}

class CWComparator implements Comparator<PVector> {

  PVector center;

  public CWComparator(PVector center) {
    this.center = center;
  }

  @Override
    public int compare(PVector b, PVector d) {
    if (Math.atan2(b.y-center.y, b.x-center.x)<Math.atan2(d.y-center.y, d.x-center.x))      
      return -1; 
    else return 1;
  }
}







class TwoDThreeD {

  // default focal length, well suited for most webcams
  float f = 700;

  // intrisic camera matrix
  float [][] K = {{f, 0, 0},
    {0, f, 0},
    {0, 0, 1}};
  float [][] invK;
  PVector invK_r1, invK_r2, invK_r3;
  Mat opencv_A, w, u, vt;
  double [][] V;

  // Real physical coordinates of the Lego board in mm
  //float boardSize = 380.f; // large Duplo board
  // float boardSize = 255.f; // smaller Lego board

  // the 3D coordinates of the physical board corners, clockwise
  float [][] physicalCorners = {
    {-128, 128, 0, 1},
    {128, 128, 0, 1},
    {128, -128, 0, 1},
    {-128, -128, 0, 1}
  };

  //Filtering variables: low-pass filter based on arFilterTrans from ARToolKit v5 */
  float[] q;
  float sampleRate;
  float cutOffFreq;
  float alpha;


  public TwoDThreeD(int width, int height, float sampleRate) {

    // set the offset to the center of the webcam image
    K[0][2] = 0.5f * width;
    K[1][2] = 0.5f * height;
    //compute inverse of K
    Mat opencv_K= new Mat(3, 3, CvType.CV_32F);
    opencv_K.put(0, 0, K[0][0]);
    opencv_K.put(0, 1, K[0][1]);
    opencv_K.put(0, 2, K[0][2]);
    opencv_K.put(1, 0, K[1][0]);
    opencv_K.put(1, 1, K[1][1]);
    opencv_K.put(1, 2, K[1][2]);
    opencv_K.put(2, 0, K[2][0]);
    opencv_K.put(2, 1, K[2][1]);
    opencv_K.put(2, 2, K[2][2]);
    Mat opencv_invK=opencv_K.inv();

    invK = new float[][]{
      { (float)opencv_invK.get(0, 0)[0], (float)opencv_invK.get(0, 1)[0], (float)opencv_invK.get(0, 2)[0] },
      { (float)opencv_invK.get(1, 0)[0], (float)opencv_invK.get(1, 1)[0], (float)opencv_invK.get(1, 2)[0] },
      { (float)opencv_invK.get(2, 0)[0], (float)opencv_invK.get(2, 1)[0], (float)opencv_invK.get(2, 2)[0] }};
    invK_r1=new PVector(invK[0][0], invK[0][1], invK[0][2]);
    invK_r2=new PVector(invK[1][0], invK[1][1], invK[1][2]);
    invK_r3=new PVector(invK[2][0], invK[2][1], invK[2][2]);

    opencv_A=new Mat(12, 9, CvType.CV_32F);
    w=new Mat();
    u=new Mat();
    vt=new Mat();
    V= new double[9][9];

    q=new float[4];
    q[3]=1;

    this.sampleRate=sampleRate;
    if (sampleRate>0) {
      cutOffFreq=sampleRate/2;
      alpha= (1/sampleRate)/(1/sampleRate + 1/cutOffFreq);
    }
  }

  public PVector get3DRotations(List<PVector> points2D) {

    // 1- Solve the extrinsic matrix from the projected 2D points
    double[][] E = solveExtrinsicMatrix(points2D);


    // 2 - Re-build a proper 3x3 rotation matrix from the camera's
    //     extrinsic matrix E
    PVector firstColumn=new PVector((float)E[0][0], (float)E[1][0], (float)E[2][0]);
    PVector secondColumn=new PVector((float)E[0][1], (float)E[1][1], (float)E[2][1]);
    firstColumn.normalize();
    secondColumn.normalize();
    PVector thirdColumn=firstColumn.cross(secondColumn);
    float [][] rotationMatrix={{firstColumn.x, secondColumn.x, thirdColumn.x},
      {firstColumn.y, secondColumn.y, thirdColumn.y},
      {firstColumn.z, secondColumn.z, thirdColumn.z}};

    if (sampleRate>0)
      filter(rotationMatrix, false);

    // 3 - Computes and returns Euler angles (rx, ry, rz) from this matrix
    return rotationFromMatrix(rotationMatrix);
  }


  public double[][] solveExtrinsicMatrix(List<PVector> points2D) {

    // p ~= K · [R|t] · P
    // with P the (3D) corners of the physical board, p the (2D)
    // projected points onto the webcam image, K the intrinsic
    // matrix and R and t the rotation and translation we want to
    // compute.
    //
    // => We want to solve: (K^(-1) · p) X ([R|t] · P) = 0

    float[][] projectedCorners = new float[4][3];

    if(points2D.size() >= 4)
    for (int i=0; i<4; i++) {
      // TODO:
      // store in projectedCorners the result of (K^(-1) · p), for each
      // corner p found in the webcam image.
      // You can use PVector dot function for computing dot product between K^(-1) lines and p.
      //Do not forget to normalize the result
      PVector point =points2D.get(i);
      projectedCorners[i][0]=point.dot(invK_r1)/point.dot(invK_r3);
      projectedCorners[i][1]=point.dot(invK_r2)/point.dot(invK_r3);
      projectedCorners[i][2]=1;
    }

    // 'A' contains the cross-product (K^(-1) · p) X P
    float[][] A= new float[12][9];

    for (int i=0; i<4; i++) {
      A[i*3][0]=0;
      A[i*3][1]=0;
      A[i*3][2]=0;

      // note that we take physicalCorners[0,1,*3*]: we drop the Z
      // coordinate and use the 2D homogenous coordinates of the physical
      // corners
      A[i*3][3]=-projectedCorners[i][2] * physicalCorners[i][0];
      A[i*3][4]=-projectedCorners[i][2] * physicalCorners[i][1];
      A[i*3][5]=-projectedCorners[i][2] * physicalCorners[i][3];

      A[i*3][6]= projectedCorners[i][1] * physicalCorners[i][0];
      A[i*3][7]= projectedCorners[i][1] * physicalCorners[i][1];
      A[i*3][8]= projectedCorners[i][1] * physicalCorners[i][3];

      A[i*3+1][0]= projectedCorners[i][2] * physicalCorners[i][0];
      A[i*3+1][1]= projectedCorners[i][2] * physicalCorners[i][1];
      A[i*3+1][2]= projectedCorners[i][2] * physicalCorners[i][3];

      A[i*3+1][3]=0;
      A[i*3+1][4]=0;
      A[i*3+1][5]=0;

      A[i*3+1][6]=-projectedCorners[i][0] * physicalCorners[i][0];
      A[i*3+1][7]=-projectedCorners[i][0] * physicalCorners[i][1];
      A[i*3+1][8]=-projectedCorners[i][0] * physicalCorners[i][3];

      A[i*3+2][0]=-projectedCorners[i][1] * physicalCorners[i][0];
      A[i*3+2][1]=-projectedCorners[i][1] * physicalCorners[i][1];
      A[i*3+2][2]=-projectedCorners[i][1] * physicalCorners[i][3];

      A[i*3+2][3]= projectedCorners[i][0] * physicalCorners[i][0];
      A[i*3+2][4]= projectedCorners[i][0] * physicalCorners[i][1];
      A[i*3+2][5]= projectedCorners[i][0] * physicalCorners[i][3];

      A[i*3+2][6]=0;
      A[i*3+2][7]=0;
      A[i*3+2][8]=0;
    }

    for (int i=0; i<12; i++)
      for (int j=0; j<9; j++)
        opencv_A.put(i, j, A[i][j]);

    Core.SVDecomp(opencv_A, w, u, vt);

    for (int i=0; i<9; i++)
      for (int j=0; j<9; j++)
        V[j][i]=vt.get(i, j)[0];

    double[][] E = new double[3][3];

    //E is the last column of V
    for (int i=0; i<9; i++) {
      E[i/3][i%3] = V[i][V.length-1] / V[8][V.length-1];
    }

    return E;
  }

  public PVector rotationFromMatrix(float[][]  mat) {

    // Assuming rotation order is around x,y,z
    PVector rot = new PVector();

    if (mat[1][0] > 0.998f) { // singularity at north pole
      rot.z = 0;
      float delta = (float) Math.atan2(mat[0][1], mat[0][2]);
      rot.y = -(float) Math.PI/2;
      rot.x = -rot.z + delta;
      return rot;
    }

    if (mat[1][0] < -0.998f) { // singularity at south pole
      rot.z = 0;
      float delta = (float) Math.atan2(mat[0][1], mat[0][2]);
      rot.y = (float) Math.PI/2;
      rot.x = rot.z + delta;
      return rot;
    }

    rot.y =-(float)Math.asin(mat[2][0]);
    rot.x = (float)Math.atan2(mat[2][1]/Math.cos(rot.y), mat[2][2]/Math.cos(rot.y));
    rot.z = (float)Math.atan2(mat[1][0]/Math.cos(rot.y), mat[0][0]/Math.cos(rot.y));

    return rot;
  }

  public int filter(float m[][], boolean reset) {

    float[] q= new float[4];
    float alpha, oneminusalpha, omega, cosomega, sinomega, s0, s1;

    mat2Quat(m, q);
    if (nomalizeQuaternion(q)<0) return -1;

    if (reset) {
      this.q[0] = q[0];
      this.q[1] = q[1];
      this.q[2] = q[2];
      this.q[3] = q[3];
    } else {
      alpha = this.alpha;

      oneminusalpha = 1.0f - alpha;

      // SLERP for orientation.
      cosomega = q[0]*this.q[0] + q[1]*this.q[1] + q[2]*this.q[2] + q[3]*this.q[3]; // cos of angle between vectors.
      if (cosomega < 0.0f) {
        cosomega = -cosomega;
        q[0] = -q[0];
        q[1] = -q[1];
        q[2] = -q[2];
        q[3] = -q[3];
      }
      if (cosomega > 0.9995f) {
        s0 = oneminusalpha;
        s1 = alpha;
      } else {
        omega = acos(cosomega);
        sinomega = sin(omega);
        s0 = sin(oneminusalpha * omega) / sinomega;
        s1 = sin(alpha * omega) / sinomega;
      }
      this.q[0] = q[0]*s1 + this.q[0]*s0;
      this.q[1] = q[1]*s1 + this.q[1]*s0;
      this.q[2] = q[2]*s1 + this.q[2]*s0;
      this.q[3] = q[3]*s1 + this.q[3]*s0;
      nomalizeQuaternion(this.q);
    }

    if (quat2Mat(this.q, m) < 0) return (-2);

    return (0);
  }


  public int nomalizeQuaternion(float[] q) {// Normalise quaternion.
    float mag2 = q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3];
    if (mag2==0) return (-1);

    float mag = sqrt(mag2);

    q[0] /= mag;
    q[1] /= mag;
    q[2] /= mag;
    q[3] /= mag;

    return (0);
  }

  public int mat2Quat(float m[][], float q[]) {
    float t, s;
    t = m[0][0] + m[1][1] + m[2][2] + 1.0f;
    if (t > 0.0001f) {
      s = sqrt(t) * 2.0f;
      q[0] = (m[1][2] - m[2][1]) / s;
      q[1] = (m[2][0] - m[0][2]) / s;
      q[2] = (m[0][1] - m[1][0]) / s;
      q[3] = 0.25f * s;
    } else {
      if (m[0][0] > m[1][1] && m[0][0] > m[2][2]) {  // Column 0:
        s  = sqrt(1.0f + m[0][0] - m[1][1] - m[2][2]) * 2.0f;
        q[0] = 0.25f * s;
        q[1] = (m[0][1] + m[1][0] ) / s;
        q[2] = (m[2][0] + m[0][2] ) / s;
        q[3] = (m[1][2] - m[2][1] ) / s;
      } else if (m[1][1] > m[2][2]) {      // Column 1:
        s  = sqrt(1.0f + m[1][1] - m[0][0] - m[2][2]) * 2.0f;
        q[0] = (m[0][1] + m[1][0] ) / s;
        q[1] = 0.25f * s;
        q[2] = (m[1][2] + m[2][1] ) / s;
        q[3] = (m[2][0] - m[0][2] ) / s;
      } else {            // Column 2:
        s  = sqrt(1.0f + m[2][2] - m[0][0] - m[1][1]) * 2.0f;
        q[0] = (m[2][0] + m[0][2] ) / s;
        q[1] = (m[1][2] + m[2][1] ) / s;
        q[2] = 0.25f * s;
        q[3] = (m[0][1] - m[1][0] ) / s;
      }
    }
    return 0;
  }

  public int quat2Mat( float q[], float m[][] )
  {
    float    x2, y2, z2;
    float    xx, xy, xz;
    float    yy, yz, zz;
    float    wx, wy, wz;

    x2 = q[0] * 2.0f;
    y2 = q[1] * 2.0f;
    z2 = q[2] * 2.0f;

    xx = q[0] * x2;
    xy = q[0] * y2;
    xz = q[0] * z2;
    yy = q[1] * y2;
    yz = q[1] * z2;
    zz = q[2] * z2;
    wx = q[3] * x2;
    wy = q[3] * y2;
    wz = q[3] * z2;

    m[0][0] = 1.0f - (yy + zz);
    m[1][1] = 1.0f - (xx + zz);
    m[2][2] = 1.0f - (xx + yy);

    m[1][0] = xy - wz;
    m[0][1] = xy + wz;
    m[2][0] = xz + wy;
    m[0][2] = xz - wy;
    m[2][1] = yz - wx;
    m[1][2] = yz + wx;

    return 0;
  }
}
public void drawInfo(PGraphics surface, float rotationX , float rotationZ , float speed) {
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

public void setLight(PGraphics surface) {
  surface.ambientLight(150, 150, 150);
  surface.directionalLight(128, 128, 128, 1, 1, -1);
  surface.lightFalloff(1, 0, 0);
  surface.lightSpecular(0, 0, 0);
  surface.pointLight(51, 102, 126, 35, 40, 36);
  //surface.pointLight(255, 255, 255, 1, 1, -1);
}

public static float vect_norm(float ... x) {
  float sum = 0;
  for (int i = 0; i < x.length; ++i) {
    sum += x[i] * x[i];
  }
  return sum;
}

public void reset() {
  ball = new MovingBall();
  generator = new CylinderGenerator();
  rotationX = 0;
  rotationZ = 0;
  speed = 1;
  score = 0;
}


   
   
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "TangibleGame" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
