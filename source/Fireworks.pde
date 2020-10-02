ArrayList <ParticleSystem> particles;



class ParticleSystem {
  ArrayList<Particle> particles ;

  PVector origin;
  float particleRadius;
  color particleColor;

  ParticleSystem(PVector origin) {
    this.origin = origin.copy();
    particleColor = color(random(100, 255), random(100, 255), random(100, 255));
    particles = new ArrayList();
    particleRadius = random(5, 10);
    particles.add(new Particle(origin, particleColor, particleRadius));
  }

  void addParticle() {
    if (particles.size() < 100)
      particles.add(new Particle(origin, particleColor, particleRadius));
    //System.out.println(particles.size());
  }

  void run() {
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
  color c;

  Particle(PVector center, color c, float radius) {
    this.center = center.copy();
    this.radius = radius;
    speed = random(1);
    angle = random(0, 2*PI);
    this.c = c;
  }

  void run() {
    update();
    display();
  }

  void update() {
    radius -=0.1;
    center.x += sin(angle)*speed;
    center.y += tan(angle)*speed;
    center.z += cos(angle)*speed;
  }

  void display() {
    if (!isDead()) {
      background(123);
      strokeWeight(radius);
      stroke(c);
      //ellipse(center.x, center.y, radius, radius);
      point(center.x, center.y, center.z);
      //noFill();
    }
  }

  boolean isDead() { 
    return radius<=0;
  }
}
