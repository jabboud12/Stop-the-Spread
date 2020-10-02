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
  void draw(PGraphics surface) {

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

  void drawPoints(PGraphics surface) {
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



  void drawShitMode(PGraphics surface) {
    for ( Cylinder c : cylinders)
      c.drawShitMode(surface);
  }


  boolean isInside(MovingBall ball) {
    return cylinders.get(0).isInside(ball);
  }

  void update( Plate plate, MovingBall ball ) {



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
      int index = int(random(cylinders.size()));
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
