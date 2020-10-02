class KalmanFilter2D {
  float q = 1; // process variance
  float r = 2.0; // estimate of measurement variance, change to see effect
  PVector vhat = new PVector(0, 0); // a posteriori estimate of x
  PVector vhatminus; // a priori estimate of x
  float p = 1.0; // a posteriori error estimate
  float pminus; // a priori error estimate
  float kG = 0.0; // kalman gain

  KalmanFilter2D() {
  }

  KalmanFilter2D(float q, float r) {
    q(q);
    r(r);
  }

  void q(float q) {
    this.q = q;
  }

  void r(float r) {
    this.r = r;
  }

  PVector vhat() {
    return this.vhat;
  }

  void predict() {
    vhatminus = vhat.copy();
    pminus = p + q;
  }

  PVector correct(PVector v) {
    kG = pminus / (pminus + r);
    vhat.x = vhatminus.x +kG * (v.x - vhatminus.x);
    vhat.y = vhatminus.y +kG * (v.y - vhatminus.y);
    p = (1 - kG) * pminus;
    return vhat;
  }

  PVector predict_and_correct(PVector v) {
    predict();
    return correct(v);
  }
}
