class Plate {

// plane settings


float x = 300;
float y = 5;
float z = 300;


void draw(PGraphics surface){
    surface.noStroke();
    surface.fill(0xC0, 0xFF, 0xEE);
    surface.box(x, y, z);
}


boolean isInside(float mx , float my){
    return (mx > -x/2) && (mx < x/2) && (my > -z/2) && (my < z/2);
}


boolean isInside(Cylinder c ){
    return !((c.x - c.cylinderBaseSize < -x/2) ||
           (c.x + c.cylinderBaseSize > x/2)  || 
           (c.y - c.cylinderBaseSize < -z/2) || 
           (c.y + c.cylinderBaseSize > z/2));
}

boolean isInside(CylinderGenerator c ){
    return isInside(c.cylinders.get(0));
}

void drawAxis(PGraphics surface) {
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
